package com.defenestrate.chukkars.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.defenestrate.chukkars.shared.Player;

public class CronServiceImpl extends HttpServlet 
{
	//////////////////////////////// CONSTANTS /////////////////////////////////
	private static final Logger LOG = Logger.getLogger( CronServiceImpl.class.getName() );

	
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
			Method invokeMethod = this.getClass().getMethod(path, null);
			invokeMethod.invoke(this, null);
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
	
	public void removeAllPlayers() 
	{
		PersistenceManager pm = PersistenceManagerHelper.getPersistenceManager();
		
		try 
		{
			Extent<Player> e = pm.getExtent(Player.class);
			Iterator<Player> iter = e.iterator();
			
			while( iter.hasNext() )
			{
				Player delPlayer = iter.next();
				pm.deletePersistent(delPlayer);
			}
		} 
		catch(Exception e)
		{
			LOG.log(
				Level.SEVERE, 
				"Error encountered trying to remove all players:\n" + e.getMessage(), 
				e);
		}
		finally 
		{
			pm.close();
		}
		
		logTask(CronTask.RESET);
	}
	
	private void logTask(String taskName)
	{
		PersistenceManager pm = PersistenceManagerHelper.getPersistenceManager();
		
		Transaction tx = pm.currentTransaction();

		try
		{
		    tx.begin();

		    Query q = pm.newQuery(CronTask.class);
			q.setFilter("_name == name");
			q.declareParameters("String name");
			q.setUnique(true);
			CronTask currTask = (CronTask)q.execute(taskName);

			if(currTask != null) 
			{
				currTask.setRunDateToNow();
			}
			else
			{
				//create the task entry
				CronTask newTask = new CronTask(taskName);
				pm.makePersistent(newTask);
			}

		    tx.commit();
		}
		catch(Exception e)
		{
			LOG.log(
				Level.SEVERE, 
				"Error encountered trying to log task " + taskName + ":\n" + e.getMessage(), 
				e);
			
		    if( tx.isActive() )
		    {
		        tx.rollback();
		    }
		}
		finally 
		{
			pm.close();
		}
	}
	
	private MessageAdmin getEnabledMessageAdmin()
	{
		MessageAdmin ret = null;
		PersistenceManager pm = PersistenceManagerHelper.getPersistenceManager();
	    
		try 
		{
			Query q = pm.newQuery(MessageAdmin.class);
			q.setFilter("_weeklyEmailsEnabled == true");
			q.setUnique(true);
			ret = (MessageAdmin)q.execute();
			
			if(ret == null) 
			{
				LOG.info("found no enabled MessageAdmin entries (_weeklyEmailsEnabled = true)");
			}
		} 
		finally 
		{
			pm.close();
		}
		
		return ret;
	}
	
	public void sendSignupNoticeEmail() throws ServletException
	{
		MessageAdmin data = getEnabledMessageAdmin();
		
		if(data != null)
		{
			String msgBody = data.getSignupNoticeMessage();
			EmailServiceImpl.sendEmail("signup for weekend", msgBody, data);
		}
	}
	
	public void sendSignupReminderEmail() throws ServletException
	{
		MessageAdmin data = getEnabledMessageAdmin();
		
		if(data != null)
		{
			String msgBody = data.getSignupReminderMessage();
			EmailServiceImpl.sendEmail("signup by 12 noon", msgBody, data);
		}
	}
}