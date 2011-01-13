package com.defenestrate.chukkars.client;

import java.util.Date;
import java.util.List;

import com.defenestrate.chukkars.client.controller.ChukkarSignupController;
import com.defenestrate.chukkars.shared.Day;
import com.defenestrate.chukkars.shared.LoginInfo;
import com.defenestrate.chukkars.shared.MessageAdminClientCopy;
import com.defenestrate.chukkars.shared.Player;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.DialogBox.Caption;
import com.google.gwt.user.client.ui.HTMLTable.Cell;

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
	static private final int ROW_NUM_COLUMN_INDEX = 5;


	//////////////////////////// MEMBER VARIABLES //////////////////////////////
	private FlexTable _satSignupTable;
	private FlexTable _sunSignupTable;
	private TextBox _satNameTxt;
	private TextBox _sunNameTxt;
	private TextBoxExtend _updateTxt;
	private TextBox _satChukkarsTxt;
	private TextBox _sunChukkarsTxt;
	private Button _satAddRowBtn;
	private Button _sunAddRowBtn;
	private Label _satTotalChukkarsLbl;
	private Label _sunTotalChukkarsLbl;
	private Label _satGameChukkarsLbl;
	private Label _sunGameChukkarsLbl;
	private TabPanel _tabPanel;
	private DockPanel _topLevelPanel;
	InsertPanelExample _dndExample;
	
	private CheckBox _enableEmailChk;
	private TextBox _recipientEmailTxt;
	private TextArea _signupNoticeTxt;
	private TextArea _signupReminderTxt;
	private Button _saveAdminSettingsBtn;
	private FlexTable _adminTable;
	
	private Anchor _logOutLink;
	private BusyIndicator _busy;
	
	private LoginInfo _loginInfo;
	private MessageAdminClientCopy _adminData;
	
	private ChukkarSignupController _ctrl;
	

	///////////////////////////////// METHODS //////////////////////////////////
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad()
	{
		init();
		
		String initToken = History.getToken();
		_ctrl.loginRequest(initToken);
	}
	
	private void init()
	{
		_loginInfo = null;
		_ctrl = new ChukkarSignupController(this);
		_busy = new BusyIndicator();
	}
	
	public void setLoginInfo(LoginInfo loginInfo)
	{
		_loginInfo = loginInfo;
	}
	
	public void renderModule(String initToken)
	{
		layoutComponents(initToken);
		setupListeners();
	}
	
	public void loadLogin(String loginURL) 
	{
	    //directly navigate to the login page
	    Window.Location.assign(loginURL);
	}
	
	private void layoutComponents(String initToken)
	{
		//top level panel
		_topLevelPanel = new DockPanel();
		_topLevelPanel.addStyleName("mainPanel");
		
		layoutSignupComponents();
		layoutAdminComponents();
		
		
//TODO:	_topLevelPanel.add(linkPanel, DockPanel.WEST);
		
		if( !initToken.equals("admin") )
		{
			_topLevelPanel.add(_tabPanel, DockPanel.CENTER);
			_ctrl.loadPlayers();
		}
		else
		{
			_topLevelPanel.add(_adminTable, DockPanel.CENTER);
			_ctrl.loadAdminData(_loginInfo);
		}
/**@todo		
		//Dnd components
	    HTML eventTextArea = new HTML();
	    //eventTextArea.setSize(boundaryPanel.getOffsetWidth() + "px", "10em");

	    // instantiate shared drag handler to listen for events
	    DemoDragHandler demoDragHandler = new DemoDragHandler(eventTextArea);
	    _dndExample = new InsertPanelExample(demoDragHandler);
	    
	    //----------------------------------
	    
		_topLevelPanel.add(eventTextArea, DockPanel.EAST);
*/		
		
		//remove anything previously there
		int numWidgets = RootPanel.get("signupPanel").getWidgetCount();
		for(int i=numWidgets-1; i>=0; i--)
		{
			RootPanel.get("signupPanel").remove(i);
		}
		
		RootPanel.get("signupPanel").add(_topLevelPanel);
	}
	
	private void layoutSignupComponents()
	{
		_updateTxt = new TextBoxExtend();
		_updateTxt.addStyleDependentName("numChukkars");
		
		//-------------
		
		// Create tables for chukkar signup.
		_satSignupTable = new FlexTable();
		
		_satSignupTable.setText(0, TIME_COLUMN_INDEX, "Time");
		_satSignupTable.setText(0, NAME_COLUMN_INDEX, "Name");
		_satSignupTable.setText(0, CHUKKARS_COLUMN_INDEX, "Chukkars");
		_satSignupTable.setText(0, ACTION_COLUMN_INDEX, "Action");
		
		_satNameTxt = new TextBox();
		_satSignupTable.setWidget(1, NAME_COLUMN_INDEX, _satNameTxt); 
		_satChukkarsTxt = new TextBox();
		_satChukkarsTxt.addStyleDependentName("numChukkars");
		_satSignupTable.setWidget(1, CHUKKARS_COLUMN_INDEX, _satChukkarsTxt);
		_satAddRowBtn = new Button("Add");
		_satAddRowBtn.addStyleDependentName("action");
		_satSignupTable.setWidget(1, ACTION_COLUMN_INDEX, _satAddRowBtn);
		
		_satSignupTable.setCellPadding(3);
		_satSignupTable.getRowFormatter().addStyleName(0, "signupHeader");
		_satSignupTable.addStyleName("signupTable");
		_satSignupTable.getCellFormatter().addStyleName(1, TIME_COLUMN_INDEX, "signupTimeColumn");
		_satSignupTable.getCellFormatter().addStyleName(0, CHUKKARS_COLUMN_INDEX, "signupNumericColumn");
		_satSignupTable.getCellFormatter().addStyleName(0, ACTION_COLUMN_INDEX, "signupActionColumn");
		
		
		Label satTotalChukkarsTitleLbl = new Label("# Chukkars Total:");
		satTotalChukkarsTitleLbl.addStyleDependentName("totalChukkarsLbl");
		
		_satTotalChukkarsLbl = new Label();
		
		Label satGameChukkarsTitleLbl = new Label("# Game Chukkars:");
		satGameChukkarsTitleLbl.addStyleDependentName("gameChukkarsLbl");
		
		_satGameChukkarsLbl = new Label();
		
		VerticalPanel satGameChukkarsPanel = new VerticalPanel();
		satGameChukkarsPanel.addStyleName("chukkarsPanel");
		satGameChukkarsPanel.add(satGameChukkarsTitleLbl);
		satGameChukkarsPanel.add(_satGameChukkarsLbl);
		
		
		HorizontalPanel mainPanel = new HorizontalPanel();
		mainPanel.add(_satSignupTable);
		mainPanel.add(satGameChukkarsPanel);
		
		_tabPanel = new TabPanel();
		_tabPanel.setAnimationEnabled(true);
		_tabPanel.add(mainPanel, "Saturday");
		
		//-----------------------------------
		
		_sunSignupTable = new FlexTable();
		
		_sunSignupTable.setText(0, TIME_COLUMN_INDEX, "Time");
		_sunSignupTable.setText(0, NAME_COLUMN_INDEX, "Name");
		_sunSignupTable.setText(0, CHUKKARS_COLUMN_INDEX, "Chukkars");
		_sunSignupTable.setText(0, ACTION_COLUMN_INDEX, "Action");
		
		_sunNameTxt = new TextBox();
		_sunSignupTable.setWidget(1, NAME_COLUMN_INDEX, _sunNameTxt); 
		_sunChukkarsTxt = new TextBox();
		_sunChukkarsTxt.addStyleDependentName("numChukkars");
		_sunSignupTable.setWidget(1, CHUKKARS_COLUMN_INDEX, _sunChukkarsTxt);
		_sunAddRowBtn = new Button("Add");
		_sunAddRowBtn.addStyleDependentName("action");
		_sunSignupTable.setWidget(1, ACTION_COLUMN_INDEX, _sunAddRowBtn);
		
		_sunSignupTable.setCellPadding(3);
		_sunSignupTable.getRowFormatter().addStyleName(0, "signupHeader");
		_sunSignupTable.addStyleName("signupTable");
		_sunSignupTable.getCellFormatter().addStyleName(1, TIME_COLUMN_INDEX, "signupTimeColumn");
		_sunSignupTable.getCellFormatter().addStyleName(0, CHUKKARS_COLUMN_INDEX, "signupNumericColumn");
		_sunSignupTable.getCellFormatter().addStyleName(0, ACTION_COLUMN_INDEX, "signupActionColumn");
		
		
		Label sunTotalChukkarsTitleLbl = new Label("# Chukkars Total:");
		sunTotalChukkarsTitleLbl.addStyleDependentName("totalChukkarsLbl");
		
		_sunTotalChukkarsLbl = new Label();
		
		Label sunGameChukkarsTitleLbl = new Label("# Game Chukkars:");
		sunGameChukkarsTitleLbl.addStyleDependentName("gameChukkarsLbl");
		
		_sunGameChukkarsLbl = new Label();
		
		VerticalPanel sunGameChukkarsPanel = new VerticalPanel();
		sunGameChukkarsPanel.addStyleName("chukkarsPanel");
		sunGameChukkarsPanel.add(sunGameChukkarsTitleLbl);
		sunGameChukkarsPanel.add(_sunGameChukkarsLbl);
		
		
		mainPanel = new HorizontalPanel();
		mainPanel.add(_sunSignupTable);
		mainPanel.add(sunGameChukkarsPanel);
		
		_tabPanel.add(mainPanel, "Sunday");
		
		//--------------------------------
		
		// Show the 'Saturday' tab initially.
		_tabPanel.selectTab(0);
		
		//_tabPanel.addStyleName("mainPanel");
		
		//----------------------------------
		
		//links to different "pages"
		Hyperlink signupLink = new Hyperlink("Chukkar&nbsp;Signup", true, "signup");
	    Hyperlink lineupLink = new Hyperlink("Create&nbsp;Lineup", true, "lineup");
	    
	    VerticalPanel linkPanel = new VerticalPanel();
	    linkPanel.add(signupLink);
	    linkPanel.add(lineupLink);
		
		//----------------------------------
	    
		if( (_loginInfo != null) && _loginInfo.isLoggedIn() )
		{
			// Set up log out hyperlink.
			_logOutLink = new Anchor("Logout");
			_logOutLink.setHref( _loginInfo.getLogoutUrl() );
			_logOutLink.addStyleDependentName("logoutLink");
			
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
			
			
			if( _loginInfo.isAdmin() ) 
		    {
    			//only show the "delete" column if logged in user is an admin
    			_satSignupTable.setText(0, DELETE_COLUMN_INDEX, "Delete");
    			_satSignupTable.getCellFormatter().addStyleName(0, DELETE_COLUMN_INDEX, "signupActionColumn");
    			
    			//only show the "row num" column if logged in user is an admin
    			_satSignupTable.setText(0, ROW_NUM_COLUMN_INDEX, "Row");
    			_satSignupTable.getCellFormatter().addStyleName(0, ROW_NUM_COLUMN_INDEX, "signupActionColumn");
    			
    			
    			//only show the "delete" column if logged in user is an admin
    			_sunSignupTable.setText(0, DELETE_COLUMN_INDEX, "Delete");
    			_sunSignupTable.getCellFormatter().addStyleName(0, DELETE_COLUMN_INDEX, "signupActionColumn");
    			
    			//only show the "row num" column if logged in user is an admin
    			_sunSignupTable.setText(0, ROW_NUM_COLUMN_INDEX, "Row");
    			_sunSignupTable.getCellFormatter().addStyleName(0, ROW_NUM_COLUMN_INDEX, "signupActionColumn");
    			
    			
    			//only show these labels if logged in user is an admin
    			satGameChukkarsPanel.insert(_satTotalChukkarsLbl, 0);
    			satGameChukkarsPanel.insert(satTotalChukkarsTitleLbl, 0);
    			
    			sunGameChukkarsPanel.insert(_sunTotalChukkarsLbl, 0);
    			sunGameChukkarsPanel.insert(sunTotalChukkarsTitleLbl, 0);
		    }
		}
	}
	
	private void layoutAdminComponents()
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

		
		_adminTable = new FlexTable();
		_adminTable.addStyleName("adminTable");
		
		_adminTable.setText(0, 0, "Enable weekly emails:");
		_adminTable.getCellFormatter().addStyleName(0, 0, "adminLbl");
		
		_adminTable.setText(1, 0, "To:");
		_adminTable.getCellFormatter().addStyleName(1, 0, "adminLbl");
		
		_adminTable.setText(2, 0, "Signup notice body:");
		_adminTable.getCellFormatter().addStyleName(2, 0, "adminLbl");
		
		_adminTable.setText(3, 0, "Signup reminder body:");
		_adminTable.getCellFormatter().addStyleName(3, 0, "adminLbl");
		
		
		_adminTable.setWidget(0, 1, _enableEmailChk);
		_adminTable.setWidget(1, 1, _recipientEmailTxt);
		_adminTable.setWidget(2, 1, _signupNoticeTxt);
		_adminTable.setWidget(3, 1, _signupReminderTxt);
		_adminTable.setWidget(4, 1, _saveAdminSettingsBtn);
	}
	
	private void setupListeners()
	{
		// Add history listener
	    History.addValueChangeHandler(new ValueChangeHandler<String>()
		{
    		public void onValueChange(ValueChangeEvent<String> event) 
    		{
		        if( "lineup".equalsIgnoreCase(event.getValue()) )
		        {
		        	_topLevelPanel.remove(_tabPanel);
		        	_topLevelPanel.remove(_adminTable);
		        	
		        	_topLevelPanel.add(_dndExample, DockPanel.CENTER);
		        }
		        else if( "signup".equalsIgnoreCase(event.getValue()) )
		        {
//TODO:		        	_topLevelPanel.remove(_dndExample);
		        	_topLevelPanel.remove(_adminTable);
		        	
		        	_topLevelPanel.add(_tabPanel, DockPanel.CENTER);
		        	
		        	_ctrl.loadPlayers();
		        }
		        else if( "admin".equalsIgnoreCase(event.getValue()) )
		        {
		        	if( _loginInfo.isLoggedIn() )
		        	{
//TODO:		        	_topLevelPanel.remove(_dndExample);
			        	_topLevelPanel.remove(_tabPanel);
			        	
			        	_topLevelPanel.add(_adminTable, DockPanel.CENTER);
			        	
			        	_ctrl.loadAdminData(_loginInfo);
		        	}
		        	else
		        	{
		        		_ctrl.loginRequest( event.getValue() );
		        	}
		        }
		        else if( "login".equalsIgnoreCase(event.getValue()) )
		        {
	        		_ctrl.loginRequest( event.getValue() );
		        }
    		}
		});

		_satAddRowBtn.addClickHandler(new ClickHandler()
		{
			public void onClick(ClickEvent event)
			{
				_ctrl.addPlayer(Day.SATURDAY);
			}
		});
		
		_satChukkarsTxt.addKeyPressHandler(new KeyPressHandler()
	    {
	    	public void onKeyPress(KeyPressEvent event) 
	    	{
	    		if (event.getCharCode() == KeyCodes.KEY_ENTER) 
	    		{
	    			_ctrl.addPlayer(Day.SATURDAY);
	    		}
	    	}
	    });
		
		_sunAddRowBtn.addClickHandler(new ClickHandler()
		{
			public void onClick(ClickEvent event)
			{
				_ctrl.addPlayer(Day.SUNDAY);
			}
		});
		
		_sunChukkarsTxt.addKeyPressHandler(new KeyPressHandler()
	    {
	    	public void onKeyPress(KeyPressEvent event) 
	    	{
	    		if (event.getCharCode() == KeyCodes.KEY_ENTER) 
	    		{
	    			_ctrl.addPlayer(Day.SUNDAY);
	    		}
	    	}
	    });
		
		_satSignupTable.addClickHandler(new ClickHandler()
		{
			public void onClick(ClickEvent event)
			{
				startEditingChukkars(event);
			}
		});
		
		_sunSignupTable.addClickHandler(new ClickHandler()
		{
			public void onClick(ClickEvent event)
			{
				startEditingChukkars(event);
			}
		});
		
		_updateTxt.addKeyUpHandler(new KeyUpHandler()
		{
			public void onKeyUp(KeyUpEvent event)
			{
				Day dayOfWeek;
    			int tab = _tabPanel.getTabBar().getSelectedTab();
				
				if(tab == 0)
				{
					dayOfWeek = Day.SATURDAY;
				}
				else
				{
					dayOfWeek = Day.SUNDAY;
				}
				
				
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
			public void onBeforeSelection(BeforeSelectionEvent<Integer> event)
			{
				Day dayOfWeek;
				int prevTab = _tabPanel.getTabBar().getSelectedTab();
				
				if(prevTab == 0)
				{
					dayOfWeek = Day.SATURDAY;
				}
				else
				{
					dayOfWeek = Day.SUNDAY;
				}
				
				stopEditingChukkars(dayOfWeek, false);
			}
		});
		
		_saveAdminSettingsBtn.addClickHandler(new ClickHandler()
		{
			public void onClick(ClickEvent event)
			{
				writeAdminSettings();
				_ctrl.saveAdminSettings(_adminData);
			}
		});
	}
	
	private void writeAdminSettings()
	{
		_adminData.setWeeklyEmailsEnabled( _enableEmailChk.getValue() );
		_adminData.setRecipientEmailAddress( _recipientEmailTxt.getText().trim() );
		_adminData.setSignupNoticeMessage( _signupNoticeTxt.getText().trim() );
		_adminData.setSignupReminderMessage( _signupReminderTxt.getText().trim() );
	}
	
	private void startEditingChukkars(ClickEvent event)
	{
		FlexTable signupTable = (FlexTable)event.getSource();
		Cell clickCell = signupTable.getCellForEvent(event);
		
		if(clickCell != null) 
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
			FlexTable signupTable;
			
			if(dayOfWeek == Day.SATURDAY)
			{
				signupTable = _satSignupTable;
			}
			else
			{
				signupTable = _sunSignupTable;
			}
			
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
	
	public void showErrorDialog(String errMsgStr, final Day dayOfWeek, final Exception e)
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
    	alert.addCloseHandler(new CloseHandler<PopupPanel>()
		{
			public void onClose(CloseEvent<PopupPanel> event)
			{
				TextBox nameTxtBox;
				TextBox numChukkarsTxtBox;
				
				if(dayOfWeek == Day.SATURDAY)
				{
					nameTxtBox = _satNameTxt;
					numChukkarsTxtBox = _satChukkarsTxt;
				}
				else
				{
					nameTxtBox = _sunNameTxt;
					numChukkarsTxtBox = _sunChukkarsTxt;
				}
				
				
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
	
	public void loadPlayers(List<Player> playersList)
	{
		clearSignupPage();
		
		for(Player currPlayer : playersList)
		{
			FlexTable signupTable;
			if(currPlayer.getRequestDay() == Day.SATURDAY)
			{
				signupTable = _satSignupTable;
			}
			else
			{
				signupTable = _sunSignupTable;
			}
			
			addRow( signupTable, 
				    currPlayer.getRequestDay(), 
				    currPlayer.getId(),
				    currPlayer.getCreateDate(),
				    currPlayer.getName(), 
				    Integer.toString(currPlayer.getChukkarCount()) );
		}
	}
	
	private void clearSignupPage()
	{
		FlexTable[] tableArray = 
		{
			_satSignupTable,
			_sunSignupTable
		};
			
		for(FlexTable signupTable : tableArray)
		{
			//row 0 is the header row
			//last row is the "add" row
			for(int i=signupTable.getRowCount()-2; i>=1; i--)
			{
				signupTable.removeRow(i);
			}
		}
		
		calculateGameChukkars(Day.SATURDAY);
		calculateGameChukkars(Day.SUNDAY);
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
		FlexTable signupTable;
		TextBox nameTxtBox;
		TextBox numChukkarsTxtBox;
		
		if(dayOfWeek == Day.SATURDAY)
		{
			signupTable = _satSignupTable;
			nameTxtBox = _satNameTxt;
			numChukkarsTxtBox = _satChukkarsTxt;
		}
		else
		{
			signupTable = _sunSignupTable;
			nameTxtBox = _sunNameTxt;
			numChukkarsTxtBox = _sunChukkarsTxt;
		}
		
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
		updateButton.addStyleDependentName("action");
	    updateButton.addClickHandler(new ClickHandler() 
	    {
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
	    
	    if( (_loginInfo != null) && _loginInfo.isLoggedIn() && _loginInfo.isAdmin() )
		{
			//only show the "delete" column if logged in user is an admin
			Button deleteButton = new ButtonExtend("x", rowInd, playerId);
			deleteButton.addStyleDependentName("delete");
		    deleteButton.addClickHandler(new ClickHandler() 
		    {
		    	public void onClick(ClickEvent event) 
		    	{
		    		ButtonExtend btnSrc = (ButtonExtend)event.getSource();
		    		_ctrl.removePlayer(btnSrc.getPlayerId(), btnSrc.getRow(), dayOfWeek);
		    	}
		    });
		    
		    signupTable.setWidget(rowInd, DELETE_COLUMN_INDEX, deleteButton);
		    signupTable.getCellFormatter().addStyleName(rowInd, DELETE_COLUMN_INDEX, "signupActionColumn");
		    
		    //only show the "row num" column if logged in user is an admin
		    signupTable.setText( rowInd, ROW_NUM_COLUMN_INDEX, Integer.toString(rowInd) );
		    signupTable.getCellFormatter().addStyleName(rowInd, ROW_NUM_COLUMN_INDEX, "signupActionColumn");
		}
	    
	    //----------------------------
	    
	    signupTable.getCellFormatter().addStyleName(rowInd, TIME_COLUMN_INDEX, "signupTimeColumn");
	    signupTable.getCellFormatter().addStyleName(rowInd, CHUKKARS_COLUMN_INDEX, "signupNumericColumn");
		signupTable.getCellFormatter().addStyleName(rowInd, ACTION_COLUMN_INDEX, "signupActionColumn");
		
		calculateGameChukkars(dayOfWeek);
	}
	
	public void removeRow(int rowInd, Day dayOfWeek)
	{
		FlexTable signupTable;
		
		if(dayOfWeek == Day.SATURDAY)
		{
			signupTable = _satSignupTable;
		}
		else
		{
			signupTable = _sunSignupTable;
		}
		
		signupTable.removeRow(rowInd);
		
		//row 0 is the header row
		//last row is the "add" row
		for(int i=rowInd, n=signupTable.getRowCount()-1; i<n; i++)
		{
			//update row referenced by "delete" button
			ButtonExtend btn = (ButtonExtend)signupTable.getWidget(i, DELETE_COLUMN_INDEX);
			btn.setRow(i);
			
			//update row number
			signupTable.setText( i, ROW_NUM_COLUMN_INDEX, Integer.toString(i) );
		}
		
		calculateGameChukkars(dayOfWeek);
	}
	
	private void calculateGameChukkars(Day dayOfWeek)
	{
		FlexTable signupTable;
		Label totalChukkarsLbl;
		Label gameChukkarsLbl;
		
		if(dayOfWeek == Day.SATURDAY)
		{
			signupTable = _satSignupTable;
			totalChukkarsLbl = _satTotalChukkarsLbl;
			gameChukkarsLbl = _satGameChukkarsLbl;
		}
		else
		{
			signupTable = _sunSignupTable;
			totalChukkarsLbl = _sunTotalChukkarsLbl;
			gameChukkarsLbl = _sunGameChukkarsLbl;
		}
		
		int totalChukkars = 0;
		
		//last row is the "add" row
		int n = signupTable.getRowCount() - 1;
		
		//row 0 is the header row
		for(int i=1; i<n; i++)
		{
			int currChukkarCount = Integer.parseInt( signupTable.getText(i, CHUKKARS_COLUMN_INDEX) );
			totalChukkars += currChukkarCount;
		}
		
		totalChukkarsLbl.setText( Integer.toString(totalChukkars) );

		int numGameChukkars;
		if(n <= 4)
		{
			numGameChukkars = 0;
		}
		else if(n > 6)
		{
			numGameChukkars = totalChukkars / 6;
		}
		else
		{
			numGameChukkars = totalChukkars / 4;
		}
		
		gameChukkarsLbl.setText( Integer.toString(numGameChukkars) );
	}

	public String getPlayerNameToBeAdded(Day dayOfWeek)
	{
		TextBox nameTxtBox;
		
		if(dayOfWeek == Day.SATURDAY)
		{
			nameTxtBox = _satNameTxt;
		}
		else
		{
			nameTxtBox = _sunNameTxt;
		}
		
		return nameTxtBox.getText();
	}
	
	public Long getEditingPlayerId(Day dayOfWeek)
	{
		FlexTable signupTable;
		
		if(dayOfWeek == Day.SATURDAY)
		{
			signupTable = _satSignupTable;
		}
		else
		{
			signupTable = _sunSignupTable;
		}
		
		
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
		TextBox numChukkarsTxtBox;
		
		if(dayOfWeek == Day.SATURDAY)
		{
			numChukkarsTxtBox = _satChukkarsTxt;
		}
		else
		{
			numChukkarsTxtBox = _sunChukkarsTxt;
		}
		
		return numChukkarsTxtBox.getText();
	}
}
