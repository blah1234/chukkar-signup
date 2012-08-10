package com.defenestrate.chukkars.server.listener;

import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.defenestrate.chukkars.server.PersistenceManagerHelper;
import com.defenestrate.chukkars.server.entity.DayOfWeek;
import com.defenestrate.chukkars.shared.Day;

public class ServerStartupListener implements ServletContextListener
{
	//////////////////////////////// CONSTANTS /////////////////////////////////
	static private final Logger LOG = Logger.getLogger( ServerStartupListener.class.getName() );


	///////////////////////////////// METHODS //////////////////////////////////
	/**
	 * This method is invoked when the Web Application is ready to service requests
	 */
	@Override
	public void contextInitialized(ServletContextEvent event)
	{
		loadDaysConfiguration();
	}

	static public void loadDaysConfiguration()
	{
		//initialize persisted state of Day enums
		Day[] allDays = Day.getAll();

		//find the Day in the DB and copy over persisted state
		PersistenceManager pm = PersistenceManagerHelper.getPersistenceManager();

		try
		{
			for(Day currDay : allDays)
			{
				Query q = pm.newQuery(DayOfWeek.class);
				q.setFilter("_numRepresentation == number");
				q.declareParameters("int number");
				q.setUnique(true);
				DayOfWeek persistedDay = (DayOfWeek)q.execute( currDay.getNumber() );

				if(persistedDay != null)
				{
					currDay.setEnabled( persistedDay.isEnabled() );
				}
				else
				{
					//go ahead and create the Day in the DB
					LOG.info("Did not find : " + currDay + " in DB. Creating it now.");

					DayOfWeek newPersistDay = new DayOfWeek(
						currDay.toString(),
						currDay.getNumber(),
						currDay.isEnabled() );

					pm.makePersistent(newPersistDay);
				}
			}
		}
		finally
		{
			pm.close();
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent event)
	{
	}
}
