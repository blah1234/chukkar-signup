package com.defenestrate.chukkars.android;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import pl.polidea.customwidget.TheMissingTabActivity;
import pl.polidea.customwidget.TheMissingTabHost;
import pl.polidea.customwidget.TheMissingTabHost.TheMissingTabSpec;
import pl.polidea.customwidget.TheMissingTabWidget;

import com.defenestrate.chukkars.android.persistence.SignupDbAdapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class ChukkarSignup extends TheMissingTabActivity
{
	//////////////////////////////// CONSTANTS /////////////////////////////////
	static private final String RESET_DATE_FILENAME = "last-reset-date.txt";
	static final String TAB_INDEX_KEY = "TAB_INDEX_KEY";
	
	
	//////////////////////////// MEMBER VARIABLES //////////////////////////////
	private Handler _handler;


	//////////////////////////// Activity METHODS //////////////////////////////
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		_handler = new Handler() 
        {
            @Override
            public void handleMessage(Message msg) 
            {
                if(msg.what == R.id.message_what_error)
                {
	                if(msg.arg1 != 0)
	                {
	            		ErrorToast.show( ChukkarSignup.this, 
	            						 getResources().getString(msg.arg1) );
	                }
                }
            }
        };
		
		queryWebAppResetDate();
		
	    setContentView(R.layout.tab_content);

	    Resources res = getResources(); // Resource object to get Drawables
	    TheMissingTabHost tabHost = getTabHost();  // The activity TabHost
	    TheMissingTabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab
	    
	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, SignupActivity.class);
	    intent.putExtra(TAB_INDEX_KEY, 0);

	    // Initialize a TabSpec for each tab and add it to the TabHost
	    View tabView = getTabIndicator(
	    	res.getString(R.string.day0_init_tab_title), 
	    	"", 
	    	tabHost.getTabWidget() );
	    spec = tabHost.newTabSpec("day0").setIndicator(tabView).setContent(intent);
	    tabHost.addTab(spec);

	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, SignupActivity.class);
	    intent.putExtra(TAB_INDEX_KEY, 1);
	    
	    tabView = getTabIndicator(
	    	res.getString(R.string.day1_init_tab_title), 
	    	"", 
	    	tabHost.getTabWidget() );
	    spec = tabHost.newTabSpec("day1").setIndicator(tabView).setContent(intent);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTab(0);
	}
	
	/**
	 * See when the last time all signup data in the webapp was reset, and clear
	 * out any saved player ids in local DB accordingly 
	 */
	private void queryWebAppResetDate()
	{
		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				InputStream is = null;
				String result = "";
				
				try
				{
					//http get
					HttpClient httpclient = new DefaultHttpClient();
					HttpGet get = new HttpGet( getResources().getString(R.string.query_reset_url) );
					HttpResponse response = httpclient.execute(get);
					
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
				            
			    	result = strWrite.toString().trim();
			    	
			    	//get rid of quotes
			    	if( result.startsWith("\"") )
			    	{
			    		result = result.substring(1);
			    	}
			    	
			    	if( result.endsWith("\"") )
			    	{
			    		result = result.substring(0, result.length() - 1);
			    	}
			    	
			    	DateFormat inParser = DateFormat.getDateTimeInstance();
			    	Date resetDate = inParser.parse(result);
			    	Date prevResetDate = getPreviousWebAppResetDate();
			    	
			    	if( (prevResetDate == null) || resetDate.after(prevResetDate) )
			    	{
			    		writeResetDate(result);
			    		SignupDbAdapter db = new SignupDbAdapter(ChukkarSignup.this);
			    		db.open();
			    		db.deleteAllPlayers();
			    		db.close();
			    	}
			    }
				catch(IOException e)
				{
					//this is if for some reason app can't connect to server.
					//fail silently. We will show an error dialog when tab 
					//attemps to load the lineup
				}
				catch(ParseException e)
				{
					//should never happen
					//show error toast on GUI thread
					Message msg = _handler.obtainMessage(R.id.message_what_error);
			    	msg.arg1 = R.string.unexpected_json_error;
					_handler.sendMessage(msg);
					
					Log.e(this.getClass().getName(), e.getMessage() + "\n\nHTTP response:\n" + result, e);
				}
			}
		});
		
        thread.start();
    }
	
	private Date getPreviousWebAppResetDate()
	{
		String[] existingFiles = fileList();
		boolean doesFileExist = false;
    	
    	for(String currFilename : existingFiles)
    	{
    		if( RESET_DATE_FILENAME.equals(currFilename) )
    		{
    			doesFileExist = true;
    			break;
    		}
    	}
    	
    	if(doesFileExist)
    	{
			FileInputStream fis = null;
			
			try
			{
				fis = openFileInput(RESET_DATE_FILENAME);
				BufferedReader reader = new BufferedReader( new InputStreamReader(fis, "utf-8") );
				StringWriter strWrite = new StringWriter();
		    	
		    	String line = null;
		    	while( (line = reader.readLine()) != null ) 
		    	{
		    		strWrite.append(line + "\n");
		    	}
			            
		    	String result = strWrite.toString();
		    	
		    	DateFormat inParser = DateFormat.getDateTimeInstance();
		    	Date prevResetDate = inParser.parse(result);
		    	
		    	return prevResetDate;
	    	}
	    	catch(IOException e)
	    	{
	    		//can't open and read reset date file. Go ahead and return the
	    		//current date then. This will have the effect of preserving
	    		//the player id's that this user has previously created and
	    		//modified. That way, any warninng messages on subsequent edits 
	    		//will be based on the preserved state. 
	            return new Date();
	    	}
	    	catch(ParseException e)
	    	{
	    		//this means date in the file is corrupted somehow. Go ahead and
	    		//return null, so the file will overwritten with fresh data. 
	    		Log.e(this.getClass().getName(),e.getMessage(), e);
	    		
	    		return null;
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
    	}

    	
    	return null;
	}
	
	private void writeResetDate(String resetDate)
	{
		FileOutputStream fos = null;
		
		try
		{
	    	//write json data to file
	    	fos = openFileOutput(RESET_DATE_FILENAME, Context.MODE_PRIVATE);
	    	fos.write( resetDate.getBytes() );
	    	fos.flush();
		}
		catch(IOException e)
		{
			//unable to write the specified date in the file. Close the file
			//with empty or corrupted data. On next open, getPreviousWebAppResetDate() 
			//will return a null on ParseException, which will then lead to this
			//corrupted file being overwritten.
			Log.e(this.getClass().getName(),e.getMessage(), e);
		}
	    finally
	    {
	    	try
	    	{
	    		if(fos != null)
	    		{
	    			fos.close();
	    		}
	    	}
	    	catch(IOException e) {}
	    }
	}

	/**
     * How to create a tab indicator that has 2 labels.
     */
    private View getTabIndicator(String title1, String title2, TheMissingTabWidget tabWidget)
    {
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View tabIndicator = inflater.inflate(
        	R.layout.signup_2_line_tab,
            tabWidget, // tab widget is the parent
            false); // no inflate params

        final TextView tv1 = (TextView)tabIndicator.findViewById(R.id.title1);
        tv1.setText(title1);
        
        final TextView tv2 = (TextView)tabIndicator.findViewById(R.id.title2);
        tv2.setText(title2);

        return tabIndicator;
    }
}
