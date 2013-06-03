package com.defenestrate.chukkars.android.widget;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.drawable.BitmapDrawable;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageSwitcher;
import android.widget.TextView;

import com.defenestrate.chukkars.android.R;
import com.defenestrate.chukkars.android.util.BaseRow;
import com.defenestrate.chukkars.android.util.BaseRow.BaseViewHolder;
import com.defenestrate.chukkars.android.util.Row;
import com.defenestrate.chukkars.android.util.Row.ViewHolder;


public abstract class BaseListSubdapter extends BaseAdapter implements OnItemClickListener {

	/////////////////////////////// CONSTANTS //////////////////////////////////

	/** List view type for a default row that assigns its own onClick event handler. */
    protected static final int ROW_TYPE_SPECIAL = -1;

	/** List view type for a row. */
    protected static final int ROW_TYPE_DEFAULT = 0;

    /** List view type for a divider. */
    protected static final int ROW_TYPE_DIVIDER = 1;

    /** List view type for a header (first row that is not considered to be part of the list content). */
    protected static final int ROW_TYPE_HEADER = 2;

    /** The next available row type value. */
    protected static final int NEXT_ROW_TYPE = 3;

    /** Padding between TextView and shuffle icon. */ //MHIV-545
    protected static final int PADDING_BETWEEN_TEXT_IMAGE = 40;


	/////////////////////////// MEMBER VARIABLES ///////////////////////////////
    protected Context mContext;

    /** A reference to the layout inflater for inflating the constituent views. */
    protected LayoutInflater mInflater;

    /** Utility to initialize a row. */
    protected final BaseRow mBaseRow;

    /**
     * Indices of the row dividers.
     * TreeSet to correctly decrement in-place the list item indices in
	 * {@link #getView(int, View, ViewGroup)} and {@link #getItem(int)}
	 * @see {@link #getCount()}
	 * @see {@link #getView(int, View, ViewGroup)}
	 * @see {@link #getItem(int)}
	 */
	private Set<Integer> mDividers;

	private final ArrayList<DataSetObserver> dataSetObservers = new ArrayList<DataSetObserver>();

    /** The list header label. */
    private String mHeaderText;

    /** The list header icon. */
    private BitmapDrawable mHeaderIcon;


