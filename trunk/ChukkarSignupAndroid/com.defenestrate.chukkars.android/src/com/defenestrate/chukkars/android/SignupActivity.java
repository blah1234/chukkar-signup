package com.defenestrate.chukkars.android;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.defenestrate.chukkars.android.entity.Day;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

abstract public class SignupActivity extends Activity 
{
	//////////////////////////// Activity METHODS //////////////////////////////
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_table);
    }
    
    
	///////////////////////////////// METHODS //////////////////////////////////
    protected void loadPlayers(Day selectedDay)
    {
    	FileInputStream fis = null;
    	String result = "";
    	
    	try
    	{
    		fis = openFileInput(ChukkarSignup.SERVER_DATA_FILENAME);
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
	    	JSONArray jArray = new JSONArray(result);
	    	
	    	/* Find Tablelayout defined in main.xml */
            TableLayout tl = (TableLayout)findViewById(R.id.players_display_table);
            LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            
            DateFormat inParser = DateFormat.getDateTimeInstance();
            DateFormat outFormatter = new SimpleDateFormat("EEE, M/d h:mm a");
	    	
            for(int i=0, n=jArray.length(); i<n; i++)
            {
                JSONObject data = jArray.getJSONObject(i);
                
                Day currRequestDay = Day.valueOf( data.getString( res.getString(R.string.player_requestDay_field)) );
                if(currRequestDay == selectedDay)
                {
                	/* Create and add a new row */
                	View rootView = inflater.inflate(R.layout.signup_table_row, tl, true);

                    //assign the data to the appropriate columns
                    TextView idText = (TextView)rootView.findViewById(R.id.player_id_col);
                    idText.setText( data.getString(res.getString(R.string.player_id_field)) );
                    
                    //------------
                    
                	TextView timeText = (TextView)rootView.findViewById(R.id.player_time_col);
                	try
            		{
                		String createDate = data.getString( res.getString(R.string.player_createDate_field) );
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
                	nameText.setText( data.getString(res.getString(R.string.player_name_field)) );
                	
                	//-----------
                	
                	TextView chukkarsText = (TextView)rootView.findViewById(R.id.player_chukkars_col);
                	chukkarsText.setText( data.getString(res.getString(R.string.player_numChukkars_field)) );
                }
            }
	    }
	    catch(JSONException e)
	    {
		    	//TODO:
            throw new RuntimeException(e);
	    }
    }
}