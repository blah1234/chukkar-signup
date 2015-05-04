package com.defenestrate.chukkars.server;

import com.defenestrate.chukkars.server.entity.CronTask;
import com.defenestrate.chukkars.server.entity.DeviceDatastore;
import com.defenestrate.chukkars.server.entity.MessageAdmin;
import com.defenestrate.chukkars.server.gcm.SendMessageServlet;
import com.defenestrate.chukkars.server.gcm.SendMessageServlet.MessageType;
import com.defenestrate.chukkars.shared.Day;
import com.defenestrate.chukkars.shared.Player;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import org.apache.commons.io.IOUtils;

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

public class CronServiceImpl extends HttpServlet
{
	//////////////////////////////// CONSTANTS /////////////////////////////////
	private static final String CUTOFF_DAY_OVERRIDE = "cutoff_day_override";
	private static final String[] REQ_AND_RESP_PARAM_METHODS = {
		"sendSignupReminderEmail",
		"sendExportSignupEmail"
	};

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
			boolean isRequestRequired = false;

			for(String methodName : REQ_AND_RESP_PARAM_METHODS) {
				if( methodName.equals(path) ) {
					isRequestRequired = true;
					break;
				}
			}

			Method invokeMethod;

			if(!isRequestRequired) {
				invokeMethod = this.getClass().getMethod(
					path,
					HttpServletResponse.class);

				invokeMethod.invoke(this, resp);
			} else {
				invokeMethod = this.getClass().getMethod(
					path,
					HttpServletRequest.class,
					HttpServletResponse.class);

				invokeMethod.invoke(this, req, resp);
			}
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
        boolean enablePerDaySignupCutoff = Boolean.parseBoolean( strings.getString("enablePerDaySignupCutoff") );
        Calendar cal = Calendar.getInstance();
		cal.setTimeZone( TimeZone.getTimeZone("America/Los_Angeles") );

