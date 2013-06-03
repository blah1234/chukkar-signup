/**
 * @author			<a href="mailto:lwang@mspot.com">Larry Wang</a>
 * @copyright		Copyright (c) 2012 mSpot, Inc. All rights reserved.
 */
package com.defenestrate.chukkars.menlo.android;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.defenestrate.chukkars.menlo.android.widget.FancyScrollListAdapter;
import com.defenestrate.chukkars.menlo.android.widget.FancyScrollListAdapter.FancyScrollListSubadapter.FancyScrollListSubadapterCallback;

/**
 * Wraps calls to {@link FancyScrollListAdapter}.
 */
public abstract class FancyScrollListFragment extends ChukkarSignupBaseFragment {

	public static final String LOG_TAG = "FancyScrollListFragment";

    /** The fancy list adapter. */
    protected FancyScrollListAdapter mListAdapter;

    /** The list view for this fragment. */
    protected ListView mListView;

    /** Index of the subadapter to select. */
    private int mSelectedSubAdapter;

    /** Optional view to add to the bottom of the list */
    private View mFooterView;

    protected LayoutInflater mInflater;

    /** The activity that contains information about what page is being looked at */
    private Activity currentActivity = null;

    @Override
    protected int getLayoutId() {
        return R.layout.fancy_scroll_list;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = super.onCreateView(inflater, container, savedInstanceState);

        mInflater = inflater;

        mListView = (ListView) v.findViewById(android.R.id.list);
        if (mFooterView != null) mListView.addFooterView(mFooterView);

        if(mListAdapter != null) {
        	mListAdapter.onDestroy();
        	mListAdapter = null;
        	Log.i(LOG_TAG, "onCreateView : initialize list adapter.");
        }

        refreshView(inflater, (FrameLayout) v.findViewById(R.id.list_container), false);
        registerForContextMenu(mListView);

        return v;
    }

    /**
     * some adapter is not created in {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)},
     * so this method will create list adpater again and initialize. <br>
     * {@link #onRefreshView()} callback will be invoked if it's self refresh (such as login, network status changed) <br>
     * to refresh adapter again this method will call onRefreshView/onPause/onResume as sequentially <br>
     * @param inflater
     * @param container
     * @param self		<code>false</code> if it's called from framework callback such as {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} <br>
     */
    protected void refreshView(LayoutInflater inflater, FrameLayout container, boolean self) {
    	if (mListAdapter == null) {
            mListAdapter = createListAdapter(inflater, container);
            if (mListAdapter != null) {
            	mListAdapter.onCreate();
            	mListView.setAdapter(mListAdapter);
            	mListView.setOnItemClickListener(mListAdapter);
            	mListView.setOnScrollListener(mListAdapter);
            } else {
            	Log.i(this.toString(), "onRefreshView : mListAdapter is null!!");
            }
    	} else {
    		Log.i(this.toString(), "onRefreshView : mListAdapter is already created");
    	}
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mListAdapter != null) {
        	mListAdapter.setCurrentSubadapter(0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mListAdapter != null) {
        	mListAdapter.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mListAdapter != null) {
        	mListAdapter.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mListAdapter != null) {
        	mListAdapter.onDestroy();
        }
    }

    /** {@inheritDoc} */
    @Override public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	boolean handleContextMenu = false;

    	if(menuInfo != null) {
			final AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
			handleContextMenu = isAncestor(getListView(), info.targetView);
    	} else if( isFragmentVisible() ) {
    		handleContextMenu = true;
    	}

    	if(handleContextMenu) {
    		mListAdapter.onCreateContextMenu(menu, v, menuInfo);
    	}
    }

    /** {@inheritDoc} */
    @Override public boolean onContextItemSelected(MenuItem item) {
    	boolean handleContextMenu = false;
    	ContextMenuInfo menuInfo = item.getMenuInfo();

    	if(menuInfo != null) {
			final AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
			handleContextMenu = isAncestor(getListView(), info.targetView);
    	} else if( isFragmentVisible() ) {
    		handleContextMenu = true;
    	}

    	if(handleContextMenu) {
    		return mListAdapter.onContextItemSelected(item);
    	} else {
    		return false;
    	}
    }

    /**
     * Indicates whether or not the specified parent view is an ancestor of the
     * specified child view. This method will return true if parent and child
     * are the same view.
     * @param parent potential parent view of the specified child view
     * @param child potential child view of specified parent view
     * @return <code>true</code> if the specified parent view is an ancestor of
     * the specified child view; <code>false</code> otherwise
     */
    private boolean isAncestor(View parent, View child) {
    	if(parent == child) {
    		return true;
    	}

    	boolean ret = true;
    	ViewParent childCandidate = child.getParent();

    	while(parent != childCandidate) {
    		if(childCandidate == null) {
    			ret = false;
    			break;
    		}

    		childCandidate = childCandidate.getParent();
    	}

    	return ret;
    }

    /**
     * @return <code>true</code> if the fragment is currently visible to the user.
     * Need this custom implementation because when fragments are attached to
     * a ViewPager, the fragment immediately to the right and offscreen of the
     * currently visible fragment is counted as "visible" in Android's default
     * implementation.
     */
    private boolean isFragmentVisible() {
    	Activity activity = getActivity();

		if(activity instanceof ViewPagerActivity) {
    		Fragment currentPage = ( (ViewPagerActivity)activity ).getCurrentPage();

    		if( (currentPage instanceof FancyScrollListSubadapterCallback) &&
    			(this instanceof FancyScrollListSubadapterCallback) ) {
    			FancyScrollListSubadapterCallback current = (FancyScrollListSubadapterCallback)currentPage;
    			FancyScrollListSubadapterCallback self = (FancyScrollListSubadapterCallback)this;

    			return current.getDataId().equals( self.getDataId() );
    		}
		}

		return isVisible();
    }

    /**
     * @return <code>true</code> if the fragment is on the top of screen.<br>
     */
    protected boolean isFragmentVisibleOnTop() {
    	return isResumed() && isFragmentVisible();
    }


    /**
     * A subclass may override this method to return a pref name to persist
     * the position of the last selected tab across app sessions,
     * so that the selection is restored when the fragment is re-created,
     *
     * @return              The pref name used to persist the position
     *                      of the last selected tab.
     */
    public String getSelectedTabPrefName() {
        return null;
    }

    /**
     * @return          The list view used by this fragment.
     */
    public AbsListView getListView() {
        return mListView;
    }

    /**
     * @return The enclosing, parent list adapter for any fancy scrolling subadapters.
     * @see {@link FancyScrollListSubadapterCallback#getSuperAdapter()}
     */
    public FancyScrollListAdapter getSuperAdapter() {
    	return mListAdapter;
    }

    /**
     * @param inflater  The layout inflater.
     * @param parent    The list view's root container.
     * @return          The list adapter for this fragment.
     */
    protected abstract FancyScrollListAdapter createListAdapter(LayoutInflater inflater, FrameLayout parent);

    /**
     * Call this method before {@link #onCreateView} is called, because the footer view
     * must be set before setAdapter is called.
     *
     * @param footer        Optional view to add to the bottom of the list.
     */
    protected void setFooterView(View footer) {
        mFooterView = footer;
    }
}
