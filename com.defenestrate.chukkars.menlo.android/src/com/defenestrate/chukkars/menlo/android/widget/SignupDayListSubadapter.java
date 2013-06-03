package com.defenestrate.chukkars.menlo.android.widget;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;

import com.defenestrate.chukkars.menlo.android.R;
import com.defenestrate.chukkars.menlo.android.util.BaseRow.BaseViewHolder;
import com.defenestrate.chukkars.menlo.android.util.PlayerSignupData;
import com.defenestrate.chukkars.menlo.android.util.Row;
import com.defenestrate.chukkars.menlo.android.util.Row.ViewHolder;
import com.defenestrate.chukkars.menlo.android.widget.FancyScrollListAdapter.FancyScrollListSubadapter;

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
        	setHeader(
        		mContext.getResources().getString(R.string.empty_signup_message),
        		(BitmapDrawable)mContext.getResources().getDrawable(R.drawable.icon_add_player) );
        } else {
        	setHeader(null, null);
        }
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
