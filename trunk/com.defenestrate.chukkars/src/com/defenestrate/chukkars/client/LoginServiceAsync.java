package com.defenestrate.chukkars.client;

import com.defenestrate.chukkars.shared.LoginInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LoginServiceAsync 
{
	void login(String baseRequestUri, String uriToken, AsyncCallback<LoginInfo> async);
}