package com.defenestrate.chukkars.client;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.defenestrate.chukkars.shared.Day;
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
	 * Saves the state of the {@link Day} enumeration to <code>DayOfWeek</code>
	 * entities in the DB.
	 * @param activeDayNames set of names of all active days
	 */
	void saveDaysConfig(Set<String> activeDayNames);

	/**
	 * Returns the date and time past which signup is closed
	 * @return map of date and time past which signup is closed
	 */
	Map<Day, Date> getSignupClosed();
}