        if(enableSignupCutoff)
        {
        	Integer lastDay = null;
    		boolean isOneDayEnabled = false;
    		Day[] allDays = Day.getAll();
    		for(Day currDay : allDays)
    		{
    			if( currDay.isEnabled() )
    			{
    				isOneDayEnabled = true;

    				//log the time when signup should be closed
    				cal.setTime( new Date() );

    				if(lastDay == null || enablePerDaySignupCutoff) {
    					lastDay = getLastSignupDayOfWeek(currDay);
    				}

    				do
    				{
    					cal.add(Calendar.DAY_OF_WEEK, 1);
    				} while(cal.get(Calendar.DAY_OF_WEEK) != lastDay.intValue());

    				cal.set(Calendar.HOUR_OF_DAY, 12);	 //noon
    				cal.set(Calendar.MINUTE, 30);

    				logTask( CronTask.CLOSE_SIGNUP + currDay.getNumber(), cal.getTime() );
    			}
    		}


    		if(!isOneDayEnabled)
    		{
    			//otherwise signup is never closed
        		logTask( CronTask.CLOSE_SIGNUP + 1, getNever() );
    		}
        }
        else
        {
    		//otherwise signup is never closed
    		logTask( CronTask.CLOSE_SIGNUP + 1, getNever() );
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

	/**
	 * @return the programatic representation for "never" in this app
	 */
	static public Date getNever()
	{
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone( TimeZone.getTimeZone("America/Los_Angeles") );
		cal.set(Calendar.YEAR, 9999);
		Date never = cal.getTime();
		return never;
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

			//------------------

			sendAndroidNotification(MessageType.SIGNUP_NOTICE);
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

	private boolean isTodayCutoffDay()
	{
		Calendar nowCal = Calendar.getInstance();
		nowCal.setTimeZone( TimeZone.getTimeZone("America/Los_Angeles") );
		int dayOfWeekNow = nowCal.get(Calendar.DAY_OF_WEEK);

		Day dayNow;

		switch (dayOfWeekNow)
		{
		case Calendar.MONDAY:
			dayNow = Day.MONDAY;
			break;
		case Calendar.TUESDAY:
			dayNow = Day.TUESDAY;
			break;
		case Calendar.WEDNESDAY:
			dayNow = Day.WEDNESDAY;
			break;
		case Calendar.THURSDAY:
			dayNow = Day.THURSDAY;
			break;
		case Calendar.FRIDAY:
			dayNow = Day.FRIDAY;
			break;
		case Calendar.SATURDAY:
			dayNow = Day.SATURDAY;
			break;
		case Calendar.SUNDAY:
			dayNow = Day.SUNDAY;
			break;
		default:
			throw new IllegalArgumentException("dayOfWeekNow not recognized: " + dayOfWeekNow);
		}

		Day possibleGameDay = dayNow.tomorrow();
		boolean isGameDay = possibleGameDay.isEnabled();

		ResourceBundle strings = ResourceBundle.getBundle("com.defenestrate.chukkars.shared.resources.DisplayStrings");
        boolean enablePerDaySignupCutoff = Boolean.parseBoolean( strings.getString("enablePerDaySignupCutoff") );

        if(enablePerDaySignupCutoff)
        {
        	return isGameDay;
        }
        else
        {
        	boolean isFirstGameDay = possibleGameDay.isFirstGameDayOfTheWeek();
			return isGameDay && isFirstGameDay;
		}
	}

	public void sendSignupReminderEmail(HttpServletRequest req, HttpServletResponse resp) throws ServletException
	{
		//Determine if today is the cutoff day
		boolean isCutoff = isTodayCutoffDay();

		//Determine if cutoff override is in params
		String[] values = req.getParameterValues(CUTOFF_DAY_OVERRIDE);
		boolean isCutoffOverride = false;

		if( (values != null) && (values.length > 0) ) {
			isCutoffOverride = Boolean.parseBoolean( values[0] );
		}

		MessageAdmin data = null;

		if(isCutoff || isCutoffOverride)
		{
			data = getEnabledMessageAdmin();

			if(data != null)
			{
				String msgBody = data.getSignupReminderMessage();
				ResourceBundle strings = ResourceBundle.getBundle("com.defenestrate.chukkars.shared.resources.DisplayStrings");
				EmailServiceImpl.sendEmail(strings.getString("clubAbbreviation") + " signup by 12 noon", msgBody, data, false);

				//----------------

				sendAndroidNotification(MessageType.SIGNUP_REMINDER);
			}
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
			else if(isCutoff || isCutoffOverride)
			{
				msg = "Weekly emails are not enabled. No email sent.";
			}
			else
			{
				msg = "Today is not the cutoff day for signups. No email sent.";
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

	private void sendAndroidNotification(MessageType msgType) {
		List<String> devices = DeviceDatastore.getDevices();

		if( !devices.isEmpty() ) {
			Queue queue = QueueFactory.getQueue(SendMessageServlet.GCM_QUEUE_NAME);
			// NOTE: check below is for demonstration purposes; a real
			// application could always send a multicast, even for just one recipient
			if (devices.size() == 1) {
				String device = devices.get(0);

				// send a single message using plain post
				TaskOptions opts = TaskOptions.Builder.withUrl(SendMessageServlet.SEND_URL)
					.param(SendMessageServlet.PARAMETER_DEVICE, device)
					.param(SendMessageServlet.PARAMETER_COLLAPSE_KEY, SendMessageServlet.COLLAPSE_KEY_VALUE)
					.method(TaskOptions.Method.POST);

				if(msgType == MessageType.SIGNUP_NOTICE) {
					opts.param(SendMessageServlet.PARAMETER_MESSAGE_TYPE, MessageType.SIGNUP_NOTICE.toString());
					opts.param(SendMessageServlet.PARAMETER_REMINDER_DAY_OF_WEEK, Integer.toString(Calendar.TUESDAY));
				} else if(msgType == MessageType.SIGNUP_REMINDER) {
					opts.param(SendMessageServlet.PARAMETER_MESSAGE_TYPE, MessageType.SIGNUP_REMINDER.toString());
				}

				queue.add(opts);
			} else {
				// send a multicast message using JSON
				// must split in chunks of 1000 devices (GCM limit)
				int total = devices.size();
				List<String> partialDevices = new ArrayList<String>(total);
				int counter = 0;
				int tasks = 0;

				for (String device : devices) {
					counter++;
					partialDevices.add(device);
					int partialSize = partialDevices.size();

					if(partialSize == DeviceDatastore.MULTICAST_SIZE || counter == total) {
						String multicastKey = DeviceDatastore.createMulticast(partialDevices);

						LOG.fine("Queuing " + partialSize + " devices on multicast " + multicastKey);

						TaskOptions taskOptions = TaskOptions.Builder.withUrl(SendMessageServlet.SEND_URL)
							.param(SendMessageServlet.PARAMETER_MULTICAST, multicastKey)
							.param(SendMessageServlet.PARAMETER_COLLAPSE_KEY, SendMessageServlet.COLLAPSE_KEY_VALUE)
							.method(TaskOptions.Method.POST);

						if(msgType == MessageType.SIGNUP_NOTICE) {
							taskOptions.param(SendMessageServlet.PARAMETER_MESSAGE_TYPE, MessageType.SIGNUP_NOTICE.toString());

							Day[] allDays = Day.getAll();
							Day firstGameDay = null;

							for(Day currDay : allDays) {
								if( currDay.isEnabled() ) {
									firstGameDay = currDay;
									break;
								}
							}

							if(firstGameDay != null) {
								taskOptions.param(SendMessageServlet.PARAMETER_REMINDER_DAY_OF_WEEK, Integer.toString(firstGameDay.toJavaUtilCalendarDay()));
							}
						} else if(msgType == MessageType.SIGNUP_REMINDER) {
							taskOptions.param(SendMessageServlet.PARAMETER_MESSAGE_TYPE, MessageType.SIGNUP_REMINDER.toString());
						}

						queue.add(taskOptions);
						partialDevices.clear();
						tasks++;
					}
				}
			}
		}
	}

	public void sendExportSignupEmail(HttpServletRequest req, HttpServletResponse resp) throws ServletException
	{
		//Determine if today is the cutoff day
		boolean isCutoff = isTodayCutoffDay();

		//Determine if cutoff override is in params
		String[] values = req.getParameterValues(CUTOFF_DAY_OVERRIDE);
		boolean isCutoffOverride = false;

		if( (values != null) && (values.length > 0) ) {
			isCutoffOverride = Boolean.parseBoolean( values[0] );
		}

		MessageAdmin data = null;
		String managerEmails = null;

		if(isCutoff || isCutoffOverride)
		{
			data = getEnabledMessageAdmin();

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
				buf.append("<head>");
				buf.append("<style type=\"text/css\">");
				buf.append("table.mystyle {");
				buf.append("border-width: 0 0 1px 1px;");
				buf.append("border-spacing: 0;");
				buf.append("border-collapse: collapse;");
				buf.append("border-style: solid;}");
				buf.append(".mystyle td, .mystyle th {");
				buf.append("margin: 0;");
				buf.append("padding: 4px;");
				buf.append("border-width: 1px 1px 0 0;");
				buf.append("border-style: solid;}");
				buf.append("</style></head>");
				buf.append("<body>");

				for( Day currDay : dayToPlayers.keySet() )
				{
					buf.append("<h2>");
					buf.append(currDay);
					buf.append(":</h2>");

					List<Player> valList = dayToPlayers.get(currDay);
					ChukkarCount counts = calculateGameChukkars(valList);

					buf.append("<table>");
					buf.append("<tr>");
					buf.append("<th align=\"left\"># Chukkars Total:</th>");
					buf.append("<td align=\"left\">");
					buf.append(counts._numTotalChukkars);
					buf.append("</td></tr>");

					buf.append("<tr align=\"left\">");
					buf.append("<th># Game Chukkars:</th>");
					buf.append("<td align=\"left\">");
					buf.append(counts._numGameChukkars);
					buf.append("</td></tr></table>");
					buf.append("\n");

					buf.append("<table class=\"mystyle\">");

					for(Player currPlayer : valList)
					{
						buf.append("<tr>");

						buf.append("<td align=\"left\">");
						buf.append( currPlayer.getName() );
						buf.append("</td>");

						buf.append("<td align=\"right\">");
						buf.append( currPlayer.getChukkarCount() );
						buf.append("</td>");

						buf.append("</tr>");
					}

					buf.append("</table>");

					buf.append("\n<hr>\n");
				}

				if(buf.length() == 0) {
					buf.append("Nobody signed up for " + Day.valueOf((nowDay.getNumber() % 7) + 1) + " through the end of the week!");
				}

				buf.append("</body>");

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
			else if(isCutoff || isCutoffOverride)
			{
				msg = "Weekly emails are not enabled. No email sent.";
			}
			else
			{
				msg = "Today is not the cutoff day for signups. No email sent.";
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