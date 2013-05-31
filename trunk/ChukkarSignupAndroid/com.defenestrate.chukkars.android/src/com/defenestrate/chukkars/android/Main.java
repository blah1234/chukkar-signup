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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.defenestrate.chukkars.android.entity.Day;
import com.defenestrate.chukkars.android.persistence.SignupDbAdapter;
import com.defenestrate.chukkars.android.util.Constants;
import com.defenestrate.chukkars.android.util.HttpUtil;
import com.defenestrate.chukkars.android.util.PropertiesUtil;


public class Main extends ViewPagerActivity
				  implements SignupDayFragment.OnPlayerModificationListener, Constants {

	/////////////////////////////// CONSTANTS //////////////////////////////////
	static private final String STARTUP_CONFIG_PREFS_NAME = "startup-config";


	/////////////////////////// MEMBER VARIABLES ///////////////////////////////
	private Handler mHandler;
	private boolean mHasNewData;
	private Day mInitialVisibleDay;


	//////////////////////////////// METHODS ///////////////////////////////////
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        mHasNewData = true;

        // Show the page indexer.
        setUsePagerIndexer(true);
	}

	@Override
	public void onPlayerModificationSave(Day selectedDay) {
		mHasNewData = true;
		mInitialVisibleDay = selectedDay;
	}

	@Override
	public void onPlayerModificationCancel() {
		mHasNewData = false;
	}

	@Override
	protected void onStart() {
		super.onStart();

		initPages( HttpUtil.hasDataConnection(this) );
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if( intent.hasExtra(HAS_NETWORK_CONNECTIVITY_KEY) ) {
			initPages( intent.getBooleanExtra(HAS_NETWORK_CONNECTIVITY_KEY, true) );
		}
	}

	private void initPages(boolean hasDataConnection) {
		if(hasDataConnection) {
			if(mHasNewData) {
				//remove all pages. Next time activity starts up, we will requery
				//or use the cached server data, as appropriate
			    removeAllPages();

				//start the data load process
		        loadActiveDaysAsync();
			} else {
				mHasNewData = false;
			}
		} else {
			removeAllPages();
			addPage(NoConnectionFragment.class, null);
			mHasNewData = true;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		//clear out cached data for players, their requested days and chukkars
		resetCachedPlayerSignups();
	}

	private void resetAllCachedData()
	{
		//also erase the active days data
		SharedPreferences settings = getSharedPreferences(STARTUP_CONFIG_PREFS_NAME, MODE_PRIVATE);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.remove(ACTIVE_DAYS_KEY);
	    editor.commit();

	    //------------

	    resetCachedPlayerSignups();
	}

	private void resetCachedPlayerSignups() {
		SharedPreferences settings = getSharedPreferences(SERVER_DATA_PREFS_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
	    editor.remove(CONTENT_KEY);
	    editor.remove(LAST_MODIFIED_KEY);
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
		    		int selectedPosition = 0;

		    		for(int i=0, n=activeDaysList.size(); i<n; i++) {
		    			Day currDay = activeDaysList.get(i);

		    			if(currDay == mInitialVisibleDay) {
		    				selectedPosition = i;
		    				mInitialVisibleDay = null;
		    			}

		    			Bundle args = new Bundle();
		    			args.putString( SignupDayFragment.SIGNUP_DAY_KEY, currDay.name() );
		    			args.putInt(SignupDayFragment.PAGE_INDEX_KEY, i);

		    			addPage(SignupDayFragment.class, args);
		    		}

		    		selectPage(selectedPosition);
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
	    			resetAllCachedData();
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
    	SharedPreferences settings = getSharedPreferences(STARTUP_CONFIG_PREFS_NAME, Context.MODE_PRIVATE);
        String resetDate = settings.getString(RESET_DATE_KEY, null);

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
		SharedPreferences settings = getSharedPreferences(STARTUP_CONFIG_PREFS_NAME, MODE_PRIVATE);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putString(RESET_DATE_KEY, resetDate);

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
    	SharedPreferences settings = getSharedPreferences(STARTUP_CONFIG_PREFS_NAME, Context.MODE_PRIVATE);
        String activeDaysData = settings.getString(ACTIVE_DAYS_KEY, null);

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
			SharedPreferences settings = getSharedPreferences(STARTUP_CONFIG_PREFS_NAME, MODE_PRIVATE);
		    SharedPreferences.Editor editor = settings.edit();
		    editor.putString(ACTIVE_DAYS_KEY, result);

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
	    	SharedPreferences settings = getSharedPreferences(STARTUP_CONFIG_PREFS_NAME, Context.MODE_PRIVATE);
	        result = settings.getString(ACTIVE_DAYS_KEY, null);

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
