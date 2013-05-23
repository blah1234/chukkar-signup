package com.defenestrate.chukkars.android.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

public class PropertiesUtil {
	static private final String APP_PROPS_FILE = "application.properties";
	static private final String LOG_TAG = PropertiesUtil.class.getSimpleName();

	static private Properties mAppProps;


	static public String getURLProperty(Resources res, String key) {
		if(mAppProps == null) {
			AssetManager assetMgr = res.getAssets();

			//read from the /assets directory
			try {
			    InputStream inputStream = assetMgr.open(APP_PROPS_FILE);
			    mAppProps = new Properties();
			    mAppProps.load(inputStream);
			} catch (IOException e) {
			    Log.e(LOG_TAG, "Unable to load " + APP_PROPS_FILE + ": " + e.getMessage(), e);
			}
		}

		if(mAppProps != null && mAppProps.containsKey(key)) {
			return mAppProps.getProperty("base_url") + mAppProps.getProperty(key);
		} else {
			return null;
		}
	}
}
