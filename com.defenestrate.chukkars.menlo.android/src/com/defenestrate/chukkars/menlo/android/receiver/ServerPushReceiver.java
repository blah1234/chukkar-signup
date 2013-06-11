package com.defenestrate.chukkars.menlo.android.receiver;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.defenestrate.chukkars.menlo.android.Main;
import com.defenestrate.chukkars.menlo.android.R;
import com.defenestrate.chukkars.menlo.android.util.Constants;
import com.defenestrate.chukkars.menlo.android.util.CoverArtUtil;
import com.defenestrate.chukkars.menlo.android.util.CoverArtUtil.CoverArtData;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class ServerPushReceiver extends BroadcastReceiver implements Constants {
	/////////////////////////////// CONSTANTS //////////////////////////////////
	public static final String NOTIFICATION_TAG = ServerPushReceiver.class.getName();
    public static final int NOTIFICATION_ID = 1;

	static private final String LOG_TAG = ServerPushReceiver.class.getSimpleName();


	//////////////////////////////// METHODS ///////////////////////////////////
    @Override
    public void onReceive(Context context, Intent intent) {
    	GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        String messageType = gcm.getMessageType(intent);

        if(GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            sendNotification( context, intent.getExtras() );
        }

        setResultCode(Activity.RESULT_OK);
    }

    private void sendNotification(Context ctx, Bundle intentExtras) {
    	String messageType = intentExtras.getString( PayloadDataKey.MESSAGE_TYPE.toString() );

    	if(messageType != null) {
    		int titleResId = -1;
    		String contentTxt = null;

    		if(MessageType.valueOf(messageType) == MessageType.SIGNUP_NOTICE) {
    			titleResId = R.string.signup_notice_title;

    			contentTxt = ctx.getString(R.string.signup_notice_message);
    			String day = intentExtras.getString( PayloadDataKey.REMINDER_DAY_OF_WEEK.toString() );

    			if(day != null) {
    				try {
	    				int dayNum = Integer.parseInt(day);
	    				Calendar cal = Calendar.getInstance();
	    				cal.set(Calendar.DAY_OF_WEEK, dayNum);
	    				String dayOfWeek = cal.getDisplayName(
	    					Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault() );

	    				contentTxt = MessageFormat.format(contentTxt, new Object[] {dayOfWeek});
    				} catch(NumberFormatException e) {
    					Log.e(LOG_TAG,
			    			"GCM message received an unparsable \"reminder day of week\" in the payload: " + day,
			    			new Throwable().fillInStackTrace() );
    				}
    			}
    		} else if(MessageType.valueOf(messageType) == MessageType.SIGNUP_REMINDER) {
    			titleResId = R.string.signup_reminder_title;
    			contentTxt = ctx.getString(R.string.signup_reminder_message);
    		} else {
    			Log.e(LOG_TAG,
	    			"GCM message received an invalid \"message type\" in the payload: " + messageType,
	    			new Throwable().fillInStackTrace() );
    		}


    		if(titleResId != -1 && contentTxt != null) {
    			sendNotification(ctx, titleResId, contentTxt);
    		}
    	} else {
    		Log.e(LOG_TAG,
    			"GCM message received without a \"message type\" in the payload. No notification sent.",
    			new Throwable().fillInStackTrace() );
    	}
    }

    /**
     * Put the GCM message into a notification and post it.
     */
	private void sendNotification(Context ctx, int titleResId, String contentText) {
    	NotificationManager nm = (NotificationManager)
    		ctx.getSystemService(Context.NOTIFICATION_SERVICE);

    	Intent i = new Intent(ctx, Main.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        //For some reason this combo of PendingIntent flags are needed to get
        //the above Intent flags to work.  Weird.  See:
        //http://stackoverflow.com/questions/5538969/click-on-notification-starts-activity-twice
    	PendingIntent contentIntent = PendingIntent.getActivity(
    		ctx, 0, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

    	CoverArtData data = CoverArtUtil.getRandomCoverArt( ctx.getResources() );
    	CoverArtUtil.freeCoverArtId(data.mCoverArtId);	//Don't need to reserve the cover from other fragments

    	Notification notify = new NotificationCompat.BigPictureStyle(
    		new NotificationCompat.Builder(ctx)
    			.setContentTitle( ctx.getString(titleResId) )
    			.setLargeIcon( ((BitmapDrawable)ctx.getResources().getDrawable(R.drawable.tw_action_bar_icon_add_player_holo_light)).getBitmap() )
    			.setSmallIcon(R.drawable.ic_stat_signup_notify)
    			.setContentText(contentText)
    			.setContentIntent(contentIntent))
    		.bigPicture( ((BitmapDrawable)data.mCoverArtDrawable).getBitmap() )
    		.setSummaryText(contentText)
    		.build();

    	nm.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notify);
    }


	//////////////////////////// INNER CLASSES /////////////////////////////////
	private enum PayloadDataKey {
		MESSAGE_TYPE,
		/** Day of the week the signup reminder is for */
		REMINDER_DAY_OF_WEEK
	}

	private enum MessageType {
		/** The signup notice that goes out at the start of the week */
		SIGNUP_NOTICE,
		/** The signup reminder that goes out on the cutoff day */
		SIGNUP_REMINDER
	}
}
