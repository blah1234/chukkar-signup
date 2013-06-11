package com.defenestrate.chukkars.android.exception;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

public class CrashReportExceptionHandler implements UncaughtExceptionHandler {
	/////////////////////////////// CONSTANTS //////////////////////////////////
	/** Directory name in the cache where crash reports are written */
    private static final String CRASH_REPORT_DIR = "crash_report";

    private static final String LOG_TAG = CrashReportExceptionHandler.class.getSimpleName();


	/////////////////////////// MEMBER VARIABLES ///////////////////////////////
    private final Context mCtx;
    private final UncaughtExceptionHandler mDefault;


	///////////////////////////// CONSTRUCTORS /////////////////////////////////
    public CrashReportExceptionHandler(Context ctx, UncaughtExceptionHandler defaultHandler) {
    	mCtx = ctx;
        mDefault = defaultHandler;
    }


	//////////////////////////////// METHODS ///////////////////////////////////
    public void uncaughtException(Thread t, Throwable e) {
        //write to file on SD card
        String basePath = getMemorycardCacheUriPrefix();

        if(basePath != null) {
            Date now = new Date();
            String dirPath = basePath + CRASH_REPORT_DIR + "/";
            String path = dirPath + now.getTime() + ".txt";

            FileOutputStream os = null;
            PrintStream ps = null;

            try {
            	File dir = new File(dirPath);

                if( !dir.exists() ) {
                    dir.mkdir();
                }

            	File crashFile = new File(path);
                os = new FileOutputStream(crashFile);
                ps = new PrintStream(os);
                e.printStackTrace(ps);
            } catch(IOException ex) {
                Log.e(LOG_TAG, "Unable to write crash report: " + ex.getMessage(), ex);
            } finally {
            	if(os != null) {
            		try {
						os.close();
					} catch(IOException ex) {
						Log.e(LOG_TAG, "Unable to close file output stream: " + ex.getMessage(), ex);
					}
            	}

            	if(ps != null) {
        			ps.close();
            	}
            }
        }

        //call original handler
        if(mDefault != null) {
            mDefault.uncaughtException(t, e);
        }
    }

    private String getMemorycardCacheUriPrefix() {
        // For Froyo and later, use the external cache dir,
        // which is automatically deleted when the app is uninstalled.
    	if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			//External dir will be null if storage isn't currently mounted.
    		File externalDir = mCtx.getExternalCacheDir();

    		if(externalDir != null) {
    			return externalDir.getAbsolutePath() + "/";
    		}
    	}

        return Environment.getExternalStorageDirectory().getAbsolutePath().toString() + "/";
    }
}
