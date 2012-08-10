package com.defenestrate.chukkars.client.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	    		@Override
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

		    	@Override
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
	    	@Override
			public void onFailure(Throwable error)
	    	{
	    		//render regular signup page by default
    			_view.renderModule("");
	    	}

	    	@Override
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

	public void getSignupCloseDates(final String initToken)
	{
	    AsyncCallback<Map<Day, Date>> async = new AsyncCallback<Map<Day, Date>>()
	    {
	    		@Override
			public void onFailure(Throwable error)
		    	{
		    		//render by default with no close date
		    		Map<Day, Date> ret = new HashMap<Day, Date>();
		    		_view.renderModule(initToken, ret);
		    	}

		    	@Override
			public void onSuccess(Map<Day, Date> result)
		    	{
		    		_view.renderModule(initToken, result);
		    	}
	    };

	    _configSvc.getSignupClosed(async);
	}

	public void isBootstrapping(final String initToken)
	{
		AsyncCallback<Boolean> async = new AsyncCallback<Boolean>()
	    {
		    	@Override
			public void onFailure(Throwable error)
		    	{
		    		//render by default under the assumption of not bootstrapping
	    			_view.renderModule(initToken, false);
		    	}

		    	@Override
			public void onSuccess(Boolean result)
		    	{
		    		//no existing admins means that we're bootstrapping
		    		_view.renderModule(initToken, !result);
		    	}
	    };

	    _adminSvc.doAdminsExist(async);
	}

	public void addPlayer(final Day dayOfWeek)
	{
		_view.setBusy(true);
		_view.stopEditingChukkars(dayOfWeek, false);
	    final String playerName = _view.getPlayerNameToBeAdded(dayOfWeek);

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
			    		@Override
					public void onFailure(Throwable caught)
				    	{
				    		_view.setBusy(false);
				    		_view.showErrorDialog("Unable to add \"" + playerName + "\". The humans behind the scenes are busy catching bugs and appreciate your patience.", dayOfWeek, caught);
				    	}

				    	@Override
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
				    	@Override
					public void onFailure(Throwable caught)
				    	{
				    		_view.stopEditingChukkars(dayOfWeek, false);
				    		_view.setBusy(false);
				    		_view.showErrorDialog("Unable to change # of chukkars. The humans behind the scenes are busy catching bugs and appreciate your patience.", dayOfWeek, caught);
				    	}

				    	@Override
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
		    	_view.showErrorDialog("Unable to change # of chukkars. The humans behind the scenes are busy catching bugs and appreciate your patience.", dayOfWeek, null);
	    }
	}

	public void removePlayer(long playerId, final int delRowIndex, final Day dayOfWeek)
	{
		_view.setBusy(true);

		AsyncCallback<Void> callback = new AsyncCallback<Void>()
	    {
	    		@Override
			public void onFailure(Throwable caught)
		    	{
		    		_view.setBusy(false);
		    		_view.showErrorDialog("Unable to remove the player. The humans behind the scenes are busy catching bugs and appreciate your patience.", dayOfWeek, caught);
		    	}

		    	@Override
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
	    		@Override
			public void onFailure(Throwable caught)
		    	{
		    		_view.setBusy(false, false);
		    		_view.showErrorDialog("Unable to load all players. The humans behind the scenes are busy catching bugs and appreciate your patience.", null, null);
		    	}

		    	@Override
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
	    		@Override
			public void onFailure(Throwable caught)
		    	{
		    		_view.setBusy(false, false);
		    		_view.showErrorDialog("Unable to load email settings. The humans behind the scenes are busy catching bugs and appreciate your patience.", null, null);
		    	}

		    	@Override
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
			@Override
			public void onFailure(Throwable caught)
		    	{
		    		_view.showErrorDialog("Unable to save email settings.", null, null);
		    		_view.setBusy(false, false);
		    	}

		    	@Override
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
			@Override
			public void onFailure(Throwable caught)
		    	{
		    		_view.showErrorDialog("Unable to export signups to Google Spreadsheets. See App Engine log for stack trace.", null, null);
		    		_view.setBusy(false, false);
		    	}

		    	@Override
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
			@Override
			public void onFailure(Throwable caught)
		    	{
		    		_view.showErrorDialog("Unable to import lineups from Google Spreadsheets. See App Engine log for stack trace.", null, null);
		    		_view.setBusy(false, false);
		    	}

		    	@Override
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
			@Override
			public void onFailure(Throwable caught)
		    	{
		    		_view.showErrorDialog("Unable to publish lineups by email. See App Engine log for stack trace.", null, null);
		    		_view.setBusy(false, false);
		    	}

		    	@Override
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
			@Override
			public void onFailure(Throwable caught)
		    	{
		    		_view.showErrorDialog("Unable to create new admin user: (" + emailAddr + ", " + nickname + ")", null, null);
		    		_view.setBusy(false, false);
		    	}

			@Override
			public void onSuccess(Void ignore)
		    	{
		    		_view.setBusy(false, false);
		    		_view.setStatus("New admin successfully created: (" + emailAddr + ", " + nickname + ")");
		    	}
	    };

		_adminSvc.createAdminUser(emailAddr, nickname, callback);
	}

	public void saveDaysConfig(Set<String> activeDayNames)
	{
		_view.setBusy(true);

		AsyncCallback<Void> callback = new AsyncCallback<Void>()
	    {
			@Override
			public void onFailure(Throwable caught)
		    	{
		    		_view.showErrorDialog("Unable to save game days.", null, null);
		    		_view.setBusy(false, false);
		    	}

		    	@Override
			public void onSuccess(Void ignore)
		    	{
		    		//make sure client enumerations are consistent with what
		    		//was just saved on the server
		    		Day.disableAll();
		    		configActiveDays();

		    		_view.setStatus("Game days saved");
		    	}
	    };

		_configSvc.saveDaysConfig(activeDayNames, callback);
	}
}
