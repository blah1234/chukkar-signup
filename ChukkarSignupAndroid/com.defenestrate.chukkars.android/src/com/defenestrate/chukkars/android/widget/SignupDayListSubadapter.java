package com.defenestrate.chukkars.android.widget;

import java.util.List;

import android.content.Context;
import android.view.View;

import com.defenestrate.chukkars.android.R;
import com.defenestrate.chukkars.android.util.BaseRow.BaseViewHolder;
import com.defenestrate.chukkars.android.util.PlayerSignupData;
import com.defenestrate.chukkars.android.util.Row;
import com.defenestrate.chukkars.android.util.Row.ViewHolder;
import com.defenestrate.chukkars.android.widget.FancyScrollListAdapter.FancyScrollListSubadapter;

public class SignupDayListSubadapter extends FancyScrollListSubadapter {
	private List<PlayerSignupData> mPlayerSignupList;
	private Row mDataRow;


	public SignupDayListSubadapter(List<PlayerSignupData> data, Context ctx, FancyScrollListSubadapterCallback callback) {
		super(ctx, callback);

		mPlayerSignupList = data;
		mDataRow = new Row(ctx);
	}


	public void refreshHeader() {
		if(getListItemCount() == 0) {
        	setHeader(mContext.getResources().getString(R.string.empty_signup_message), null);
        } else {
        	setHeader(null, null);
        }
	}

	@Override
	public boolean isListItemEnabled(int row) {
		//make sure header rows are not enabled
    	return (row >= 0);
	}

	@Override
	protected void onItemClick(View v, int position, long id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void initRow(BaseViewHolder holder, int position) {
		PlayerSignupData playerData = mPlayerSignupList.get(position);
		mDataRow.initRow(
			(ViewHolder)holder,
			null,
			playerData.mName,
			playerData.mDate,
			playerData.mNumChukkars);
	}

	@Override
	protected int getListItemCount() {
		return mPlayerSignupList.size();
	}

	@Override
	protected Object getListItem(int position) {
		return mPlayerSignupList.get(position);
	}
}
