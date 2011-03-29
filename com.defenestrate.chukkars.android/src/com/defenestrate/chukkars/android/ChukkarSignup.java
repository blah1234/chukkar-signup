package com.defenestrate.chukkars.android;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class ChukkarSignup extends TabActivity
{
	//////////////////////////////// CONSTANTS /////////////////////////////////
	static protected final String SERVER_DATA_FILENAME = "all-players.json";
	

	//////////////////////////// Activity METHODS //////////////////////////////
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.signup_tabs);

	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab
	    
	    writeServerData();

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, SaturdaySignupActivity.class);

	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("saturday").setIndicator(
	    	"Saturday", res.getDrawable(R.drawable.ic_tab_sat) ).setContent(intent);
	    tabHost.addTab(spec);

	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, SundaySignupActivity.class);
	    spec = tabHost.newTabSpec("sunday").setIndicator(
	    	"Sunday", res.getDrawable(R.drawable.ic_tab_sun) ).setContent(intent);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTab(0);
	}
	
	
	///////////////////////////////// METHODS //////////////////////////////////
	private void writeServerData() 
	{
		InputStream is = null;
		String result = "";
		Resources res = getResources(); 

		//http get
		try
		{
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet get = new HttpGet( res.getString(R.string.get_players_url) );
			HttpResponse response = httpclient.execute(get);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
		}
		catch(Exception e)
		{
			//TODO:
            throw new RuntimeException(e);
	    }

	    //convert response to string
	    try
	    {
	    	BufferedReader reader = new BufferedReader( new InputStreamReader(is,"utf-8") );
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
	    		if(is != null)
	    		{
	    			is.close();
	    		}
	    	}
	    	catch(IOException e) {}
	    }
	    
	    
	    //write json data to file
	    FileOutputStream fos = null;
	    try
	    {
	    	fos = openFileOutput(SERVER_DATA_FILENAME, Context.MODE_PRIVATE);
	    	fos.write( result.getBytes() );
	    	fos.close();
	    }
	    catch(FileNotFoundException e)
	    {
	    	//TODO:
            throw new RuntimeException(e);
	    }
		catch (IOException e)
		{
			//TODO:
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
