package com.defenestrate.chukkars.android.widget;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.defenestrate.chukkars.android.R;



/**
 *	A list adapter that allows the use of a selector to switch between sub list adapters.
 *
 * A FrameLayout is needed around the list in order to do the floating selector.
 * <pre>
 * {@code
 *     <FrameLayout
 *          android:id="@+id/list_container"
 *          android:layout_width="match_parent"
 *          android:layout_height="match_parent"
 *      >
 *      <ListView
 *          android:id="@+id/android:list"
 *          android:layout_width="match_parent"
 *          android:layout_height="match_parent"
 *      />
 *  </FrameLayout>
 *  }
 *  </pre>
}
 *
 *
 */
public class FancyScrollListAdapter extends BaseAdapter
									implements OnClickListener, OnScrollListener, OnItemClickListener, OnCreateContextMenuListener {

	private static final String LOG_TAG = FancyScrollListAdapter.class.getSimpleName();

    /** The default row type in a list. */
	private static final int ROW_TYPE_DEFAULT          = BaseListSubdapter.ROW_TYPE_DEFAULT;

    /** The divider row type in a list. */
    private static final int ROW_TYPE_DIVIDER          = BaseListSubdapter.ROW_TYPE_DIVIDER;

	/** The header above the list. */
	private static final int ROW_TYPE_HEADER           = BaseListSubdapter.NEXT_ROW_TYPE;

	/** The non-scrollable, tab-like selector for switching among lists. */
	private static final int ROW_TYPE_LIST_SELECTOR    = BaseListSubdapter.NEXT_ROW_TYPE + 1;

    /** The next available row type value. */
    private static final int NEXT_ROW_TYPE             = BaseListSubdapter.NEXT_ROW_TYPE + 2;

    /** Used for rows who set their own clickable event handlers. */
    private static final int ROW_TYPE_SPECIAL          = BaseListSubdapter.ROW_TYPE_SPECIAL;

    /** Key used to store and retrieve the adapter position for which a context menu is being displayed. */
    public static final String CONTEXT_MENU_INFO_POSITION				= "com.defenestrate.chukkars.android.context_menu_info_position";

    /** Key used to indicate whether to modify this position before passing it on to be handled. */
    public static final String CONTEXT_MENU_INFO_POSITION_MODIFY		= "com.defenestrate.chukkars.android.context_menu_info_position_modify";

    private final List<FancyScrollListSubadapter> mSubadapters = new ArrayList<FancyScrollListSubadapter>();

	private FancyScrollListSubadapter mCurrentAdapter = null;

	private int DEFAULT_HEIGHT_FOR_TAB = 48;

	private int mCurrentSubadapterIndex = -1;

	private final List<DataSetObserver> mDataSetObservers = new ArrayList<DataSetObserver>();

	/**
	 * <code>true</code> to support a static header above the list.
	 * By default, this field is <code>true</code>.
	 * A subclass may change this value in the ctor.
	 */
	public boolean mIsHeaderEnabled = true;

    /**
     * <code>true</code> to support a tab-like header above the list.
     * By default, this field is <code>true</code> to support showing multiple lists.
     * A subclass may change this value in the ctor.
     */
	protected boolean mIsSelectorEnabled = true;

	protected final Context mContext;

	protected final LayoutInflater mInflater;

    private ViewGroup mListContainer = null;

	/** The scroll view that contains the selector buttons. */
	private View rowSelectorScroller = null;

    /** The floating scroll view that contains the selector buttons. */
	private View floatingSelectorScroller = null;

    /** The container of the floating scroll view. */
    private View mFloatingSelectorContainer = null;

	private ViewGroup rowSelector;
	private ViewGroup floatingSelector;

    /** Stores the index of first visible item in the last {@link #onScroll(AbsListView, int, int, int)} call. */
    private int mFirstVisibleItem;

    /** Stores the number of visible items in the last {@link #onScroll(AbsListView, int, int, int)} call. */
    private int mVisibleItemCount;

    /** Stores the total number of items in the last {@link #onScroll(AbsListView, int, int, int)} call. */
    private int mTotalItemCount;

    /** <code>true</code> if the list is flung by the user. */
    private boolean mIsFlinging;

    /* Indicates whether onCreate has been called yet */
    private boolean mIsSelectorViewsCreated = false;

    /* Tab index to set after tabs are created */
    private int mDelayedTabSelectionIndex = -1;

	public FancyScrollListAdapter(Context context, LayoutInflater inflater, FrameLayout listContainer) {
		mContext = context;
		mInflater = inflater;
        mListContainer = listContainer;

		ListView listView = (ListView)listContainer.findViewById(android.R.id.list);
		listView.setItemsCanFocus(true);
		listView.setFocusable(false);
	}

    /**
     * Similar to <code>Activity.onCreate()</code>
     * A <code>Activity</code> or <code>Fragment</code> should
     * call this method in its <code>onCreate()</code> method.
     */
    public void onCreate() {
        mIsSelectorViewsCreated = true;
        rowSelectorScroller = initSelectorView(null, null);
        mFloatingSelectorContainer = initFloatingSelectorView(null, mListContainer);
        mFloatingSelectorContainer.setVisibility(View.INVISIBLE);
        mListContainer.addView(mFloatingSelectorContainer);
        if(mIsSelectorEnabled) {
            final int numTabs = mSubadapters.size();
            for (int pos = 0; pos < numTabs; pos += 1) {
                addSelectorButton(mSubadapters.get(pos), pos);
            }
            reSizeSelectorButtons();
        }
        if (mDelayedTabSelectionIndex >= 0) {
            setCurrentSubadapter(mDelayedTabSelectionIndex);
        }
    }

    /**
     * Similar to <code>Activity.onResume()</code>
     * A <code>Activity</code> or <code>Fragment</code> should
     * call this method in its <code>onResume()</code> method.
     */
    public void onResume() {
        if (mCurrentAdapter != null && !mCurrentAdapter.isVisible()) {
            mCurrentAdapter.onResume();
        }
    }

    /**
     * Similar to <code>Activity.onPause()</code>
     * A <code>Activity</code> or <code>Fragment</code> should
     * call this method in its <code>onPause()</code> method.
     */
    public void onPause() {
        if (mCurrentAdapter != null && mCurrentAdapter.isVisible()) {
            mCurrentAdapter.onPause();
        }
    }

    /**
     * Similar to <code>Activity.onDestroy()</code>
     * A <code>Activity</code> or <code>Fragment</code> should
     * call this method in its <code>onDestroy()</code> method.
     */
    public void onDestroy() {
        for (FancyScrollListSubadapter subadapter : mSubadapters) {
            subadapter.onDestroy();
        }
    }

    /**
     * @return      The current list subadapter,
     *              <code>null</code> if none.
     */
    public FancyScrollListSubadapter getCurrentSubadapter() {
        return mCurrentAdapter;
    }

    /**
     * @param index the index of the requested subadapter
     * @return the subadater at the specified index
     */
    public FancyScrollListSubadapter getSubadapter(int index) {
    	if( (index < 0) || (index >= getSubadapterCount()) ) {
    		throw new IndexOutOfBoundsException("Subadapter count: " + getSubadapterCount() + "; requested index: " + index);
    	}

    	return mSubadapters.get(index);
    }

    public int getSubadapterCount() {
        int count = 0;
        if (mSubadapters != null) {
            count = mSubadapters.size();
        }
        return count;
    }

	public void addSubadapter(FancyScrollListSubadapter adapter) {
		adapter.onCreate();
		mSubadapters.add(adapter);
	}

	protected void addSelectorButton(FancyScrollListSubadapter adapter, int position) {
		String title = adapter.getListTitle();

		View selector = mInflater.inflate(getSelectorButtonLayout(), rowSelector, false);
		TextView selectorButton = (TextView) selector.findViewById(R.id.title);

		// Store the position of the button in the tag.
		final Integer tag = Integer.valueOf(position);
		if(selectorButton != null) {
			selectorButton.setText(title);
			selectorButton.setOnClickListener(this);
			selectorButton.setFocusable(true);
			selectorButton.setClickable(true);
			selectorButton.setBackgroundResource(R.drawable.tab_indicator_ab_holo);
			selectorButton.setTag(tag);
// GUI DOC : The max number of characters is 12 in One line.
			//PLM-1262
			//PLM-1262
			rowSelector.addView(selectorButton);
		}

		selector = mInflater.inflate(getSelectorButtonLayout(), floatingSelector, false);
		selectorButton = (TextView) selector.findViewById(R.id.title);
		if(selectorButton != null) {
			selectorButton.setText(title);
			selectorButton.setOnClickListener(this);
            selectorButton.setTag(tag);
			floatingSelector.addView(selectorButton);
		}
	}

	//PLM-1328
	/*
	 * Returns the tab View object
	 */
	public View getTabView(int index)
	{
		return floatingSelector.getChildAt(index);
	}

	protected void reSizeSelectorButtons(){
		if(mSubadapters.size() > 0){
			Rect bounds = new Rect();
			int pixels = getDefaultTabWidth();
			for(int i = 0; i < mSubadapters.size(); i++){
				if(rowSelector != null){
					setWidthForTabs(rowSelector, i, pixels, bounds);
				}
				if(floatingSelector != null){
					setWidthForTabs(floatingSelector, i, pixels, bounds);
				}
			}
		}
	}

    /**
     * This function's goal is to correctly size the tabs based on
     * a) The number of tabs
     * b) The content inside of the tab itself.
     *
     */
	private void setWidthForTabs(ViewGroup parentView, int tag, int pixels, Rect bounds){
		TextView view;
		Paint textPaint;
		int textWidth;
		int textHeight = 0;
		int paddingHorizontal = 0;
		int paddingVertical = 0;
		boolean isMultiWord = false;
		String[] multiWord;
		float defHeight = DEFAULT_HEIGHT_FOR_TAB;

		view = (TextView) parentView.findViewWithTag(tag);

		if(view != null){
			textPaint = view.getPaint();
			paddingHorizontal = view.getPaddingLeft();
			paddingVertical = view.getPaddingTop();

			String text = (String)view.getText();
			textPaint.getTextBounds(text,0, text.length(),bounds);
			textWidth = bounds.width();
			if(text.contains(" ") && textWidth > (pixels-(paddingHorizontal * 2))){
				isMultiWord = true;
				multiWord = text.split(" ");
				int tempWidth = 0;
				for(int i = 0; i < multiWord.length; i++){
					textPaint.getTextBounds(multiWord[i],0, multiWord[i].length(),bounds);
					textHeight += bounds.height();
					//if the split word is larger than the default width,
					//then set the new default to be the length of the word
					if(tempWidth < bounds.width()){
						textWidth = bounds.width();
						tempWidth = textWidth;
					}
				}
			}
			//if the text is longer than the predefined width, then reset that tab to be the size of the text.
			if(isMultiWord){
				int padding = (pixels-(paddingHorizontal * 2)) - textWidth;
//				view.setPadding(0, 0, 0, 0);
//				view.setWidth(textWidth + padding);
//				view.setHeight(textHeight + (paddingVertical*2));
				view.setPadding(4, 8, 4, 8);
			    view.setTextSize(14);
			}
			//if the text is longer than the default width, set the view to be the size of the text.
			else if(textWidth >= (pixels-(paddingHorizontal + paddingHorizontal))){
//				view.setPadding(0, paddingVertical, 0, paddingVertical);
//				view.setWidth(textWidth + (paddingHorizontal * 2) + (textWidth-pixels));
//				view.setHeight((int)(density * defHeight));
				view.setPadding(4, 8, 4, 8);
			    view.setTextSize(14);
			}
			//else, use the default
			else{
//				view.setWidth(pixels);
//				view.setHeight((int)(density * defHeight));
				view.setPadding(4, 14, 4, 14);
				view.setTextSize(15);
			}
		}
	}

    /**
     * @return      the width of the tab in pixels
     */
	private int getDefaultTabWidth(){
		float width;
		width = mContext.getResources().getDisplayMetrics().widthPixels;
		if(mSubadapters.size() >= 2 && mSubadapters.size() < 5){
			return (int) width/mSubadapters.size();
		}else if (mSubadapters.size() == 1){
			return (int)width;
		}else{
			return (int)(2*width/7); // 3.5 tabs per screen width
		}
	}

	/**
	 *
	 * @return		The layout id of the selector row.
	 */
	protected int getRowSelectorLayout() {
		return R.layout.list_selector;
	}

	/**
	 *
	 * @return		The layout id of the selector floater.
	 */
	protected int getFloatingSelectorLayout() {
		return R.layout.list_selector_floating;
	}

	/**
	 * Returns the layout to inflate for the selector button.  There MUST
	 * be a button with id __list_selector_unassigned.
	 *
	 * @return		The layout id of the selector button.
	 */
	protected int getSelectorButtonLayout() {
        return R.layout.list_selector_button_stretch;
	}

	/**
	 * Sets the current subadapter for list content.
	 * This method is used to implement a tab-like behavior
	 * in the fancy scroller.
	 *
	 * @param index            Index of the subadapter.
	 */
	public void setCurrentSubadapter(int index) {
        if (!mIsSelectorViewsCreated) {
            mDelayedTabSelectionIndex = index;
            return;
        }
        //PLM-1914
		//Added a null check to avoid NullPointerException
        if(mSubadapters.size() == 0 || index < 0 || index >= mSubadapters.size() || index == mCurrentSubadapterIndex || (mSubadapters.get(index) != null && !mSubadapters.get(index).isEnabled())) {
			return;
		} else {
		    if (mCurrentAdapter != null) {
		    	if(mIsSelectorEnabled) {
			        // Turn off the selection for both selectors,
			        // regardless of the selector's visibility.
			        // (The selection is set when an invisible selector becomes visible later).
		    		setFloatingSelectedIndex(mCurrentSubadapterIndex, false);
		    		setRowSelectedIndex(mCurrentSubadapterIndex, false);
		    	}

		        mCurrentAdapter.onPause();
		    }

		    if(index < 0 ) {
		        mCurrentAdapter = null;
    		} else {
    			if(mIsSelectorEnabled) {
	    		    // Set the selection of the selector that's visible.
    				if(mFloatingSelectorContainer.getVisibility() == View.VISIBLE) {
    					setFloatingSelectedIndex(index, true);
    				} else {
    					setRowSelectedIndex(index, true);
    				}
    			}

                mCurrentAdapter = mSubadapters.get(index);
    			mCurrentAdapter.onResume();
    		}
		    mCurrentSubadapterIndex = index;
		}
		notifyDataSetChanged();
	}

	public void setSubadatperEnabled(int index, boolean enable) {
		if(mSubadapters.size() == 0 || index >= mSubadapters.size()) {
			return;
		} else {
			FancyScrollListSubadapter adapter = mSubadapters.get(index);
			adapter.setEnabled(enable);
			if(enable) {
				setChildVisibility(floatingSelector, index, View.VISIBLE);
				setChildVisibility(rowSelector, index, View.VISIBLE);
			} else {
				setChildVisibility(floatingSelector, index, View.GONE);
				setChildVisibility(rowSelector, index, View.GONE);

				if(adapter == mCurrentAdapter) {
					if(index + 1 < mSubadapters.size()) {
						setCurrentSubadapter(index + 1);
					} else {
						setCurrentSubadapter(index - 1);
					}
				}
			}
		}
	}

	private void setChildVisibility(ViewGroup view, int index, int visibility) {
		int size = view.getChildCount();
		for(int i = 0; i < size; i++) {
			View v = view.getChildAt(i);
			int position = (Integer) v.getTag();
			if(position == index)
				v.setVisibility(visibility);
		}

	}

	/**
	 * @return             The index of the current active list subadapter.
	 */
	public int getCurrentSubadapterIndex() {
	    return mCurrentSubadapterIndex;
	}

	@Override
	public int getCount() {
		int count = 0;
		if(mIsHeaderEnabled) {
			count++;
		}
		if(mIsSelectorEnabled) {
			count++;
		}
		if(mCurrentAdapter != null)
			count += mCurrentAdapter.getCount();
		return count;
	}

	@Override
	public Object getItem(int position) {
		if(mCurrentAdapter != null)
			return mCurrentAdapter.getItem(getModifiedPosition(position));
		return null;
	}

	@Override
	public long getItemId(int position) {
		if(mCurrentAdapter != null)
			return mCurrentAdapter.getItemId(getModifiedPosition(position));
		return 0;
	}

	@Override
	public int getItemViewType(int position) {
		if(mIsHeaderEnabled) {
			if(position == 0)
				return ROW_TYPE_HEADER;
		}
		if(mIsSelectorEnabled) {
			int selectorid = mIsHeaderEnabled ? 1 : 0;
			if(position == selectorid)
				return ROW_TYPE_LIST_SELECTOR;
		}
		if(mCurrentAdapter != null) {
		    final int type = mCurrentAdapter.getItemViewType(getModifiedPosition(position));
		    if (type < NEXT_ROW_TYPE) {
		        return type;
		    } else {
    		    // Offset the custom view type value by the number of custom views
    		    // of the subadapters that come before it.
    		    // This allows each subadapter to keep its own range of view type constants
    		    // without conflicting with its siblings.
    	        int count = 0;
    	        for (FancyScrollListSubadapter subadapter : mSubadapters) {
    	            if (subadapter != mCurrentAdapter) {
        	            // -2 to offset the value of the default row type and divider
        	            // in BaseListAdapter.
        	            // The default and divider row types are
        	            // already included in the ROW_TYPES count.
        	            count += (subadapter.getViewTypeCount() - 2);
    	            } else {
    	                break;
    	            }
    	        }

    	        return count + type;
		    }

		}
		return ROW_TYPE_DEFAULT;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int rowType = getItemViewType(position);
		View ret = null;
		switch (rowType) {
		case ROW_TYPE_HEADER:
			ret = initHeaderView(convertView, parent);
			break;
		case ROW_TYPE_LIST_SELECTOR:
			ret = rowSelectorScroller;
			break;
		case ROW_TYPE_SPECIAL:
		case ROW_TYPE_DIVIDER:
			if (mCurrentAdapter != null) {
				ret = mCurrentAdapter.getView(getModifiedPosition(position), convertView, parent);
			}
			break;
		default:
			if (mCurrentAdapter != null) {
				ret = mCurrentAdapter.getView(getModifiedPosition(position), convertView, parent);
				ret.setFocusable(true);
				ret.setClickable(true);
				ret.setOnClickListener(rowClickListener);
			}
			break;
		}
		ret.setTag(R.id.position_tag, position);
		return ret;
	}

	OnClickListener rowClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v == null) return;

			int position = (Integer)v.getTag(R.id.position_tag);
			if(isEnabled(position))
				onItemClick(null, v, position, 0);
		}
	};

	/**
	 * Initializes the row selector view if it hasn't been initialized yet.
	 * @param convertView
	 * @param parent
	 * @return the row selector view
	 */
	protected View initSelectorView(View convertView, ViewGroup parent) {
		View view = mInflater.inflate(getRowSelectorLayout(), null);
		rowSelector = (ViewGroup) view.findViewById(R.id.list_selector);

		if(rowSelector instanceof LinearLayout) {
			LinearLayout linLayout = (LinearLayout)rowSelector;

			if(linLayout.getOrientation() == LinearLayout.HORIZONTAL) {
				linLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
			}
		}

		return view;
	}

	/**
	 * Initializes the floating row selector view if it hasn't been initialized yet.
	 * @param convertView
	 * @param parent
	 * @return the floating row selector view
	 */
	protected View initFloatingSelectorView(View convertView, ViewGroup parent) {
		View view = mInflater.inflate(getFloatingSelectorLayout(), parent, false);
		floatingSelectorScroller = view.findViewById(R.id.list_selector_scroller);
		floatingSelector = (ViewGroup) view.findViewById(R.id.list_selector);

		if(floatingSelector instanceof LinearLayout) {
			LinearLayout linLayout = (LinearLayout)floatingSelector;

			if(linLayout.getOrientation() == LinearLayout.HORIZONTAL) {
				linLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
			}
		}

		if(floatingSelectorScroller != null) {
			//remove right padding since the floating selector layout adding the shadow adds its own padding
			floatingSelectorScroller.setPadding(
					floatingSelectorScroller.getPaddingLeft(),
					floatingSelectorScroller.getPaddingTop(),
					0,
					floatingSelectorScroller.getPaddingBottom());
		}

		return view;
	}

	/**
	 * Initializes the header row view.
	 * @param convertView
	 * @param parent
	 * @return
	 */
	protected View initHeaderView(View convertView, ViewGroup parent) {
	    // If isHeaderEnabled is true,
	    // a subclass must override this method to return a view.
	    return null;
	}

	@Override
	public int getViewTypeCount() {
	    // This method is called only once, when an AbsListView
	    // initializes the list adapter.
	    // So we query all subadapters for their view type count
	    // so that the AbsListView will allocate an array that's big enough.
	    int count = 0;
	    for (FancyScrollListSubadapter subadapter : mSubadapters) {
            // -2 to offset the value of the default row type and divider
	        // in BaseListAdapter.
	        // The default and divider row types are
	        // already included in the ROW_TYPES count.
	        count += (subadapter.getViewTypeCount() - 2);
	    }
		return NEXT_ROW_TYPE + count;
	}

	@Override
	public boolean hasStableIds() {
		if(mCurrentAdapter != null)
			return mCurrentAdapter.hasStableIds();
		return false;
	}

	@Override
	public boolean isEmpty() {
		if(mIsHeaderEnabled || mIsSelectorEnabled)
			return false;
		else if(mCurrentAdapter != null)
			return mCurrentAdapter.isEmpty();

		return false;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		//check contain, to avoid register several time
		if (!mDataSetObservers.contains(observer)) {
			mDataSetObservers.add(observer);
		} else {
			Log.e(LOG_TAG, "registerDataSetObserver : observer is already in array. [" + observer + "]");
		}
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		if (mDataSetObservers.contains(observer)) {
			mDataSetObservers.remove(observer);
		} else {
			Log.e(LOG_TAG, "unregisterDataSetObserver : does not contain that observer [" + observer + "]");
		}
	}

	/**
	 * Notifies the attached observers that the underlying data has been changed and any View reflecting the data set should refresh itself.
	 */
	public void notifyDataSetChanged() {
		for(DataSetObserver d : mDataSetObservers) {
			d.onChanged();
		}
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		int type = getItemViewType(position);
		switch(type) {
		case ROW_TYPE_HEADER:
			return false;
		case ROW_TYPE_LIST_SELECTOR:
			return false;
		default:
            return (mCurrentAdapter != null) ?
                mCurrentAdapter.isEnabled(getModifiedPosition(position)) :
                super.isEnabled(position);
		}
	}

	/**
	 * Indicates whether or not rows in this list can be dragged and dropped.
	 * @return <code>true</code> if rows in this list can be dragged and
	 * dropped; <code>false</code> otherwise
	 */
	public boolean isListRowDragAndDropEnabled() {
		return false;
	}

	/**
	 * Gets the row position accounting for the header and row selector.
	 *
	 * @param position
	 * @return
	 */
	private int getModifiedPosition(int position) {
		if(mIsHeaderEnabled)
			position--;
		if(mIsSelectorEnabled)
			position--;
		return position;
	}

	@Override
	public void onClick(View v) {
		if(mIsSelectorEnabled) {
		    scrollSelector(v);

			int position = (Integer) v.getTag();
            setCurrentSubadapter(position);
		}
	}

    /**
     * Scrolls the selector container to
     * horizontally center a selector button.
     *
     * @param v                The selector button.
     */
    public void scrollSelector(View v) {
    	if(mIsSelectorEnabled) {
	        final View scroller = (mFloatingSelectorContainer.getVisibility() == View.VISIBLE) ? floatingSelectorScroller : rowSelectorScroller;
	        final int w = scroller.getWidth();
	        final int scrollX = scroller.getScrollX();
	        if (v.getLeft() - scrollX < 0 || v.getRight() - scrollX > w) {

	            // Horizontally center the tab header.
		        final int containerCenter = w / 2;
		        final int buttonCenter = v.getLeft() + v.getWidth() / 2;

		        final int x = buttonCenter - containerCenter;
		        scroller.scrollTo(x, scroller.getScrollY());
	        }
    	}
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		int modPos = getModifiedPosition(position);
		if(modPos >= 0 && mCurrentAdapter != null) {
			mCurrentAdapter.onItemClick(parent, view, modPos, id);
		}
	}

	/** {@inheritDoc} */
	@Override public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if(mCurrentAdapter != null) {
			if(menuInfo != null) {
				final AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
				int position = info.position;

				int modPos = getModifiedPosition(position);
				mCurrentAdapter.onCreateContextMenu(menu, v, menuInfo, modPos);
			}
        } else {
        	Log.e(
        		LOG_TAG,
        		"Attemtping to create a context menu but no current subadapter to handle the logic!",
        		new Throwable().fillInStackTrace() );
        }
	}

	public boolean onContextItemSelected(MenuItem item) {
		if(mCurrentAdapter != null) {
			ContextMenuInfo menuInfo = item.getMenuInfo();
			int position = -1;
			int contextPos = -1;
			boolean modifyPosition = true;

			if(menuInfo != null) {
				final AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
				position = info.position;
			} else {
				//This is a hack to get around this bug: http://code.google.com/p/android/issues/detail?id=7139
                //MenuItem.getMenuInfo() returns null for sub-menu items
				Intent i = item.getIntent();
				if(i != null) {
					position = i.getIntExtra(CONTEXT_MENU_INFO_POSITION, -1);
					modifyPosition = i.getBooleanExtra(CONTEXT_MENU_INFO_POSITION_MODIFY, true);
				}
			}

			contextPos = modifyPosition ? getModifiedPosition(position) : position;

			if(contextPos > -1) {
				return mCurrentAdapter.onContextItemSelected(item, contextPos, modifyPosition);
			}
        } else {
        	Log.e(
        		LOG_TAG,
        		"Attemtping to select a context menu item but no current subadapter to handle the logic!",
        		new Throwable().fillInStackTrace() );
        }

        return false;
	}

    /**
     * @return              <code>true</code> if the list was flung by the user.
     */
    public boolean isFlinging() {
        return mIsFlinging;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // WARNING: This API is wildly inconsistent on different phones. For example, when the user
        // is using the fast-scroll thumb to navigate the list the Nexus 1 reports the scroll state
        // as SCROLL_STATE_TOUCH_SCROLL but the Motorola Droid reports it as SCROLL_STATE_FLING.
        // Scroll state transitions also seem to occur at different points on different devices. Try
        // the HTC Evo. It seems as though this API is best avoided.

        // On some devices, onScrollStateChanged() is called even though
        // the list is at the first or last row,
        // and no scrolling is being done.
        // So we need to disambiguate this.
        final boolean isInMiddle = (mFirstVisibleItem > 0) && (mFirstVisibleItem < mTotalItemCount - 1 - mVisibleItemCount);
        final boolean isFlinging = isInMiddle && (OnScrollListener.SCROLL_STATE_FLING == scrollState);
        if (mIsFlinging != isFlinging) {
            mIsFlinging = isFlinging;
            if (!mIsFlinging) {
                // Update the primary icons (which were not updated while flinging).
                int rowType;
                final int start = view.getFirstVisiblePosition();
                for (int i = start, j = view.getLastVisiblePosition(); i <= j; i++) {
                    rowType = getItemViewType(i);
                    switch (rowType) {
                    case ROW_TYPE_HEADER:
                    case ROW_TYPE_DIVIDER:
                    case ROW_TYPE_LIST_SELECTOR:
                        break;
                    default:
                        if (mCurrentAdapter != null) {
                            final View v = view.getChildAt(i - start);
                            int normalIndex = mCurrentAdapter.normalizeRawRowIndex(i);

                        	mCurrentAdapter.updateViewOnScroll(normalIndex, v);
                        }
                        break;
                    }
                }
            }
        }
    }

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if(mIsSelectorEnabled) {
			int top = rowSelectorScroller.getTop();
			if(top < 0 || firstVisibleItem > 1) { //if top is above the list or if first item visible is below the selector
			    if (mFloatingSelectorContainer.getVisibility() == View.INVISIBLE) {
			        // Set the scroll offset only if re-showing the floating selector.
			    	if(rowSelectorScroller != null && floatingSelectorScroller != null)
			    		floatingSelectorScroller.scrollTo(rowSelectorScroller.getScrollX(), floatingSelectorScroller.getScrollY());
				    setFloatingSelectedIndex(mCurrentSubadapterIndex, true);
				    mFloatingSelectorContainer.setVisibility(View.VISIBLE);

				    if(view != null)
				    {
					    // Enable fast scroll when scrolling past the headers.
					    view.setFastScrollEnabled(true);
				    }
			    }
			}
			else {
                if (mFloatingSelectorContainer.getVisibility() == View.VISIBLE) {
                    // Set the scroll offset only if re-showing the selector.
                	if(rowSelectorScroller != null && floatingSelectorScroller != null)
                		rowSelectorScroller.scrollTo(floatingSelectorScroller.getScrollX(), rowSelectorScroller.getScrollY());
                    setRowSelectedIndex(mCurrentSubadapterIndex, true);
                    mFloatingSelectorContainer.setVisibility(View.INVISIBLE);

                    if(view != null)
                    {
	                    // Disable fast scroll when scrolling back to the origin.
	                    view.setFastScrollEnabled(false);
                    }
                }
			}
		}

        mFirstVisibleItem = firstVisibleItem;
        mVisibleItemCount = visibleItemCount;
        mTotalItemCount = totalItemCount;

	}

	/**
	 * Sets the index to be selected for the row selector.
	 * @param index
	 * @param selected
	 */
	protected void setRowSelectedIndex(int index, boolean selected) {
		rowSelector.getChildAt(index).setSelected(selected);
	}

	/**
	 * Sets the index to be selected for the floating selector.
	 * @param index
	 * @param selected
	 */
	protected void setFloatingSelectedIndex(int index, boolean selected) {
		floatingSelector.getChildAt(index).setSelected(selected);
	}

	/**
	 * Subadapter added to {@link FancyScrollListAdapter} for a tab-like UI.
	 * This class is used to emulate the lifecycle methods of a <code>Fragment</code>
	 * when switching between subadapters.
	 */
	public static abstract class FancyScrollListSubadapter extends BaseListSubdapter {

        /** The next available row type value. */
        protected static final int NEXT_ROW_TYPE = FancyScrollListAdapter.NEXT_ROW_TYPE;

        /** Rows that set their own onClick event hander. */
        protected static final int ROW_TYPE_SPECIAL = FancyScrollListAdapter.ROW_TYPE_SPECIAL;

        /** <code>true</code> if the list is currently in the foreground. */
        private boolean mIsVisible;

        private boolean mEnabled = true;

        /** Callback methods for the subadapter. */
        protected FancyScrollListSubadapterCallback mCallback;

        protected OnDataLoadingListener mOnDataLoadingListener;

	    /**
	     * @param context				  The context for this adapter.
	     * @param callback                The callback for this adapter.
	     */
	    public FancyScrollListSubadapter(Context context, FancyScrollListSubadapterCallback callback) {
	        super(context);
	        mCallback = callback;
	    }

	    /**
	     * set listener for data loading
	     * @param listener
	     */
	    public void setOnDataLoadingListener(OnDataLoadingListener listener) {
	        mOnDataLoadingListener = listener;
	    }

	    /**
	     * Indicates whether or not a divider is present at the raw row index.
	     * This method normalizes out:
	     * - the "fake" rows in the parent super adapter (i.e., the fancy scrolling header and selector rows)
	     * - the "fake" rows in the subadapter (e.g., a "Shuffle" row)
	     * @param index raw row index
	     * @return <code>true</code> if a divider is present at the specified
	     * raw row index; <code>false</code> otherwise
	     */
	    public boolean isRawRowIndexDivider(int index) {
	    	int position = preDividerNormalizeRawRowIndex(index);

	    	if (position >= 0) {
	    		return isDivider(position);
	    	} else {
	    		return false;
	    	}
	    }

	    /**
	     * Normalize a raw row index to account for:<br>
	     * - the "fake" rows in the parent super adapter (i.e., the fancy scrolling header and selector rows)<br>
	     * - the "fake" rows in the subadapter (e.g., a "Shuffle" row)<br>
	     * - the presence of any dividers
	     * @param index raw row index
	     * @return normalized row index to pass to this subadapter's methods
	     */
	    public int normalizeRawRowIndex(int index) {
	    	int position = preDividerNormalizeRawRowIndex(index);

	        if (position >= 0) {
	            // Calculate offsets for dividers.
	            position = getTranslatedPosition(position);
	        }

	        return position;
	    }

	    private int preDividerNormalizeRawRowIndex(int index) {
	    	//first normalize for the "fake" rows in the parent super adapter
	    	//(i.e., the fancy scrolling header and selector rows)
	    	int position = mCallback.getSuperAdapter().getModifiedPosition(index);

	    	//now normalize for the "fake" rows in the subadapter
	    	if (hasHeader()) {	//e.g., a "Shuffle" row
	            // offset the position to account for the header
	            position--;
	        }

	    	return position;
	    }

	    /**
	     * Similar to <code>Activity.onCreate()</code>
	     * when creating a subadapter.
	     */
	    public void onCreate() { }

        /**
         * Similar to <code>Activity.onResume()</code>
         * when switching to a subadapter.
         */
	    public void onResume() {
            mIsVisible = true;
	    }

        /**
         * Similar to <code>Activity.onPause()</code>
         * when switching away from a subadapter.
         */
	    public void onPause() {
            mIsVisible = false;
	    }

        /**
         * Similar to <code>Activity.onDestroy()</code>
         * when finished with a subadapter.
         */
	    public void onDestroy() {
	    	release();
	    	mCallback=null;
	    }

        /**
         * @return      <code>true</code> if this adapter is visible to the user.
         */
        protected boolean isVisible() {
            return mIsVisible;
        }

        public boolean isEnabled() {
        	return mEnabled;
        }

        public void setEnabled(boolean enable) {
        	mEnabled = enable;
        }

        protected View getListItemView(int position, View convertView, ViewGroup parent) {
        	View v = super.getListItemView(position, convertView, parent);
        	mCallback.getActivity().registerForContextMenu(v);
            return v;
        }

        /** {@inheritDoc} */
        @Override public void notifyDataSetChanged() {
        	//the subadapter is refreshed...
        	super.notifyDataSetChanged();

        	//...but the superadapter needs to be notified, too, before the UI will refresh
            FancyScrollListAdapter superAdapter = (mCallback == null) ? null : mCallback.getSuperAdapter();
        	if(superAdapter != null) {
        		superAdapter.notifyDataSetChanged();
        	}
        }

        /**
	     * Callback for the subadapter.
	     */
	    public interface FancyScrollListSubadapterCallback {

	        /**
	         * @return        The list view for this adapter.
	         */
	        public AbsListView getListView();

	        /**
	         * @return        The activity that contains this adapter.
	         */
	        public Activity getActivity();

	        /**
	         * @return the unique Id of this adapter's data. e.g., if
		     * this adapter's data is the contents of a playlist, then the Id returned
		     * by this method should be the unique playlist id. Two different
		     * instantiations with the SAME data MUST return EQUAL
		     * Ids from this method.
	         */
	        public String getDataId();

	        /**
	         * @return The enclosing, parent list adapter for this subadapter.
	         */
	        public FancyScrollListAdapter getSuperAdapter();
	    }

	    /**
	     * Interface definition for a callback to be invoked when data is started or finished to get data
	     */
	    public interface OnDataLoadingListener {
	    	/**
	    	 * this callback will be invoked when start to get load data.
	    	 */
	    	public void onStartLoading();

	    	/**
	    	 * this callback will be invoked when loading data is finshed.
	    	 */
	    	public void onLoadingFinshed();

	    	/**
	    	 * this callback may be invoked when there is an error loading data.
	    	 * @param type        the type of error that occurred. One of
	    	 *                    <ul>
	    	 *                    <li>{@link AriaConstants#ERROR_NETWORK}</li>
	    	 *                    <li>{@link AriaConstants#ERROR_GENERAL}</li>
	    	 *                    </ul>
	    	 * @param e           The exception that occurred while trying to get data, if any
	    	 */
	    	public void onDataLoadingError(int type, Exception e);
	    }
	}
}