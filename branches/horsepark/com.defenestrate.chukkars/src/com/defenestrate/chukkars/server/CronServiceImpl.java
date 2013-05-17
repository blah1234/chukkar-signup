package com.defenestrate.chukkars.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
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
	@Override
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
		removeAllPlayersImpl(resp);
	}

	static public void removeAllPlayersImpl(HttpServletResponse resp) throws ServletException
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

		//-----------------------
		//reset all CronTasks as well

		pm = PersistenceManagerHelper.getPersistenceManager();

		try
		{
			Extent<CronTask> e = pm.getExtent(CronTask.class);
			Iterator<CronTask> iter = e.iterator();

			while( iter.hasNext() )
			{
				CronTask delTask = iter.next();
				pm.deletePersistent(delTask);
			}
		}
		catch(Exception e)
		{
			LOG.log(
				Level.SEVERE,
				"Error encountered trying to remove all cron tasks:\n" + e.getMessage(),
				e);

			//display stack trace to browser
			throw new ServletException(e);
		}
		finally
		{
			pm.close();
		}

		//-------------------

		logTask(CronTask.RESET);

		ResourceBundle strings = ResourceBundle.getBundle("com.defenestrate.chukkars.shared.resources.DisplayStrings");
        boolean enableSignupCutoff = Boolean.parseBoolean( strings.getString("enableSignupCutoff") );
        Calendar cal = Calendar.getInstance();
		cal.setTimeZone( TimeZone.getTimeZone("America/Los_Angeles") );

        if(enableSignupCutoff)
        {
        		boolean isOneDayEnabled = false;
        		Day[] allDays = Day.getAll();
        		for(Day currDay : allDays)
        		{
        			if( currDay.isEnabled() )
        			{
        				isOneDayEnabled = true;

        				//log the time when signup should be closed
        				cal.setTime( new Date() );

        				int lastDay = getLastSignupDayOfWeek(currDay);

        				do
        				{
        					cal.add(Calendar.DAY_OF_WEEK, 1);
        				} while(cal.get(Calendar.DAY_OF_WEEK) != lastDay);

        				cal.set(Calendar.HOUR_OF_DAY, 12);	 //noon
        				cal.set(Calendar.MINUTE, 30);

        				logTask( CronTask.CLOSE_SIGNUP + currDay.getNumber(), cal.getTime() );
        			}
        		}


        		if(!isOneDayEnabled)
        		{
        			//otherwise signup is never closed
            		cal.set(Calendar.YEAR, 9999);
            		logTask( CronTask.CLOSE_SIGNUP + 1, cal.getTime() );
        		}
        }
        else
        {
        		//otherwise signup is never closed
        		cal.set(Calendar.YEAR, 9999);
        		logTask( CronTask.CLOSE_SIGNUP + 1, cal.getTime() );
        }

		//------------------

		if(resp != null)
		{
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
	}

	static private void logTask(String taskName) throws ServletException
	{
		logTask(taskName, null);
	}

	static private void logTask(String taskName, Date runDate) throws ServletException
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

		    throw new ServletException(e);
		}
		finally
		{
			pm.close();
		}
	}

	static private int getLastSignupDayOfWeek()
	{
		//Get the first day in the week that a game will be played
		Day[] allDays = Day.getAll();
		for(Day currDay : allDays)
		{
			if( currDay.isEnabled() )
			{
				return getLastSignupDayOfWeek(currDay);
			}
		}

		//default: return Sunday, if no days of the week are being played
		return Calendar.SUNDAY;
	}

	/**
	 * Returns the day immediately before the specified first game day
	 * of the week.
	 */
	static private int getLastSignupDayOfWeek(Day firstGameDayOfWeek)
	{
		if(firstGameDayOfWeek == Day.MONDAY)
		{
			return Calendar.SUNDAY;
		}
		else if(firstGameDayOfWeek == Day.TUESDAY)
		{
			return Calendar.MONDAY;
		}
		else if(firstGameDayOfWeek == Day.WEDNESDAY)
		{
			return Calendar.TUESDAY;
		}
		else if(firstGameDayOfWeek == Day.THURSDAY)
		{
			return Calendar.WEDNESDAY;
		}
		else if(firstGameDayOfWeek == Day.FRIDAY)
		{
			return Calendar.THURSDAY;
		}
		else if(firstGameDayOfWeek == Day.SATURDAY)
		{
			return Calendar.FRIDAY;
		}
		else if(firstGameDayOfWeek == Day.SUNDAY)
		{
			return Calendar.SATURDAY;
		}
		else
		{
			throw new IllegalArgumentException("firstGameDayOfWeek not recognized: " + firstGameDayOfWeek);
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
			ResourceBundle strings = ResourceBundle.getBundle("com.defenestrate.chukkars.shared.resources.DisplayStrings");
			EmailServiceImpl.sendEmail(strings.getString("clubAbbreviation") + " signup for the upcoming week", msgBody, data, false);
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
			ResourceBundle strings = ResourceBundle.getBundle("com.defenestrate.chukkars.shared.resources.DisplayStrings");
			EmailServiceImpl.sendEmail(strings.getString("clubAbbreviation") + " signup by 12 noon", msgBody, data, false);
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
		String managerEmails = null;

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

					EmailServiceImpl.sendEmail("hwang.shawn@gmail.com", "Export signup error", strWrite.toString(), data, false);
				}
				finally
				{
					IOUtils.closeQuietly(pWrite);
				}
			}


			Comparator<Day> dayComp = new Comparator<Day>()
			{
				@Override
				public int compare(Day o1, Day o2)
				{
					int ret = o1.getNumber() - o2.getNumber();
					return ret;
				}
			};
			Map<Day, List<Player>> dayToPlayers = new TreeMap<Day, List<Player>>(dayComp);
			Calendar nowCal = Calendar.getInstance();
			nowCal.setTimeZone( TimeZone.getTimeZone("America/Los_Angeles") );
			nowCal.setTime( new Date() );
			Day nowDay = calendarDayOfWeekToDay( nowCal.get(Calendar.DAY_OF_WEEK) );

			for(Player currPlayer : allPlayersList)
			{
				Day currDay = currPlayer.getRequestDay();

				//TODO: BelAir: don't show days that are already in the past
				//if( nowDay.getNumber() <= currDay.getNumber() )
				//TODO: Menlo & HPPC: only show days in the future
				if( (nowDay.getNumber() % 7) < currDay.getNumber() )
				{
					//% 7 so that when export is sent on Sun (number
					//representation = 7), Mon (number = 1) will be
					//counted as a day in the future.
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
			}


			StringBuffer buf = new StringBuffer();

			for( Day currDay : dayToPlayers.keySet() )
			{
				buf.append(currDay);
				buf.append(":\n");

				List<Player> valList = dayToPlayers.get(currDay);
				ChukkarCount counts = calculateGameChukkars(valList);

				buf.append("# Chukkars Total: ");
				buf.append(counts._numTotalChukkars);
				buf.append("\n");
				buf.append("# Game Chukkars: ");
				buf.append(counts._numGameChukkars);
				buf.append("\n\n");

				for(Player currPlayer : valList)
				{
					buf.append( currPlayer.getName() );
					buf.append("\t");
					buf.append( currPlayer.getChukkarCount() );
					buf.append("\n");
				}

				buf.append("\n\n\n");
			}

			if(buf.length() == 0) {
				buf.append("Nobody signed up for " + Day.valueOf((nowDay.getNumber() % 7) + 1) + " through the end of the week!");
			}

			DateFormat outFormatter = new SimpleDateFormat("EEE, M/d h:mm a");
            outFormatter.setTimeZone( TimeZone.getTimeZone("America/Los_Angeles") );
            ResourceBundle strings = ResourceBundle.getBundle("com.defenestrate.chukkars.shared.resources.DisplayStrings");
            String subject = strings.getString("clubAbbreviation") + " chukkar signups: " + outFormatter.format( new Date() );

            managerEmails = strings.getString("managerEmail");
            String[] mgrEmailArray = managerEmails.split(",");

            for(String currEmail : mgrEmailArray)
            {
            		EmailServiceImpl.sendEmail(currEmail.trim(), subject, buf.toString(), data, true);
            }
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
				msg = "Signup export email successfully sent to " + managerEmails;
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

	private Day calendarDayOfWeekToDay(int calendarDayOfWeek)
	{
		switch (calendarDayOfWeek)
		{
		case Calendar.MONDAY:
			return Day.MONDAY;
		case Calendar.TUESDAY:
			return Day.TUESDAY;
		case Calendar.WEDNESDAY:
			return Day.WEDNESDAY;
		case Calendar.THURSDAY:
			return Day.THURSDAY;
		case Calendar.FRIDAY:
			return Day.FRIDAY;
		case Calendar.SATURDAY:
			return Day.SATURDAY;
		case Calendar.SUNDAY:
			return Day.SUNDAY;
		default:
			throw new IllegalArgumentException("calendarDayOfWeek not recognized: " + calendarDayOfWeek);
		}
	}

	private ChukkarCount calculateGameChukkars(List<Player> allDayPlayers)
	{
		int totalChukkars = 0;
		int totalPlayers = allDayPlayers.size();

		for(Player currPlayer : allDayPlayers)
		{
			int currChukkarCount = currPlayer.getChukkarCount();

			if(currChukkarCount == 0)
			{
				totalPlayers--;
			}

			totalChukkars += currChukkarCount;
		}

		//------------------------

		ResourceBundle strings = ResourceBundle.getBundle("com.defenestrate.chukkars.shared.resources.DisplayStrings");
		int playersPerChukkar = Integer.parseInt( strings.getString("playersPerChukkar") );
		int minPlayersPerChukkar = Integer.parseInt( strings.getString("minPlayersPerChukkar") );
		int numGameChukkars;

		if(totalPlayers < minPlayersPerChukkar)
		{
			numGameChukkars = 0;
		}
		else if(totalPlayers >= playersPerChukkar)
		{
			numGameChukkars = totalChukkars / playersPerChukkar;
		}
		else
		{
			numGameChukkars = totalChukkars / minPlayersPerChukkar;
		}


		ChukkarCount ret = new ChukkarCount(totalChukkars, numGameChukkars);
		return ret;
	}


	////////////////////////////// INNER CLASSES ///////////////////////////////
	private class ChukkarCount
	{
		int _numTotalChukkars;
		int _numGameChukkars;


		ChukkarCount(int numTotalChukkars, int numGameChukkars)
		{
			_numTotalChukkars = numTotalChukkars;
			_numGameChukkars = numGameChukkars;
		}
	}
}