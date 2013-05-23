package com.defenestrate.chukkars.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.defenestrate.chukkars.android.entity.Day;
import com.defenestrate.chukkars.android.persistence.SignupDbAdapter;
import com.defenestrate.chukkars.android.util.Constants;
import com.defenestrate.chukkars.android.util.PropertiesUtil;


public class Main extends ViewPagerActivity
				  implements Constants {

	/////////////////////////////// CONSTANTS //////////////////////////////////
	static private final String STARTUP_CONFIG_PREFS_NAME = "startup-config";


	/////////////////////////// MEMBER VARIABLES ///////////////////////////////
	private Handler mHandler;


	//////////////////////////////// METHODS ///////////////////////////////////
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SignupDayFragment.resetUsedCoverArtIds();

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == R.id.message_what_error) {
	                if(msg.arg1 != 0) {
	            		ErrorToast.show( Main.this,
	            						 getResources().getString(msg.arg1) );
	                }
                }
            }
        };

        // Show the page indexer.
        setUsePagerIndexer(true);

        //start the data load process
        loadActiveDaysAsync();
	}

	@Override
    public void onStop()
	{
    	// The activity is no longer visible (it is now "stopped")
		super.onStop();

		Resources res = getResources();
		SharedPreferences settings = getSharedPreferences(SERVER_DATA_PREFS_NAME, MODE_PRIVATE);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.remove( res.getString(R.string.content_key) );
	    editor.remove( res.getString(R.string.last_modified_key) );

	    // Commit the edits!
	    editor.commit();
	}

	private void loadActiveDaysAsync()
	{
		AsyncTask<Void, Void, List<Day>> task = new AsyncTask<Void, Void, List<Day>>()
		{
			@Override
			protected void onPreExecute()
			{
				//show a "loading" indicator
				showLoading(true);
			}

		    @Override
		    protected List<Day> doInBackground(Void... params)
		    {
		    	queryWebAppResetDate();
		    	List<Day> ret = loadActiveDays();

				return ret;
		    }

		    @Override
		    protected void onPostExecute(List<Day> activeDaysList)
		    {
		    	showLoading(false);

		    	if(activeDaysList != null)
			    {
		    		for(int i=0, n=activeDaysList.size(); i<n; i++) {
		    			Day currDay = activeDaysList.get(i);

		    			Bundle args = new Bundle();
		    			args.putString( SignupDayFragment.SIGNUP_DAY_KEY, currDay.name() );
		    			args.putInt(SignupDayFragment.PAGE_INDEX_KEY, i);

		    			addPage(SignupDayFragment.class, args);
		    		}

		    		showInitialIndicator();
			    }
		    }
		};

		task.execute();
    }

	/**
	 * See when the last time all signup data in the webapp was reset, and clear
	 * out any saved player ids in local DB accordingly. Will also clear out
	 * the active days json file accordingly, too.
	 * @see {@link #getActiveDays()}
	 */
	private void queryWebAppResetDate() {
		InputStream is = null;
		String result = "";

		try
		{
			//http get
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet get = new HttpGet( PropertiesUtil.getURLProperty(getResources(), "query_reset_url") );
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

	    	DateFormat inParser = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.US);
	    	inParser.setTimeZone( TimeZone.getTimeZone("GMT") );
	    	Date resetDate = inParser.parse(result);
	    	Date prevResetDate = getPreviousWebAppResetDate();

	    	if( (prevResetDate == null) || resetDate.after(prevResetDate) )
	    	{
	    		writeResetDate(result);
	    		SignupDbAdapter db = new SignupDbAdapter(Main.this);
	    		db.open();
	    		db.deleteAllPlayers();
	    		db.close();

	    		if(prevResetDate != null)
	    		{
		    		//also erase the active days data
	    			Resources res = getResources();
	    			SharedPreferences settings = getSharedPreferences(STARTUP_CONFIG_PREFS_NAME, MODE_PRIVATE);
	    		    SharedPreferences.Editor editor = settings.edit();
	    		    editor.remove( res.getString(R.string.active_days_key) );
	    		    editor.commit();
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
			Message msg = mHandler.obtainMessage(R.id.message_what_error);
	    	msg.arg1 = R.string.unexpected_json_error;
			mHandler.sendMessage(msg);

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
				DateFormat inParser = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.US);
		    	inParser.setTimeZone( TimeZone.getTimeZone("GMT") );
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

	private List<Day> loadActiveDays() {
    	List<Day> activeDaysList = null;

    	try
    	{
    		activeDaysList = getActiveDays();
    	}
    	catch(IOException e)
		{
			//unable to connect to server
    		//show error toast on GUI thread
			Message msg = mHandler.obtainMessage(R.id.message_what_error);
	    	msg.arg1 = R.string.server_connect_error;
			mHandler.sendMessage(msg);

			Log.e(this.getClass().getName(), e.getMessage(), e);
		}
    	catch(JSONException e)
	    {
	    	//JSON response string does not match what we are expecting
    		//show error toast on GUI thread
			Message msg = mHandler.obtainMessage(R.id.message_what_error);
	    	msg.arg1 = R.string.unexpected_json_error;
			mHandler.sendMessage(msg);

			//already logged in loadActiveDaysData();
	    }

    	return activeDaysList;
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
			HttpGet get = new HttpGet( PropertiesUtil.getURLProperty(getResources(), "get_active_days_url") );
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
}
