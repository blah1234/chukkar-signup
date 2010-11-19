package util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

public class SkypeTestSystem
{
	//////////////////////////// MEMBER VARIABLES //////////////////////////////
	static private final Logger _log = Logger.getLogger(SkypeTestSystem.class);
	static private final Latency _latencyMetric = new Latency();
	static private final AtomicLong _timeCount = new AtomicLong(-1);
	static private volatile boolean _shutdown = false;
	static private final long _nanosPerTimeIncrement;
	static
	{
		//figure out how long each simulated time increment really is
		long startTime = System.nanoTime();
		_timeCount.incrementAndGet();
		_nanosPerTimeIncrement = System.nanoTime() - startTime;
		
		Runnable runClock = new Runnable()
		{
			public void run()
			{
				while(!_shutdown)
				{
					_timeCount.incrementAndGet();
				}
			}
		};
		
		new Thread(runClock).start();
	}
 
	
	///////////////////////////////// METHODS //////////////////////////////////
	/**
	 * Returns the the current time in seconds of SIMULATED TIME.
	 */
	public static long currentTimeSecs()
	{
		return _timeCount.get();
	}
	
	public static void stopClock()
	{
		_shutdown = true;
	}
	
	public static void addNotificationLatencyMetric(long elapsedTimeSecs)
	{
		if( _log.isDebugEnabled() )
		{
			_log.debug("Elapsed time to add: " + elapsedTimeSecs);
		}
		
		_latencyMetric._totalElapsedTime.addAndGet(elapsedTimeSecs);
		_latencyMetric._numDataPoints.incrementAndGet();
	}
	
	public static float computeAverageNotificationLatencySecs()
	{
		return _latencyMetric.computeAverageLatency();
	}
	
	public static long getNanosPerSimulatedTimeIncrement()
	{
		return _nanosPerTimeIncrement;
	}

	
	////////////////////////////// INNER CLASSES ///////////////////////////////
	private static class Latency
	{
		AtomicLong _totalElapsedTime = new AtomicLong(0);
		AtomicInteger _numDataPoints = new AtomicInteger(0);
		
		
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
