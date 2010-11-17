package simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.log4j.Logger;

import client.Node;
import client.Presence;

import jms.JMSUtils;
import jms.JetStreamConnectionFactoryBean;

public class Simulator
{
	//////////////////////////// MEMBER VARIABLES //////////////////////////////
	private Connection _jmsConn;
	private Session _sendSession;
	private Session _receiveSession;
	private Queue _nodeInput;
	private Topic _broadcastOut;
	private List<StateHelper> _allNodes;
	private Random _rand = new Random();
	private volatile boolean _isKilled;
	private static final Logger _log = Logger.getLogger(Simulator.class);
	

	/////////////////////////////// CONSTRUCTORS ///////////////////////////////
	public Simulator(int numNodes, int numBuddies)
	{
		try
		{
			createJMSConstructs();
			createMessageForwarder();
		}
		catch(Exception e)
		{
			_log.fatal("Unable to create JMS constructs: " + e.getMessage(), e);
			throw new RuntimeException(e);
		}
		
		createNodes(numNodes, numBuddies);
		startMainEventLoop();
		
		//TODO: close connection and sessions
	}
	
	private void createJMSConstructs() throws Exception
	{
		JetStreamConnectionFactoryBean factoryBean = new JetStreamConnectionFactoryBean(); 

    	ConnectionFactory conFactory = (ConnectionFactory)factoryBean.getObject();
    	
    	// Create a JMS connection
    	_jmsConn = conFactory.createConnection();
    	_jmsConn.start();
    	
    	_sendSession = _jmsConn.createSession(false, Session.AUTO_ACKNOWLEDGE);
    	_receiveSession = _jmsConn.createSession(false, Session.AUTO_ACKNOWLEDGE);
    	
    	_nodeInput = _receiveSession.createQueue("NodeInput");
    	_broadcastOut = _sendSession.createTopic("BroadcastOut");
	}
	
	private void createMessageForwarder() throws JMSException
	{
		MessageConsumer cons = _receiveSession.createConsumer(_nodeInput);
		cons.setMessageListener(new MessageListener()
		{
			long _count = 1;
			
			public void onMessage(Message msg)
			{
				try
				{
					//The simulator simulates packet loss by not 
					//forwarding 5% of messages.
					if( (_count++ % 20) != 0 )
					{
						MessageProducer prod = _sendSession.createProducer(_broadcastOut);
						prod.setDeliveryMode(DeliveryMode.PERSISTENT);
						prod.setTimeToLive(0);	//0 is unlimited
						
						prod.send(msg);
						prod.close();
					}
				}
				catch(JMSException e)
				{
					_log.error(e.getMessage(), e);
				}
			}
		});
	}
	
	private void createNodes(int numNodes, int numBuddies)
	{
		_allNodes = Collections.synchronizedList( new ArrayList<StateHelper>(numNodes) );
		
		for(int i=0; i<numNodes; i++)
		{
			//choose the init state randomly
			boolean randBool = _rand.nextBoolean();
			Presence initState = randBool ? Presence.ONLINE : Presence.OFFLINE;
			
			StateHelper currHelper = new StateHelper();
			Node currNode = new Node(initState, _jmsConn, _nodeInput, _broadcastOut);
			
			currHelper._node = currNode;
			currHelper._lastStateChangeTime = System.currentTimeMillis();
			
			//random # of seconds between 0 and 4000
			currHelper._currentStateChangeIntervalMillis = _rand.nextInt(4001) * 1000;
			
			_allNodes.add(currHelper);
		}
		
		
		int n = _allNodes.size();
		for(StateHelper currHelper : _allNodes)
		{
			for(int i=0; i<numBuddies;)
			{
				//choose buddies randomly
				int buddyIndex = _rand.nextInt(n);
				boolean isSuccess = currHelper._node.addBuddy( _allNodes.get(buddyIndex)._node );
				
				if(isSuccess)
				{
					i++;
				}
			}
		}
		
		
		for(StateHelper currHelper : _allNodes)
		{
			currHelper._node.start();
		}
	}
	
	private void startMainEventLoop()
	{
		Runnable changeState = new Runnable()
		{
			public void run()
			{
				while(!_isKilled)
				{
					synchronized(_allNodes)
					{
						for(StateHelper currHelper : _allNodes)
						{
							long timeElapsedMillis = System.currentTimeMillis() - currHelper._lastStateChangeTime;
							if(timeElapsedMillis >= currHelper._currentStateChangeIntervalMillis)
							{
								currHelper._node.toggleState();
								currHelper._currentStateChangeIntervalMillis = _rand.nextInt(4001) * 1000;
							}
						}
					}
					
					
					try
					{
						Thread.sleep(1000);
					}
					catch(InterruptedException e)
					{
						//don't really care
					}
				}
			}
		};
		
		new Thread(changeState).start();
	}
	
	/**
	 * Kills the Simulator. Shuts down all message sending and receiving.
	 */
	private void kill()
	{
		_isKilled = true;
		
		synchronized(_allNodes)
		{
			for(StateHelper currHelper : _allNodes)
			{
				currHelper._node.kill();
			}
		}
		
		JMSUtils.closeSilently(_receiveSession);
		JMSUtils.closeSilently(_sendSession);
		JMSUtils.closeSilently(_jmsConn);
		
		//TODO: maybe drain out the queue?
	}
	
	static public void main(String[] args) throws Exception
    {
    	new Simulator(1000, 20);
    }
	

	////////////////////////////// INNER CLASSES ///////////////////////////////
	private class StateHelper
	{
		Node _node;
		long _lastStateChangeTime;
		long _currentStateChangeIntervalMillis;
	}
}
