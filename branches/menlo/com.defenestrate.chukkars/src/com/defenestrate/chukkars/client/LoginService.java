package com.defenestrate.chukkars.client;

import com.defenestrate.chukkars.shared.LoginInfo;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("login")
public interface LoginService extends RemoteService 
{
	LoginInfo login(String baseRequestUri, String uriToken);
}