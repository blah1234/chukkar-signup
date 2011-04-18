package com.defenestrate.chukkars.client;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("layoutConfig")
public interface LayoutConfigService extends RemoteService 
{
	/**
	 * Returns a list strings representing all days marked active in the DB. 
	 * No guarantee is made as to the order of the day strings within the 
	 * returned list.
	 * @return a list of all days marked active in the DB.
	 */
	List<String> getActiveDays();
	
	/**
	 * Returns the date and time past which signup is closed  
	 * @return the date and time past which signup is closed
	 */
	Date getSignupClosed();
}