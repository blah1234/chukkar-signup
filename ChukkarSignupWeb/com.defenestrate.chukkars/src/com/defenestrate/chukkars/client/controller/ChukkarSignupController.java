package com.defenestrate.chukkars.client.controller;

import java.util.List;

import com.defenestrate.chukkars.client.AdminService;
import com.defenestrate.chukkars.client.AdminServiceAsync;
import com.defenestrate.chukkars.client.EmailService;
import com.defenestrate.chukkars.client.EmailServiceAsync;
import com.defenestrate.chukkars.client.LayoutConfigService;
import com.defenestrate.chukkars.client.LayoutConfigServiceAsync;
import com.defenestrate.chukkars.client.LoginService;
import com.defenestrate.chukkars.client.LoginServiceAsync;
import com.defenestrate.chukkars.client.PlayerService;
import com.defenestrate.chukkars.client.PlayerServiceAsync;
import com.defenestrate.chukkars.client.SignupView;
import com.defenestrate.chukkars.shared.Day;
import com.defenestrate.chukkars.shared.LoginInfo;
import com.defenestrate.chukkars.shared.MessageAdminClientCopy;
import com.defenestrate.chukkars.shared.Player;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ChukkarSignupController
{
	//////////////////////////// MEMBER VARIABLES //////////////////////////////
	private SignupView _view;
	private PlayerServiceAsync _playerSvc;
	private LoginServiceAsync _loginSvc;
	private AdminServiceAsync _adminSvc;
	private EmailServiceAsync _emailSvc;
	private LayoutConfigServiceAsync _configSvc;
	

	/////////////////////////////// CONSTRUCTORS ///////////////////////////////
	public ChukkarSignupController(SignupView view)
	{
		_view = view;
		_playerSvc = GWT.create(PlayerService.class);
		_loginSvc = GWT.create(LoginService.class);
		_adminSvc = GWT.create(AdminService.class);
		_emailSvc = GWT.create(EmailService.class);
		_configSvc = GWT.create(LayoutConfigService.class);
	}
	

	///////////////////////////////// METHODS //////////////////////////////////
	public void configActiveDays()
	{
		_view.setBusy(true, true);
		
	    AsyncCallback<List<String>> async = new AsyncCallback<List<String>>() 
	    {
	    	public void onFailure(Throwable error) 
	    	{
    			StackTraceElement[] elems = error.getStackTrace();
    			StringBuffer buf = new StringBuffer();
    			buf.append( error.getMessage() );
    			buf.append("\n");
    			
    			for(StackTraceElement currElem : elems)
    			{
	    			buf.append( currElem.toString() );
	    			buf.append("\n");
	    		}
    			
    			_view.onPostInit();
    			_view.showErrorDialog(buf.toString(), null, error);
	    	}

	    	public void onSuccess(List<String> result) 
	    	{
	    		for(String currStr : result)
	    		{
	    			Day currDay = Day.valueOf(currStr);
	    			currDay.setEnabled(true);
	    		}
	    		
	    		_view.onPostInit();
	    	}
	    };
	    
	    _configSvc.getActiveDays(async);
	}
	
	public void loginRequest(final String initToken)
	{
		// Check login status using login service.
	    AsyncCallback<LoginInfo> async = new AsyncCallback<LoginInfo>() 
	    {
	    	public void onFailure(Throwable error) 
	    	{
	    		//render regular signup page by default
    			_view.renderModule("");
	    	}

	    	public void onSuccess(LoginInfo result) 
	    	{
	    		_view.setLoginInfo(result);
	    		
	    		if( !result.isLoggedIn() &&
	    			(initToken.equals("login") || initToken.equals("email") || initToken.equals("lineup") || initToken.equals("accounts")) )
	    		{
	    			_view.loadLogin( result.getLoginUrl() );
	    		}
	    		else
	    		{
	    			//regular signup page by default
	    			_view.renderModule(initToken);
	    		}
	    	}
	    };
	    
	    _loginSvc.login(GWT.getHostPageBaseURL(), initToken, async);
	}
	
	public void addPlayer(final Day dayOfWeek)
	{
		_view.setBusy(true);
		_view.stopEditingChukkars(dayOfWeek, false);
	    String playerName = _view.getPlayerNameToBeAdded(dayOfWeek);
	    
	    if( (playerName != null) && (playerName.length() > 0) )
	    {
		    String numChukkarsStr = _view.getChukkarCountToBeAdded(dayOfWeek);
		    int numChukkars;
		    
		    try
		    {
		    	numChukkars = Integer.parseInt(numChukkarsStr);
		    	if(numChukkars < 0)
		    	{
		    		throw new NumberFormatException("Negative numbers are not allowed.");
		    	}
		    	
		    	AsyncCallback<Player> callback = new AsyncCallback<Player>() 
			    {
			    	public void onFailure(Throwable caught) 
			    	{
			    		//TODO log error?
			    		_view.setBusy(false);
			    	}

			    	public void onSuccess(Player player) 
			    	{
			    		_view.addRow( dayOfWeek, player.getId(), player.getCreateDate() );
			    		_view.setBusy(false);
			    	}
			    };
			    
		    	_playerSvc.addPlayer(playerName, numChukkars, dayOfWeek, callback);
		    }
		    catch(NumberFormatException e)
		    {
		    	_view.setBusy(false);
		    	_view.showErrorDialog("Enter a valid number for chukkar count.", dayOfWeek, e);	
		    }
	    }
	    else
	    {
	    	_view.setBusy(false);
	    	_view.showErrorDialog("Player name cannot be blank.", dayOfWeek, null);
	    }
	}
	
	public void editChukkars(final Day dayOfWeek)
	{
		Long playerId = _view.getEditingPlayerId(dayOfWeek);
		editChukkars(playerId, dayOfWeek);
	}
	
	public void editChukkars(Long playerId, final Day dayOfWeek)
	{
		_view.setBusy(true);
	    
	    if(playerId != null)
	    {
		    String numChukkarsStr = _view.getEditedChukkarCount();
		    int numChukkars;
		    
		    try
		    {
		    	numChukkars = Integer.parseInt(numChukkarsStr);
		    	if(numChukkars < 0)
		    	{
		    		throw new NumberFormatException("Negative numbers are not allowed.");
		    	}
		    	
		    	AsyncCallback<Void> callback = new AsyncCallback<Void>() 
			    {
			    	public void onFailure(Throwable caught) 
			    	{
			    		_view.stopEditingChukkars(dayOfWeek, false);
			    		_view.setBusy(false);
			    		_view.showErrorDialog("Unable to change # of chukkars. The humans behind this program are busy catching bugs and appreciate your patience.", dayOfWeek, null);
			    	}

			    	public void onSuccess(Void ignore) 
			    	{
			    		_view.stopEditingChukkars(dayOfWeek, true);
			    		_view.setBusy(false);
			    	}
			    };
			    
		    	_playerSvc.editChukkars(playerId, numChukkars, callback);
		    }
		    catch(NumberFormatException e)
		    {
		    	_view.setBusy(false);
		    	_view.showErrorDialog("Enter a valid number for chukkar count.", dayOfWeek, e);	
		    }
	    }
	    else
	    {
	    	_view.setBusy(false);
	    	_view.showErrorDialog("Unable to change # of chukkars. The humans behind this program are busy catching bugs and appreciate your patience.", dayOfWeek, null);
	    }
	}
	
	public void removePlayer(long playerId, final int delRowIndex, final Day dayOfWeek)
	{
		_view.setBusy(true);
		
		AsyncCallback<Void> callback = new AsyncCallback<Void>() 
	    {
	    	public void onFailure(Throwable caught) 
	    	{
	    		//TODO log error?
	    		_view.setBusy(false);
	    	}

	    	public void onSuccess(Void ignore) 
	    	{
	    		_view.removeRow(delRowIndex, dayOfWeek);
	    		_view.setBusy(false);
	    	}
	    };
		
		_playerSvc.removePlayer(playerId, callback);
	}
	
	public void loadPlayers()
	{
		_view.setBusy(true, true);
		
		AsyncCallback<List<Player>> callback = new AsyncCallback<List<Player>>() 
	    {
	    	public void onFailure(Throwable caught) 
	    	{
	    		//TODO log error?
	    		_view.setBusy(false, false);
	    	}

	    	public void onSuccess(List<Player> playersList) 
	    	{
	    		_view.loadPlayers(playersList);
	    		_view.setBusy(false, false);
	    	}
	    };
		
		_playerSvc.getPlayers(callback);
	}
	
	public void loadAdminData(LoginInfo currLogin)
	{
		_view.setBusy(true, true);
		
		AsyncCallback<MessageAdminClientCopy> callback = new AsyncCallback<MessageAdminClientCopy>() 
	    {
	    	public void onFailure(Throwable caught) 
	    	{
	    		//TODO log error?
	    		_view.setBusy(false, false);
	    	}

	    	public void onSuccess(MessageAdminClientCopy data) 
	    	{
	    		_view.loadAdminData(data);
	    		_view.setBusy(false, false);
	    	}
	    };
		
		_adminSvc.getMessageData(currLogin, callback);
	}
	
	public void saveAdminSettings(MessageAdminClientCopy adminData)
	{
		_view.setBusy(true);
		
		AsyncCallback<Void> callback = new AsyncCallback<Void>() 
	    {
	    	public void onFailure(Throwable caught) 
	    	{
	    		//TODO log error?
	    		_view.setBusy(false, false);
	    	}

	    	public void onSuccess(Void ignore) 
	    	{
	    		_view.setBusy(false, false);
	    		_view.setStatus("Settings saved");
	    	}
	    };
		
		_adminSvc.saveMessageData(adminData, callback);
	}
	
	public void exportSignups()
	{
		_view.setBusy(true);
		
		AsyncCallback<String> callback = new AsyncCallback<String>() 
	    {
	    	public void onFailure(Throwable caught) 
	    	{
	    		_view.showErrorDialog("Unable to export signups to Google Spreadsheets. See App Engine log for stack trace.", null, null);
	    		_view.setBusy(false, false);
	    	}

	    	public void onSuccess(String spreadsheetLink) 
	    	{
	    		_view.addCopiedSignupsLink(spreadsheetLink);
	    		_view.setBusy(false, false);
	    	}
	    };
		
		_adminSvc.exportSignups(callback);
	}
	
	public void importLineup(final Day dayOfWeek, final String msgBodyPrefix)
	{
		_view.setBusy(true);
		
		AsyncCallback<String> callback = new AsyncCallback<String>() 
	    {
	    	public void onFailure(Throwable caught) 
	    	{
	    		_view.showErrorDialog("Unable to import lineups from Google Spreadsheets. See App Engine log for stack trace.", null, null);
	    		_view.setBusy(false, false);
	    	}

	    	public void onSuccess(String importedLineup) 
	    	{
	    		_view.setImportedLineup(dayOfWeek, msgBodyPrefix + "\n\n" + importedLineup);
	    		_view.setBusy(false, false);
	    	}
	    };
		
		_adminSvc.importLineup(dayOfWeek, callback);
	}
	
	public void publishLineup(final Day dayOfWeek, LoginInfo login, final String recipientAddress, String msgBody)
	{
		_view.setBusy(true);
		
		AsyncCallback<Void> callback = new AsyncCallback<Void>() 
	    {
	    	public void onFailure(Throwable caught) 
	    	{
	    		_view.showErrorDialog("Unable to publish lineups by email. See App Engine log for stack trace.", null, null);
	    		_view.setBusy(false, false);
	    	}

	    	public void onSuccess(Void ignore) 
	    	{
	    		_view.setBusy(false, false);
	    		_view.setStatus(dayOfWeek.toString() + " lineup successfully emailed to \"" + recipientAddress + "\"");
	    	}
	    };
		
		_emailSvc.sendEmail(recipientAddress, dayOfWeek + " lineup", msgBody, login, callback);
	}
	
	public void createAdminUser(final String emailAddr, final String nickname)
	{
		_view.setBusy(true);
		
		AsyncCallback<Void> callback = new AsyncCallback<Void>() 
	    {
	    	public void onFailure(Throwable caught) 
	    	{
	    		_view.showErrorDialog("Unable to create new admin user: (" + emailAddr + ", " + nickname + ")", null, null);
	    		_view.setBusy(false, false);
	    	}

	    	public void onSuccess(Void ignore) 
	    	{
	    		_view.setBusy(false, false);
	    		_view.setStatus("New admin successfully created: (" + emailAddr + ", " + nickname + ")");
	    	}
	    };
		
		_adminSvc.createAdminUser(emailAddr, nickname, callback);
	}
}
