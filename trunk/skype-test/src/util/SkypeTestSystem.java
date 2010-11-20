package util;

import java.util.concurrent.atomic.AtomicLong;


public class SkypeTestSystem
{
	//////////////////////////// MEMBER VARIABLES //////////////////////////////
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
		
		new Thread(runClock, "Simulated Time Clock").start();
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
	
	public static long getNanosPerSimulatedTimeIncrement()
	{
		return _nanosPerTimeIncrement;
	}
}
