package com.defenestrate.chukkars.android;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ErrorToast
{
	/**
	 * Show an error toast notification on the device. 
	 * @param ctx context of the toast
	 * @param errMsg message to display
	 */
	static public void show(Activity ctx, String errMsg)
	{
		LayoutInflater inflater = (LayoutInflater)
			ctx.getSystemService(ctx.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(
			R.layout.error_toast, 
			(ViewGroup) ctx.findViewById(R.id.error_toast_layout_root) );

		ImageView image = (ImageView)layout.findViewById(R.id.image);
		image.setImageResource(R.drawable.android_fail_whale);
		TextView text = (TextView)layout.findViewById(R.id.text);
		text.setText(errMsg);

		Toast toast = new Toast(ctx);
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.show();
	}
}
