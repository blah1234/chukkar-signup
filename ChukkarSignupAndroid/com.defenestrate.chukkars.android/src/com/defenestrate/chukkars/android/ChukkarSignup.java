package com.defenestrate.chukkars.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.defenestrate.chukkars.android.entity.Day;
import com.defenestrate.chukkars.android.persistence.SignupDbAdapter;

public class ChukkarSignup extends Activity
{
	//////////////////////////////// CONSTANTS /////////////////////////////////
	static private final String STARTUP_CONFIG_PREFS_NAME = "startup-config";
	static final String TAB_INDEX_KEY = "TAB_INDEX_KEY";


	//////////////////////////// MEMBER VARIABLES //////////////////////////////
	private Handler _handler;
	private CountDownLatch _createDoneSignal;


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

        _createDoneSignal = new CountDownLatch(1);



	    AsyncTask<Void, Void, List<Day>> task = new AsyncTask<Void, Void, List<Day>>()
		{
			@Override
			protected void onPreExecute()
			{
				//show the "busy" dialog
				Intent i = new Intent(ChukkarSignup.this, ProgressDialogActivity.class);
				startActivityForResult(i, R.id.get_server_data_request);
			}

		    @Override
		    protected List<Day> doInBackground(Void... params)
		    {
		    	List<Day> activeDaysList = null;

		    	try
		    	{
		    		activeDaysList = getActiveDays();
		    	}
		    	catch(IOException e)
	    		{
	    			//unable to connect to server
		    		//show error toast on GUI thread
					Message msg = _handler.obtainMessage(R.id.message_what_error);
			    	msg.arg1 = R.string.server_connect_error;
					_handler.sendMessage(msg);

					Log.e(this.getClass().getName(), e.getMessage(), e);
	    		}
		    	catch(JSONException e)
			    {
			    	//JSON response string does not match what we are expecting
		    		//show error toast on GUI thread
					Message msg = _handler.obtainMessage(R.id.message_what_error);
			    	msg.arg1 = R.string.unexpected_json_error;
					_handler.sendMessage(msg);

					//already logged in loadActiveDaysData();
			    }

		    	_createDoneSignal.countDown();

		    	return activeDaysList;
		    }

		    @Override
		    protected void onPostExecute(List<Day> activeDaysList)
		    {
		    	ChukkarSignup.this.finishActivity(R.id.get_server_data_request);


			    Intent intent;  // Reusable Intent for each tab

			    if(activeDaysList != null)
			    {
				    for(int i=0, n=activeDaysList.size(); i<n; i++)
				    {
				    	Day currDay = activeDaysList.get(i);

					    // Create an Intent to launch an Activity for the tab (to be reused)
					    intent = new Intent().setClass(ChukkarSignup.this, SignupActivity.class);
					    intent.putExtra(TAB_INDEX_KEY, i);

					    // Initialize a TabSpec for each tab and add it to the TabHost
//					    View tabView = getTabIndicator(
//					    	currDay.toString(),
//					    	"",
//					    	tabHost.getTabWidget() );
//					    spec = tabHost.newTabSpec(currDay.toString()).setIndicator(tabView).setContent(intent);
//					    tabHost.addTab(spec);
				    }
			    }
		    }
		};