	//////////////////////////////// METHODS ///////////////////////////////////
	public BaseListSubdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
        mBaseRow = new BaseRow(context);
	}

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
	public boolean isEnabled(int position) {
		//When the FancyScrollListAdapter calls this method,
		//FancyScrollListAdapter.getModifiedPosition(int position) SHOULD have
		//already been invoked. As such, the fancy scrolling header and selector
		//rows have already been normalized out of the index.
		int modPosition = position;

		//now normalize for the "fake" rows in the subadapter
    	if (hasHeader()) {	//e.g., a "Shuffle" row
            // offset the position to account for the header
            modPosition--;
        }

    	boolean isDivider = false;

    	if (modPosition >= 0) {
    		isDivider = isDivider(modPosition);

    		// Calculate offsets for dividers.
    		if(!isDivider) {
    			modPosition = getTranslatedPosition(modPosition);
    		}
        }

		return !isDivider && isListItemEnabled(modPosition);
	}

    /**
     * Return true if the row is enabled.
     * @param position
     * @return			<b>true</b> if row is enabled.
     */
    public boolean isListItemEnabled(int row) {
    	return true;
    }

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		dataSetObservers.add(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		dataSetObservers.remove(observer);
	}

	/**
	 * Notifies the attached observers that the underlying data has been changed and any View reflecting the data set should refresh itself.
	 */
	public void notifyDataSetChanged() {
		for(DataSetObserver d : dataSetObservers) {
			d.onChanged();
		}
	}

	/**
	 * This method returns the number of list items *plus* the number of dividers and header.
	 *
	 * To get only the number of list items, call getListItemCount().
	 */
    @Override
    public final int getCount() {
        int count = getListItemCount();
        if (hasHeader()) {
            count++;
        }
        if (count > 0) {
            count += getDividerCount();
        }

        return count;
    }

    /**
     * Retrieves the actual number of available data items, which may be
     * greater than getCount() if multiple items are shown in a single row.
     *
     * @return Number of actual data items
     */
    public int getDataCount() {
        return getCount();
    }

    /**
     * How many data (i.e., non-divider) items are in this list.
     * @return Count of items.
     */
    protected abstract int getListItemCount();

    @Override
    public final Object getItem(int position) {
        if (hasHeader()) {
            // offset the position to account for the header
            position--;
        }

        if (position >= 0) {
            if (!isDivider(position)) {
                if (mDividers != null) {
                    for (Integer currDividerIndex : mDividers) {
                        if (position > currDividerIndex) {
                            // Offset by the divider
                            position--;
                        }
                    }
                }

                return getListItem(position);
            } else {
                return getDividerText(position);
            }
        } else {
            return null;
        }
    }

    /**
     * Get the data item associated with the specified position in the concrete subclass data set.
     *
     * @param position Position of the item whose data we want. This position
     * argument has already been translated to normalize for the presence of
     * non-selectable and non-clickable dividers, if any. So implementing subclasses
     * can treat this argument as a given with respect to their own data structures,
     * and not have to do any index translation or manipulation.
     * @return The data at the specified position.
     */
    protected abstract Object getListItem(int position);

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public final int getItemViewType(int position) {
        if (position == 0 && hasHeader()) {
            return ROW_TYPE_HEADER;
        }

        if (hasHeader()) {
            // offset the position to account for the header
            position--;
        }

        if (isDivider(position)) {
            return getDividerViewType(position);
        } else {
            return getListItemViewType(getTranslatedPosition(position));
        }
    }

    /**
     * Gets the view type for a divider row.
     *
     * @param position          The raw row index.
     * @return                  The view type for <code>position</code>.
     */
    public int getDividerViewType(int position) {
        return ROW_TYPE_DIVIDER;
    }

    /**
     * Gets the view type for a list item (non-divider) row.
     *
     * @param position          The row index (ignores divider rows).
     * @return                  The view type for <code>position</code>.
     */
    public int getListItemViewType(int position) {
    	return ROW_TYPE_DEFAULT;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        if (hasHeader()) {
            // offset the position to account for the header
            position--;
        }

        boolean isDivider = isDivider(position);
        if (position >= 0 && !isDivider) {
            // Calculate offsets for dividers.
            position = getTranslatedPosition(position);
        }

        final View v;
        if (position!=-1 && convertView != null) {
            v = convertView;
        } else if (-1 == position) {
            // Header row.
            v = getListHeaderView(parent);
        } else if (isDivider) {
            v = getDividerView(position, convertView, parent);
        } else {
            v = getListItemView(position, convertView, parent);
        }

        if (position >= 0) {
            // Not a header row.
            if (isDivider) {
                initDividerRow(v, position);
            } else {
                initRow(v, position);
            }
        }

        return v;
    }

    /** @return <code>true</code> iff this list has a header. */
    protected final boolean hasHeader() {
        return (mHeaderText != null);
    }

    /**
     * @return the text of the header. May be <code>null</code> if no header
     * is present
     */
    protected final String getHeaderText() {
    	return mHeaderText;
    }

    /**
     * Initializes a divider row.
     * This method may be overridden by a subclass
     * that doesn't use a {@link BaseViewHolder} for a row.
     *
     * @param v             The view of the row.
     * @param position      The row position.
     */
    protected void initDividerRow(View v, int position) {
    	if (v == null) return;
        final BaseViewHolder holder = (BaseViewHolder) v.getTag();
        initDividerRow(holder, position);
    }

    /**
     * Initializes a content row.
     * This method may be overridden by a subclass
     * that doesn't use a {@link BaseViewHolder} for a row.
     *
     * @param v             The view of the row.
     * @param position      The row position. This position
     * argument has already been translated to normalize for the presence of a
     * header and non-selectable/non-clickable dividers, if any. So implementing subclasses
     * can treat this argument as a given with respect to their own data structures,
     * and not have to do any index translation or manipulation.
     */
    public void initRow(View v, int position) {
    	if (v == null) {
    		return;
    	}

    	final BaseViewHolder holder = (BaseViewHolder) v.getTag();
    	initRow(holder, position);
    }

    /**
     *
     * @return The layout to use for divider rows.
     */
    protected int getDividerLayout() {
    	return R.layout.list_divider;
    }

    /**
     *
     * @param position This position argument MUST already have been normalized
     * for the presence, if any, of a header (e.g., "Shuffle") row
     * @return	The position translated to remove divider rows.
     */
    protected int getTranslatedPosition(int position) {
        int translatedPos = position;
        boolean isDivider = isDivider(position);
        if (!isDivider) {
            if (mDividers != null) {
                for(Integer currDividerIndex : mDividers) {
                    if(position > currDividerIndex) {
                        // Offset by the divider
                        translatedPos--;
                    }
                }
            }
        }
        return translatedPos;
    }

    /**
     * Updates the contents of a single row in the list when the list is scrolled.
     * e.g., update the row icon(s), if any
     *
     * @param position          The index of the row. This index argument has
     * already been translated to normalize for the presence of headers and
     * non-selectable and non-clickable rows, if any. So implementing subclasses
     * can treat this argument as a given with respect to their own data structures,
     * and not have to do any index translation or manipulation.
     * @param view              The view that corresponds to <code>position</code>.
     */
    protected void updateViewOnScroll(int position, View view) {
        // no-op by default.
    }

    /**
     * Updates the contents of a single row in the list when the list is scrolled.
     * e.g., update the row icon(s), if any. The single row contains
     * multiple selectable items.
     *
     * @param position          The index of the row. This index argument has
     * already been translated to normalize for the presence of headers and
     * non-selectable and non-clickable rows, if any. So implementing subclasses
     * can treat this argument as a given with respect to their own data structures,
     * and not have to do any index translation or manipulation.
     * @param view              The view that corresponds to <code>position</code>.
     */
    protected void updateMultiItemViewOnScroll(int position, View view) {
        // no-op by default.
    }

    /**
     * Gets a view for the list header (first row, not part of the list content).
     *
     * @param parent            The parent container.
     * @return                  The view for the header row.
     */
	protected View getListHeaderView(ViewGroup parent) {

		// Safety against Stress test
		if(mInflater==null)
			return null;
		final View songView = mInflater.inflate(R.layout.list_item_header, parent, false);
		songView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onHeaderClick(v);
			}
		});
		//PLM-1636
		songView.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean focus) {
				// TODO Auto-generated method stub
				v.setSelected(false);
			}
		});
        final TextView tv = (TextView) songView.findViewById(R.id.label);
    	tv.setCompoundDrawablesWithIntrinsicBounds(mHeaderIcon, null, null, null);
    	tv.setCompoundDrawablePadding(PADDING_BETWEEN_TEXT_IMAGE); //Adding Padding between the TextView and the image : MHIV-545
    	tv.setText(mHeaderText);
    	return songView;
    }

    /**
     * Get a View that displays the divider at the specified position in the data set.
     * This method is to support subclass custom row layout, if any.
     * If a subclass overrides this method, it should set the view's tag to a
     * <code>ViewHolder</code> (<code>View.setTag()</code>).
     * If the
     * subclass does not override this method, the default list row view and layout will
     * be used.
     * @param position The position of the item within the adapter's data set of
     * the item whose view we want. This position argument has NOT been
     * translated to normalize for the presence of non-selectable and non-clickable
     * dividers, if any. So implementing subclasses can treat this argument as a
     * given with respect to their own data structures, and not have to do any
     * index translation or manipulation.
     * @param convertView The old view to reuse, if possible. Note: You should
     * check that this view is non-null and of an appropriate type before using.
     * If it is not possible to convert this view to display the correct data,
     * this method can create a new view.
     * @param parent The parent that this view will eventually be attached to
     * @return A row layout
     */
    protected View getDividerView(int position, View convertView, ViewGroup parent) {
    	return inflateView(getDividerLayout(), parent, true);
    }

    /**
     * Get a View that displays the data at the specified position in the data set.
     * This method is to support subclass custom row layout, if any.
     * If a subclass overrides this method, it should set the view's tag to a
     * <code>ViewHolder</code> (<code>View.setTag()</code>).
     * If the
     * subclass does not override this method, the default list row view and layout will
     * be used.
     * @param position The position of the item within the adapter's data set of
     * the item whose view we want. This position argument has already been
     * translated to normalize for the presence of non-selectable and non-clickable
     * dividers, if any. So implementing subclasses can treat this argument as a
     * given with respect to their own data structures, and not have to do any
     * index translation or manipulation.
     * @param convertView The old view to reuse, if possible. Note: You should
     * check that this view is non-null and of an appropriate type before using.
     * If it is not possible to convert this view to display the correct data,
     * this method can create a new view.
     * @param parent The parent that this view will eventually be attached to
     * @return A row layout
     */
    protected View getListItemView(int position, View convertView, ViewGroup parent) {
        return inflateView(R.layout.list_item, parent, false);
    }

    /**
     * Convenience method to inflate a row layout.
     *
     * @param layoutId      Layout ID of the row layout.
     * @param parent        The parent that the row will be attached to.
     * @return              The inflated row object.
     */
    protected View inflateView(int layoutId, ViewGroup parent, boolean isDivider) {
        if(mInflater==null)
        	return null;
    	final View v = mInflater.inflate(layoutId, parent, false);
        final BaseViewHolder holder;

        if(!isDivider) {
        	holder = new Row.ViewHolder(
        		mContext,
        		(ImageSwitcher) v.findViewById(R.id.icon),
	            (TextView) v.findViewById(R.id.label1),
	            (TextView) v.findViewById(R.id.label2),
	            (TextView) v.findViewById(R.id.label3) );
        } else {
        	holder = new Row.ViewHolder(
        		mContext,
        		(ImageSwitcher) v.findViewById(R.id.icon),
	            (TextView) v.findViewById(R.id.label1),
	            (TextView) v.findViewById(R.id.label2),
	            (TextView) v.findViewById(R.id.below_divider_label) );
        }

    	v.setTag(holder);

        return v;
    }

    /**
     * Initializes a non-divider row.
     *
     * @param holder                The view holder with the row's child views.
     * @param position              The position of the row. This position
     * argument has already been translated to normalize for the presence of
     * non-selectable and non-clickable dividers, if any. So implementing subclasses
     * can treat this argument as a given with respect to their own data structures,
     * and not have to do any index translation or manipulation.
     */
    public abstract void initRow(BaseViewHolder holder, int position);

    /**
     * Initializes a divider row.
     *
     * @param holder                The view holder with the row's child views.
     * @param position              The position of the row.
     */
    private void initDividerRow(BaseViewHolder holder, int position) {
        final String label = getDividerText(position);
        holder.label1.setText(label);

        String subLabel = getDividerSubText(position);

        if(subLabel != null) {
        	( (ViewHolder)holder ).label3.setText(subLabel);
        	( (ViewHolder)holder ).label3.setVisibility(View.VISIBLE);
        } else {
        	( (ViewHolder)holder ).label3.setVisibility(View.GONE);
        }
    }

    /**
     * @param position The position of the row.
     * @return The text to display by the divider at the specified row index
     */
    protected String getDividerText(int position) {
        return null;
    }

    /**
     * @param position The position of the row.
     * @return The subtitle text to display immediately below the divider at the
     * specified row index; return <code>null</code> to indicate no subtitle text.
     */
    protected String getDividerSubText(int position) {
    	return null;
    }

    /**
     * To handle a click on a row,
     * a subclass should override {@link #onItemClick(View, int, long)}
     * to account for the offset for dividers.
     */
	@Override
	public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (hasHeader()) {
            // offset the position to account for the header
            position--;
        }

        if (-1 == position) {
            onHeaderClick(view);
        } else if (!isDivider(position)) {
    	    if (mDividers != null) {
                for (Integer currDividerIndex : mDividers) {
                    if (position > currDividerIndex) {
            			// Offset by the divider
            			position--;
            		}
            	}
    	    }

    	    onItemClick(view, position, id);
        }
    }

    /**
     * This method will be called when an item in the list is selected.
     * @param v The view that was clicked within the ListView
     * @param position The position of the view in the list. This position
     * argument has already been translated to normalize for the presence of
     * non-selectable and non-clickable dividers, if any. So implementing subclasses
     * can treat this argument as a given with respect to their own data structures,
     * and not have to do any index translation or manipulation.
     * @param id The row id of the item that was clicked
     */
	protected abstract void onItemClick(View v, int position, long id);

    /**
     * This method will be called when a header in the list is selected.
     *
     * @param v The view that was clicked within the ListView
     */
    protected void onHeaderClick(View v) {
        // overridden by a subclass with a list header.
    }

    /**
     * Gets the ID for of the option menu for this subadapter.
     *
     * @return          The ID of the options menu, <code>-1</code> if none.
     */
    protected int getOptionMenuId() {
        return -1;
    }

    /**
     * Creates an options menu for this subadapter.
     *
     * @param menu              The options menu.
     * @param inflator          The menu inflator.
     */
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflator) {
        final int id = getOptionMenuId();
        if (id > 0) {
            inflator.inflate(id, menu);
        }
    }

    /**
     * Updates the options menu for this subadapter.
     *
     * @param menu              The options menu.
     */
    public void onPrepareOptionsMenu(Menu menu) {
    }

	/**
	 * Same functionality as
	 * {@link OnCreateContextMenuListener#onCreateContextMenu(ContextMenu, View, ContextMenuInfo)}
	 * but with an additional parameter position. It is not safe to hold onto
	 * the menu after this method returns.
	 * @param menu The context menu that is being built
	 * @param v The view for which the context menu is being built
	 * @param menuInfo Extra information about the item for which the context
	 * menu should be shown. This information will vary depending on the class of v.
	 * @param position list index for which the context menu is being built. This
	 * position argument has already been normalized to account for the presence
	 * of the header and row selector in the parent FancyScrollListAdapter.
	 */
    public final void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo, int position) {
        if (hasHeader()) {
            // offset the position to account for the header
            position--;
        }

        if (position >= 0) {
            if (!isDivider(position)) {
                if (mDividers != null) {
                    for (Integer currDividerIndex : mDividers) {
                        if (position > currDividerIndex) {
                            // Offset by the divider
                            position--;
                        }
                    }
                }

                onCreateListContextMenu(menu, v, menuInfo, position);
            }
    	}
	}

	/**
	 * Same functionality as
	 * {@link OnCreateContextMenuListener#onCreateContextMenu(ContextMenu, View, ContextMenuInfo)}
	 * but with an additional parameter position. It is not safe to hold onto
	 * the menu after this method returns.
	 * @param menu The context menu that is being built
	 * @param v The view for which the context menu is being built
	 * @param menuInfo Extra information about the item for which the context
	 * menu should be shown. This information will vary depending on the class of v.
	 * @param position list index for which the context menu is being built. This
	 * position argument has already been normalized to account for the presence
	 * of the header and row selector in the parent FancyScrollListAdapter, AND
	 * any non-selectable and non-clickable dividers in the list.
	 */
	protected void onCreateListContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo, int position) {
	    // subclass to override
	}

	/**
	 * Same functionality as {@link Activity#onContextItemSelected(MenuItem)}
	 * Use getMenuInfo() to get extra information set by the View that added this menu item.
	 * Derived classes should call through to the base class for it to perform the default menu handling.
	 * @param item The context menu item that was selected.
	 * @param position list index for which the selected context menu item was built. This
	 * position argument has already been normalized to account for the presence
	 * of the header and row selector in the parent FancyScrollListAdapter.
	 * @return <code>false</code> to allow normal context menu processing to proceed,
	 * <code>true</code> to consume it here.
	 */
    public final boolean onContextItemSelected(MenuItem item, int position, boolean modifyPosition) {
        if (hasHeader()) {
            // offset the position to account for the header
            position--;
        }

        if (position >= 0) {
            if (!isDivider(position) && modifyPosition) {
                if (mDividers != null) {
                    for (Integer currDividerIndex : mDividers) {
                        if (position > currDividerIndex) {
                            // Offset by the divider
                            position--;
                        }
                    }
                }

                return onListContextItemSelected(item, position);
            } else if (!modifyPosition) {
                return onListContextItemSelected(item, position);
            }
        }

		return false;
	}

	/**
	 * Same functionality as {@link Activity#onContextItemSelected(MenuItem)}
	 * Use getMenuInfo() to get extra information set by the View that added this menu item.
	 * Derived classes should call through to the base class for it to perform the default menu handling.
	 * @param item The context menu item that was selected.
	 * @param position list index for which the selected context menu item was built. This
	 * position argument has already been normalized to account for the presence
	 * of the header and row selector in the parent FancyScrollListAdapter, AND
	 * any non-selectable and non-clickable dividers in the list.
	 * @return <code>false</code> to allow normal context menu processing to proceed,
	 * <code>true</code> to consume it here.
	 */
	public boolean onListContextItemSelected(MenuItem item, int position) {
	    // subclass to override
	    return false;
	}

    @Override
    public int getViewTypeCount() {
        // Can have up to three types if there is a header and divider
        return NEXT_ROW_TYPE; //this number must NOT be conditional on the presence of dividers or header. See http://stackoverflow.com/a/2597318
    }

	/**
	 * Gets the title of this list.
	 * @return     A string representing the title of this list,
	 *             <code>null</code> if none.
	 */
	public String getListTitle() {
	    return null;
	}

    /**
     * Sets the indices of the dividers in the list view.
     * A divider is an unselectable row, with an optional label.
     *
     * @param dividers              The indices of the rows that are dividers.
     */
    protected void setDividers(int... dividers) {
        if (null == mDividers) {
            mDividers = initDividerSet();
        }

		//remove old dividers
        mDividers.clear();

        if (dividers != null) {
        	for(int currIndex : dividers) {
        		mDividers.add(currIndex);
        	}
        }
    }

    /**
     * Sets the indices of the dividers in the list view.
     * A divider is an unselectable row, with an optional label.
     *
     * @param divSet Set of divider row indices
     */
    protected void setDividers(final Set<Integer> divSet) {
        if (null == mDividers) {
            mDividers = initDividerSet();
        }
        // Remove all of the old dividers
        mDividers.clear();
        // Add the new divider positions, if any
        if (divSet != null) {
            mDividers.addAll(divSet);
        }
    }

    protected Set<Integer> initDividerSet() {
        return new TreeSet<Integer>(new Comparator<Integer>() {
            @Override public int compare(Integer lhs, Integer rhs) {
                //descending order
                return rhs.compareTo(lhs);
            }
        });
    }

    /**
     * Sets/clears the list header. Calling this method multiple times will
     * overwrite any previously set header. Calling this method with
     * <code>null</code> as the <code>label</code> will remove any previously
     * set header.
     *
     * @param label             The header text or <code>null</code> to remove.
     * @param icon              An optional icon to display next to the label.
     */
    protected void setHeader(String label, BitmapDrawable icon) {
        setHeader(label, icon, true);
    }

    /**
     * Sets/clears the list header. Calling this method multiple times will
     * overwrite any previously set header. Calling this method with
     * <code>null</code> as the <code>label</code> will remove any previously
     * set header.
     *
     * @param label             The header text or <code>null</code> to remove.
     * @param icon              An optional icon to display next to the label.
     * @param isRefresh			<code>true</code> to notify any attached
     * observers that the underlying data has been changed and any View
     * reflecting the data set should refresh itself; <code>false</code>
     * otherwise
     */
    protected void setHeader(String label, BitmapDrawable icon, boolean isRefresh) {
    	mHeaderText = label;
        mHeaderIcon = icon;

        if(isRefresh) {
        	notifyDataSetChanged();
        }
    }

    /**
     * Indicates whether or not the specified position is a divider in this list
     * @param position the index of the row in this list
     * @return <code>true</code> if the specified position is a divider;
     * <code>false</code> otherwise
     */
    protected boolean isDivider(int position) {
    	return mDividers != null && mDividers.contains(position);
    }

    /**
     * @return number of dividers in this list
     */
    private int getDividerCount() {
    	return (mDividers != null) ? mDividers.size() : 0;
    }

    /**
     * Convenience method to set the text in a list view.
     *
     * @param tv                The text view to set.
     * @param text              The text to set. If <code>text</code> is <code>null</code>
     *                          or empty, then <code>tv</code> is hidden.
     */
    protected void setText(TextView tv, CharSequence text) {
        tv.setText(text);
        tv.setVisibility((text != null && text.length() > 0) ? View.VISIBLE : View.GONE);
    }

    /**
     * Checks if a row shows multiple items (e.g., radio stations, albums, etc).
     *
     * @param position  Row number. This position argument MUST already be
     * translated to normalize for the presence of headers, selector rows,
     * non-selectable and non-clickable dividers, if any. So overriding
     * subclasses can treat this argument as a given with respect to their
     * own data structures, and not have to do any index translation or
     * manipulation.
     * @return			<code>true</code> if the row shows multiple items.
     */
	public boolean isMultiItemRow(int position) {
		return false;
	}

	/**
	 * nulls out reference to Inflater, this helps prevent leaking activities, as the inflater has a reference to the activity.
	 * So by holding reference to inflater, we are holding the reference to the MainActivity as well
	 */
	public void release(){

		mInflater=null;
	}
}
