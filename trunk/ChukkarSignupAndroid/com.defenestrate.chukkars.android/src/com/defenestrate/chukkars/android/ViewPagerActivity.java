package com.defenestrate.chukkars.android;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.defenestrate.chukkars.android.widget.PageIndexer;

/**
 * Base class of a view pager
 * (horizontal swiping of multiple pages).
 */
public class ViewPagerActivity extends ChukkarsActivity implements ViewPager.OnPageChangeListener {

	private static final int FADE_IN 			= 0;
	private static final int FADE_OUT 			= 1;
	private static final int FADE_IN_AND_OUT	= 2;

    protected ViewPager mViewPager;
    protected PageIndexer mPageIndexer;
    private MusicHubPagerAdapter mPagerAdapter;
    private LinkedHashSet<OnPageChangeListener> mOnPageChangeLstnrs;

    /** Name of the persisted state for the index of the selected page. */
    private static final String STATE_SELECTED_PAGE = "com.defenestrate.chukkars.android.ViewPagerActivity.selected_page";
	public static final String TAG = "ViewPagerActivity" ;

    private boolean mUseIndexer = false;

    private ImageView mSwipeIndicatorLeft;
    private ImageView mSwipeIndicatorRight;

    private boolean mRightIndicatorShown = false;
    private boolean mLeftIndicatorShown = false;
    private boolean mIsSwipeShownOnce = false;


