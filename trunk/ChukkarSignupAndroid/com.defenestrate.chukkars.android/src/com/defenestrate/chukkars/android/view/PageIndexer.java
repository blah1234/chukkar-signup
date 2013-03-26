package com.defenestrate.chukkars.android.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.defenestrate.chukkars.android.R;

/**
 * Graphical bar representation of the page index of a View Pager.<br>
 * this class displays the index of {@link ViewPager} on the top of screen. <br>
 */
public class PageIndexer extends LinearLayout implements ViewPager.OnPageChangeListener {

	public static final String TAG = PageIndexer.class.getSimpleName();

	private ViewPager mViewPager = null;
	private View mCue = null;

	private boolean mNeedsRedraw = false;

	private int mCurrentPosition = -1;
	private int mPageSize = 0;

	public PageIndexer(Context context) {
		super(context);
		initialize(context, null);
	}

	public PageIndexer(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context, attrs);
	}

	public PageIndexer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(context, attrs);
	}

	private void initialize(Context context, AttributeSet attr) {
		mCue = createIndexCue(context);
		addView(mCue, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	/**
	 * set {@link ViewPager} which you want to display index.
	 * this is strong reference, so please make null if you don't use anymore.
	 * @param pager
	 */
	public void setViewPager(ViewPager pager) {
		mViewPager = pager;

	}

	/**
	 * override this if you want to change index cue.
	 * @param context
	 * @return
	 */
	protected View createIndexCue(Context context) {
		LinearLayout view = new LinearLayout(context);
		view.setBackgroundColor(getResources().getColor(R.color.page_index_cue_background));
		return view;
	}


	/**
	 * calculate width of index cue.<br>
	 * index size cue is proportional to {@link ViewPager} children size.<br>
	 * call this method when {@link ViewPager}'s child size is changed. <br>
	 * <br>
	 * Note that index cue will be disappear if ViewPager child count is 1 <br>
	 */
	public void invalidateIndexer() {
		if (mViewPager != null && mViewPager.getAdapter() != null) {
			int child_cnt = mViewPager.getAdapter().getCount();

			// To avoid divide by zero.
			if (child_cnt <= 0) {
				Log.i(TAG, "invalidateIndexer. not yet added child");
				return ;
			}
			ViewGroup.LayoutParams params = mCue.getLayoutParams();
			int screen_width = getMeasuredWidth();
			int width = screen_width;
			params.width = width / child_cnt;
			mCue.setLayoutParams(params);

			mCue.setVisibility(child_cnt <= 1 ? View.GONE : View.VISIBLE);

			if (child_cnt > 1) {
				params = mCue.getLayoutParams();
				screen_width = getMeasuredWidth();
				width = screen_width;
				params.width = width / child_cnt;
				mCue.setLayoutParams(params);
				handleScroll(mViewPager.getCurrentItem(), 0);
			}
		} else {
			Log.w(TAG, "invalidateIndexer(). view pager or adapter is null.");
		}
	}


	protected void handleScroll(int position, float offset) {
		if (mViewPager != null && mViewPager.getAdapter() != null) {
			int child_cnt = mViewPager.getAdapter().getCount();

			if (child_cnt != mPageSize) {
				mPageSize = child_cnt;
				invalidateIndexer();
			}

			int screen_width = getMeasuredWidth();

			// there's some case does not measure width yet.
			// so in this case we need to redraw index.
			if (screen_width == 0) {
				mNeedsRedraw = true;
				mCurrentPosition = position;
			}

			int thumbWidth = screen_width / child_cnt;
			int startX = position * thumbWidth;
			mCue.setX(startX + offset * thumbWidth);
			mCue.invalidate();
		} else {
			Log.w(TAG, "invalidateIndexer(). view pager or adapter is null.");
		}
	}

	@Override
	public void onPageSelected(int position) {
		if (getVisibility() != View.VISIBLE) {
			return;
		}

		handleScroll(position, 0.0f);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		if (getVisibility() != View.VISIBLE) {
			return;
		}

		invalidateIndexer();
	}

	@Override
	public void onPageScrolled(int position, float offset, int offsetPixels) {
		if (getVisibility() != View.VISIBLE) {
			return;
		}

		handleScroll(position, offset);
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		// empty
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		if (getVisibility() != View.VISIBLE) {
			return;
		}

		if (mNeedsRedraw) {
			mNeedsRedraw = false;
			handleScroll(mCurrentPosition, 0.0f);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		if (getVisibility() != View.VISIBLE) {
			return;
		}

		if (mNeedsRedraw) {
			mNeedsRedraw = false;
			handleScroll(mCurrentPosition, 0.0f);
		}
	}

}
