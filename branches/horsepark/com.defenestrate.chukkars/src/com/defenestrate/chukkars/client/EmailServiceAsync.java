package com.defenestrate.chukkars.client;

import com.defenestrate.chukkars.shared.LoginInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface EmailServiceAsync 
{
	void sendEmail(String recipientAddress, 
				   String subject, 
				   String msgBody, 
				   LoginInfo data, 
				   AsyncCallback<Void> async);
}