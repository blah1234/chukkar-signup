package com.defenestrate.chukkars.client;

import com.defenestrate.chukkars.shared.Day;
import com.defenestrate.chukkars.shared.LoginInfo;
import com.defenestrate.chukkars.shared.MessageAdminClientCopy;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AdminServiceAsync
{
	void getMessageData(LoginInfo currLogin, AsyncCallback<MessageAdminClientCopy> async);
	void saveMessageData(MessageAdminClientCopy data, AsyncCallback<Void> async);
	void exportSignups(AsyncCallback<String> async);
	void importLineup(Day dayOfWeek, AsyncCallback<String> async);
	void createAdminUser(String emailAddress, String nickname, AsyncCallback<Void> async);
	void doAdminsExist(AsyncCallback<Boolean> async);
}