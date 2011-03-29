package com.defenestrate.chukkars.server;

import java.io.Serializable;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.defenestrate.chukkars.shared.LoginInfo;
import com.google.appengine.api.datastore.Text;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class MessageAdmin implements Serializable 
{
	//////////////////////////// MEMBER VARIABLES //////////////////////////////
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long _id;
	
	@Persistent(defaultFetchGroup = "true")
	private LoginInfo _loginInfo;
	
	@Persistent
	@Column(name = "MessageAdmin_EnableWeeklyEmails")
	private boolean _weeklyEmailsEnabled = true;
	
	@Persistent
	@Column(name = "MessageAdmin_RecipientEmailAddress")
	private String _recipeintEmailAddress;
	
	@Persistent
	@Column(name = "MessageAdmin_SignupNotice")
	private Text _signupNotice;
	
	@Persistent
	@Column(name = "MessageAdmin_SignupReminder")
	private Text _signupReminder;

	
	///////////////////////////////// METHODS //////////////////////////////////
	public Long getId()
	{
		return _id;
	}
	
	public LoginInfo getAdmin()
	{
		return _loginInfo;
	}

	public void setAdmin(LoginInfo admin)
	{
		_loginInfo = admin;
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
		return _signupNotice.getValue();
	}

	public void setSignupNoticeMessage(String msg) 
	{
		_signupNotice = new Text(msg);
	}

	public String getSignupReminderMessage() 
	{
		return _signupReminder.getValue();
	}

	public void setSignupReminderMessage(String msg) 
	{
		_signupReminder = new Text(msg);
	}
}