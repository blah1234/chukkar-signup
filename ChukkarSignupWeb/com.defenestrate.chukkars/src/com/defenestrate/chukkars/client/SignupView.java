package com.defenestrate.chukkars.client;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.defenestrate.chukkars.client.controller.ChukkarSignupController;
import com.defenestrate.chukkars.client.dnd.AbsolutePositionExample;
import com.defenestrate.chukkars.client.dnd.DraggableFactory;
import com.defenestrate.chukkars.client.dnd.RedBoxDraggableWidget;
import com.defenestrate.chukkars.client.resources.DisplayPageClientBundle;
import com.defenestrate.chukkars.shared.Day;
import com.defenestrate.chukkars.shared.LoginInfo;
import com.defenestrate.chukkars.shared.MessageAdminClientCopy;
import com.defenestrate.chukkars.shared.Player;
import com.defenestrate.chukkars.shared.resources.DisplayStrings;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DecoratedStackPanel;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DialogBox.Caption;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class SignupView implements EntryPoint
{
	//////////////////////////////// CONSTANTS /////////////////////////////////
	static private final int TIME_COLUMN_INDEX = 0;
	static private final int NAME_COLUMN_INDEX = 1;
	static private final int CHUKKARS_COLUMN_INDEX = 2;
	static private final int ACTION_COLUMN_INDEX = 3;
	static private final int DELETE_COLUMN_INDEX = 4;


	//////////////////////////// MEMBER VARIABLES //////////////////////////////
	private TextBoxExtend _updateTxt;
	private TabPanel _tabPanel;
	private DockPanel _topLevelPanel;
	private Map<Day, DayLayout> _dayToLayoutMap;

	private Button _copyBtn;
	private FlexTable _copyLineupTable;
	private DockPanel _lineupPanel;

	private CheckBox _enableEmailChk;
	private TextBox _recipientEmailTxt;
	private TextArea _signupNoticeTxt;
	private TextArea _signupReminderTxt;
	private Button _saveAdminSettingsBtn;
	private FlexTable _emailSettingsTable;

	private TextBox _emailAddrTxt;
	private TextBox _nicknameTxt;
	private Button _createAdminBtn;
	private DockPanel _accountsPanel;

	private Button _saveDaysConfigBtn;
	private DockPanel _dndDaysPanel;
	private AbsolutePositionExample _dndComponent;

	private StackPanel _linkPanel;

	private Anchor _logInLink;
	private Anchor _logOutLink;
	private BusyIndicator _busy;
	private Label _statusLbl;
	private Map<Day, Date> _signupClosedMap;

	private LoginInfo _loginInfo;
	private MessageAdminClientCopy _adminData;

	private ChukkarSignupController _ctrl;
	private boolean _isMobile;


	///////////////////////////////// METHODS //////////////////////////////////
	/**
	 * This is the entry point method.
	 */
	@Override
	public void onModuleLoad()
	{
		init();
	}

	private void init()
	{
		_loginInfo = null;
		_ctrl = new ChukkarSignupController(this);
		_busy = new BusyIndicator();

		_ctrl.configActiveDays();
	}

	public void onPostInit()
	{
		String initToken = History.getToken();
		_ctrl.loginRequest(initToken);
	}

	public void setLoginInfo(LoginInfo loginInfo)
	{
		_loginInfo = loginInfo;
	}

	public void renderModule(String initToken)
	{
		_ctrl.getSignupCloseDates(initToken);
	}

	public void renderModule(String initToken, Map<Day, Date> signupClosedMap)
	{
		_signupClosedMap = signupClosedMap;
		_ctrl.isBootstrapping(initToken);
	}

	public void renderModule(String initToken, boolean isBootstrapping)
	{
		layoutComponents(initToken, isBootstrapping);
		setupListeners();
	}

	public void loadLogin(String loginURL)
	{
	    //directly navigate to the login page
	    Window.Location.assign(loginURL);
	}

	private void layoutComponents(String initToken, boolean isBootstrapping)
	{
		_isMobile = initToken.equals("m");

		//top level panel
		_topLevelPanel = new DockPanel();

		if(!_isMobile)
		{
			_topLevelPanel.addStyleName("mainPanel");
		}

		layoutNavigationComponents();
		Day firstGameDay = layoutSignupComponents();
		layoutLineupComponents();
		layoutEmailSettingsComponents();
		layoutAccountsComponents();
		layoutConfigDaysComponents();


		if( (_loginInfo != null) && _loginInfo.isLoggedIn() && _loginInfo.isAdmin() )
		{
			_topLevelPanel.add(_linkPanel, DockPanel.WEST);
		}

		boolean isBusy = false;

		if( (initToken.isEmpty() && !isBootstrapping) ||
			initToken.equals("signup") ||
			initToken.equals("m") ||
			initToken.equals("login") ||
			(!_loginInfo.isAdmin() && !isBootstrapping) )
		{
			_topLevelPanel.add(_tabPanel, DockPanel.CENTER);
			_ctrl.loadPlayers();
			_linkPanel.showStack(0);
			isBusy = true;
		}
		else if( initToken.equals("email") )
		{
			_topLevelPanel.add(_emailSettingsTable, DockPanel.CENTER);
			_ctrl.loadAdminData(_loginInfo);
			_linkPanel.showStack(1);
			isBusy = true;
		}
		else if( initToken.equals("lineup") )
		{
			_topLevelPanel.add(_lineupPanel, DockPanel.CENTER);
			_linkPanel.showStack(1);
		}
		else if( (initToken.isEmpty() && isBootstrapping) ||
				 initToken.equals("accounts") )
		{
			_topLevelPanel.add(_accountsPanel, DockPanel.CENTER);
			_linkPanel.showStack(1);
		}
		else if( initToken.equals("days") )
		{
			_topLevelPanel.add(_dndDaysPanel, DockPanel.CENTER);
			_linkPanel.showStack(1);
		}

		if(!isBusy)
		{
			setBusy(false, false);
		}


		_statusLbl = new Label();
		_statusLbl.addStyleDependentName("status");
		_topLevelPanel.add(_statusLbl, DockPanel.SOUTH);


		//remove anything previously there
		int numWidgets = RootPanel.get("signupPanel").getWidgetCount();
		for(int i=numWidgets-1; i>=0; i--)
		{
			RootPanel.get("signupPanel").remove(i);
		}

		RootPanel.get("signupPanel").add(_topLevelPanel);


		if(firstGameDay != null)
		{
			boolean isClosed = isSignupClosed(firstGameDay);

			if(isClosed)
			{
				handleSignupClosed(firstGameDay, isClosed);
			}
		}
	}

	private boolean isSignupClosed(Day gameDay)
	{
		if( _signupClosedMap.containsKey(gameDay) )
		{
			Date signupClosed = _signupClosedMap.get(gameDay);
			Date now = new Date();
			return ( !_loginInfo.isAdmin() && now.after(signupClosed) );
		}
		else
		{
			return false;
		}
	}

	private void handleSignupClosed(Day gameDay, boolean isClosed)
	{
		DayLayout currLayout = _dayToLayoutMap.get(gameDay);
		currLayout._addRowBtn.setEnabled(!isClosed);
		currLayout._chukkarsTxt.setEnabled(!isClosed);
		currLayout._nameTxt.setEnabled(!isClosed);

		if(isClosed)
		{
			showSignupClosedDialog();
		}
	}

	private void layoutNavigationComponents()
	{
		//links to different "pages"
		Hyperlink signupLink = new Hyperlink("Signup&nbsp;Page", true, "signup");
	    Hyperlink lineupLink = new Hyperlink("Create&nbsp;Lineup", true, "lineup");
	    Hyperlink emailLink = new Hyperlink("Email&nbsp;Settings", true, "email");
	    Hyperlink accountsLink = new Hyperlink("Accounts", true, "accounts");
	    Hyperlink configDaysLink = new Hyperlink("Game&nbsp;Days", true, "days");

	    VerticalPanel signupLinkPanel = new VerticalPanel();
	    signupLinkPanel.setSpacing(5);
	    signupLinkPanel.add(signupLink);

	    VerticalPanel adminLinkPanel = new VerticalPanel();
	    adminLinkPanel.setSpacing(5);
	    adminLinkPanel.add(lineupLink);
	    adminLinkPanel.add(emailLink);
	    adminLinkPanel.add(accountsLink);
	    adminLinkPanel.add(configDaysLink);

	    _linkPanel = new DecoratedStackPanel();
	    _linkPanel.addStyleDependentName("linkPanel");
	    _linkPanel.add(signupLinkPanel, "Chukkar Signup");
	    _linkPanel.add(adminLinkPanel, "Admin Tasks");
	}

	/**
	 * @return the first day in the week that a game will be played
	 */
	private Day layoutSignupComponents()
	{
		_updateTxt = new TextBoxExtend();
		_updateTxt.addStyleDependentName("numChukkars");

		_tabPanel = new DecoratedTabPanel();
		_tabPanel.setAnimationEnabled(true);

		_dayToLayoutMap = new HashMap<Day, DayLayout>();

		//-------------

		Day ret = null;
		Day[] allDays = Day.getAll();
		for(Day currDay : allDays)
		{
			if( currDay.isEnabled() )
			{
				if(ret == null)
				{
					ret = currDay;
				}

				DayLayout layout = new DayLayout();
				_dayToLayoutMap.put(currDay, layout);

				// Create tables for chukkar signup.
				layout._signupTable = new FlexTable();
				layout._signupTable.setTitle( currDay.toString() );

				layout._signupTable.setText(0, TIME_COLUMN_INDEX, "Time");
				layout._signupTable.setText(0, NAME_COLUMN_INDEX, "Name");
				layout._signupTable.setText(0, CHUKKARS_COLUMN_INDEX, "Chukkars");
				layout._signupTable.setText(0, ACTION_COLUMN_INDEX, "Action");

				layout._nameTxt = new TextBox();
				layout._signupTable.setWidget(1, NAME_COLUMN_INDEX, layout._nameTxt);

				layout._chukkarsTxt = new TextBox();
				layout._chukkarsTxt.addStyleDependentName("numChukkars");
				layout._signupTable.setWidget(1, CHUKKARS_COLUMN_INDEX, layout._chukkarsTxt);

				layout._addRowBtn = new Button("Add");
				layout._signupTable.setWidget(1, ACTION_COLUMN_INDEX, layout._addRowBtn);

				layout._signupTable.setCellPadding(3);
				layout._signupTable.getRowFormatter().addStyleName(0, "signupHeader");
				layout._signupTable.addStyleName("signupTable");

				if(!_isMobile)
				{
					layout._addRowBtn.addStyleDependentName("action");

					layout._signupTable.getCellFormatter().addStyleName(1, TIME_COLUMN_INDEX, "signupTimeColumn");
				}
				else
				{
					layout._nameTxt.addStyleDependentName("mobileNameEntry");
					layout._addRowBtn.addStyleDependentName("mobileAction");

					layout._signupTable.getCellFormatter().addStyleName(1, TIME_COLUMN_INDEX, "mobileSignupTimeColumn");
					layout._signupTable.getCellFormatter().addStyleName(0, NAME_COLUMN_INDEX, "mobileSignupNameColumn");
				}

				layout._signupTable.getCellFormatter().addStyleName(0, CHUKKARS_COLUMN_INDEX, "signupNumericColumn");
				layout._signupTable.getCellFormatter().addStyleName(0, ACTION_COLUMN_INDEX, "signupActionColumn");


				layout._totalChukkarsTitleLbl = new Label("# Chukkars Total:");
				layout._totalChukkarsTitleLbl.addStyleDependentName("totalChukkarsLbl");

				layout._totalChukkarsLbl = new Label();

				Label gameChukkarsTitleLbl = new Label("# Game Chukkars:");
				gameChukkarsTitleLbl.addStyleDependentName("gameChukkarsLbl");

				layout._gameChukkarsLbl = new Label();

				layout._gameChukkarsPanel = new VerticalPanel();
				layout._gameChukkarsPanel.addStyleName("chukkarsPanel");
				layout._gameChukkarsPanel.add(gameChukkarsTitleLbl);
				layout._gameChukkarsPanel.add(layout._gameChukkarsLbl);


				CellPanel mainPanel;
				if(!_isMobile)
				{
					mainPanel = new HorizontalPanel();
					mainPanel.add(layout._signupTable);
					mainPanel.add(layout._gameChukkarsPanel);
				}
				else
				{
					mainPanel = new VerticalPanel();
					mainPanel.add(layout._gameChukkarsPanel);
					mainPanel.add(layout._signupTable);
				}

				_tabPanel.add( mainPanel, currDay.name() );
			}
		}

		//--------------------------------

		if(_tabPanel.getTabBar().getTabCount() == 0)
		{
			Label errLbl = new Label("Unable to configure active signup days. Please try again later. Contact Shawn.");
			errLbl.addStyleDependentName("status");

			_tabPanel.add(errLbl, "Error");
		}

		// Show the lexicographically smallest tab initially.
		_tabPanel.selectTab(0);

		//----------------------------------

		if( (_loginInfo != null) && _loginInfo.isLoggedIn() )
		{
			if(!_isMobile)
			{
				// Set up log out hyperlink.
				_logOutLink = new Anchor("Logout");
				_logOutLink.setHref( _loginInfo.getLogoutUrl() );
				_logOutLink.addStyleDependentName("loginLogoutLink");

				Label nicknameLbl = new Label( _loginInfo.getNickname() + ":" );
				nicknameLbl.addStyleDependentName("userNicknameLbl");

				Label emailLbl = new Label( _loginInfo.getEmailAddress() );
				emailLbl.addStyleDependentName("userEmailLbl");

				Label pipeLbl = new Label("|");
				pipeLbl.addStyleDependentName("pipeLbl");

				HorizontalPanel loginPanel = new HorizontalPanel();
				loginPanel.addStyleName("loginPanel");
				loginPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
				loginPanel.add(nicknameLbl);
				loginPanel.add(emailLbl);
				loginPanel.add(pipeLbl);
				loginPanel.add(_logOutLink);
				_topLevelPanel.add(loginPanel, DockPanel.NORTH);
			}

			if( _loginInfo.isAdmin() )
		    {
				for( DayLayout currLayout : _dayToLayoutMap.values() )
				{
		    			//only show the "delete" column if logged in user is an admin
		    			currLayout._signupTable.setText(0, DELETE_COLUMN_INDEX, "Delete");
		    			currLayout._signupTable.getCellFormatter().addStyleName(0, DELETE_COLUMN_INDEX, "signupActionColumn");

		    			//only show these labels if logged in user is an admin
		    			currLayout._gameChukkarsPanel.insert(currLayout._totalChukkarsLbl, 0);
		    			currLayout._gameChukkarsPanel.insert(currLayout._totalChukkarsTitleLbl, 0);
				}
		    }
		}
		else
		{
			if(!_isMobile)
			{
				// Set up log in hyperlink.
				_logInLink = new Anchor("Login");
				_logInLink.setHref( _loginInfo.getLoginUrl() );
				_logInLink.addStyleDependentName("loginLogoutLink");

				HorizontalPanel loginPanel = new HorizontalPanel();
				loginPanel.addStyleName("loginPanel");
				loginPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
				loginPanel.add(_logInLink);
				_topLevelPanel.add(loginPanel, DockPanel.NORTH);
			}
		}

		return ret;
	}

	private void layoutLineupComponents()
	{
		_copyBtn = new Button("Copy");
		_copyBtn.addStyleDependentName("copy");

		_copyLineupTable = new FlexTable();
		_copyLineupTable.addStyleName("adminTable");

		_copyLineupTable.setText(0, 0, "Signups to spreadsheet:");
		_copyLineupTable.getCellFormatter().addStyleName(0, 0, "rightCenterAlignLbl");

		_copyLineupTable.setText(1, 0, "Copied signups at:");
		_copyLineupTable.getCellFormatter().addStyleName(1, 0, "rightCenterAlignLbl");

		_copyLineupTable.setWidget(0, 1, _copyBtn);

		//--------------------

		TabPanel tabPanel = new DecoratedTabPanel();
		tabPanel.setAnimationEnabled(true);

		Day[] allDays = Day.getAll();
		for(Day currDay : allDays)
		{
			if( currDay.isEnabled() )
			{
				DayLayout layout = _dayToLayoutMap.get(currDay);

				layout._lineupRecipientEmailTxt = new TextBox();
				layout._lineupRecipientEmailTxt.addStyleDependentName("recipientEmail");
				DisplayStrings strings = (DisplayStrings)GWT.create(DisplayStrings.class);
				layout._lineupRecipientEmailTxt.setText( strings.clubListEmail() );

				String satMsgBodyPrefix = "Here it is for " + currDay + ".  Start time at 11am.  _ chukkars total.  Printable lineup available at: ";
				layout._lineupMsgBodyTxt = new TextArea();
				layout._lineupMsgBodyTxt.addStyleDependentName("signupTxt");
				layout._lineupMsgBodyTxt.setText(satMsgBodyPrefix);

				layout._importBtn = new Button("Import");
				layout._importBtn.addStyleDependentName("copy");

				layout._publishBtn = new Button("Publish");
				layout._publishBtn.addStyleDependentName("saveAdminSettings");

				FlexTable lineupTable = new FlexTable();
				lineupTable.addStyleName("adminTable");

				lineupTable.setText(0, 0, "Spreadsheet lineup:");
				lineupTable.getCellFormatter().addStyleName(0, 0, "rightCenterAlignLbl");

				lineupTable.setText(1, 0, "To:");
				lineupTable.getCellFormatter().addStyleName(1, 0, "rightCenterAlignLbl");

				lineupTable.setText(2, 0, "Lineup message body:");
				lineupTable.getCellFormatter().addStyleName(2, 0, "rightTopAlignLbl");

				lineupTable.setWidget(0, 1, layout._importBtn);
				lineupTable.setWidget(1, 1, layout._lineupRecipientEmailTxt);
				lineupTable.setWidget(2, 1, layout._lineupMsgBodyTxt);
				lineupTable.setWidget(3, 1, layout._publishBtn);


				tabPanel.add( lineupTable, currDay.toString() );
			}
		}

		if(tabPanel.getTabBar().getTabCount() == 0)
		{
			Label errLbl = new Label("Unable to configure active signup days. Please try again later. Contact Shawn.");
			errLbl.addStyleDependentName("status");

			tabPanel.add(errLbl, "Error");
		}

		// Show the lexicographically smallest tab initially.
		tabPanel.selectTab(0);

		//----------------------

		_lineupPanel = new DockPanel();
		_lineupPanel.addStyleName("lineupPanel");
		_lineupPanel.add(_copyLineupTable, DockPanel.NORTH);
		_lineupPanel.add(tabPanel, DockPanel.CENTER);
	}

	public void addCopiedSignupsLink(String spreadsheetLink)
	{
		Anchor link = new Anchor(spreadsheetLink);
		link.setHref(spreadsheetLink);
		link.setTarget("_blank");

		_copyLineupTable.setWidget(1, 1, link);
	}

	public void setImportedLineup(Day dayOfWeek, String msgBody)
	{
		TextArea msgTxt = _dayToLayoutMap.get(dayOfWeek)._lineupMsgBodyTxt;

		msgTxt.setText(msgBody);
	}

	private void layoutEmailSettingsComponents()
	{
		_enableEmailChk = new CheckBox();

		_recipientEmailTxt = new TextBox();
		_recipientEmailTxt.addStyleDependentName("recipientEmail");

		_signupNoticeTxt = new TextArea();
		_signupNoticeTxt.addStyleDependentName("signupTxt");

		_signupReminderTxt = new TextArea();
		_signupReminderTxt.addStyleDependentName("signupTxt");

		_saveAdminSettingsBtn = new Button("Save");
		_saveAdminSettingsBtn.addStyleDependentName("saveAdminSettings");


		_emailSettingsTable = new FlexTable();
		_emailSettingsTable.addStyleName("adminTable");

		_emailSettingsTable.setText(0, 0, "Enable weekly emails:");
		_emailSettingsTable.getCellFormatter().addStyleName(0, 0, "rightTopAlignLbl");

		_emailSettingsTable.setText(1, 0, "To:");
		_emailSettingsTable.getCellFormatter().addStyleName(1, 0, "rightCenterAlignLbl");

		_emailSettingsTable.setText(2, 0, "Signup notice body:");
		_emailSettingsTable.getCellFormatter().addStyleName(2, 0, "rightTopAlignLbl");

		_emailSettingsTable.setText(3, 0, "Signup reminder body:");
		_emailSettingsTable.getCellFormatter().addStyleName(3, 0, "rightTopAlignLbl");


		_emailSettingsTable.setWidget(0, 1, _enableEmailChk);
		_emailSettingsTable.setWidget(1, 1, _recipientEmailTxt);
		_emailSettingsTable.setWidget(2, 1, _signupNoticeTxt);
		_emailSettingsTable.setWidget(3, 1, _signupReminderTxt);
		_emailSettingsTable.setWidget(4, 1, _saveAdminSettingsBtn);
	}

	private void layoutAccountsComponents()
	{
		_emailAddrTxt = new TextBox();
		_emailAddrTxt.addStyleDependentName("recipientEmail");

		_nicknameTxt = new TextBox();
		_nicknameTxt.addStyleDependentName("recipientEmail");

		_createAdminBtn = new Button("Create");
		_createAdminBtn.addStyleDependentName("saveAdminSettings");


		FlexTable createAdminTable = new FlexTable();
		createAdminTable.addStyleName("adminTable");

		createAdminTable.setText(0, 0, "Email Address:");
		createAdminTable.getCellFormatter().addStyleName(0, 0, "rightCenterAlignLbl");

		createAdminTable.setText(1, 0, "Nickname:");
		createAdminTable.getCellFormatter().addStyleName(1, 0, "rightCenterAlignLbl");


		createAdminTable.setWidget(0, 1, _emailAddrTxt);
		createAdminTable.setWidget(1, 1, _nicknameTxt);
		createAdminTable.setWidget(2, 1, _createAdminBtn);

		//----------------

		Label createAdminLbl = new Label("Create New Admin");
		createAdminLbl.addStyleDependentName("title");

		_accountsPanel = new DockPanel();
		_accountsPanel.addStyleName("lineupPanel");
		_accountsPanel.add(createAdminLbl, DockPanel.NORTH);
		_accountsPanel.add(createAdminTable, DockPanel.CENTER);
	}

	private void layoutConfigDaysComponents()
	{
		// create the main common boundary panel to which drag operations will be restricted
		int boundaryPanelWidth = AbsolutePositionExample.DROP_TARGET_WIDTH + 100;
		int boundaryPanelHeight = AbsolutePositionExample.DROP_TARGET_HEIGHT * 2;
		AbsolutePanel boundaryPanel = new AbsolutePanel();
	    boundaryPanel.addStyleName("demo-main-boundary-panel");
	    boundaryPanel.setPixelSize(boundaryPanelWidth, boundaryPanelHeight);

	    // instantiate the common drag controller used the less specific examples
	    PickupDragController dragController = new PickupDragController(boundaryPanel, true);
	    dragController.setBehaviorMultipleSelection(false);
	    dragController.setBehaviorConstrainedToBoundaryPanel(true);

	    //add the inactive days; the active days are added in AbsolutePositionExample.onInitialLoad()
	    Day[] allDays = Day.getAll();
	    int inactiveX = 100;
	    int inactiveY = (boundaryPanelHeight / 2) + 50;

	    for(Day currDay : allDays)
	    {
	    		Widget draggable;

	    		if( !currDay.isEnabled() )
	    		{
	    			draggable = DraggableFactory.createDraggableRedBox( dragController, currDay.toString() );

	    			boundaryPanel.add(draggable, inactiveX, inactiveY);

	    			inactiveX += RedBoxDraggableWidget.DRAGGABLE_SIZE;
	    			inactiveX += 10;
	    		}
	    }

	    /*
		// for debug purposes only: text area to log drag events as they are triggered
	    final HTML eventTextArea = new HTML();
	    eventTextArea.addStyleName("demo-event-text-area");
	    eventTextArea.setSize(boundaryPanel.getOffsetWidth() + "px", "10em");
	    _topLevelPanel.add(eventTextArea, DockPanel.EAST);

	    // instantiate shared drag handler to listen for events
	    DemoDragHandler demoDragHandler = new DemoDragHandler(eventTextArea);
	    dragController.addDragHandler(demoDragHandler);
	    */

	    _dndComponent = new AbsolutePositionExample(dragController);
	    boundaryPanel.add(_dndComponent);

	    //----------------

	    _saveDaysConfigBtn = new Button("Save");
		_saveDaysConfigBtn.addStyleDependentName("saveAdminSettings");

		Label instructions = new Label("Days dragged into the smaller gray panel will become game days on save.");

		//----------------

		_dndDaysPanel = new DockPanel();
		_dndDaysPanel.addStyleName("lineupPanel");
		_dndDaysPanel.add(instructions, DockPanel.NORTH);
		_dndDaysPanel.add(boundaryPanel, DockPanel.CENTER);
		_dndDaysPanel.add(_saveDaysConfigBtn, DockPanel.SOUTH);
	}

	private void setupListeners()
	{
		// Add history listener
	    History.addValueChangeHandler(new ValueChangeHandler<String>()
		{
    			@Override
			public void onValueChange(ValueChangeEvent<String> event)
	    		{
	    			String navToken = event.getValue();
	    			handleNavigation(navToken);
	    		}
		});


	    final Day[] dayParam = new Day[1];

	    for( Day currDay : _dayToLayoutMap.keySet() )
	    {
		    	dayParam[0] = currDay;
		    	DayLayout currLayout = _dayToLayoutMap.get(currDay);

		    	currLayout._addRowBtn.addClickHandler(new ClickHandler()
			{
		    		Day dayOfWeek = dayParam[0];

				@Override
				public void onClick(ClickEvent event)
				{
					_ctrl.addPlayer(dayOfWeek);
				}
			});

		    	currLayout._chukkarsTxt.addKeyPressHandler(new KeyPressHandler()
		    {
		    		Day dayOfWeek = dayParam[0];

			    	@Override
				public void onKeyPress(KeyPressEvent event)
			    	{
			    		if (event.getCharCode() == KeyCodes.KEY_ENTER)
			    		{
			    			_ctrl.addPlayer(dayOfWeek);
			    		}
			    	}
		    });

		    	currLayout._signupTable.addClickHandler(new ClickHandler()
			{
				@Override
				public void onClick(ClickEvent event)
				{
					startEditingChukkars(event);
				}
			});

	   	 	//--------------------

	  	  	currLayout._importBtn.addClickHandler(new ClickHandler()
			{
	  	  		Day dayOfWeek = dayParam[0];

				@Override
				public void onClick(ClickEvent event)
				{
					_ctrl.importLineup(
						dayOfWeek,
						_dayToLayoutMap.get(dayOfWeek)._lineupMsgBodyTxt.getValue().trim() );
				}
			});

	  	  	currLayout._publishBtn.addClickHandler(new ClickHandler()
			{
	  	  		Day dayOfWeek = dayParam[0];

				@Override
				public void onClick(ClickEvent event)
				{
					_ctrl.publishLineup(
						dayOfWeek,
						_loginInfo,
						_dayToLayoutMap.get(dayOfWeek)._lineupRecipientEmailTxt.getValue().trim(),
						_dayToLayoutMap.get(dayOfWeek)._lineupMsgBodyTxt.getValue().trim() );
				}
			});
	    }


		_updateTxt.addKeyUpHandler(new KeyUpHandler()
		{
			@Override
			public void onKeyUp(KeyUpEvent event)
			{
				int tab = _tabPanel.getTabBar().getSelectedTab();
	    			String tabText = _tabPanel.getTabBar().getTabHTML(tab);
	    			Day dayOfWeek = Day.valueOf(tabText);

				if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
				{
					_ctrl.editChukkars(dayOfWeek);
				}
				else if(event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE)
	    			{
					stopEditingChukkars(dayOfWeek, false);
	    			}
			}
		});

		_tabPanel.addBeforeSelectionHandler(new BeforeSelectionHandler<Integer>()
		{
			@Override
			public void onBeforeSelection(BeforeSelectionEvent<Integer> event)
			{
				int prevTab = _tabPanel.getTabBar().getSelectedTab();
				String tabText = _tabPanel.getTabBar().getTabHTML(prevTab);
    				Day dayOfWeek = Day.valueOf(tabText);

				stopEditingChukkars(dayOfWeek, false);
			}
		});

		_tabPanel.addSelectionHandler(new SelectionHandler<Integer>()
		{
			@Override
			public void onSelection(SelectionEvent<Integer> event)
			{
				int selectedTab = event.getSelectedItem();
				String tabText = _tabPanel.getTabBar().getTabHTML(selectedTab);
				Day dayOfWeek = Day.valueOf(tabText);
				boolean isClosed = isSignupClosed(dayOfWeek);

				handleSignupClosed(dayOfWeek, isClosed);
			}
		});

		//--------------------------

		_saveAdminSettingsBtn.addClickHandler(new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent event)
			{
				writeAdminSettings();
				_ctrl.saveAdminSettings(_adminData);
			}
		});

		//--------------------------

		_copyBtn.addClickHandler(new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent event)
			{
				_ctrl.exportSignups();
			}
		});

		//--------------------------

		_createAdminBtn.addClickHandler(new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent event)
			{
				_ctrl.createAdminUser( _emailAddrTxt.getValue().trim(),
									   _nicknameTxt.getValue().trim() );

				_emailAddrTxt.setText("");
				_nicknameTxt.setText("");
			}
		});

		//--------------------------

		_saveDaysConfigBtn.addClickHandler(new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent event)
			{
				showWarningDialog("Configuring new game days will delete all existing players. Continue?",
					new ClickHandler()
					{
						@Override
						public void onClick(ClickEvent event)
						{
							Set<String> activeDayNames = getActiveDaysFromDndComponent();
							_ctrl.saveDaysConfig(activeDayNames);
						}
					});
			}
		});
	}

	private void handleNavigation(String navToken)
	{
		if( "lineup".equalsIgnoreCase(navToken) )
        {
	        	if( _loginInfo.isLoggedIn() )
	        	{
	        		if( _loginInfo.isAdmin() )
	        		{
			        	_topLevelPanel.remove(_tabPanel);
			        	_topLevelPanel.remove(_emailSettingsTable);
			        	_topLevelPanel.remove(_accountsPanel);
			        	_topLevelPanel.remove(_dndDaysPanel);

			        	_topLevelPanel.add(_lineupPanel, DockPanel.CENTER);
	        		}
	        		else
	        		{
	        			History.newItem("signup");
	        		}
	        	}
	        	else
	        	{
	        		_ctrl.loginRequest(navToken);
	        	}
        }
        else if( "signup".equalsIgnoreCase(navToken) ||
        			 "m".equalsIgnoreCase(navToken) )
        {
	        	_topLevelPanel.remove(_lineupPanel);
	        	_topLevelPanel.remove(_emailSettingsTable);
	        	_topLevelPanel.remove(_accountsPanel);
	        	_topLevelPanel.remove(_dndDaysPanel);

	        	_topLevelPanel.add(_tabPanel, DockPanel.CENTER);

	        	_ctrl.loadPlayers();
        }
        else if( "email".equalsIgnoreCase(navToken) )
        {
	        	if( _loginInfo.isLoggedIn() )
	        	{
	        		if( _loginInfo.isAdmin() )
	        		{
		        		_topLevelPanel.remove(_lineupPanel);
			        	_topLevelPanel.remove(_tabPanel);
			        	_topLevelPanel.remove(_accountsPanel);
			        	_topLevelPanel.remove(_dndDaysPanel);

			        	_topLevelPanel.add(_emailSettingsTable, DockPanel.CENTER);

			        	_ctrl.loadAdminData(_loginInfo);
	        		}
	        		else
	        		{
	        			History.newItem("signup");
	        		}
	        	}
	        	else
	        	{
	        		_ctrl.loginRequest(navToken);
	        	}
        }
        else if( "accounts".equalsIgnoreCase(navToken) )
        {
	        	if( _loginInfo.isLoggedIn() )
	        	{
	        		if( _loginInfo.isAdmin() )
	        		{
		        		_topLevelPanel.remove(_lineupPanel);
			        	_topLevelPanel.remove(_tabPanel);
			        	_topLevelPanel.remove(_emailSettingsTable);
			        	_topLevelPanel.remove(_dndDaysPanel);

			        	_topLevelPanel.add(_accountsPanel, DockPanel.CENTER);
	        		}
	        		else
	        		{
	        			History.newItem("signup");
	        		}
	        	}
	        	else
	        	{
	        		_ctrl.loginRequest(navToken);
	        	}
        }
        else if( "days".equalsIgnoreCase(navToken) )
        {
	        	if( _loginInfo.isLoggedIn() )
	        	{
	        		if( _loginInfo.isAdmin() )
	        		{
		        		_topLevelPanel.remove(_lineupPanel);
			        	_topLevelPanel.remove(_tabPanel);
			        	_topLevelPanel.remove(_emailSettingsTable);
			        	_topLevelPanel.remove(_accountsPanel);

			        	_topLevelPanel.add(_dndDaysPanel, DockPanel.CENTER);
	        		}
	        		else
	        		{
	        			History.newItem("days");
	        		}
	        	}
	        	else
	        	{
	        		_ctrl.loginRequest(navToken);
	        	}
        }
        else if( "login".equalsIgnoreCase(navToken) )
        {
    			_ctrl.loginRequest(navToken);
        }
	}

	private void writeAdminSettings()
	{
		_adminData.setWeeklyEmailsEnabled( _enableEmailChk.getValue() );
		_adminData.setRecipientEmailAddress( _recipientEmailTxt.getText().trim() );
		_adminData.setSignupNoticeMessage( _signupNoticeTxt.getText().trim() );
		_adminData.setSignupReminderMessage( _signupReminderTxt.getText().trim() );
	}

	private Set<String> getActiveDaysFromDndComponent()
	{
		List<Widget> widgets = _dndComponent.getAllWidgetsInDropTarget();
		HashSet<String> ret = new HashSet<String>();

		for(Widget currWidget : widgets)
		{
			if(currWidget instanceof RedBoxDraggableWidget)
			{
				ret.add( ((RedBoxDraggableWidget)currWidget).getText() );
			}
		}

		return ret;
	}

	private void startEditingChukkars(ClickEvent event)
	{
		FlexTable signupTable = (FlexTable)event.getSource();
		Day gameDay = Day.valueOf( signupTable.getTitle() );
		Cell clickCell = signupTable.getCellForEvent(event);

		if( (clickCell != null) && !isSignupClosed(gameDay) )
		{
			int row = clickCell.getRowIndex();
			int col = clickCell.getCellIndex();

			startEditingChukkars(signupTable, row, col);
		}
	}

	private void startEditingChukkars(FlexTable signupTable, int row, int col)
	{
		if( (col == CHUKKARS_COLUMN_INDEX) && 					//only "Chukkars" col
			(row > 0) &&										//not the header row
			(row < (signupTable.getRowCount() - 1)) &&			//only existing rows
			(signupTable.getWidget(row, col) != _updateTxt) )	//not already editing cell
		{
			String cellTxt = signupTable.getText(row, col);
			signupTable.setWidget(row, col, _updateTxt);

			if(_updateTxt.getPreviousText() != null)
			{
				//reset previous table cell
				signupTable.setText( _updateTxt.getRow(),
									 _updateTxt.getColumn(),
									 _updateTxt.getPreviousText() );

				( (Button)signupTable.getWidget(_updateTxt.getRow(), ACTION_COLUMN_INDEX) ).setText("Update");
			}

			_updateTxt.setRow(row);
			_updateTxt.setColumn(col);
			_updateTxt.setPreviousText(cellTxt);

			_updateTxt.setText(cellTxt);
			_updateTxt.setSelectionRange(0, cellTxt.length());
			_updateTxt.setFocus(true);

			( (Button)signupTable.getWidget(row, ACTION_COLUMN_INDEX) ).setText("Save");
		}
	}

	public void stopEditingChukkars(Day dayOfWeek, boolean acceptChanges)
	{
		if( _updateTxt.isAttached() && (_updateTxt.getPreviousText() != null) )
		{
			FlexTable signupTable = _dayToLayoutMap.get(dayOfWeek)._signupTable;

			//reset previous table cell
			signupTable.setText(
				_updateTxt.getRow(),
				_updateTxt.getColumn(),
				acceptChanges ? _updateTxt.getText() : _updateTxt.getPreviousText() );

			( (Button)signupTable.getWidget(_updateTxt.getRow(), ACTION_COLUMN_INDEX) ).setText("Update");

			_updateTxt.resetPreviousData();

			if(acceptChanges)
			{
				calculateGameChukkars(dayOfWeek);
			}
		}
	}

	public void setBusy(boolean isBusy, boolean isGlassEnabled)
	{
		_busy.setGlassEnabled(isGlassEnabled);
		setBusy(isBusy);
	}

	public void setBusy(boolean isBusy)
	{
		if(isBusy)
		{
			_busy.center();
		}
		else
		{
			_busy.hide();
		}
	}

	public void showErrorDialog(String errMsgStr, final Day dayOfWeek, final Throwable e)
	{
		final DialogBox alert = new DialogBox(true, true);
		Caption cap = alert.getCaption();
		if(cap instanceof UIObject)
		{
			( (UIObject)cap ).addStyleDependentName("errorDialog");
		}

	    	alert.setAnimationEnabled(true);
	    	alert.setGlassEnabled(true);
	    	alert.setText("Doh!");

	    	if(dayOfWeek != null)
	    	{
		    	alert.addCloseHandler(new CloseHandler<PopupPanel>()
			{
				@Override
				public void onClose(CloseEvent<PopupPanel> event)
				{
					DayLayout layout = _dayToLayoutMap.get(dayOfWeek);
					TextBox nameTxtBox = layout._nameTxt;
					TextBox numChukkarsTxtBox = layout._chukkarsTxt;

					TextBox selectTxtBox;
					if(e instanceof NumberFormatException)
					{
						if( !_updateTxt.isAttached() )
						{
							selectTxtBox = numChukkarsTxtBox;
						}
						else
						{
							selectTxtBox = _updateTxt;
						}
					}
					else
					{
						selectTxtBox = nameTxtBox;
					}


					selectTxtBox.setSelectionRange( 0, selectTxtBox.getText().length() );
					selectTxtBox.setFocus(true);
				}
			});
	    	}

	    	HorizontalPanel horizPanel = new HorizontalPanel();
	    	horizPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

	    	DisplayPageClientBundle myImageBundle = GWT.create(DisplayPageClientBundle.class);
		ImageResource busyImgResource = myImageBundle.errorIcon();
		horizPanel.add( new Image(busyImgResource) );

		Label errMsg = new Label(errMsgStr, true);
		errMsg.addStyleDependentName("errorDialog");
		horizPanel.add(errMsg);

	    	Button ok = new Button("OK");
	    	ok.addStyleDependentName("errorDialog");
	    ok.addClickHandler(new ClickHandler()
	    {
	        	@Override
			public void onClick(ClickEvent event)
	        	{
	        		alert.hide();
	        	}
	    });

	    VerticalPanel vertPanel = new VerticalPanel();
	    vertPanel.addStyleName("errorDialogPanel-main");
	    vertPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
	    vertPanel.add(horizPanel);
	    vertPanel.add(ok);

	    alert.setWidget(vertPanel);
	    	alert.center();
	    	ok.setFocus(true);
	}

	private void showSignupClosedDialog()
	{
		final DialogBox alert = new DialogBox(true, true);
		Caption cap = alert.getCaption();
		if(cap instanceof UIObject)
		{
			( (UIObject)cap ).addStyleDependentName("status");
		}

	    	alert.setAnimationEnabled(true);
	    	alert.setGlassEnabled(true);
	    	alert.setText("Too late!");


	    	HorizontalPanel horizPanel = new HorizontalPanel();
	    	horizPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

	    	DisplayPageClientBundle myImageBundle = GWT.create(DisplayPageClientBundle.class);
		ImageResource busyImgResource = myImageBundle.errorIcon();
		horizPanel.add( new Image(busyImgResource) );

		Label errMsg = new Label("Signup is closed. Try signing up for another day.", true);
		errMsg.addStyleDependentName("status");
		horizPanel.add(errMsg);

		Button ok = new Button("OK");
    		ok.addStyleDependentName("errorDialog");
        ok.addClickHandler(new ClickHandler()
        {
        		@Override
			public void onClick(ClickEvent event)
    			{
        			alert.hide();
    			}
        });

        VerticalPanel vertPanel = new VerticalPanel();
        vertPanel.addStyleName("errorDialogPanel-main");
        vertPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        vertPanel.add(horizPanel);
        vertPanel.add(ok);

        alert.setWidget(vertPanel);
        alert.center();
	}

	public void showWarningDialog(String warnMsgStr, ClickHandler okAction)
	{
		final DialogBox alert = new DialogBox(true, true);
		Caption cap = alert.getCaption();
		if(cap instanceof UIObject)
		{
			( (UIObject)cap ).addStyleDependentName("errorDialog");
		}

	    	alert.setAnimationEnabled(true);
	    	alert.setGlassEnabled(true);
	    	alert.setText("Warning");

	    	HorizontalPanel horizPanel = new HorizontalPanel();
	    	horizPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

	    	DisplayPageClientBundle myImageBundle = GWT.create(DisplayPageClientBundle.class);
		ImageResource warnImgResource = myImageBundle.warningIcon();
		horizPanel.add( new Image(warnImgResource) );

		Label warnMsg = new Label(warnMsgStr, true);
		warnMsg.addStyleDependentName("errorDialog");
		horizPanel.add(warnMsg);

	    	Button ok = new Button("OK");
	    	ok.addStyleDependentName("errorDialog");
	    ok.addClickHandler(new ClickHandler()
	    {
	        	@Override
			public void onClick(ClickEvent event)
	        	{
	        		alert.hide();
	        	}
	    });

	    ok.addClickHandler(okAction);

	    Button cancel = new Button("Cancel");
	    cancel.addStyleDependentName("errorDialog");
	    cancel.addClickHandler(new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent event)
			{
				alert.hide();
			}
		});

	    HorizontalPanel buttonPanel = new HorizontalPanel();
	    buttonPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
	    buttonPanel.setSpacing(10);
	    buttonPanel.add(ok);
	    buttonPanel.add(cancel);

	    VerticalPanel vertPanel = new VerticalPanel();
	    vertPanel.addStyleName("errorDialogPanel-main");
	    vertPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
	    vertPanel.add(horizPanel);
	    vertPanel.add(buttonPanel);

	    alert.setWidget(vertPanel);
	    	alert.center();
	    	ok.setFocus(true);
	}

	public void loadPlayers(List<Player> playersList)
	{
		clearSignupPage();

		for(Player currPlayer : playersList)
		{
			if( _dayToLayoutMap.containsKey(currPlayer.getRequestDay()) )
			{
				FlexTable signupTable = _dayToLayoutMap.get(currPlayer.getRequestDay())._signupTable;

				addRow( signupTable,
					    currPlayer.getRequestDay(),
					    currPlayer.getId(),
					    currPlayer.getCreateDate(),
					    currPlayer.getName(),
					    Integer.toString(currPlayer.getChukkarCount()) );
			}
		}
	}

	private void clearSignupPage()
	{
		for( Day currDay : _dayToLayoutMap.keySet() )
		{
			DayLayout currLayout = _dayToLayoutMap.get(currDay);
			FlexTable signupTable = currLayout._signupTable;

			//row 0 is the header row
			//last row is the "add" row
			for(int i=signupTable.getRowCount()-2; i>=1; i--)
			{
				signupTable.removeRow(i);
			}

			calculateGameChukkars(currDay);
		}
	}

	public void loadAdminData(MessageAdminClientCopy data)
	{
		_adminData = data;

		_enableEmailChk.setValue( data.isWeeklyEmailsEnabled() );
		_recipientEmailTxt.setText( data.getRecipientEmailAddress() );
		_signupNoticeTxt.setText( data.getSignupNoticeMessage() );
		_signupReminderTxt.setText( data.getSignupReminderMessage() );
	}

	public void addRow(Day dayOfWeek, Long playerId, Date createDate)
	{
		DayLayout layout = _dayToLayoutMap.get(dayOfWeek);

		FlexTable signupTable = layout._signupTable;
		TextBox nameTxtBox = layout._nameTxt;
		TextBox numChukkarsTxtBox = layout._chukkarsTxt;

		String playerName = nameTxtBox.getText();
		String numChukkars = numChukkarsTxtBox.getText();

		addRow(signupTable, dayOfWeek, playerId, createDate, playerName, numChukkars);

		nameTxtBox.setText("");
		numChukkarsTxtBox.setText("");

		nameTxtBox.setFocus(true);
	}

	private void addRow(final FlexTable signupTable,
						final Day dayOfWeek,
						Long playerId,
						Date createDate,
						String playerName,
						String numChukkars)
	{
		int rowInd = signupTable.insertRow(signupTable.getRowCount() - 1);

		DateTimeFormat formatter = DateTimeFormat.getFormat("EEE, M/d h:mm a");

		signupTable.setText( rowInd, TIME_COLUMN_INDEX, formatter.format(createDate) );
		signupTable.setText(rowInd, NAME_COLUMN_INDEX, playerName);
		signupTable.setText(rowInd, CHUKKARS_COLUMN_INDEX, numChukkars);

		//-------------------

		Button updateButton = new ButtonExtend("Update", rowInd, playerId);
		updateButton.setTitle("Click on \"Chukkars\" column to change");
	    updateButton.addClickHandler(new ClickHandler()
	    {
	    		@Override
			public void onClick(ClickEvent event)
	    		{
		    		if(signupTable.getWidget(((ButtonExtend)event.getSource()).getRow(), CHUKKARS_COLUMN_INDEX) == _updateTxt)
		    		{
			    		_ctrl.editChukkars(
		    				((ButtonExtend)event.getSource()).getPlayerId(),
			    			dayOfWeek );
		    		}
		    		else
		    		{
		    			startEditingChukkars(
		    				signupTable,
		    				((ButtonExtend)event.getSource()).getRow(),
		    				CHUKKARS_COLUMN_INDEX);
		    		}
		    	}
	    });

	    signupTable.setWidget(rowInd, ACTION_COLUMN_INDEX, updateButton);

	    //-------------------------

		updateButton.setEnabled( !isSignupClosed(dayOfWeek) );

	    //-------------------------

	    if( (_loginInfo != null) && _loginInfo.isLoggedIn() && _loginInfo.isAdmin() )
		{
			//only show the "delete" column if logged in user is an admin
			Button deleteButton = new ButtonExtend("x", rowInd, playerId);
			deleteButton.addStyleDependentName("delete");
		    deleteButton.addClickHandler(new ClickHandler()
		    {
		    		@Override
				public void onClick(ClickEvent event)
			    	{
			    		ButtonExtend btnSrc = (ButtonExtend)event.getSource();
			    		_ctrl.removePlayer(btnSrc.getPlayerId(), btnSrc.getRow(), dayOfWeek);
			    	}
		    });

		    signupTable.setWidget(rowInd, DELETE_COLUMN_INDEX, deleteButton);
		    signupTable.getCellFormatter().addStyleName(rowInd, DELETE_COLUMN_INDEX, "signupActionColumn");
		}

	    //----------------------------

	    if(!_isMobile)
	    {
	    		updateButton.addStyleDependentName("action");

	    		signupTable.getCellFormatter().addStyleName(rowInd, TIME_COLUMN_INDEX, "signupTimeColumn");
	    }
	    else
	    {
	    		updateButton.addStyleDependentName("mobileAction");

	    		signupTable.getCellFormatter().addStyleName(rowInd, TIME_COLUMN_INDEX, "mobileSignupTimeColumn");
	    		signupTable.getCellFormatter().addStyleName(rowInd, NAME_COLUMN_INDEX, "mobileSignupNameColumn");
	    }

	    signupTable.getCellFormatter().addStyleName(rowInd, CHUKKARS_COLUMN_INDEX, "signupNumericColumn");
		signupTable.getCellFormatter().addStyleName(rowInd, ACTION_COLUMN_INDEX, "signupActionColumn");

		calculateGameChukkars(dayOfWeek);
	}

	public void removeRow(int rowInd, Day dayOfWeek)
	{
		FlexTable signupTable = _dayToLayoutMap.get(dayOfWeek)._signupTable;

		signupTable.removeRow(rowInd);

		//row 0 is the header row
		//last row is the "add" row
		for(int i=rowInd, n=signupTable.getRowCount()-1; i<n; i++)
		{
			//update row referenced by "delete" button
			ButtonExtend btn = (ButtonExtend)signupTable.getWidget(i, DELETE_COLUMN_INDEX);
			btn.setRow(i);
		}

		calculateGameChukkars(dayOfWeek);
	}

	private void calculateGameChukkars(Day dayOfWeek)
	{
		DayLayout layout = _dayToLayoutMap.get(dayOfWeek);

		FlexTable signupTable = layout._signupTable;
		Label totalChukkarsLbl = layout._totalChukkarsLbl;
		Label gameChukkarsLbl = layout._gameChukkarsLbl;

		int totalChukkars = 0;

		//last row is the "add" row
		//row 0 is the header row
		int n = signupTable.getRowCount() - 2;
		int totalPlayers = n;

		//row 0 is the header row
		for(int i=1; i<=n; i++)
		{
			int currChukkarCount = Integer.parseInt( signupTable.getText(i, CHUKKARS_COLUMN_INDEX) );

			if(currChukkarCount == 0)
			{
				totalPlayers--;
			}

			totalChukkars += currChukkarCount;
		}

		totalChukkarsLbl.setText( Integer.toString(totalChukkars) );

		DisplayStrings strings = (DisplayStrings)GWT.create(DisplayStrings.class);
		int numGameChukkars;

		if(totalPlayers < strings.minPlayersPerChukkar())
		{
			numGameChukkars = 0;
		}
		else if( totalPlayers >= strings.playersPerChukkar() )
		{
			numGameChukkars = totalChukkars / strings.playersPerChukkar();
		}
		else
		{
			numGameChukkars = totalChukkars / strings.minPlayersPerChukkar();
		}

		gameChukkarsLbl.setText( Integer.toString(numGameChukkars) );
	}

	public String getPlayerNameToBeAdded(Day dayOfWeek)
	{
		TextBox nameTxtBox = _dayToLayoutMap.get(dayOfWeek)._nameTxt;

		return nameTxtBox.getText();
	}

	public Long getEditingPlayerId(Day dayOfWeek)
	{
		FlexTable signupTable = _dayToLayoutMap.get(dayOfWeek)._signupTable;

		if(_updateTxt.getRow() != -1)
		{
			ButtonExtend updateBtn = (ButtonExtend)signupTable.getWidget(
				_updateTxt.getRow(), ACTION_COLUMN_INDEX);

			return updateBtn.getPlayerId();
		}
		else
		{
			return null;
		}
	}

	public String getEditedChukkarCount()
	{
		return _updateTxt.getText();
	}

	public String getChukkarCountToBeAdded(Day dayOfWeek)
	{
		TextBox numChukkarsTxtBox = _dayToLayoutMap.get(dayOfWeek)._chukkarsTxt;

		return numChukkarsTxtBox.getText();
	}

	public void setStatus(String status)
	{
		_statusLbl.setText(status);

		Timer t = new Timer()
		{
			@Override
			public void run()
			{
		        _statusLbl.setText("");
			}
		};

		// Schedule the timer to run once in 5 seconds.
		t.schedule(5000);
	}


	////////////////////////////// INNER CLASSES ///////////////////////////////
	private class DayLayout
	{
		FlexTable _signupTable;
		TextBox _nameTxt;
		TextBox _chukkarsTxt;
		Button _addRowBtn;
		Label _totalChukkarsTitleLbl;
		Label _totalChukkarsLbl;
		Label _gameChukkarsLbl;
		VerticalPanel _gameChukkarsPanel;

		Button _importBtn;
		Button _publishBtn;
		TextBox _lineupRecipientEmailTxt;
		TextArea _lineupMsgBodyTxt;
	}
}
