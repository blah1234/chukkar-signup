package com.defenestrate.chukkars.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

				//also make sure in-memory enumeration is consistent with
				//what was just saved on the server
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
	public Date getSignupClosed()
	{
		return getSignupClosedImpl();
	}

	static public Date getSignupClosedImpl()
	{
		PersistenceManager pm = PersistenceManagerHelper.getPersistenceManager();

		//default is "never"
		Date ret = new Date(Long.MAX_VALUE);

		try
		{
		    Query q = pm.newQuery(CronTask.class);
			q.setFilter("_name == name");
			q.declareParameters("String name");
			q.setUnique(true);
			CronTask currTask = (CronTask)q.execute(CronTask.CLOSE_SIGNUP);

			if(currTask != null)
			{
				ret = currTask.getRunDate();
			}
		}
		catch(Exception e)
		{
			LOG.log(
				Level.SEVERE,
				"Error encountered trying to find task " + CronTask.CLOSE_SIGNUP + ":\n" + e.getMessage(),
				e);
		}
		finally
		{
			pm.close();
		}

		return ret;
	}
}