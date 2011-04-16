package com.defenestrate.chukkars.server;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.defenestrate.chukkars.client.LayoutConfigService;
import com.defenestrate.chukkars.server.entity.DayOfWeek;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class LayoutConfigServiceImpl extends RemoteServiceServlet 
							  		 implements LayoutConfigService 
{
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
}