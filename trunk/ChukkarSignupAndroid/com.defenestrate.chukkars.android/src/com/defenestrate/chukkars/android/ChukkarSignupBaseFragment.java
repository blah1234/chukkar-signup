package com.defenestrate.chukkars.android;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.defenestrate.chukkars.android.util.HttpUtil;

/**
 * Base activity for a fragment in the app.
 * Contains common UI elements.
 */
public abstract class ChukkarSignupBaseFragment extends Fragment {
	// Error types for data
    /** Error connecting to network. */
    private static final int ERROR_NETWORK = 0;
    /** General error. */
    private static final int ERROR_GENERAL = 1;
    /** Socket time out error. */
    public static final int ERROR_NETWORK_SOCKET_TIMEOUT = 2;


    /** The title label in the custom action bar. */
    private TextView mSubtitle;

    /** internal loading dialog view*/
    private View mLoadingView;

    /** no data connection view, this view would be null if {@link #getNoDataConnectionContainer(View)} return null */
    private ViewGroup mViewNoDataConnection;

	protected boolean mBannerShown;

    /**
     * Gets the layout resource ID for this fragment.
     *
     * @return              The layout resource ID.
     */
    protected abstract int getLayoutId();

    public ChukkarSignupBaseFragment() {
        super();
    }


    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mBannerShown = getArguments().getBoolean("banner_shown");
		}
    }

	@Override
	public void onResume() {
		super.onResume();

    	if (shouldHandleNoDataConnection()) {
    		if (!hasDataConnection()) {
    			showNetworkErrorPage();
    		} else {
    			hideErrorPage();
    		}
    	}
	}

	private boolean hasDataConnection() {
    	Context context = getActivity();
        return HttpUtil.hasDataConnection(context);
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(getLayoutId(), container, false);
        mSubtitle = (TextView) v.findViewById(R.id.subtitle);


        // PLM-1289
        // there's no retry logic
        mViewNoDataConnection = getNoDataConnectionContainer(v);
        if (mViewNoDataConnection != null) {
        	inflater.inflate(R.layout.no_data_connection_layout, mViewNoDataConnection);;
        }

        final ViewGroup loadingContainer = getLoadingContainer(v);
        if (loadingContainer != null) {
        	mLoadingView = createLoadingView(inflater);
        	if (mLoadingView != null) {
        		mLoadingView.setVisibility(View.GONE);
        		loadingContainer.addView(mLoadingView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        	}
        }


    	if (shouldHandleNoDataConnection()) {
    		if (!hasDataConnection()) {
    			showNetworkErrorPage();
    		}
    	}

        return v;
    }

	/**
	 * this callback will be invoked when network status is changed.<br>
	 * @param connection	<code>true</code> if network is connected state.
	 */
	public void onNetworkStatusChanged(boolean connection) {
		Log.i(this.toString(), "onNetworkStatusChanged : " + connection);

		if (!connection) {
			showNetworkErrorPage();
		}
	}

    /**
     * return the container that loading progress view is going to be added. <br>
     * should override this to use internal loading progress instead of {@link MusicHubActivity#showLoading(boolean)} <br>
     *
     * @param createdView
     * @return
     */
    protected ViewGroup getLoadingContainer(View createdView) {
    	return null;
    }

    /**
     * return the container that no data connection layout is going to be added. <br>
     * we will add no data connection layout if you return not null. <br>
     *
     * @param createdView	created view from {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}. <br>
     * @return container	which no data connection layout is going to be added.
     */
    public ViewGroup getNoDataConnectionContainer(View createdView) {
    	return null;
    }

    /**
     * Displays the No Data Connection/Lost Connection page. See UI Section 3.3
     */
    protected void showNetworkErrorPage() {
        showErrorPage(ERROR_NETWORK);
    }

    /**
     * Displays the Error Loading page. See UI Section 3.3
     */
    protected void showLoadingErrorPage() {
        showErrorPage(ERROR_GENERAL);
    }

    /**
     * If container exists, displays an error page.
     *
     * @param what          the type of error to display. One of
     *                      <ul>
     *                      <li>{@link AriaConstants#ERROR_NETWORK}</li>
     *                      <li>{@link AriaConstants#ERROR_GENERAL}</li>
     *                      </ul>
     */
    protected void showErrorPage(int what) {
        if (mViewNoDataConnection != null) {
            int errorMsgId;
            boolean showTryAgainBtn;
            int errorImgId;

            switch(what) {
            case ERROR_NETWORK:
                errorMsgId = R.string.error_no_data_connection;
                showTryAgainBtn = true;
                errorImgId = R.drawable.no_data_connection;
                break;
            case ERROR_NETWORK_SOCKET_TIMEOUT:
            	errorMsgId = R.string.unable_to_display;
            	showTryAgainBtn = false;
            	errorImgId = R.drawable.no_data_connection;
                break;
            case ERROR_GENERAL:
            default:
                errorMsgId = R.string.error_loading;
                showTryAgainBtn = false;
                errorImgId = R.drawable.no_data_connection;
            }

            ((TextView) mViewNoDataConnection.findViewById(R.id.txt_no_data_connection)).setText(errorMsgId);
            ((ImageView) mViewNoDataConnection.findViewById(R.id.img_no_data_connection)).setImageResource(errorImgId);
            mViewNoDataConnection.setVisibility(View.VISIBLE);
        }
    }

    protected void hideErrorPage() {
        if (mViewNoDataConnection != null) {
            mViewNoDataConnection.setVisibility(View.GONE);
        }
    }

    /**
     * create loading progress view <br>
     *
     * @param inflater
     * @return	default loading view, {@link R.layout#view_loading}
     */
    protected View createLoadingView(LayoutInflater inflater) {
    	return inflater.inflate(R.layout.view_loading, null);
    }

    /**
     * @return	<code>true</code> if this fragment support internal loading progress
     */
    public boolean isSupportFragmentLoading() {
    	return mLoadingView != null;
    }

    /**
     * @return	<code>true</code> if this fragment should handle no data connection case
     */
    public boolean shouldHandleNoDataConnection() {
    	final boolean handle = mViewNoDataConnection != null;
    	Log.i(this.toString(), "shouldHandleNoDataConnection : " + handle);
    	return handle;
    }

    /**
     * Sets the page subtitle.
     *
     * @param subtitle      The new page subtitle.
     */
    public void setSubtitle(CharSequence subtitle) {
        mSubtitle.setText(subtitle);
        mSubtitle.setVisibility((subtitle != null) ? View.VISIBLE : View.GONE);
    }

    /**
     * Sets the page subtitle.
     *
     * @param subtitleId    The resource ID of the new page subtitle.
     *                      If <code>0</code>, then the text is cleared.
     */
    public void setSubtitle(int subtitleId) {
        if (subtitleId != 0) {
            mSubtitle.setVisibility(View.VISIBLE);
            mSubtitle.setText(subtitleId);
        } else {
            mSubtitle.setVisibility(View.GONE);
            mSubtitle.setText(null);
        }
    }

    /**
     * show/hide loading progress <br>
     * two kinds of progress are there, one of them is internal progress dialog only for this Fragment <br>
     * another is global progess dialog which shared by Activity <br>
     * this method check Fragment support progress dialog using {@link #isSupportFragmentLoading()}
     * and if it's true show/hide internal progress, else show/hide global progress<br>
     *
     * @param bShow		visibility of progress
     */
    public void showLoading(boolean bShow) {
    	Log.i(getTag(), "showLoading : " + bShow);
    	if (isSupportFragmentLoading()) {
    		mLoadingView.setVisibility(bShow ? View.VISIBLE : View.GONE);
    	} else {
    		( (ViewPagerActivity)getActivity() ).showLoading(bShow);
    		Log.e(getTag(), "showLoading : does not support internal fragment loading");
    	}
    }
}