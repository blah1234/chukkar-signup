package com.defenestrate.chukkars.menlo.android;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

abstract public class ChukkarsActivity extends Activity {
	static private final String LOG_TAG = ChukkarsActivity.class.getSimpleName();


	/**
     * Show/hide a loading indicator.
     *
     * @param show          <code>true</code> to show a loading indicator,
     *                      <code>false</code> to hide a loading indicator.
     */
    public void showLoading(boolean show, String text) {
        final View v = findViewById(R.id.page_loading);
        if (v != null) {
        	v.setVisibility(show ? View.VISIBLE : View.GONE);
        	final TextView msg = (TextView) v.findViewById(R.id.loading_msg);
        	if (msg != null) {
        		if (text != null) {
        			msg.setText(text);
        			msg.setVisibility(show ? View.VISIBLE : View.GONE);
        		} else {
        			msg.setVisibility(View.GONE);
        		}
        	}
        } else {
            Log.w(LOG_TAG, "showLoading: view for R.id.loading doesn't exist");
        }
    }

    /**
     * Show/hide a loading indicator.
     *
     * @param show          <code>true</code> to show a loading indicator,
     *                      <code>false</code> to hide a loading indicator.
     */
    public void showLoading(boolean show) {
    	showLoading(show, null);
    }
}
