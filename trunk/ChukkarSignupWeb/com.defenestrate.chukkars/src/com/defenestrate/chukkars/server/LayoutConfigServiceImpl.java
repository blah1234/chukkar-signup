package com.defenestrate.chukkars.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.defenestrate.chukkars.client.LayoutConfigService;
import com.defenestrate.chukkars.server.entity.CronTask;
import com.defenestrate.chukkars.server.entity.DayOfWeek;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class LayoutConfigServiceImpl extends RemoteServiceServlet 
							  		 implements LayoutConfigService 
{
	////////////////////////////////CONSTANTS /////////////////////////////////
	private static final Logger LOG = Logger.getLogger( LayoutConfigServiceImpl.class.getName() );
	
	
	///////////////////////////////// METHODS //////////////////////////////////
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