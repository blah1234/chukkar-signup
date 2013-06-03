package com.defenestrate.chukkars.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ProgressDialogActivity extends Activity
{
	//////////////////////////// Activity METHODS //////////////////////////////
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		//Remove title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	    setContentView(R.layout.progress_dialog);
	    
	    ProgressBar bar = (ProgressBar)findViewById(android.R.id.progress);
	    bar.setIndeterminate(true);
	    
	    TextView text = (TextView)findViewById(android.R.id.message);
	    text.setText( getResources().getString(R.string.load_dialog_message) );
	}
}
