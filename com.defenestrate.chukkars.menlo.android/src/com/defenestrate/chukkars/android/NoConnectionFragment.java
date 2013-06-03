package com.defenestrate.chukkars.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment to display No Network connection message in case of no Network connection
 *
 */
public class NoConnectionFragment extends ChukkarSignupBaseFragment {

	final public static String LOG_TAG = NoConnectionFragment.class.getName();

	@Override
	protected int getLayoutId() {
		return R.layout.no_data_connection_layout;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		showNetworkErrorPage();
		return v;
	}

	@Override
	public ViewGroup getNoDataConnectionContainer(View createdView) {
		return (ViewGroup) createdView.findViewById(R.id.no_data_connection);
	}
}
