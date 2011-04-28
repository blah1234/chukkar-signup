package com.defenestrate.chukkars.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
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

import org.apache.commons.io.IOUtils;

import com.defenestrate.chukkars.server.entity.CronTask;
import com.defenestrate.chukkars.server.entity.MessageAdmin;
import com.defenestrate.chukkars.shared.Day;
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
	
	public void removeAllPlayers(HttpServletResponse resp) throws ServletException
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
			
			//display stack trace to browser
			throw new ServletException(e);
		}
		finally 
		{
			pm.close();
		}
		
		logTask(CronTask.RESET);
		
		//log the time when signup should be closed
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone( TimeZone.getTimeZone("America/Los_Angeles") );
		cal.setTime( new Date() );
		while(cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
		{
			cal.add(Calendar.DAY_OF_WEEK, 1);
		}
		cal.set(Calendar.HOUR_OF_DAY, 12);	 //noon
		cal.set(Calendar.MINUTE, 30);
		
		logTask( CronTask.CLOSE_SIGNUP, cal.getTime() );
		
		//------------------
		
		resp.setContentType("text/plain;charset=UTF-8");
		String msg =  null;
		PrintWriter charWriter = null;
		
		try
		{
			charWriter = resp.getWriter();
			
			msg = "All players successfully removed.";
			charWriter.write(msg);
			
			//commit the response
			charWriter.flush();
		}
		catch (IOException e)
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
	
	private void logTask(String taskName)
	{
		logTask(taskName, null);
	}
	
	private void logTask(String taskName, Date runDate)
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
				if(runDate == null)
				{
					currTask.setRunDateToNow();
				}
				else
				{
					currTask.setRunDate(runDate);
				}
			}
			else
			{
				//create the task entry
				CronTask newTask = new CronTask(taskName);
				
				if(runDate != null)
				{
					newTask.setRunDate(runDate);
				}
				
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
	
	static protected Date getTaskRunDate(String taskName)
	{
		PersistenceManager pm = PersistenceManagerHelper.getPersistenceManager();
		
		//default is "now"
		Date ret = new Date();
		
		try
		{
		    Query q = pm.newQuery(CronTask.class);
			q.setFilter("_name == name");
			q.declareParameters("String name");
			q.setUnique(true);
			CronTask currTask = (CronTask)q.execute(taskName);

			if(currTask != null) 
			{
				ret = currTask.getRunDate();
			}
		}
		catch(Exception e)
		{
			LOG.log(
				Level.SEVERE, 
				"Error encountered trying to find task " + taskName + ":\n" + e.getMessage(), 
				e);
		}
		finally 
		{
			pm.close();
		}
		
		return ret;
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
	
	public void sendSignupNoticeEmail(HttpServletResponse resp) throws ServletException
	{
		MessageAdmin data = getEnabledMessageAdmin();
		
		if(data != null)
		{
			String msgBody = data.getSignupNoticeMessage();
			EmailServiceImpl.sendEmail("signup for weekend", msgBody, data);
		}
		
		//------------------
		
		resp.setContentType("text/plain;charset=UTF-8");
		String msg =  null;
		PrintWriter charWriter = null;
		
		try
		{
			charWriter = resp.getWriter();
			
			if(data != null)
			{
				msg = "Signup notice email successfully sent.";
			}
			else
			{
				msg = "Weekly emails are not enabled. No email sent.";
			}
			
			charWriter.write(msg);
			
			//commit the response
			charWriter.flush();
		}
		catch (IOException e)
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
	
	public void sendSignupReminderEmail(HttpServletResponse resp) throws ServletException
	{
		MessageAdmin data = getEnabledMessageAdmin();
		
		if(data != null)
		{
			String msgBody = data.getSignupReminderMessage();
			EmailServiceImpl.sendEmail("signup by 12 noon", msgBody, data);
		}
		
		//------------------
		
		resp.setContentType("text/plain;charset=UTF-8");
		String msg =  null;
		PrintWriter charWriter = null;
		
		try
		{
			charWriter = resp.getWriter();
			
			if(data != null)
			{
				msg = "Signup reminder email successfully sent.";
			}
			else
			{
				msg = "Weekly emails are not enabled. No email sent.";
			}
			
			charWriter.write(msg);
			
			//commit the response
			charWriter.flush();
		}
		catch (IOException e)
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
	
	public void sendExportSignupEmail(HttpServletResponse resp) throws ServletException
	{
		MessageAdmin data = getEnabledMessageAdmin();
		
		if(data != null)
		{
			List<Player> allPlayersList = null;
			
			try
			{
				allPlayersList = PlayerServiceImpl.getPlayersImpl();
			}
			catch(Exception e)
			{
				LOG.log(Level.SEVERE,
						"Error getting all signed up players",
						e);
				
				PrintWriter pWrite = null;
				
				try
				{
					StringWriter strWrite = new StringWriter();
					pWrite = new PrintWriter(strWrite);
					e.printStackTrace(pWrite);
				
					EmailServiceImpl.sendEmail("hwang.shawn@gmail.com", "Export signup error", strWrite.toString(), data);
				}
				finally
				{
					IOUtils.closeQuietly(pWrite);
				}
			}
			
			
			Comparator<Day> dayComp = new Comparator<Day>()
			{
				public int compare(Day o1, Day o2)
				{
					int ret = o1.getNumber() - o2.getNumber();
					return ret;
				}
			};
			Map<Day, List<Player>> dayToPlayers = new TreeMap<Day, List<Player>>(dayComp);
			
			for(Player currPlayer : allPlayersList)
			{
				Day currDay = currPlayer.getRequestDay();
				List<Player> valList;
				
				if( dayToPlayers.containsKey(currDay) )
				{
					valList = dayToPlayers.get(currDay);
				}
				else
				{
					valList = new ArrayList<Player>();
					dayToPlayers.put(currDay, valList);
				}
				
				valList.add(currPlayer);
			}
			
			
			StringBuffer buf = new StringBuffer();
			
			for( Day currDay : dayToPlayers.keySet() )
			{
				buf.append(currDay);
				buf.append(":\n");
				
				List<Player> valList = dayToPlayers.get(currDay);
				for(Player currPlayer : valList)
				{
					buf.append( currPlayer.getName() );
					buf.append("\t");
					buf.append( currPlayer.getChukkarCount() );
					buf.append("\n");
				}
				
				buf.append("\n\n\n");
			}
			
			EmailServiceImpl.sendEmail("erikwrghtw@aol.com", "HPPC chukkar signups", buf.toString(), data);
			EmailServiceImpl.sendEmail("hwang.shawn@gmail.com", "HPPC chukkar signups", buf.toString(), data);
		}
		
		//------------------------------
		
		resp.setContentType("text/plain;charset=UTF-8");
		String msg = null;
		PrintWriter charWriter = null;
		
		try
		{
			charWriter = resp.getWriter();
			
			if(data != null)
			{
				msg = "Signup export email successfully sent to Erik.";
			}
			else
			{
				msg = "Weekly emails are not enabled. No email sent.";
			}
			
			charWriter.write(msg);
			
			//commit the response
			charWriter.flush();
		}
		catch (IOException e)
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