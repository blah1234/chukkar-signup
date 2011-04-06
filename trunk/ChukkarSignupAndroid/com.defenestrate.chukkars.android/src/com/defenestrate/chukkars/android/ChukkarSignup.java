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

import com.defenestrate.chukkars.android.persistence.SignupDbAdapter;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class ChukkarSignup extends TabActivity
{
	//////////////////////////////// CONSTANTS /////////////////////////////////
	static private final String RESET_DATE_FILENAME = "last-reset-date.txt";
	static final String TAB_INDEX_KEY = "TAB_INDEX_KEY"; 


	//////////////////////////// Activity METHODS //////////////////////////////
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		queryWebAppResetDate();
		
	    setContentView(R.layout.signup_tabs);

	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab
	    
	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, SaturdaySignupActivity.class);
	    intent.putExtra(TAB_INDEX_KEY, 0);

	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("day0").setIndicator(
	    	res.getString(R.string.day0_init_tab_title), res.getDrawable(R.drawable.ic_tab_sat) ).setContent(intent);
	    tabHost.addTab(spec);

	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, SundaySignupActivity.class);
	    intent.putExtra(TAB_INDEX_KEY, 1);
	    
	    spec = tabHost.newTabSpec("day1").setIndicator(
	    	res.getString(R.string.day1_init_tab_title), res.getDrawable(R.drawable.ic_tab_sun) ).setContent(intent);
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
				            
			    	String result = strWrite.toString().trim();
			    	
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
					//TODO:
		            throw new RuntimeException(e);
				}
				catch(ParseException e)
				{
					//TODO:
		            throw new RuntimeException(e);
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
			// TODO:
			throw new RuntimeException(e);
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
}
