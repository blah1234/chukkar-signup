package com.defenestrate.chukkars.shared;

import java.io.Serializable;

public class MessageAdminClientCopy implements Serializable 
{
	//////////////////////////// MEMBER VARIABLES //////////////////////////////
	private Long _id;
	private boolean _weeklyEmailsEnabled = true;
	private String _recipeintEmailAddress;
	private String _signupNotice;
	private String _signupReminder;
	

	/////////////////////////////// CONSTRUCTORS ///////////////////////////////
	public MessageAdminClientCopy(Long id,
			  boolean weeklyEmailsEnabled,
			  String recipientEmailAddress,
			  String signupNotice,
			  String signupReminder)
	{
		_id = id;
		_weeklyEmailsEnabled = weeklyEmailsEnabled;
		_recipeintEmailAddress = recipientEmailAddress;
		_signupNotice = signupNotice;
		_signupReminder = signupReminder;
	}
	
	private MessageAdminClientCopy() 
	{
		//no arg constructor needs to be here for JDO
	}

	
	///////////////////////////////// METHODS //////////////////////////////////
	public Long getId()
	{
		return _id;
	}
	
	public void setId(Long id)
	{
		_id = id;
	}
	
	public boolean isWeeklyEmailsEnabled() 
	{
		return _weeklyEmailsEnabled;
	}

	public void setWeeklyEmailsEnabled(boolean isEnabled) 
	{
		_weeklyEmailsEnabled = isEnabled;
	}

	public String getRecipientEmailAddress() 
	{
		return _recipeintEmailAddress;
	}

	public void setRecipientEmailAddress(String addr) 
	{
		_recipeintEmailAddress = addr;
	}

	public String getSignupNoticeMessage() 
	{
		return _signupNotice;
	}

	public void setSignupNoticeMessage(String msg) 
	{
		_signupNotice = msg;
	}

	public String getSignupReminderMessage() 
	{
		return _signupReminder;
	}

	public void setSignupReminderMessage(String msg) 
	{
		_signupReminder = msg;
	}
}