    public ViewPagerActivity() {
    	mOnPageChangeLstnrs = new LinkedHashSet<ViewPager.OnPageChangeListener>();
    }

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.view_pager);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mPageIndexer = (PageIndexer) findViewById(R.id.view_pager_indexer);
        mPagerAdapter = createViewPagerAdapter(mViewPager);

        mSwipeIndicatorLeft = (ImageView)findViewById(R.id.swipe_indicator_left);
        mSwipeIndicatorRight = (ImageView)findViewById(R.id.swipe_indicator_right);

        mSwipeIndicatorLeft.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					selectLeftPage();
					return true;
				} else {
					return false;
				}
			}
		});

        mSwipeIndicatorRight.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					selectRightPage();
					return true;
				} else {
					return false;
				}
			}
		});
    }

    @Override
    protected void onResume() {
    	super.onResume();
    }

	public void showInitialIndicator() {
		if(!mIsSwipeShownOnce) {
    		mIsSwipeShownOnce = true;

			int currentPage = mViewPager.getCurrentItem();
	    	int totalPages = mPagerAdapter.getCount();


	    	if(currentPage > 0) {
	    		startFadeAnimation(mSwipeIndicatorLeft, FADE_IN_AND_OUT);
	    		mLeftIndicatorShown = true;
	    	} else if(mLeftIndicatorShown) {
	    		mLeftIndicatorShown = false;
	    		startFadeAnimation(mSwipeIndicatorLeft, FADE_OUT);
	    	}

	    	if(currentPage < totalPages - 1) {
	    		startFadeAnimation(mSwipeIndicatorRight, FADE_IN_AND_OUT);
	    		mRightIndicatorShown = true;
	    	} else if(mRightIndicatorShown) {
	    		mRightIndicatorShown = false;
	    		startFadeAnimation(mSwipeIndicatorRight, FADE_OUT);
	    	}
		}
	}

    /**
     * Creates the view pager adapter for this activity.
     * May be overridden.
     *
     * @param viewPager             The view pager.
     * @return                      The adapter for the view pager.
     */
    protected MusicHubPagerAdapter createViewPagerAdapter(ViewPager viewPager) {
        return new MusicHubPagerAdapter(this, viewPager);
    }

    /**
     * default value is false.
     * @param show
     */
    protected void setUsePagerIndexer(boolean show) {
    	mUseIndexer = show;
    	if (mUseIndexer) {
        	mPageIndexer.setVisibility(View.VISIBLE);
            addOnPageChangeListener(mPageIndexer);
            mPageIndexer.setViewPager(mViewPager);
    	} else {
            mPageIndexer.setVisibility(View.GONE);
            removeOnPageChangeListener(mPageIndexer);
            mPageIndexer.setViewPager(null);
    	}
    }

    /**
     * calculate width of index cue.
     * index size cue is proportional to ViewPager children size.
     * call this method when ViewPager's child size is changed.
     */
    protected void invalidateIndexer() {
    	mPageIndexer.invalidateIndexer();
    }

    @Override
	protected void onDestroy() {
		super.onDestroy();

		// to avoid memory leak, remove listener and make reference null.
		removeOnPageChangeListener(mPageIndexer);
		mPageIndexer.setViewPager(null);
	}

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(STATE_SELECTED_PAGE, mViewPager.getCurrentItem());
    }

	@Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if(savedInstanceState != null){ //PLM-923
        	mViewPager.setCurrentItem(savedInstanceState.getInt(STATE_SELECTED_PAGE));
        }
    }

	/**
	 * Gets the number of pages in the view pager.
	 *
	 * @return                 The number of pages in the view pager.
	 */
	protected int getCount() {
	    return mPagerAdapter.getCount();
	}

    /**
     * Adds a page to the view pager.
     *
     * @param clss              The class of the page's <code>Fragment</code>.
     * @param args              Arguments for the page's <code>Fragment</code>.
     */
    protected void addPage(Class<?> clss, Bundle args) {
    	mPagerAdapter.addPage(clss, args);
    }

    /**
     * Inserts a page to the view pager.
     *
     * @param location          The index at which to insert.
     * @param clss              The class of the page's <code>Fragment</code>.
     * @param args              Arguments for the page's <code>Fragment</code>.
     */
    protected void addPage(int location, Class<?> clss, Bundle args) {
        mPagerAdapter.addPage(location, clss, args);
    }

    /**
     * Removes a page from the view pager.
     *
     * @param position Index of the <code>Fragment</code> to remove.
     */
    public void removePage(final int position) {
        mPagerAdapter.removePage(position);
    }

    /**
     * Removes a page from the view pager and, optionally,
     * forces the pager to refresh all the pages. Use sparingly
     * as it uses more resources, designed to address problems when
     * adding and deleting pages dynamically.
     * @param position
     * @param forced
     */
    public void removePage(final int position, boolean forced) {
    	mPagerAdapter.removePage(position, forced);
    }

    /**
     * Removes all the pages from the view pager, and forces the pager to refresh
     * itself after the last page has been removed.
     */
    protected void removeAllPages() {
    	for(int i=getCount() - 1; i>=0; i--) {
    		mPagerAdapter.removePage(i, i == 0);
    	}
    }

    /**
     * Removes all pages except the specified position from the view pager.
     *
     * @param position Index of the <code>Fragment</code> to keep.
     */
    public void removeAllPagesExcept(final int position) {
        mPagerAdapter.removeAllPagesExcept(position);
    }

    /**
     * Removes all pages except the specified set from the view pager.
     *
     * @param posSet Set of the <code>Fragment</code>s to keep.
     */
    public void removeAllPagesExcept(final Set<Integer> posSet) {
        mPagerAdapter.removeAllPagesExcept(posSet);
    }

    /**
     * Selects a page in the view pager.
     *
     * @param position          Position index of the page to select.
     */
    protected void selectPage(int position) {
        mViewPager.setCurrentItem(position);
    }


    /**
     * Selects page to the left of current item. Do nothing if already at the leftmost item.
     */
    public void selectLeftPage() {
    	int currentItem = mViewPager.getCurrentItem();
    	if (currentItem > 0) {
            mViewPager.setCurrentItem(currentItem - 1);
    	}
   }

    /**
     * Selects page to the right of current item. Do nothing if already at the rightmost item.
     */
    public void selectRightPage() {
    	int currentItem = mViewPager.getCurrentItem();
    	if (currentItem < mPagerAdapter.getCount()-1) {
            mViewPager.setCurrentItem(currentItem + 1);
    	}
    }


    /**
     * Selects page to the left of current item. If already at the first item, select last item.
     */
    public void selectLeftPageCircular() {
    	int nextItem = mViewPager.getCurrentItem() - 1;
    	if (nextItem < 0) {
    		nextItem = mPagerAdapter.getCount()-1;
    	}
        mViewPager.setCurrentItem(nextItem);
    }

    /**
     * Selects page to the right of current item. If already at the last item, select first item.
     */
    public void selectRightPageCircular() {
    	int nextItem = mViewPager.getCurrentItem() + 1;
    	if (nextItem >= mPagerAdapter.getCount()) {
    		nextItem = 0;
    	}
        mViewPager.setCurrentItem(nextItem);
    }

    /**
     * @param position Position index of the page to retrieve.
     * @return a page in the view pager
     */
    public Fragment getPage(int position) {
    	return mPagerAdapter.getItem(position);
    }

    /**
     * @return the currently visible page in the view pager
     */
    public Fragment getCurrentPage() {
    	int position = mViewPager.getCurrentItem();
    	return getPage(position);
    }

    /**
     * Cumulatively add an OnPageChangeListener to this activity.
     */
    public void addOnPageChangeListener(OnPageChangeListener lstnr) {
    	mOnPageChangeLstnrs.add(lstnr);
    }

    /**
     * Remove the specified OnPageChangeListener from this activity.
     */
    public void removeOnPageChangeListener(OnPageChangeListener lstnr) {
    	mOnPageChangeLstnrs.remove(lstnr);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        int currentPage = mViewPager.getCurrentItem();
        int totalPages = mPagerAdapter.getCount();
        if( !mOnPageChangeLstnrs.isEmpty() ) {
        	for(OnPageChangeListener currLstnr : mOnPageChangeLstnrs) {
        		currLstnr.onPageScrollStateChanged(state);
        	}
        }

        /** @todo for swipe indicators for each page change
        switch(state) {
        case ViewPager.SCROLL_STATE_IDLE:
        	if(mLeftIndicatorShown) {
        		mLeftIndicatorShown = false;
        		startFadeAnimation(mSwipeIndicatorLeft, FADE_OUT);
        	}
        	if(mRightIndicatorShown) {
        		mRightIndicatorShown = false;
        		startFadeAnimation(mSwipeIndicatorRight, FADE_OUT);
        	}
        	break;
        case ViewPager.SCROLL_STATE_DRAGGING:
        	break;
        case ViewPager.SCROLL_STATE_SETTLING:
        	if(currentPage > 0) {
        		startFadeAnimation(mSwipeIndicatorLeft, FADE_IN);
        		mLeftIndicatorShown = true;
        	} else if(mLeftIndicatorShown) {
        		mLeftIndicatorShown = false;
        		startFadeAnimation(mSwipeIndicatorLeft, FADE_OUT);
        	}

        	if(currentPage < totalPages - 1) {
        		startFadeAnimation(mSwipeIndicatorRight, FADE_IN);
        		mRightIndicatorShown = true;
        	} else if(mRightIndicatorShown) {
        		mRightIndicatorShown = false;
        		startFadeAnimation(mSwipeIndicatorRight, FADE_OUT);
        	}
        	break;
        } */
    }

	/**
	 * Convenience method to start a fade animation.
	 *
	 * @param v
	 *            The view to fade.
	 * @param fadeIn
	 *            <code>true</code> to fade in, <code>false</code> to fade out.
	 */
	private void startFadeAnimation(final View v, int fade) {
		Animation prev = v.getAnimation();
		if(prev != null) {
			prev.setAnimationListener(null);
			prev.cancel();
		}
		int animId = R.anim.swipe_fade;
		boolean fadeIn = true;
		boolean fadeOut = true;
		if(fade == FADE_IN) {
			animId = R.anim.swipe_fade_in;
			fadeOut = false;
		} else if(fade == FADE_OUT) {
			animId = R.anim.swipe_fade_out;
			fadeIn = false;
		}
		final Animation anim = AnimationUtils.loadAnimation(this, animId);

		anim.setAnimationListener(new FadeListener(v, fadeIn, fadeOut));

		v.setTag(R.id.position_tag, fadeOut);
		v.startAnimation(anim);
	}

	private static class FadeListener implements AnimationListener {
		View v;
		boolean fadeIn;
		boolean fadeOut;

		FadeListener(View view, boolean fadeIn, boolean fadeOut) {
			v = view;
			this.fadeIn = fadeIn;
			this.fadeOut = fadeOut;
		}

		@Override
		public void onAnimationStart(Animation animation) {
			if(fadeIn)
				v.setVisibility(View.VISIBLE);
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			boolean targetVisibility = (Boolean)v.getTag(R.id.position_tag);
			if(fadeOut && targetVisibility)
				v.setVisibility(View.GONE);
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

	}

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    	if( !mOnPageChangeLstnrs.isEmpty() ) {
        	for(OnPageChangeListener currLstnr : mOnPageChangeLstnrs) {
        		currLstnr.onPageScrolled(position, positionOffset, positionOffsetPixels);
        	}
        }
    }

    @Override
    public void onPageSelected(int position) {
    	if( !mOnPageChangeLstnrs.isEmpty() ) {
        	for(OnPageChangeListener currLstnr : mOnPageChangeLstnrs) {
        		currLstnr.onPageSelected(position);
        	}
        }
    }

    /**
     * Adapter of the view pager.
     */
    protected class MusicHubPagerAdapter extends FragmentStatePagerAdapter {

        protected final Context mContext;
        protected final List<PageInfo> mPages = new ArrayList<PageInfo>();
        protected boolean mForcedRefresh = false;

        protected class PageInfo {
            public final Class<?> clss;
            public Bundle args;

            protected PageInfo(Class<?> _clss, Bundle _args) {
                clss = _clss;
                args = _args;
            }
        }

        protected MusicHubPagerAdapter(Activity activity, ViewPager pager) {
            super(activity.getFragmentManager());

            mContext = activity;
            pager.setAdapter(this);
            pager.setOnPageChangeListener(ViewPagerActivity.this);
        }

        /**
         * Adds a page to the view pager.
         *
         * @param clss              The class of the page's <code>Fragment</code>.
         * @param args              Arguments for the page's <code>Fragment</code>.
         */
        public void addPage(Class<?> clss, Bundle args) {
            final PageInfo info = new PageInfo(clss, args);
            mPages.add(info);
            notifyDataSetChanged();
        }

        /**
         * Inserts a page to the view pager.
         *
         * @param location          The index at which to insert.
         * @param clss              The class of the page's <code>Fragment</code>.
         * @param args              Arguments for the page's <code>Fragment</code>.
         */
        public void addPage(int location, Class<?> clss, Bundle args) {
            final PageInfo info = new PageInfo(clss, args);
            mPages.add(location, info);
            mForcedRefresh = true;
            notifyDataSetChanged();
            mForcedRefresh = false;
        }

        /**
         * Removes a page from the view pager.
         *
         * @param position Index of the <code>Fragment</code> to remove.
         */
        public void removePage(final int position) {
            removePage(position, false);
        }

        /**
         * Removes a page from the view pager and optionally
         * forces the adapter to refresh all the pages
         * @param position
         * @param forceRefresh
         */
        public void removePage(final int position, boolean forceRefresh) {
        	mPages.remove(position);
        	mForcedRefresh = forceRefresh;
        	notifyDataSetChanged();
        	mForcedRefresh = false;
        }

        @Override
        public int getItemPosition(Object object) {
        	if (mForcedRefresh) {
        		return POSITION_NONE;
        	} else {
        		return super.getItemPosition(object);
        	}
        }

        /**
         * Removes all pages except the specified position from the view pager.
         *
         * @param position Index of the <code>Fragment</code> to keep.
         */
        public void removeAllPagesExcept(final int position) {
            for (int page = mPages.size()-1; page >= 0; --page) {
                if (page != position) {
                    mPages.remove(page);
                }
            }

            notifyDataSetChanged();
        }

        /**
         * Removes all pages except the specified set from the view pager.
         *
         * @param position Index of the <code>Fragment</code>s to keep.
         */
        public void removeAllPagesExcept(final Set<Integer> posSet) {
            for (int page = mPages.size()-1; page >= 0; --page) {
                if (!posSet.contains(Integer.valueOf(page))) {
                    mPages.remove(page);
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int position) {
        	//PLM-399
        	try{
        		if(mPages != null && mPages.size() > position) {
        			final PageInfo info = mPages.get(position);
        			return Fragment.instantiate(mContext, info.clss.getName(), info.args);
        		}
        		else
        			return null;
        	}catch(Exception e){
        		Log.e(TAG, "FragmentStatePagerAdapter unable to create new instance of fragment", e);
        		return null;
        	}
        }

        @Override
        public int getCount() {
            return mPages.size();
        }

		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
			invalidateIndexer();
		}


    }


/*************************************************************************************
 *
 *  The following code implements left/right air motion gestures for ViewPagerActivity
 *  It is currently disabled at the request of the mobile UI/UX group
 *
 **********
    private final BroadcastReceiver mSecGestureReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int cmd = intent.getIntExtra(SimpleGestureManager.EXTRA_COMMAND, -1);
            if (SimpleGestureManager.ACTION_GESTURE.equals(intent.getAction())) {
                if (SimpleGestureManager.GESTURE_SWEEP_LEFT == cmd) {
                    selectRightPage();
                } else if (SimpleGestureManager.GESTURE_SWEEP_RIGHT == cmd) {
                    selectLeftPage();
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        if (mAppResources.simpleGestureManager != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(SimpleGestureManager.ACTION_GESTURE);
            registerReceiver(mSecGestureReceiver, filter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // There is no API to check if a receiver is currently registered. Just unregister it and let the exception happen if it isn't.
        try {
            unregisterReceiver(mSecGestureReceiver);
        } catch(IllegalArgumentException e) {
        }
    }

**********
*
*
*************************************************************************************/

}
