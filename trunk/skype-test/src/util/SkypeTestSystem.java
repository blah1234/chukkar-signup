package util;

import java.util.concurrent.atomic.AtomicLong;

public class SkypeTestSystem
{
	//////////////////////////// MEMBER VARIABLES //////////////////////////////
	static private final Latency _latencyMetric = new Latency();
	static private final AtomicLong _timeCount = new AtomicLong(0);
	static private volatile boolean _shutdown = false; 
	{
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
	 * Returns the the current time in milliseconds in SIMULATED TIME.
	 */
	public static long currentTimeMillis()
	{
		return _timeCount.get();
	}
	
	public static void stopClock()
	{
		_shutdown = true;
	}
	
	public static void addNotificationLatencyMetric(long elapsedTimeMillis)
	{
		synchronized(_latencyMetric)
		{
			_latencyMetric._totalElapsedTime += elapsedTimeMillis;
			_latencyMetric._numDataPoints++;
		}
	}

	
	////////////////////////////// INNER CLASSES ///////////////////////////////
	private static class Latency
	{
		long _totalElapsedTime = 0;
		int _numDataPoints = 0;
	}
}
