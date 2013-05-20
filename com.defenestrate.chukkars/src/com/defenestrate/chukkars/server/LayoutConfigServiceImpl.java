package com.defenestrate.chukkars.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import com.defenestrate.chukkars.client.LayoutConfigService;
import com.defenestrate.chukkars.server.entity.CronTask;
import com.defenestrate.chukkars.server.entity.DayOfWeek;
import com.defenestrate.chukkars.shared.Day;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class LayoutConfigServiceImpl extends RemoteServiceServlet
							  		implements LayoutConfigService
{
	////////////////////////////////CONSTANTS /////////////////////////////////
	private static final Logger LOG = Logger.getLogger( LayoutConfigServiceImpl.class.getName() );


	///////////////////////////////// METHODS //////////////////////////////////
	@Override
	public List<String> getActiveDays()
	{
		PersistenceManager pm = PersistenceManagerHelper.getPersistenceManager();

		List<String> retList = new ArrayList<String>();

		try
		{
			Query q = pm.newQuery(DayOfWeek.class);
			q.setFilter("_isEnabled == true");
			List<DayOfWeek> enabledDayList = (List<DayOfWeek>)q.execute();

			for(DayOfWeek currPersistDay : enabledDayList)
			{
				retList.add( currPersistDay.getName() );
			}
		}
		finally
		{
			pm.close();
		}

		return retList;
	}

	@Override
	public void saveDaysConfig(Set<String> activeDayNames)
	{
		Day[] allDays = Day.getAll();
		PersistenceManager pm = PersistenceManagerHelper.getPersistenceManager();

		Day dayHelper = null;
		Transaction tx = null;

		try
		{
			for(Day currDay : allDays)
			{
				dayHelper = currDay;

				tx = pm.currentTransaction();
				tx.begin();

				//find the Day in the DB and persist the new state
				Query q = pm.newQuery(DayOfWeek.class);
				q.setFilter("_numRepresentation == number");
				q.declareParameters("int number");
				q.setUnique(true);
				DayOfWeek persistedDay = (DayOfWeek)q.execute( currDay.getNumber() );

				if(persistedDay != null)
				{
					persistedDay.setEnabled( activeDayNames.contains(currDay.toString()) );
				}
				else
				{
					throw new IllegalArgumentException( "DayOfWeek not found with _numRepresentation = " + currDay.getNumber() );
				}

				tx.commit();

				//also make sure in-memory enumeration on the server
				//is consistent with what was just saved into the DB
				currDay.setEnabled( activeDayNames.contains(currDay.toString()) );
			}

			CronServiceImpl.removeAllPlayersImpl(null);
		}
		catch(Exception e)
		{
			LOG.log(
				Level.SEVERE,
				"Error encountered trying to save the enabled state of " + dayHelper + ":\n" + e.getMessage(),
				e);

		    if( tx.isActive() )
		    {
		        tx.rollback();
		    }

		    throw new RuntimeException(e);
		}
		finally
		{
			pm.close();
		}
	}

	@Override
	public Map<Day, Date> getSignupClosed()
	{
		return getSignupClosedImpl();
	}

	static public Map<Day, Date> getSignupClosedImpl()
	{
		Map<Day, Date> ret = new HashMap<Day, Date>();
		ResourceBundle strings = ResourceBundle.getBundle("com.defenestrate.chukkars.shared.resources.DisplayStrings");
        boolean enableSignupCutoff = Boolean.parseBoolean( strings.getString("enableSignupCutoff") );

        if(enableSignupCutoff)
        {
        		PersistenceManager pm = PersistenceManagerHelper.getPersistenceManager();

			try
			{
			    Query q = pm.newQuery(CronTask.class);
				q.setFilter("_name.startsWith(\"" + CronTask.CLOSE_SIGNUP + "\")");
				List<CronTask> results = (List<CronTask>)q.execute();

				if(results != null)
				{
					for(CronTask currTask : results)
					{
						Day currDay = Day.valueOf(
							Integer.parseInt(currTask.getName().substring(CronTask.CLOSE_SIGNUP.length())) );
						ret.put( currDay, currTask.getRunDate() );
					}
				}
			}
			catch(Exception e)
			{
				LOG.log(
					Level.SEVERE,
					"Error encountered trying to find tasks " + CronTask.CLOSE_SIGNUP + ":\n" + e.getMessage(),
					e);
			}
			finally
			{
				pm.close();
			}
        }
        else
        {
	        	//default is "never"
	    		Date never = CronServiceImpl.getNever();

	        	ret.put(Day.MONDAY, never);
	    		ret.put(Day.TUESDAY, never);
	    		ret.put(Day.WEDNESDAY, never);
	    		ret.put(Day.THURSDAY, never);
	    		ret.put(Day.FRIDAY, never);
	    		ret.put(Day.SATURDAY, never);
	    		ret.put(Day.SUNDAY, never);
        }

		return ret;
	}
}