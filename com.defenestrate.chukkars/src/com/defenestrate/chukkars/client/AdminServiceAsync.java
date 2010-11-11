package com.defenestrate.chukkars.client;

import com.defenestrate.chukkars.shared.LoginInfo;
import com.defenestrate.chukkars.shared.MessageAdminClientCopy;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AdminServiceAsync 
{
	void getMessageData(LoginInfo currLogin, AsyncCallback<MessageAdminClientCopy> async);
	void saveMessageData(MessageAdminClientCopy data, AsyncCallback<Void> async);
}