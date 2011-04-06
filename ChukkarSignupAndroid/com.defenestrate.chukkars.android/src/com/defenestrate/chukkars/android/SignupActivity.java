package com.defenestrate.chukkars.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import yuku.iconcontextmenu.IconContextMenu;
import yuku.iconcontextmenu.IconContextMenu.IconContextItemSelectedListener;

import com.defenestrate.chukkars.android.entity.Day;
import com.defenestrate.chukkars.android.persistence.SignupDbAdapter;
import com.defenestrate.chukkars.android.widget.NumberPicker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

abstract public class SignupActivity extends Activity 
{
	//////////////////////////////// CONSTANTS /////////////////////////////////
	static private final String SERVER_DATA_FILENAME = "all-players.json";
	
	static private final String PLAYER_ID_KEY = "PLAYER_ID_KEY"; 
	static private final String PLAYER_NAME_KEY = "PLAYER_NAME_KEY";
	static private final String PLAYER_NUM_CHUKKARS_KEY = "PLAYER_NUM_CHUKKARS_KEY";
	
	
	//////////////////////////// MEMBER VARIABLES //////////////////////////////
	private ProgressDialog _progressDlg;
	private Handler _handler;
	private Day _selectedDay;
	private long _fileLastModified = -1;
	private SignupDbAdapter _dbHelper;
	private Set<Integer> _dialogsCurrentlyShowing;


