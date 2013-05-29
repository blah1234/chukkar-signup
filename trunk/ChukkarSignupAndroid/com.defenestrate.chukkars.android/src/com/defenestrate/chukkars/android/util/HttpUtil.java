package com.defenestrate.chukkars.android.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.defenestrate.chukkars.android.exception.SignupClosedException;

public class HttpUtil implements Constants {

	/**
	 * Writes to persisted preferences the JSON data returned by a server call
	 * @param response JSON data returned by a server call
	 * @param ctx Android context from which a call to this method was made
	 * @return the date the specified data was written. The date is returned as
	 * a millisecond value. The value is the number of milliseconds since
	 * Jan. 1, 1970, midnight GMT.
	 * @throws SignupClosedException if the signup for the particular day
	 * requested of the server is closed
	 * @throws IOException if an error occurred while attempting to read the
	 * returned server response
	 */
	static public long writeServerData(HttpResponse response, Context ctx) throws SignupClosedException, IOException
	{
		InputStream is = null;

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

	    	if( SIGNUP_CLOSED.equals(result.trim()) )
	    	{
	    		throw new SignupClosedException();
	    	}

	    	//write json data to preferences
	    	Resources res = ctx.getResources();
			SharedPreferences settings = ctx.getSharedPreferences(SERVER_DATA_PREFS_NAME, Context.MODE_PRIVATE);
		    SharedPreferences.Editor editor = settings.edit();
		    editor.putString(CONTENT_KEY, result);

		    long dataLastModified = new Date().getTime();
		    editor.putLong(LAST_MODIFIED_KEY, dataLastModified);

		    // Commit the edits!
		    editor.commit();

		    return dataLastModified;
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
}
