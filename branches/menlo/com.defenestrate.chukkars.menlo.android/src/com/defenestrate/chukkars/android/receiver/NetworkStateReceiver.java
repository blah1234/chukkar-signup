package com.defenestrate.chukkars.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.defenestrate.chukkars.android.Main;
import com.defenestrate.chukkars.android.util.Constants;

public class NetworkStateReceiver extends BroadcastReceiver
								  implements Constants {

	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectivityManager mgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = mgr.getActiveNetworkInfo();
		Boolean hasConnectivity = null;

		if(ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
			//Network connected
			hasConnectivity = Boolean.TRUE;
		} else if( intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE) ) {
			//There's no network connectivity
			hasConnectivity = Boolean.FALSE;
		}


		if(hasConnectivity != null) {
			Intent i= new Intent(context, Main.class);
	        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	        i.putExtra(HAS_NETWORK_CONNECTIVITY_KEY, hasConnectivity);
	        context.startActivity(i);
		}
	}
}
