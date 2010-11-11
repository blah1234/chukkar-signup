package com.defenestrate.chukkars.shared;

import java.io.Serializable;
import java.util.Random;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Unique;


@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
@Unique(name="Unq_LoginInfo_EmailAddress", members={"_emailAddress"})
public class LoginInfo implements Serializable 
{
	//////////////////////////////// CONSTANTS /////////////////////////////////
	static private final Random ID_GEN = new Random();


	//////////////////////////// MEMBER VARIABLES //////////////////////////////
	//this is the system-generated key 
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    @Extension(vendorName="datanucleus", key="gae.encoded-pk", value="true")
	private String _id;
	
	//this is the "name" for the system-generated key
	@Persistent
    @Extension(vendorName="datanucleus", key="gae.pk-name", value="true")
    private String keyName = Long.toString( ID_GEN.nextLong() );
	
	@NotPersistent
	private boolean _loggedIn = false;
	
	@NotPersistent
	private boolean _isAdmin = false;
	
	@NotPersistent
	private String _loginUrl;
	
	@NotPersistent
	private String _logoutUrl;
	
	@Column(sqlType = "varchar(64)", name = "LoginInfo_EmailAddress")
	private String _emailAddress;
	
	@Column(name = "LoginInfo_Nickname")
	private String _nickname;

	
	///////////////////////////////// METHODS //////////////////////////////////
	public boolean isAdmin()
	{
		return _isAdmin;
	}

	public void setIsAdmin(boolean isAdmin)
	{
		_isAdmin = isAdmin;
	}
	
	public boolean isLoggedIn() 
	{
		return _loggedIn;
	}

	public void setLoggedIn(boolean loggedIn) 
	{
		_loggedIn = loggedIn;
	}

	public String getLoginUrl() 
	{
		return _loginUrl;
	}

	public void setLoginUrl(String loginUrl) 
	{
		_loginUrl = loginUrl;
	}

	public String getLogoutUrl() 
	{
		return _logoutUrl;
	}

	public void setLogoutUrl(String logoutUrl) 
	{
		_logoutUrl = logoutUrl;
	}

	public String getEmailAddress() 
	{
		return _emailAddress;
	}

	public void setEmailAddress(String emailAddress) 
	{
		_emailAddress = emailAddress;
	}

	public String getNickname() 
	{
		return _nickname;
	}

	public void setNickname(String nickname) 
	{
		_nickname = nickname;
	}
}