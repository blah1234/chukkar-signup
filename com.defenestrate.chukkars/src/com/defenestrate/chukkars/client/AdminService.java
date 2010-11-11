package com.defenestrate.chukkars.client;

import com.defenestrate.chukkars.shared.LoginInfo;
import com.defenestrate.chukkars.shared.MessageAdminClientCopy;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("admin")
public interface AdminService extends RemoteService 
{
	MessageAdminClientCopy getMessageData(LoginInfo currLogin);
	void saveMessageData(MessageAdminClientCopy data);
}