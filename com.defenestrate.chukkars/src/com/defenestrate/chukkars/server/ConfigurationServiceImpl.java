package com.defenestrate.chukkars.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.defenestrate.chukkars.server.entity.DayOfWeek;
import com.defenestrate.chukkars.server.listener.ServerStartupListener;

/**
 * Servlet that allows for manual reload of the configuration of the Day 
 * enumerations ON THE SERVER whenever the persistent state of the Day is
 * changed in the DB.
 * @author shwang
 * @see {@link DayOfWeek}
 */
public class ConfigurationServiceImpl extends HttpServlet 
{
	//////////////////////////////// CONSTANTS /////////////////////////////////
	private static final Logger LOG = Logger.getLogger( ConfigurationServiceImpl.class.getName() );

	
	///////////////////////////////// METHODS //////////////////////////////////
	public void doGet(HttpServletRequest req, HttpServletResponse resp) 
		throws ServletException, java.io.IOException
	{
		String path = req.getPathInfo();
		if( path.startsWith("/") )
		{
			path = path.substring(1);
		}
		
		try
		{
			Method invokeMethod = this.getClass().getMethod(
				path, 
				HttpServletResponse.class);
			
			invokeMethod.invoke(this, resp);
		}
		catch(NoSuchMethodException e)
		{
			throw new ServletException(e);
		}
		catch(InvocationTargetException e)
		{
			throw new ServletException(e);
		}
		catch(IllegalAccessException e)
		{
			throw new ServletException(e);
		}
	}
	
	/**
	 * Reload of the configuration of the Day enumerations ON THE SERVER 
	 * whenever the persistent state of the Day is changed in the DB.
	 * @param resp HTTPServletResponse to write a "service completed" message 
	 */
	public void loadDaysConfig(HttpServletResponse resp) throws ServletException
	{
		ServerStartupListener.loadDaysConfiguration();
		
		//------------------
		
		resp.setContentType("text/plain;charset=UTF-8");
		String msg =  null;
		PrintWriter charWriter = null;
		
		try
		{
			charWriter = resp.getWriter();
			
			msg = "Day enums successfully reconfigured.\nDid you remember to advance the RunDate of the RESET CronTask, so mobile apps can pick up this change?";
			charWriter.write(msg);
			
			//commit the response
			charWriter.flush();
		}
		catch(IOException e)
		{
			LOG.log(
				Level.SEVERE, 
				"Error encountered trying to write to the ServletResponse:\n" + msg + "\n\n" + e.getMessage(), 
				e);
			
			//display stack trace to browser
			throw new ServletException(e);
		}
		finally
		{
			if(charWriter != null)
			{
				charWriter.close();
			}
		}
	}
}