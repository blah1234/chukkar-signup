package com.defenestrate.chukkars.android.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.defenestrate.chukkars.android.R;
import com.defenestrate.chukkars.android.widget.FancyScrollListAdapter.FancyScrollListSubadapter.FancyScrollListSubadapterCallback;

import java.util.Set;


/**
 * A list adapter that allows a single list to be scrolled, while keeping a
 * floating view of the manipulation buttons at top of the screen.
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
abstract public class FancyScrollSignupDayListAdapter extends FancyScrollSingleListAdapter {

	/////////////////////////////// CONSTANTS //////////////////////////////////
	private static final String LOG_TAG = "FancyScrollSignupDayListAdapter";

    /** The key to use for retrieving the text string from a Bundle for button #1 in the song header */
    public static final String SONG_HEADER_BUTTON_1_STRING = "SONG_HEADER_BUTTON_1_STRING";

    /** The key to use for retrieving the image id (int) from a Bundle for button #1 in the song header */
    public static final String SONG_HEADER_BUTTON_1_IMAGE_ID = "SONG_HEADER_BUTTON_1_IMAGE_ID";

    /** The key to use for retrieving the text string from a Bundle for button #2 in the song header */
    public static final String SONG_HEADER_BUTTON_2_STRING = "SONG_HEADER_BUTTON_2_STRING";

    /** The key to use for retrieving the image id (int) from a Bundle for button #2 in the song header */
    public static final String SONG_HEADER_BUTTON_2_IMAGE_ID = "SONG_HEADER_BUTTON_2_IMAGE_ID";

    /** The key to use for retrieving the text string from a Bundle for button #3 in the song header */
    public static final String SONG_HEADER_BUTTON_3_STRING = "SONG_HEADER_BUTTON_3_STRING";

    /** The key to use for retrieving the image id (int) from a Bundle for button #3 in the song header */
    public static final String SONG_HEADER_BUTTON_3_IMAGE_ID = "SONG_HEADER_BUTTON_3_IMAGE_ID";

    /** The key to use for retrieving the text string from a Bundle for the bottom left circle button label in the song header */
    public static final String CIRCLE_BUTTON_BOTTOM_LEFT_STRING = "CIRCLE_BUTTON_BOTTOM_LEFT_STRING";

    /** The key to use for retrieving the text string from a Bundle for the bottom right circle button label in the song header */
    public static final String CIRCLE_BUTTON_BOTTOM_RIGHT_STRING = "CIRCLE_BUTTON_BOTTOM_RIGHT_STRING";

    /** The key to use for retrieving the text string from a Bundle for the middle circle button label in the song header */
    public static final String CIRCLE_BUTTON_MIDDLE_STRING = "CIRCLE_BUTTON_MIDDLE_STRING";

    /** The key to use for retrieving the value (float) from a Bundle for the song header circle button bottom left text sweep angle */
    public static final String CIRCLE_BUTTON_BOTTOM_LEFT_TEXT_SWEEP_ANGLE_FLOAT = "CIRCLE_BUTTON_BOTTOM_LEFT_TEXT_SWEEP_ANGLE_FLOAT";

    /** The key to use for retrieving the value (float) from a Bundle for the song header circle button bottom right text sweep angle */
    public static final String CIRCLE_BUTTON_BOTTOM_RIGHT_TEXT_SWEEP_ANGLE_FLOAT = "CIRCLE_BUTTON_BOTTOM_RIGHT_TEXT_SWEEP_ANGLE_FLOAT";

    /** The key to use for retrieving the value (float) from a Bundle for the song header circle button bottom right text start angle */
    public static final String CIRCLE_BUTTON_BOTTOM_RIGHT_TEXT_START_ANGLE_FLOAT = "CIRCLE_BUTTON_BOTTOM_RIGHT_TEXT_START_ANGLE_FLOAT";

    /** The key to use for retrieving the text string from a Bundle for lower label #1 in the song header */
    public static final String LOWER_LABEL_1_STRING = "LOWER_LABEL_1_STRING";

    /** The key to use for retrieving the text string from a Bundle for lower label #2 in the song header */
    public static final String LOWER_LABEL_2_STRING = "LOWER_LABEL_2_STRING";


    /////////////////////////// MEMBER VARIABLES ///////////////////////////////
	/** The container of the floating view. */
    private View mFloatingContainer;

    /** The view within {@link #mFloatingContainer} containing the actual songlist options buttons */
    private View mFloatingSongListOptions;

    /** The cover art image acting as the "background" of {@link #mFloatingContainer} */
    private ImageView mFloatingCoverArt;

    /** The attached header view. */
	private View mHeader;

    private Bundle mArgs;
    private Drawable mFetchedCoverArt;
    final private FancyScrollListSubadapterCallback mParent;
    boolean mIsStaticHeaderInitialized = false;
    boolean mIsFloatingHeaderInitialized = false;


	///////////////////////////// CONSTRUCTORS /////////////////////////////////
    /**
     * @param parent the fragment which displays this adapter's data
     * @param args arguments supplied when the adapter was instantiated, if any
     */
	public FancyScrollSignupDayListAdapter(Context context,
									   LayoutInflater inflater,
									   FancyScrollListSubadapterCallback parent,
									   FrameLayout listContainer,
									   Bundle args,
									   FancyScrollListSubadapter subadapter) {
		super(context, inflater, listContainer, subadapter);

		mArgs = (args != null) ? args : new Bundle();
		mParent = parent;
	}


	//////////////////////////////// METHODS ///////////////////////////////////
	/**
     * Gets the cover art image for this page.
     *
     * @return              The cover art image for the image on this page.
     */
    protected abstract Drawable getCoverArt();

    /**
     * Handles a click for button #1 in the static header or floating view
     *
     * @param v View that was clicked.
     */
    protected void onHeaderButton1Click(final View v) {
        // Default does nothing
    }

    /**
     * Handle a click for button #2 in the static header or floating view
     *
     * @param v View that was clicked.
     */
    protected void onHeaderButton2Click(final View v) {
        // Default does nothing
    }

    /**
     * Handle a click for button #3 in the static header or floating view
     *
     * @param v View that was clicked.
     */
    protected void onHeaderButton3Click(final View v) {
        // Default does nothing
    }

    /**
     * Handle a click for the circle button in the static header or floating view
     *
     * @param v View that was clicked.
     */
    protected void onHeaderCircleButtonClick(final View v) {
        // Default does nothing
    }

    /**
     * Initializes view components and determines whether to display or hide the floating view
     */
    public void initAndDisplayFloatingView() {
    	initFloatingCoverArt(mFetchedCoverArt);

		//necessary for the floating view to get bounds on
		//the header view in order to determine whether
		//or not to display itself
		if(getAttachedHeaderView() == null) {
			initHeaderView( null, mParent.getListView() );
		}

		AbsListView view = mParent.getListView();
		displayFloatingView( mListContainer, view.getFirstVisiblePosition() );
    }

	/** {@inheritDoc} */
	@Override protected int getFloatingSelectorLayout() {
		return R.layout.signup_list_floater;
	}

	/**
	 * Set arguments for the display to be reflected on the next view refresh.
	 * The display arguments in the specified bundle will overwrite any existing
	 * display arguments already present with identical bundle keys.
	 * @param args bundle of mappings between display arg constants and their values.
	 * @see the constants defined in this class
	 */
	public void addDisplayArgs(Bundle args) {
		if(mArgs == null) {
			mArgs = args;
		} else {
			mArgs.putAll(args);
		}

		onDisplayArgsChange();
		//PLM-758
		notifyDataSetChanged();
	}

	/**
	 * Set arguments for the display to be reflected on the next view refresh.
	 * Any previous bundle will be dropped, and the specified bundle of display
	 * arguments will take its place.
	 * @param args bundle of mappings between display arg constants and their values.
	 * @see the constants defined in this class
	 */
	protected void setDisplayArgs(Bundle args) {
		mArgs = args;
		onDisplayArgsChange();
	}

	/**
	 * Removes arguments for the display to be reflected on the next view refresh.
	 * @param argKeys set of keys to be removed
	 * @see the constants defined in this class
	 */
	protected void removeDisplayArgs(Set<String> argKeys) {
		if(mArgs != null) {
			for(String currKey : argKeys) {
				mArgs.remove(currKey);
			}

			onDisplayArgsChange();
		}
	}

	private void onDisplayArgsChange() {
		mIsStaticHeaderInitialized = false;
		mIsFloatingHeaderInitialized = false;
	}

	/** {@inheritDoc} */
	@Override protected View initFloatingSelectorView(View convertView, ViewGroup parent) {
		if(mFloatingContainer == null) {
			mFloatingContainer = mInflater.inflate(getFloatingSelectorLayout(), parent, false);
			mFloatingSongListOptions = mFloatingContainer.findViewById(R.id.song_list_options);
			mFloatingCoverArt = (ImageView)mFloatingContainer.findViewById(R.id.cover_art);
		}

		if(!mIsFloatingHeaderInitialized) {
			initHeaderAndFloatingView(mFloatingContainer);
			mIsFloatingHeaderInitialized = true;
		}

		initFloatingCoverArt(mFetchedCoverArt);

		return mFloatingContainer;
	}

	/** {@inheritDoc} */
	@Override protected View getAttachedHeaderView() {
		return mHeader;
	}

	/** {@inheritDoc} */
	@Override protected View initHeaderView(View convertView, ViewGroup parent) {
		final View v;
        if (convertView != null) {
            v = convertView;

            if(!mIsStaticHeaderInitialized) {
    			initHeaderAndFloatingView(v);
    			mIsStaticHeaderInitialized = true;
    		}
        } else {
            v = mInflater.inflate(R.layout.signup_list_header, parent, false);
            initHeaderAndFloatingView(v);
        }


        boolean hideCoverArt = initCoverArt(v); //PLM-504
        if(hideCoverArt)
        {
        	 v.findViewById(R.id.layoutCircle).setVisibility(View.GONE);
        	 mFloatingCoverArt.setVisibility(View.GONE);
        	 mFloatingContainer.setVisibility(View.GONE);
        }
        else
        {
        	v.findViewById(R.id.layoutCircle).setVisibility(View.VISIBLE);
        	 mFloatingCoverArt.setVisibility(View.VISIBLE);
        	 mFloatingContainer.setVisibility(View.VISIBLE);
        }

        mHeader = v;
        return v;
	}

	private boolean initCoverArt(View v) {
		boolean hideCoverArt = false;
		final ImageView imageV = (ImageView) v.findViewById(R.id.cover_art);

		if( !isListRowDragAndDropEnabled() ) {
			mFetchedCoverArt = getCoverArt();

        	if(mFetchedCoverArt == null){
        		imageV.setVisibility(View.GONE);
        		hideCoverArt = true;

        		return hideCoverArt;
        	}

        	imageV.setVisibility(View.VISIBLE);
        	imageV.setImageDrawable(mFetchedCoverArt);
		} else {
			hideCoverArt = true;
			imageV.setVisibility(View.GONE);
		}

		//update the floating view
    	initFloatingCoverArt(mFetchedCoverArt);
    	return hideCoverArt;
	}

	/**
	 * update cover art
	 * @param drawable
	 */
	public void updateCoverArtDrawable(Drawable drawable) {
		if (drawable != null) {
			mFetchedCoverArt = drawable;
			Log.i(LOG_TAG, "updateCoverArtDrawable : ");
			notifyDataSetChanged();
		}
	}

	private void initFloatingCoverArt(Drawable coverArt) {
		if( (coverArt != null) && (mFloatingCoverArt.getDrawable() != coverArt) && (mFloatingSongListOptions.getHeight() > 0) ) {
			mFloatingCoverArt.setLayoutParams(
	    		new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, mFloatingSongListOptions.getHeight()) );

			Matrix m = new Matrix();
			float scale = 1.0f;

			if( coverArt.getIntrinsicWidth() != mFloatingSongListOptions.getWidth() ) {
				//scale the image, but maintain aspect ratio
				scale = (float)mFloatingSongListOptions.getWidth() / (float)coverArt.getIntrinsicWidth();	//displayWidth / imageWidth
				m.postScale(scale, scale);
			}

			m.postTranslate(
				0,
				-((coverArt.getIntrinsicHeight() * scale) - mFloatingSongListOptions.getHeight()) );

			mFloatingCoverArt.setImageMatrix(m);
			mFloatingCoverArt.setImageDrawable(coverArt);
		} else if(mFloatingSongListOptions.getHeight() == 0) {
			Log.i( LOG_TAG, "Floating view height is 0. Can't set background image!");
		}
	}

    private void initHeaderAndFloatingView(View v) {
        // Circle button
        final CircleButton circleButton = (CircleButton) v.findViewById(R.id.circle);
        circleButton.setOnClickListener(this);

        if(mArgs != null) {
            // Button 1
            final TextView button1 = (TextView) v.findViewById(R.id.button1);
            final String btnText1 = mArgs.getString(SONG_HEADER_BUTTON_1_STRING);
            if (TextUtils.isEmpty(btnText1)) {
                button1.setVisibility(View.GONE);
            } else {
                button1.setVisibility(View.VISIBLE);
                button1.setOnClickListener(this);
                button1.setText(btnText1);
            }

            // Button 2
            final TextView button2 = (TextView) v.findViewById(R.id.button2);
            final String btnText2 = mArgs.getString(SONG_HEADER_BUTTON_2_STRING);
            if (TextUtils.isEmpty(btnText2)) {
                button2.setVisibility(View.GONE);
            } else {
                button2.setVisibility(View.VISIBLE);
                button2.setOnClickListener(this);
                button2.setText(btnText2);
            }

            // Button 3
            final TextView button3 = (TextView) v.findViewById(R.id.button3);
            final String btnText3 = mArgs.getString(SONG_HEADER_BUTTON_3_STRING);
            if (TextUtils.isEmpty(btnText3)) {
                button3.setVisibility(View.GONE);
            } else {
                button3.setVisibility(View.VISIBLE);
                button3.setOnClickListener(this);
                button3.setText(btnText3);
            }

            boolean hasCircleButton = false;
            StringBuilder description = new StringBuilder();
            // Circle button null text is OK; same as empty string
            String text = circleButton.getTextTop();
            if(text != null) {
                hasCircleButton = true;
                description.append(text + " ");
            }
            text = circleButton.getTextLower();
            if(text != null) {
                hasCircleButton = true;
                description.append(text + " ");
            }
            text = mArgs.getString(CIRCLE_BUTTON_BOTTOM_LEFT_STRING);
            if(text != null) {
                circleButton.setTextBottomLeft(text);
                hasCircleButton = true;
                description.append(text + " ");
            }
            text = mArgs.getString(CIRCLE_BUTTON_BOTTOM_RIGHT_STRING);
            if(text != null) {
                circleButton.setTextBottomRight(text);
                hasCircleButton = true;
                description.append(text + " ");
            }
            text = mArgs.getString(CIRCLE_BUTTON_MIDDLE_STRING);
            if(text != null) {
                circleButton.setTextMiddle(text);
                hasCircleButton = true;
                description.append(text + " ");
            }
            if(circleButton != null) {
                circleButton.setContentDescription(description.toString());
            }

            // Circle button drawable
            if(circleButton.getBackground() != null) {
            	circleButton.getBackground().setAlpha(150);
            	hasCircleButton = true;
            }

            // Circle button text angles
            float val = mArgs.getFloat(CIRCLE_BUTTON_BOTTOM_LEFT_TEXT_SWEEP_ANGLE_FLOAT, Float.NaN);
            if( !Float.isNaN(val) ) {
                circleButton.setTextSweepAngleBottomLeft(val);
                hasCircleButton = true;
            }

            val = mArgs.getFloat(CIRCLE_BUTTON_BOTTOM_RIGHT_TEXT_SWEEP_ANGLE_FLOAT, Float.NaN);
            if( !Float.isNaN(val) ) {
                circleButton.setTextSweepAngleBottomRight(val);
                hasCircleButton = true;
            }

            val = mArgs.getFloat(CIRCLE_BUTTON_BOTTOM_RIGHT_TEXT_START_ANGLE_FLOAT, Float.NaN);
            if( !Float.isNaN(val) ) {
                circleButton.setTextStartAngleBottomRight(val);
                hasCircleButton = true;
            }

            if(hasCircleButton) {
            	circleButton.setVisibility(View.VISIBLE);
            } else {
            	circleButton.setVisibility(View.GONE);
            }

            // Optional bottom-left labels [i.e., instead of buttons]
            boolean hasLowerLabels = false;
            final TextView label1 = (TextView)v.findViewById(R.id.lower_label1);
            if (label1 != null) {
                final String lowerLabelText1 = mArgs.getString(LOWER_LABEL_1_STRING);
                if (!TextUtils.isEmpty(lowerLabelText1)) {
                    label1.setText(lowerLabelText1);
                    label1.setVisibility(View.VISIBLE);
                    hasLowerLabels = true;
                } else {
                    label1.setVisibility(View.GONE);
                }
            }
            final TextView label2 = (TextView)v.findViewById(R.id.lower_label2);
            if (label2 != null) {
                final String lowerLabelText2 = mArgs.getString(LOWER_LABEL_2_STRING);
                if (!TextUtils.isEmpty(lowerLabelText2)) {
                    label2.setText(lowerLabelText2);
                    label2.setVisibility(View.VISIBLE);
                    hasLowerLabels = true;
                } else {
                    label2.setVisibility(View.GONE);
                }
            }

            final View lowerLabelsArea = v.findViewById(R.id.lower_labels_area);
            if (lowerLabelsArea != null) {
                if (hasLowerLabels) {
                    lowerLabelsArea.setVisibility(View.VISIBLE);
                } else {
                    lowerLabelsArea.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.button1:
            onHeaderButton1Click(v);
            break;
        case R.id.button2:
            onHeaderButton2Click(v);
            break;
        case R.id.button3:
            onHeaderButton3Click(v);
            break;
        case R.id.circle:
            onHeaderCircleButtonClick(v);
            break;
        default:
            super.onClick(v);
            break;
        }
    }
}