		task.execute();
	}

	@Override
	public void onStart()
	{
		super.onStart();

		//see if we need to re-config UI and restart
		queryWebAppResetDate();
	}

	private List<Day> getActiveDays() throws IOException, JSONException
	{
    	Resources res = getResources();
    	SharedPreferences settings = getSharedPreferences(STARTUP_CONFIG_PREFS_NAME, Context.MODE_PRIVATE);
        String activeDaysData = settings.getString(res.getString(R.string.active_days_key), null);

    	boolean doesDataExist = (activeDaysData != null);

    	if(!doesDataExist)
    	{
			writeActiveDaysData();
    	}

    	List<Day> retList = loadActiveDaysData(activeDaysData);

		return retList;
	}

	private void writeActiveDaysData() throws IOException
	{
		//get the latest data from the server
		InputStream is = null;
		String result = "";

		try
		{
			//http get
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet get = new HttpGet( /*getResources().getString(R.string.get_active_days_url)*/ );
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

	    	//write json data to file
	    	Resources res = getResources();
			SharedPreferences settings = getSharedPreferences(STARTUP_CONFIG_PREFS_NAME, MODE_PRIVATE);
		    SharedPreferences.Editor editor = settings.edit();
		    editor.putString(res.getString(R.string.active_days_key), result);

		    // Commit the edits!
		    editor.commit();
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
	}

	private List<Day> loadActiveDaysData(String data) throws JSONException
	{
		String result;

		if(data == null)
		{
	    	Resources res = getResources();
	    	SharedPreferences settings = getSharedPreferences(STARTUP_CONFIG_PREFS_NAME, Context.MODE_PRIVATE);
	        result = settings.getString(res.getString(R.string.active_days_key), null);

	        if(result == null)
	    	{
				//orientation changed in the middle of loading, or some other
	    		//way the app was stopped in the middle of loading
	    		return null;
	    	}
		}
		else
		{
			result = data;
		}


    	//parse json data
    	try
    	{
    		List<Day> retList = new ArrayList<Day>();
	    	JSONArray jArray = new JSONArray(result);

	    	for(int i=0, n=jArray.length(); i<n; i++)
	    	{
	    		String currDayStr = jArray.getString(i);
	    		retList.add( Day.valueOf(currDayStr) );
	    	}

	    	return retList;
    	}
    	catch(JSONException e)
	    {
	    	//JSON response string does not match what we are expecting
			Log.e(this.getClass().getName(), e.getMessage() + "\n\nHTTP response:\n" + result, e);

			throw e;
	    }
	}

	/**
	 * See when the last time all signup data in the webapp was reset, and clear
	 * out any saved player ids in local DB accordingly. Will also clear out
	 * the active days json file accordingly, too.
	 * @see {@link #getActiveDays()}
	 */
	private void queryWebAppResetDate()
	{
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>()
		{
		    @Override
		    protected Boolean doInBackground(Void... params)
		    {
		    	InputStream is = null;
				String result = "";
				Boolean doReload = Boolean.FALSE;

				try
				{
					//http get
					HttpClient httpclient = new DefaultHttpClient();
					HttpGet get = new HttpGet( /*getResources().getString(R.string.query_reset_url)*/ );
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

			    		if(prevResetDate != null)
			    		{
				    		//also erase the active days data
			    			try
			    			{
				    			_createDoneSignal.await();

				    			Resources res = getResources();
				    			SharedPreferences settings = getSharedPreferences(STARTUP_CONFIG_PREFS_NAME, MODE_PRIVATE);
				    		    SharedPreferences.Editor editor = settings.edit();
				    		    editor.remove( res.getString(R.string.active_days_key) );
				    		    editor.commit();

					    		doReload = Boolean.TRUE;
			    			}
			    			catch(InterruptedException e)
			    			{
			    				//Preserve evidence that the interruption occurred
			    				//so that code higher up on the call stack can learn
			    				//of the interruption and respond to it if it wants to.
			    				//This will cause the interrupt flag to be set.
			    		        Thread.currentThread().interrupt();
			    			}
			    		}
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

				return doReload;
		    }

		    @Override
		    protected void onPostExecute(Boolean result)
		    {
		    	if(result)
		    	{
		    		reload();
		    	}
		    }
		};

		task.execute();
    }

	private Date getPreviousWebAppResetDate()
	{
		Resources res = getResources();
    	SharedPreferences settings = getSharedPreferences(STARTUP_CONFIG_PREFS_NAME, Context.MODE_PRIVATE);
        String resetDate = settings.getString(res.getString(R.string.reset_date_key), null);

    	boolean doesDataExist = (resetDate != null);

    	if(doesDataExist)
    	{
    		try
    		{
				DateFormat inParser = DateFormat.getDateTimeInstance();
			    Date prevResetDate = inParser.parse(resetDate);

		    	return prevResetDate;
	    	}
	    	catch(ParseException e)
	    	{
	    		//this means date in the file is corrupted somehow. Go ahead and
	    		//return null, so the file will overwritten with fresh data.
	    		Log.e(this.getClass().getName(),e.getMessage(), e);

	    		return null;
	    	}
    	}


    	return null;
	}

	private void writeResetDate(String resetDate)
	{
		Resources res = getResources();
		SharedPreferences settings = getSharedPreferences(STARTUP_CONFIG_PREFS_NAME, MODE_PRIVATE);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putString(res.getString(R.string.reset_date_key), resetDate);

	    // Commit the edits!
	    editor.commit();
	}

	private void reload()
	{
	    Intent intent = getIntent();
	    overridePendingTransition(0, 0);
	    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
	    finish();

	    overridePendingTransition(0, 0);
	    startActivity(intent);
	}
}
