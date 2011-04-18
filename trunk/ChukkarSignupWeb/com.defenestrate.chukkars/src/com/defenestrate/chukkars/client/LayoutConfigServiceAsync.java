package com.defenestrate.chukkars.client;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LayoutConfigServiceAsync 
{
	void getActiveDays(AsyncCallback<List<String>> async);
	void getSignupClosed(AsyncCallback<Date> async );
}