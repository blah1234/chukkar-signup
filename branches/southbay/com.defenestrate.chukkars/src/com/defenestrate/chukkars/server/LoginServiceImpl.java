package com.defenestrate.chukkars.server;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.commons.lang.StringUtils;

import com.defenestrate.chukkars.client.LoginService;
import com.defenestrate.chukkars.shared.LoginInfo;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class LoginServiceImpl extends RemoteServiceServlet 
							  implements LoginService 
{
	///////////////////////////////// METHODS //////////////////////////////////
	public LoginInfo login(String baseRequestUri, String uriToken) 
	{
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		LoginInfo loginInfo;

		if(user != null) 
		{
			loginInfo = findPersistentLogin(user);
			
			if(loginInfo != null)
			{
				loginInfo.setIsAdmin(true);
			}
			else
			{
				//user is not an admin persisted to the datastore
				loginInfo = new LoginInfo();
				loginInfo.setIsAdmin(false);
				loginInfo.setEmailAddress( user.getEmail() );
				loginInfo.setNickname( user.getNickname() );
			}
			
			loginInfo.setLoggedIn(true);
			loginInfo.setLogoutUrl( userService.createLogoutURL(baseRequestUri) );
		}
		else 
		{
			loginInfo = new LoginInfo();
			loginInfo.setLoggedIn(false);
			
			String requestURI = StringUtils.isNotBlank(uriToken) ?
				baseRequestUri + "#" + uriToken :
				baseRequestUri;
			
			loginInfo.setLoginUrl( userService.createLoginURL(requestURI) );
		}
    
		return loginInfo;
	}
	
	public LoginInfo findPersistentLogin(User currUser)
	{
		LoginInfo ret = null;
		
		PersistenceManager pm = PersistenceManagerHelper.getPersistenceManager();
	    
		try 
		{
			Query q = pm.newQuery(LoginInfo.class);
			q.setFilter("_emailAddress == addr && _nickname == name");
			q.declareParameters("String addr, String name");
			q.setUnique(true);
			LoginInfo persistUser = (LoginInfo)
				q.execute( currUser.getEmail(), currUser.getNickname() );
			
			if(persistUser != null) 
			{
				ret = pm.detachCopy(persistUser);
			}
		} 
		finally 
		{
			pm.close();
		}
		
		return ret;
	}
}