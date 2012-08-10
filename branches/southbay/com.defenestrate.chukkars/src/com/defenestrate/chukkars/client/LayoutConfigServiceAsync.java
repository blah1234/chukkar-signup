package com.defenestrate.chukkars.client;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.defenestrate.chukkars.shared.Day;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LayoutConfigServiceAsync
{
	void getActiveDays(AsyncCallback<List<String>> async);
	void saveDaysConfig(Set<String> activeDayNames, AsyncCallback<Void> async);
	void getSignupClosed(AsyncCallback<Map<Day, Date>> async);
}