	//////////////////////////// Activity METHODS //////////////////////////////
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_table);
        
        _handler = new Handler() 
        {
            @Override
            public void handleMessage(Message msg) 
            {
                _progressDlg.dismiss();
                
                if(msg.what != R.id.message_what_error)
                {
                	loadPlayersImpl(msg.arg1);
                }
            }
        };
        
        _dialogsCurrentlyShowing = new HashSet<Integer>();
    }
    
    @Override
    protected void onResume()
    {
    	super.onResume();
    	
    	File file = getFileStreamPath(SERVER_DATA_FILENAME);
		long lastModified = file.lastModified();
    
		if(lastModified != _fileLastModified)
		{
			_fileLastModified = lastModified;
        
            int tabIndex = getIntent().getExtras().getInt(ChukkarSignup.TAB_INDEX_KEY);
    		loadPlayers(tabIndex);
		}
    }
    
    @Override
    public void onStop()
	{
    	// The activity is no longer visible (it is now "stopped")
		super.onStop();
		
		for(int currId : _dialogsCurrentlyShowing)
		{
			dismissDialog(currId);
		}
		
		deleteFile(SERVER_DATA_FILENAME);
	}
    
    @Override
    protected void onDestroy()
    {
    	super.onDestroy();
    	
    	if(_dbHelper != null)
    	{
	        _dbHelper.close();
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        super.onCreateOptionsMenu(menu);
        
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        // Handle item selection
        switch( item.getItemId() ) 
        {
        case R.id.new_player:
            showDialog(R.id.signup_add_dialog);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    protected Dialog onCreateDialog(int id, Bundle args)
    {
    	Dialog dialog;
        
    	switch(id) 
    	{
        case R.id.signup_add_dialog:
            dialog = createAddNewDialog();
            break;
        case R.id.signup_edit_dialog:
        	dialog = createEditChukkarsDialog();
            break;
        default:
            dialog = null;
        }
        
        return dialog;
    }
    
    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args)
    {
    	switch(id) 
    	{
        case R.id.signup_add_dialog:
            prepareAddNewDialog(dialog);
            break;
        case R.id.signup_edit_dialog:
        	prepareEditChukkarsDialog(dialog, args);
            break;
        }
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        
        //get the Player Id associated with the clicked-on table row
        TextView idCol = (TextView)v.findViewById(R.id.player_id_col);
        String playerId = idCol.getText().toString();
        
        TextView nameCol = (TextView)v.findViewById(R.id.player_name_col);
        String playerName = nameCol.getText().toString();
        
        TextView numChukkarsCol = (TextView)v.findViewById(R.id.player_chukkars_col);
        int numChukkars = Integer.parseInt( numChukkarsCol.getText().toString() );
        
        IconContextMenu cm = new IconContextMenu(this, R.menu.context_menu);
        
        Bundle args = new Bundle();
        args.putString(PLAYER_ID_KEY, playerId);
        args.putString(PLAYER_NAME_KEY, playerName);
        args.putInt(PLAYER_NUM_CHUKKARS_KEY, numChukkars);
        cm.setInfo(args);
        
        cm.setOnIconContextItemSelectedListener(new IconContextItemSelectedListener()
		{
			public void onIconContextItemSelected(MenuItem item, Object info)
			{
				switch( item.getItemId() ) 
		        {
		        case R.id.edit_player:
		        	Bundle args = (Bundle)info;
		            showDialog(R.id.signup_edit_dialog, args);
		        }
			}
		});
        
        cm.show();
    }

    
	///////////////////////////////// METHODS //////////////////////////////////
    private SignupDbAdapter getDBHelper()
    {
    	if(_dbHelper == null)
    	{
	    	_dbHelper = new SignupDbAdapter(this);
	        _dbHelper.open();
    	}
        
        return _dbHelper;
    }
    
    private Dialog createAddNewDialog()
    {
    	AlertDialog.Builder builder;
    	AlertDialog alertDialog;

    	LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
    	final View layout = inflater.inflate( 
    		R.layout.signup_add_dialog, 
    		(ViewGroup)findViewById(R.id.add_dialog_root) );
    	
    	Resources res = getResources();

    	builder = new AlertDialog.Builder(this);
    	builder.setView(layout);
    	builder.setCancelable(true);
    	builder.setTitle( res.getString(R.string.menu_add) );
    	builder.setPositiveButton(res.getString(R.string.button_save), new DialogInterface.OnClickListener() 
    	{
    		public void onClick(DialogInterface dialog, int id) 
    		{
    			EditText nameWidget = (EditText)layout.findViewById(R.id.name_edit);
    			NumberPicker numChukkarsWidget = (NumberPicker)layout.findViewById(R.id.chukkars_picker);
    			
    			if(_selectedDay != null)
    			{
    				addPlayer( _selectedDay, nameWidget.getText().toString(), numChukkarsWidget.getCurrent() );
    			}
    			else
    			{
    				throw new RuntimeException("_selectedDay cannot be null. It should have been set as a result of calls made in onResume()!");
    			}
    		}
    	});

    	builder.setNegativeButton(res.getString(R.string.button_cancel), new DialogInterface.OnClickListener() 
    	{
    		public void onClick(DialogInterface dialog, int id) 
    		{
    			dialog.cancel();
    		}
    	});
    	
    	alertDialog = builder.create();
    	
    	alertDialog.setOnShowListener(new OnShowListener()
		{
			public void onShow(DialogInterface dialog)
			{
				_dialogsCurrentlyShowing.add(R.id.signup_add_dialog);
			}
		});
    	
    	alertDialog.setOnDismissListener(new OnDismissListener()
		{
			public void onDismiss(DialogInterface dialog)
			{
				_dialogsCurrentlyShowing.remove(R.id.signup_add_dialog);
			}
		});
    	
    	return alertDialog;
    }
    
    private Dialog createEditChukkarsDialog()
    {
    	AlertDialog.Builder builder;
    	AlertDialog alertDialog;

    	LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
    	final View layout = inflater.inflate( 
    		R.layout.signup_edit_chukkars_dialog, 
    		(ViewGroup)findViewById(R.id.edit_chukkars_dialog_root) );
    	
    	Resources res = getResources();
    	
    	builder = new AlertDialog.Builder(this);
    	builder.setView(layout);
    	builder.setCancelable(true);
    	builder.setTitle( res.getString(R.string.menu_edit) );
    	builder.setPositiveButton(res.getString(R.string.button_save), new DialogInterface.OnClickListener() 
    	{
    		public void onClick(DialogInterface dialog, int id) 
    		{
    			View nameWidget = layout.findViewById(R.id.name_value);
    			String playerId = (String)nameWidget.getTag(R.id.player_id_tag);
    			
    			NumberPicker numChukkarsWidget = (NumberPicker)layout.findViewById(R.id.chukkars_picker);
    			
				editNumChukkars( playerId, numChukkarsWidget.getCurrent() );
    		}
    	});

    	builder.setNegativeButton(res.getString(R.string.button_cancel), new DialogInterface.OnClickListener() 
    	{
    		public void onClick(DialogInterface dialog, int id) 
    		{
    			dialog.cancel();
    		}
    	});
    	
    	alertDialog = builder.create();
    	
    	alertDialog.setOnShowListener(new OnShowListener()
		{
			public void onShow(DialogInterface dialog)
			{
				_dialogsCurrentlyShowing.add(R.id.signup_edit_dialog);
			}
		});
    	
    	alertDialog.setOnDismissListener(new OnDismissListener()
		{
			public void onDismiss(DialogInterface dialog)
			{
				_dialogsCurrentlyShowing.remove(R.id.signup_edit_dialog);
			}
		});
    	
    	return alertDialog;
    }
    
    private void prepareEditChukkarsDialog(Dialog dialog, Bundle args)
    {
    	SignupDbAdapter db = getDBHelper();
    	String warning = null;

    	if(db.getPlayerCount() > 0)
    	{
    		long currId = Long.parseLong( args.getString(PLAYER_ID_KEY) );
    		if( !db.containsPlayer(currId) )
    		{
    			Cursor c = db.fetchAllPlayerNames();
    			startManagingCursor(c);
    			
    			StringBuffer buf = new StringBuffer();
    			
    			for( boolean noUse = c.isBeforeFirst() ? c.moveToFirst() : false ;
    				 !c.isAfterLast();
    				 c.moveToNext() )
    			{
    				buf.append( c.getString(c.getColumnIndexOrThrow(SignupDbAdapter.KEY_NAME)) );
    				
    				if( !c.isLast() )
    				{
    					buf.append(", ");
    				}
    			}
    			
    			warning = MessageFormat.format(
		    		getResources().getString(R.string.edit_chukkars_warning), 
		    		new Object[] {buf.toString()} );
    		}
    	}
    	
    	TextView msgWidget = (TextView)dialog.findViewById(R.id.message_value);
    	
    	if(warning == null)
    	{
    		msgWidget.setText("");
    		msgWidget.setVisibility(View.GONE);
    	}
    	else
    	{
    		msgWidget.setText(warning);
    		msgWidget.setVisibility(View.VISIBLE);
    	}
    	
    	TextView nameWidget = (TextView)dialog.findViewById(R.id.name_value);
    	nameWidget.setText( args.getString(PLAYER_NAME_KEY) );
    	nameWidget.setTag( R.id.player_id_tag, args.getString(PLAYER_ID_KEY) );
    	
    	NumberPicker numPick = (NumberPicker)dialog.findViewById(R.id.chukkars_picker);
    	numPick.setRange(0, 10);
    	
    	numPick.setCurrent( args.getInt(PLAYER_NUM_CHUKKARS_KEY) );
    	
    	numPick.requestFocus();
    }
    
	private void prepareAddNewDialog(Dialog dialog)
    {
    	EditText nameEdit = (EditText)dialog.findViewById(R.id.name_edit);
    	nameEdit.setText("");
    	
    	NumberPicker numPick = (NumberPicker)dialog.findViewById(R.id.chukkars_picker);
    	numPick.setRange(0, 10);
    	numPick.setCurrent(2);
    	
    	nameEdit.requestFocus();
    }
	
	private void editNumChukkars(final String playerId, final int numChukkars)
	{
		_progressDlg = ProgressDialog.show(
	    	this, "", getResources().getString(R.string.load_dialog_message), true);
		
		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				Resources res = getResources(); 

				try
				{
					//http post
					HttpClient httpclient = new DefaultHttpClient();
					HttpPost post = new HttpPost( res.getString(R.string.edit_chukkars_url) );
					
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
					nameValuePairs.add( new BasicNameValuePair(res.getString(R.string.player_id_field), playerId) );
			        nameValuePairs.add( new BasicNameValuePair(res.getString(R.string.player_numChukkars_field), Integer.toString(numChukkars)) );
			        post.setEntity( new UrlEncodedFormEntity(nameValuePairs) );
			        
					HttpResponse response = httpclient.execute(post);
					
					int tabIndex = getIntent().getExtras().getInt(ChukkarSignup.TAB_INDEX_KEY);
					writeServerData(response, tabIndex);
			    }
				catch(IOException e)
				{
					_handler.sendEmptyMessage(R.id.message_what_error);
					
					//TODO:
		            throw new RuntimeException(e);
				}
			}
		});
		
        thread.start();
	}
	
	private void addPlayer(final Day selectedDay, final String name, final int numChukkars)
	{
		_progressDlg = ProgressDialog.show(
	    	this, "", getResources().getString(R.string.load_dialog_message), true);
		
		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				Resources res = getResources(); 

				try
				{
					//http post
					HttpClient httpclient = new DefaultHttpClient();
					HttpPost post = new HttpPost( res.getString(R.string.add_player_url) );
					
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
					nameValuePairs.add( new BasicNameValuePair(res.getString(R.string.player_requestDay_field), selectedDay.toString()) );
			        nameValuePairs.add( new BasicNameValuePair(res.getString(R.string.player_name_field), name) );
			        nameValuePairs.add( new BasicNameValuePair(res.getString(R.string.player_numChukkars_field), Integer.toString(numChukkars)) );
			        post.setEntity( new UrlEncodedFormEntity(nameValuePairs) );
			        
					HttpResponse response = httpclient.execute(post);
					
					int tabIndex = getIntent().getExtras().getInt(ChukkarSignup.TAB_INDEX_KEY);
					writeServerData(response, tabIndex);
			    }
				catch(IOException e)
				{
					_handler.sendEmptyMessage(R.id.message_what_error);
					
					//TODO:
		            throw new RuntimeException(e);
				}
			}
		});
		
        thread.start();
	}
    
    protected void loadPlayers(int tabIndex)
    {
    	String[] existingFiles = fileList();
    	boolean doesDataFileExist = false;
    	
    	for(String currFilename : existingFiles)
    	{
    		if( SERVER_DATA_FILENAME.equals(currFilename) )
    		{
    			doesDataFileExist = true;
    			break;
    		}
    	}
    	
    	if(!doesDataFileExist)
    	{
    		getServerData(tabIndex);
    	}
    	else
    	{
    		loadPlayersImpl(tabIndex);
    	}
    }
    
    private void loadPlayersImpl(int tabIndex)
    {
    	FileInputStream fis = null;
    	String result = "";
    	
    	try
    	{
    		fis = openFileInput(SERVER_DATA_FILENAME);
    		BufferedReader reader = new BufferedReader( new InputStreamReader(fis, "utf-8") );
    		StringWriter strWrite = new StringWriter();
	    	
	    	String line = null;
	    	while( (line = reader.readLine()) != null ) 
	    	{
	    		strWrite.append(line + "\n");
	    	}
		            
	    	result = strWrite.toString();
    	}
    	catch(Exception e)
    	{
    		//TODO:
            throw new RuntimeException(e);
    	}
    	finally
    	{
    		try
	    	{
	    		if(fis != null)
	    		{
	    			fis.close();
	    		}
	    	}
	    	catch(IOException e) {}
    	}
    	
    	
    	//parse json data
	    try
	    {
	    	Resources res = getResources();
	    	JSONObject data = new JSONObject(result);
	    	JSONArray jArray = data.getJSONArray( res.getString(R.string.totals_list_field) );
	    	
	    	if(jArray.length() != 2)
	    	{
	    		throw new RuntimeException("Unexpected length for _totalsList in JSON: " + jArray.toString(4));
	    	}
	    	
	    	String dayStr = jArray.getJSONObject(tabIndex).getString( res.getString(R.string.total_day_field) );
	    	_selectedDay = Day.valueOf(dayStr);
	    	
	    	//format the titles in the tabs
	    	TabHost tabHost = (TabHost)getParent().findViewById(android.R.id.tabhost);
	    	TextView tabTitle = (TextView)tabHost.getTabWidget().getChildTabViewAt(tabIndex).findViewById(android.R.id.title);
	    	String title = MessageFormat.format(
	    		res.getString(R.string.tab_title), 
	    		new Object[] {dayStr, 
	    					  jArray.getJSONObject(tabIndex).getString(res.getString(R.string.total_num_chukkars_field))} );
	    	tabTitle.setText(title);
	    	
	    	//-----------------
	    	
	    	/* Find Tablelayout defined in main.xml */
            TableLayout tl = (TableLayout)findViewById(R.id.player_display_table);
            TableRow headerRow = (TableRow)tl.findViewById(R.id.player_table_header_row);
            tl.removeAllViews();
            //now add the header back in
            tl.addView(headerRow);
            
            LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            
            DateFormat inParser = DateFormat.getDateTimeInstance();
            DateFormat outFormatter = new SimpleDateFormat("EEE, M/d h:mm a");
	    	
            jArray = data.getJSONArray( res.getString(R.string.players_list_field) );
            
            for(int i=0, n=jArray.length(); i<n; i++)
            {
                JSONObject currPlayer = jArray.getJSONObject(i);
                
                Day currRequestDay = Day.valueOf( currPlayer.getString( res.getString(R.string.player_requestDay_field)) );
                if(currRequestDay == _selectedDay)
                {
                	/* Create and add a new row */
                	View rootView = inflater.inflate(R.layout.signup_table_row, tl, false);
                	
                	//attach row to table
                	TableRow tr = (TableRow)rootView.findViewById(R.id.player_table_row);
                	tr.setClickable(true);
                	registerForContextMenu(tr);
                	tl.addView(tr);

                    //assign the data to the appropriate columns
                    TextView idText = (TextView)rootView.findViewById(R.id.player_id_col);
                    idText.setText( currPlayer.getString(res.getString(R.string.player_id_field)) );
                    
                    //------------
                    
                	TextView timeText = (TextView)rootView.findViewById(R.id.player_time_col);
                	try
            		{
                		String createDate = currPlayer.getString( res.getString(R.string.player_createDate_field) );
                		Date dateTime = inParser.parse(createDate);
                		String displayDateTime = outFormatter.format(dateTime);
            			timeText.setText(displayDateTime);
            		}
            		catch(ParseException e)
            		{
            			//TODO:
            			throw new RuntimeException(e);
            		}
                	
            		//-------------
            		
                	TextView nameText = (TextView)rootView.findViewById(R.id.player_name_col);
                	nameText.setText( currPlayer.getString(res.getString(R.string.player_name_field)) );
                	
                	//-----------
                	
                	TextView chukkarsText = (TextView)rootView.findViewById(R.id.player_chukkars_col);
                	chukkarsText.setText( currPlayer.getString(res.getString(R.string.player_numChukkars_field)) );
                }
            }
            
            
            if( data.has(res.getString(R.string.curr_player_persisted_field)) )
            {
            	//persist the Id of the newly persisted Player, ONLY if
            	//it doesn't already exist in the DB
            	JSONObject persistedPlayer = data.getJSONObject( res.getString(R.string.curr_player_persisted_field) );
            	long id = persistedPlayer.getLong( res.getString(R.string.player_id_field) );
            	SignupDbAdapter db = getDBHelper();

            	if( !db.containsPlayer(id) )
            	{
            		String name = persistedPlayer.getString( res.getString(R.string.player_name_field) );
            		db.createPlayer(id, name);
            	}
            }
	    }
	    catch(JSONException e)
	    {
	    	//TODO:
            throw new RuntimeException(e);
	    }
    }
    
	private void getServerData(final int tabIndex) 
	{
		_progressDlg = ProgressDialog.show(
	    	this, "", getResources().getString(R.string.load_dialog_message), true);
		
		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				Resources res = getResources(); 

				try
				{
					//http get
					HttpClient httpclient = new DefaultHttpClient();
					HttpGet get = new HttpGet( res.getString(R.string.get_players_url) );
					HttpResponse response = httpclient.execute(get);
					
					writeServerData(response, tabIndex);
			    }
				catch(IOException e)
				{
					_handler.sendEmptyMessage(R.id.message_what_error);
					
					//TODO:
		            throw new RuntimeException(e);
				}
			}
		});
		
        thread.start();
	}
	
	private void writeServerData(HttpResponse response, int tabIndex) throws IOException
	{
		InputStream is = null;
		FileOutputStream fos = null;
		
		try
		{
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
	
			//convert response to string
	    	BufferedReader reader = new BufferedReader( new InputStreamReader(is,"utf-8") );
	    	StringWriter strWrite = new StringWriter();
	    	
	    	String line = null;
	    	while( (line = reader.readLine()) != null ) 
	    	{
	    		strWrite.append(line + "\n");
	    	}
		            
	    	String result = strWrite.toString();
	    	
	    	//write json data to file
	    	fos = openFileOutput(SERVER_DATA_FILENAME, Context.MODE_PRIVATE);
	    	fos.write( result.getBytes() );
	    	fos.flush();
	    	
	    	Message msg = _handler.obtainMessage(R.id.message_what_success);
	    	msg.arg1 = tabIndex;
	    	_handler.sendMessage(msg);
		}
	    finally
	    {
	    	try
	    	{
	    		if(is != null)
	    		{
	    			is.close();
	    		}
	    	}
	    	catch(IOException e) {}
	    	
	    	try
	    	{
	    		if(fos != null)
	    		{
	    			fos.close();
	    		}
	    	}
	    	catch(IOException e) {}
	    }
	    
	    
	    File file = getFileStreamPath(SERVER_DATA_FILENAME);
	    _fileLastModified = file.lastModified();
	}
}