package client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import jms.JMSUtils;

import org.apache.log4j.Logger;

import util.Constants;

public class Node
{
	//////////////////////////////// CONSTANTS /////////////////////////////////
	static private final long SEND_MESSAGE_INTERVAL_MILLIS = 12000;


	//////////////////////////// MEMBER VARIABLES //////////////////////////////
	private final UUID _guid = UUID.randomUUID();
	private volatile Presence _state;
	private Map<String, Presence> _buddyIdToPresenceMap;
	private Queue _simulatorInput;
	private Topic _simulatorBroadcast;
	private Session _sendSession;
	private Session _receiveSession;
	private AtomicBoolean _isStarted;
	private volatile boolean _isKilled;
	static private final Logger _log = Logger.getLogger(Node.class);
	

	/////////////////////////////// CONSTRUCTORS ///////////////////////////////
	/**
	 * Creates a new Node object.
	 * @param initState initial state of the new Node
	 * @param jmsConn connection used to communicate with the Simulator
	 * @param simInQueue queue to pass messages from Node to Simulator
	 * @param simBroadcast topic that the Simulator uses to broadcast Node states  
	 */
	public Node(Presence initState, Connection jmsConn, Queue simInQueue, Topic simBroadcast)
	{
		_state = initState;
		
		try
		{
			_sendSession = jmsConn.createSession(false, Session.AUTO_ACKNOWLEDGE);
			_receiveSession = jmsConn.createSession(false, Session.AUTO_ACKNOWLEDGE);
		}
		catch(JMSException e)
		{
			_log.fatal("Unable to create JMS sessions: " + e.getMessage(), e);
			throw new RuntimeException(e);
		}
		
		_simulatorInput = simInQueue;
		_simulatorBroadcast = simBroadcast;
		
		_isStarted = new AtomicBoolean(false);
		_isKilled = false;
		_buddyIdToPresenceMap = new HashMap<String, Presence>();
		
		initSimulatorBroadcastListener();
	}
	

	///////////////////////////////// METHODS //////////////////////////////////
	private void initSimulatorBroadcastListener()
	{
		try
		{
			MessageConsumer cons = _receiveSession.createConsumer(_simulatorBroadcast);
			cons.setMessageListener(new MessageListener()
			{
				public void onMessage(Message msg)
				{
					//simulate communication failure if state is OFFLINE
					if(Node.this.getState() == Presence.ONLINE)
					{
						MapMessage mapMsg = (MapMessage)msg;
						
						try
						{
							String nodeGuid = mapMsg.getStringProperty(Constants.NODE_GUID);
							
							if( _buddyIdToPresenceMap.containsKey(nodeGuid) )
							{
								String state = mapMsg.getString(Constants.NODE_STATE);
								_buddyIdToPresenceMap.put( nodeGuid, Presence.valueOf(state) );
							}
						}
						catch(JMSException e)
						{
							_log.error("Unable to unpack received message\nNode:\n" + Node.this.getGUID() + 
								"\n\nMessage:\n" + msg.toString() + 
								"\n\n" + e.getMessage(), e);
						}
					}
				}
			});
		}
		catch(JMSException e)
		{
			_log.fatal("Unable to create Topic listener: " + e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Starts a Node's delivery of state messages to the Simulator for forwarding
	 * to the Node's buddies.
	 */
	public void start()
	{
		if( _isStarted.compareAndSet(false, true) )
		{
			Runnable sendState = new Runnable()
			{
				long _lastSendTime = -1;
				
				public void run()
				{
					while(!_isKilled)
					{
						//simulate communication failure if state is OFFLINE
						//node can't send more than 5 msg/min
						if( (Node.this.getState() == Presence.ONLINE) &&
							((_lastSendTime == -1) || (System.currentTimeMillis() - _lastSendTime >= SEND_MESSAGE_INTERVAL_MILLIS)) )
						{
							try
							{
								MessageProducer prod = _sendSession.createProducer(_simulatorInput);
								prod.setDeliveryMode(DeliveryMode.PERSISTENT);
								prod.setTimeToLive(0);	//0 is unlimited
								
								MapMessage msg = _sendSession.createMapMessage();
								msg.setStringProperty( Constants.NODE_GUID, Node.this.getGUID() );
								msg.setString( Constants.NODE_STATE, Node.this.getState().toString() );
								prod.send(msg);
								
								_lastSendTime = System.currentTimeMillis();
		
								prod.close();
							}
							catch(JMSException e)
							{
								_log.error("Error occured trying to send state message to Simulator. Trying again: " + 
									e.getMessage(), e);
							}
						}
						
						try
						{
							Thread.sleep(2000);
						}
						catch(InterruptedException e)
						{
							//don't really care
						}
					}
					
					JMSUtils.closeSilently(_sendSession);
				}
			};
			
			new Thread(sendState).start();
		}
	}
	
	/**
	 * Kills this node. Shuts down all message sending and receiving.
	 */
	public void kill()
	{
		_isKilled = true;
		JMSUtils.closeSilently(_receiveSession);
	}
	
	public String getGUID()
	{
		return _guid.toString();
	}
	
	public Presence getState()
	{
		return _state;
	}
	
	public void toggleState()
	{
		if(_state == Presence.OFFLINE)
		{
			_state = Presence.ONLINE;
		}
		else
		{
			_state = Presence.OFFLINE;
		}
	}
	
	/**
	 * Adds a buddy to this Node.
	 * @param buddy another Node acting as a buddy.
	 * @return <code>true</code> if buddy Node is added successfully; 
	 * <code>false</code> otherwise. Unsuccessful return when the 
	 * specified input Node is already a buddy of this Node or is 
	 * this current Node (itself).  
	 */
	public boolean addBuddy(Node buddy)
	{
		if(buddy == this)
		{
			return false;
		}
		else if( !_buddyIdToPresenceMap.containsKey(buddy.getGUID()) )
		{
			_buddyIdToPresenceMap.put( buddy.getGUID(), buddy.getState() );
			return true;
		}
		else
		{
			return false;
		}
	}
}
