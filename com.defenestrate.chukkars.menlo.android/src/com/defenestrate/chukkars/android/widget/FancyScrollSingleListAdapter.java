package com.defenestrate.chukkars.android.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;


/**
 * A list adapter that allows a single list to be scrolled, while keeping a
 * floating view stationary at top of the screen.  More specifically, the floating
 * view should be identical to some arbitrary bottom portion of the header view.
 * @see {@link #initHeaderView(View, ViewGroup)}
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
 */
abstract public class FancyScrollSingleListAdapter extends FancyScrollListAdapter {

	/////////////////////////// MEMBER VARIABLES ///////////////////////////////
	protected FrameLayout mListContainer;


	///////////////////////////// CONSTRUCTORS /////////////////////////////////
	public FancyScrollSingleListAdapter(Context context, LayoutInflater inflater, FrameLayout listContainer, FancyScrollListSubadapter subadapter) {
		super(context, inflater, listContainer);

		//true to support a tab-like header above the list.
        //In superclass, this field is <code>true</code> to support showing multiple lists.
		mIsSelectorEnabled = false;
		mListContainer = listContainer;

        super.addSubadapter(subadapter);
        super.setCurrentSubadapter(0);
	}


	//////////////////////////////// METHODS ///////////////////////////////////
	/**
	 * {@inheritDoc}
	 * This method is not applicable to this class.
	 * Pass the single subadapter in the ctor.
	 */
	@Override public void addSubadapter(FancyScrollListSubadapter adapter) {
	    throw new IllegalStateException("This class only supports 1 subadapter! (Pass the subadapter in the ctor).");
	}

	/** {@inheritDoc} */
	@Override protected int getRowSelectorLayout() {
		throw new IllegalStateException("Method not implemented and should never be invoked!");
	}

	/** {@inheritDoc} */
	@Override protected int getSelectorButtonLayout() {
		throw new IllegalStateException("Method not implemented and should never be invoked!");
	}

	/** {@inheritDoc} */
	@Override protected View initSelectorView(View convertView, ViewGroup parent) {
		//no selector because there's only a single list
		return null;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		//determine to display or hide the floating view
		displayFloatingView(mListContainer, firstVisibleItem);

		//check to see if display fast scroll
		//can't modify displayFloatingView to have this code because doesn't take view as parameter and is used by other classes
		View staticHeader = getAttachedHeaderView();

		if(staticHeader != null) {
			View floatingView = initFloatingSelectorView(null, mListContainer);
			int top = staticHeader.getBottom() - floatingView.getHeight();

			if(top < 0 || firstVisibleItem > 0) { //if top is above the list or if first item visible is below the header
				//enable when past top
			    view.setFastScrollEnabled(true);
			}
			else {
				//disable when at top
			    view.setFastScrollEnabled(false);
			}
		}
	}

	/**
	 * Determine to display or hide the floating view
	 * @param view FrameLayout containing the fancy-scrolling list over which the floating view floats
	 * @param firstVisibleItem the index of the first visible cell in the list
	 */
	protected void displayFloatingView(FrameLayout view, int firstVisibleItem) {
		View staticHeader = getAttachedHeaderView();

		if(staticHeader != null) {
			View floatingView = initFloatingSelectorView(null, view);
			int top = staticHeader.getBottom() - floatingView.getHeight();

			if(top < 0 || firstVisibleItem > 0) { //if top is above the list or if first item visible is below the header
			    if (floatingView.getVisibility() == View.INVISIBLE) {
				    floatingView.setVisibility(View.VISIBLE);
			    }

			    //Setting the header to be invisible so tab and arrow key
			    //behavior is well defined when using a Bluetooth keyboard
			    staticHeader.setVisibility(View.INVISIBLE);
			}
			else {
	            if (floatingView.getVisibility() == View.VISIBLE) {
	                floatingView.setVisibility(View.INVISIBLE);
	            }

			    staticHeader.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * @return the instance of the header view that has already been attached
	 * to this adapter's associated list.
	 */
	abstract protected View getAttachedHeaderView();

	/** {@inheritDoc} */
	@Override abstract protected int getFloatingSelectorLayout();

	/** {@inheritDoc} */
	@Override abstract protected View initFloatingSelectorView(View convertView, ViewGroup parent);
}