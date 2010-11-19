package client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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
import util.SkypeTestSystem;

public class Node
{
	//////////////////////////////// CONSTANTS /////////////////////////////////
	static private final long SEND_MESSAGE_INTERVAL_SEC = 12;


	//////////////////////////// MEMBER VARIABLES //////////////////////////////
	private final UUID _guid = UUID.randomUUID();
	private volatile State _state;
	private Map<String, Presence> _buddyIdToPresenceMap;
	private Queue _simulatorInput;
	private Topic _simulatorBroadcast;
	private Session _sendSession;
	private Session _receiveSession;
	private AtomicBoolean _isStarted;
	private volatile Boolean _isKilled;
	static private final Latency _latencyMetric = new Latency();
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
		_state = new State( initState, SkypeTestSystem.currentTimeSecs() );
		
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
		_isKilled = Boolean.FALSE;
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
					MapMessage mapMsg = (MapMessage)msg;
					
					try
					{
						String nodeGuid = mapMsg.getStringProperty(Constants.NODE_GUID);
						
						if( _buddyIdToPresenceMap.containsKey(nodeGuid) )
						{
							State currState = Node.this.getState();
							long stateChangeTime = mapMsg.getLong(Constants.NODE_STATE_CHANGE_TIME);

							//simulate communication failure if state is OFFLINE
							if(currState.getPresence() == Presence.ONLINE)
							{
								String state = mapMsg.getString(Constants.NODE_STATE);
								Presence newState = Presence.valueOf(state);
								
								Presence oldState = _buddyIdToPresenceMap.put(nodeGuid, newState);
								
								if( _log.isDebugEnabled() )
								{
									_log.debug("Node receive: " + nodeGuid +
										" | old state: " + oldState +
										" | new state: " + newState +
										" | current time: " + SkypeTestSystem.currentTimeSecs() + 
										" | last state change time: " + stateChangeTime);
								}
								
								if(oldState != newState)
								{
									//record the latency between actual state change and notification
									long elapsedTime = SkypeTestSystem.currentTimeSecs() - stateChangeTime;
									Node.this.addNotificationLatencyMetric(elapsedTime);
								}
							}
							else if( _log.isDebugEnabled() )
							{
								_log.debug("Node offline: " + nodeGuid +
									" | current time: " + SkypeTestSystem.currentTimeSecs() + 
									" | last state change time: " + stateChangeTime);
							}
						}
					}
					catch(JMSException e)
					{
						_log.error("Unable to unpack received message\nNode:\n" + Node.this.getGUID() + 
							"\n\nMessage:\n" + msg.toString() + 
							"\n\n" + e.getMessage(), e);
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
					while(true)
					{
						//synchronized so we won't kill off the connection in 
						//the middle of the loop execution
						synchronized (_isKilled)
						{
							if(!_isKilled)
							{
								State currState = Node.this.getState();
		
								//simulate communication failure if state is OFFLINE
								//node can't send more than 5 msg/min
								if( (currState.getPresence() == Presence.ONLINE) &&
									((_lastSendTime == -1) || (SkypeTestSystem.currentTimeSecs() - _lastSendTime >= SEND_MESSAGE_INTERVAL_SEC)) )
								{
									try
									{
										MessageProducer prod = _sendSession.createProducer(_simulatorInput);
										prod.setDeliveryMode(DeliveryMode.PERSISTENT);
										prod.setTimeToLive(0);	//0 is unlimited
										
										MapMessage msg = _sendSession.createMapMessage();
										msg.setStringProperty( Constants.NODE_GUID, Node.this.getGUID() );
										msg.setString( Constants.NODE_STATE, currState.getPresence().toString() );
										msg.setLong( Constants.NODE_STATE_CHANGE_TIME, currState.getLastStateChangeTime() );
										
										_lastSendTime = SkypeTestSystem.currentTimeSecs();
										
										if( _log.isDebugEnabled() )
										{
											_log.debug( "Node send: " + Node.this.getGUID() + 
												" | current time: " + _lastSendTime + 
												" | last state change time: " + currState.getLastStateChangeTime() );
										}
										
										prod.send(msg);
				
										prod.close();
									}
									catch(JMSException e)
									{
										_log.error("Error occured trying to send state message to Simulator. Trying again: " + 
											e.getMessage(), e);
									}
								}
							}
							else
							{
								break;
							}
						}
					}
					
					JMSUtils.closeSilently(_sendSession);
				}
			};
			
			new Thread( sendState, "Node Send Message - " + getGUID() ).start();
		}
	}
	
	/**
	 * Kills this node. Shuts down all message sending and receiving.
	 */
	public void kill()
	{
		synchronized(_isKilled)
		{
			_isKilled = Boolean.TRUE;
		}
		
		JMSUtils.closeSilently(_receiveSession);
	}
	
	public String getGUID()
	{
		return _guid.toString();
	}
	
	/**
	 * Returns a CLONE of the current state of this Node. The actual state of
	 * this Node may continue to change after the current state is
	 * obtained from this method. 
	 * @return a CLONE of the current state of this Node.
	 */
	public State getState()
	{
		synchronized(_state)
		{
			return _state.clone();
		}
	}
	
	public void toggleState()
	{
		synchronized(_state)
		{
			if(_state.getPresence() == Presence.OFFLINE)
			{
				_state.setPresence(Presence.ONLINE);
			}
			else
			{
				_state.setPresence(Presence.OFFLINE);
			}
			
			_state.setLastStateChangeTime( SkypeTestSystem.currentTimeSecs() );
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
			_buddyIdToPresenceMap.put( buddy.getGUID(), buddy.getState().getPresence() );
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public int getBuddyCount()
	{
		return _buddyIdToPresenceMap.size();
	}

	static public float computeAverageNotificationLatencySecs()
	{
		return _latencyMetric.computeAverageLatency();
	}

	private void addNotificationLatencyMetric(long elapsedTimeSecs)
	{
		if( _log.isDebugEnabled() )
		{
			_log.debug("Elapsed time to add: " + elapsedTimeSecs);
		}
		
		Node._latencyMetric._totalElapsedTime.addAndGet(elapsedTimeSecs);
		Node._latencyMetric._numDataPoints.incrementAndGet();
	}


	////////////////////////////// INNER CLASSES ///////////////////////////////
	private class State
	{
		private volatile Presence _presence;
		private volatile long _lastStateChangeTime;
		
		
		State(Presence init, long initTime)
		{
			_presence = init;
			_lastStateChangeTime = initTime;
		}
		
		Presence getPresence()
		{
			return _presence;
		}
		
		void setPresence(Presence toSet)
		{
			_presence = toSet;
		}
		
		long getLastStateChangeTime()
		{
			return _lastStateChangeTime;
		}
		
		void setLastStateChangeTime(long toSet)
		{
			_lastStateChangeTime = toSet;
		}
		
		public State clone()
		{
			return new State(this._presence, this._lastStateChangeTime);
		}
	}


	private static class Latency
	{
		public AtomicLong _totalElapsedTime = new AtomicLong(0);
		public AtomicInteger _numDataPoints = new AtomicInteger(0);
		
		
		public float computeAverageLatency()
		{
			float ret = _totalElapsedTime.get() / (float)_numDataPoints.get();
			return ret; 				
		}
		
		public void resetMetrics()
		{
			_totalElapsedTime.set(0);
			_numDataPoints.set(0);
		}
	}
}
