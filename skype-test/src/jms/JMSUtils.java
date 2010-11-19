package jms;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;

public class JMSUtils
{
	///////////////////////////////// METHODS //////////////////////////////////
	static public void closeSilently(Session toClose)
	{
		try
		{
			toClose.close();
		}
		catch(Exception e) 
		{
			//silent
		}
	}
	
	static public void closeSilently(Connection toClose)
	{
		try
		{
			toClose.close();
		}
		catch(Exception e) 
		{
			//silent
		}
	}
}
