package com.defenestrate.chukkars.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.defenestrate.chukkars.android.SignupDayFragment.OnPlayerModificationListener.ModificationType;
import com.defenestrate.chukkars.android.entity.Day;
import com.defenestrate.chukkars.android.exception.PlayerNotFoundException;
import com.defenestrate.chukkars.android.exception.SignupClosedException;
import com.defenestrate.chukkars.android.util.Constants;
import com.defenestrate.chukkars.android.util.CoverArtUtil;
import com.defenestrate.chukkars.android.util.CoverArtUtil.CoverArtData;
import com.defenestrate.chukkars.android.util.HttpUtil;
import com.defenestrate.chukkars.android.util.PlayerSignupData;
import com.defenestrate.chukkars.android.util.PropertiesUtil;
import com.defenestrate.chukkars.android.widget.FancyScrollListAdapter.FancyScrollListSubadapter;
import com.defenestrate.chukkars.android.widget.FancyScrollListAdapter.FancyScrollListSubadapter.FancyScrollListSubadapterCallback;
import com.defenestrate.chukkars.android.widget.FancyScrollSignupDayListAdapter;
import com.defenestrate.chukkars.android.widget.SignupDayListSubadapter;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


public class SignupDayFragment extends FancyScrollListFragment
							   implements FancyScrollListSubadapterCallback, Constants {

	/////////////////////////////// CONSTANTS //////////////////////////////////
	private static final String LOG_TAG = "SignupDayFragment";


	/////////////////////////// MEMBER VARIABLES ///////////////////////////////
    /** The default cover art image. */
    private int mCoverArtId = -1;
    private long _dataLastModified = -1;
    private Day _selectedDay;
    private List<PlayerSignupData> mPlayerSignupList;
    private int mTotalGameChukkars;
    private AsyncTask<Integer, Void, Integer> mTask;
    private boolean mViewDestroyed = false;
    private OnPlayerModificationListener mCallback;
    private Handler _errHandler;
    private ActionMode.Callback mActionModeCallback;
    private ActionMode mActionMode;
    private View mPrevSelectedView;
    private OnPageChangeListener mOnPageChangeLstnr;
    private PlayerSignupData mSelectedPlayer;
    private Resources mRes;


	//////////////////////////////// METHODS ///////////////////////////////////
    /** {@inheritDoc} */
	@Override protected int getLayoutId() {
        return R.layout.fancy_scroll_signup_list;
    }

	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mRes = activity.getResources();

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnPlayerModificationListener)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnActivityLaunchListener");
        }

        if(activity instanceof ViewPagerActivity) {
    		mOnPageChangeLstnr = new ViewPager.SimpleOnPageChangeListener() {

    			@Override
    			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    				closeActionMode();
            	}
    		};

            ( (ViewPagerActivity)activity ).addOnPageChangeListener(mOnPageChangeLstnr);
        } else {
            mOnPageChangeLstnr = null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPlayerSignupList = new ArrayList<PlayerSignupData>();

    	_errHandler = new Handler( Looper.getMainLooper() )
        {
            @Override
            public void handleMessage(Message msg)
            {
            	if(!mViewDestroyed) {
                    if( (msg.what == R.id.message_what_error) && (msg.arg1 != 0) )
                    {
                		ErrorToast.show( getActivity(),
                						 mRes.getString(msg.arg1) );
                    }
                    else if( (msg.what == R.id.message_what_info) && (msg.arg1 != 0) )
                    {
                    	CharSequence text = mRes.getString(msg.arg1);
                    	int duration = Toast.LENGTH_LONG;

                    	Toast toast = Toast.makeText(getActivity(), text, duration);
                    	toast.show();
                    }
            	}
            }
        };

        mActionModeCallback = new ActionMode.Callback() {

            // Called when the action mode is created; startActionMode() was called
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate a menu resource providing context menu items
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.context_menu, menu);
                return true;
            }

            // Called each time the action mode is shown. Always called after onCreateActionMode, but
            // may be called multiple times if the mode is invalidated.
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false; // Return false if nothing is done
            }

            // Called when the user selects a contextual menu item
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.edit_player:
                        launchEditPlayerPage();
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    default:
                        return false;
                }
            }

            // Called when the user exits the action mode
            @Override
            public void onDestroyActionMode(ActionMode mode) {
            	mActionMode = null;
                closeActionMode();
            }
        };
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View ret = super.onCreateView(inflater, container, savedInstanceState);

    	ret.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				if(mListAdapter instanceof FancyScrollSignupDayListAdapter) {
					//Needed in case fragment is restored from a previously saved
					//state, where the floating view is visible
					((FancyScrollSignupDayListAdapter)mListAdapter).initAndDisplayFloatingView();
				}
			}
		});

    	SharedPreferences settings = getActivity().getSharedPreferences(SERVER_DATA_PREFS_NAME, Context.MODE_PRIVATE);
        String data = settings.getString(CONTENT_KEY, null);
        long lastModified = settings.getLong(LAST_MODIFIED_KEY, 0);

    	boolean doesDataExist = (data != null);

		if( !doesDataExist || (lastModified != _dataLastModified) )
		{
			_dataLastModified = lastModified;

			Bundle args = getArguments();
            int pageIndex = args.getInt(PAGE_INDEX_KEY);
    		loadPlayers(data, pageIndex);
		}

		return ret;
    }

    @Override
    public void onResume() {
    	super.onResume();

    	Bundle args = getArguments();

    	if( args.getBoolean(SCROLL_TO_END_KEY, false) ) {
    		mListView.post(new Runnable() {
    			@Override
				public void run() {
    				mListView.setSelection(mListView.getCount() - 1);
    			}
    		});

    		args.remove(SCROLL_TO_END_KEY);
    	}
    }

    @Override
    public void onDestroyView() {
    	super.onDestroyView();

    	mViewDestroyed = true;

    	if(mTask != null) {
    		mTask.cancel(true);
    		mTask = null;
    	}

    	if( (mOnPageChangeLstnr != null) &&
    		(getActivity() instanceof ViewPagerActivity) ) {
    		( (ViewPagerActivity)getActivity() ).removeOnPageChangeListener(mOnPageChangeLstnr);
    	}

    	CoverArtUtil.freeCoverArtId(mCoverArtId);
    }

    private void loadPlayers(String data, int pageIndex)
    {
    	boolean doesDataExist = (data != null);

    	if(!doesDataExist)
    	{
    		getServerData(pageIndex);
    	}
    	else
    	{
    		loadPlayersImpl(data, pageIndex);
    	}
    }

    private void getServerData(int pageIndex)
	{
    	if(mTask != null) {
    		mTask.cancel(true);
    	}

		AsyncTask<Integer, Void, Integer> task = new AsyncTask<Integer, Void, Integer>()
		{
			@Override
			protected void onPreExecute()
			{
				showLoading(true);
			}

		    @Override
		    protected Integer doInBackground(Integer... params)
		    {
				try
				{
					//http get
					HttpClient httpclient = new DefaultHttpClient();
					HttpGet get = new HttpGet( PropertiesUtil.getURLProperty(mRes, GET_PLAYERS_URL_KEY) );
					HttpResponse response = httpclient.execute(get);

					if( !isCancelled() ) {
						_dataLastModified = HttpUtil.writeServerData(response, getActivity());
					}

					Integer pageIndexArg = params[0];
					return pageIndexArg;
			    }
				catch(SignupClosedException e)
				{
					//will never happen, but show error toast on GUI thread just
					//in case
					Message msg = _errHandler.obtainMessage(R.id.message_what_info);
			    	msg.arg1 = R.string.signup_closed;
					_errHandler.sendMessage(msg);

					return null;
				}
				catch(PlayerNotFoundException e)
				{
					//will never happen, but show error toast on GUI thread just
					//in case
					Message msg = _errHandler.obtainMessage(R.id.message_what_info);
			    	msg.arg1 = R.string.player_not_found;
					_errHandler.sendMessage(msg);

					return null;
				}
				catch(IOException e)
				{
					//unable to connect to server
					//show error toast on GUI thread
					Message msg = _errHandler.obtainMessage(R.id.message_what_error);
			    	msg.arg1 = R.string.server_connect_error;
					_errHandler.sendMessage(msg);

					Log.e(this.getClass().getName(), e.getMessage(), e);
					return null;
				}
		    }

		    @Override
		    protected void onPostExecute(Integer result)
		    {
		    	mTask = null;
		    	showLoading(false);

		    	if(result != null)
		    	{
		    		loadPlayersImpl(result);
		    	}
		    }
		};

		mTask = task;
		task.execute(pageIndex);
	}

    private void loadPlayersImpl(int pageIndex)
    {
    	SharedPreferences settings = getActivity().getSharedPreferences(SERVER_DATA_PREFS_NAME, Context.MODE_PRIVATE);
        String data = settings.getString(CONTENT_KEY, null);

        if(data != null)
        {
        	//if orientation changed in the middle of loading, or some other
    		//way the app was stopped in the middle of loading, just ignore
        	//current load request. The activity will load once again.
        	loadPlayersImpl(data, pageIndex);
        }
    }

    private void loadPlayersImpl(String result, int pageIndex)
    {
    	//parse json data
	    try
	    {
	    	JSONObject data = new JSONObject(result);
	    	JSONArray jArray = data.getJSONArray(TOTALS_LIST_FIELD);

	    	if(jArray.length() <= pageIndex)
	    	{
	    		//incorrect app state
	    		//show error toast on GUI thread
				Message msg = _errHandler.obtainMessage(R.id.message_what_error);
		    	msg.arg1 = R.string.incorrect_app_state_error;
				_errHandler.sendMessage(msg);

				String errMsg = "Unexpected length for _totalsList in JSON: " + jArray.toString(4);
				Log.e(LOG_TAG, errMsg, new Throwable().fillInStackTrace() );

				return;
	    	}

	    	String dayStr = jArray.getJSONObject(pageIndex).getString(TOTAL_DAY_FIELD);
	    	_selectedDay = Day.valueOf(dayStr);
	    	mTotalGameChukkars = jArray.getJSONObject(pageIndex).getInt(TOTAL_NUM_CHUKKARS_FIELD);

	    	if( (mListAdapter != null) && (mListAdapter instanceof FancyScrollSignupDayListAdapter) ) {
	    		Bundle args = new Bundle();
	    		args.putString( FancyScrollSignupDayListAdapter.LOWER_LABEL_1_STRING, _selectedDay.name() );

	            String gameChukkarsTitle = MessageFormat.format(
	        		getString(R.string.tab_title),
	        		new Object[] {mTotalGameChukkars} );
	            args.putString(FancyScrollSignupDayListAdapter.LOWER_LABEL_2_STRING, gameChukkarsTitle);

	            ((FancyScrollSignupDayListAdapter)mListAdapter).addDisplayArgs(args);
	    	}

	    	//-----------------

	    	DateFormat inParser = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.US);
	    	inParser.setTimeZone( TimeZone.getTimeZone("GMT") );
            DateFormat outFormatter = new SimpleDateFormat("EEE, M/d h:mm a", Locale.US);
            outFormatter.setTimeZone( TimeZone.getTimeZone("America/Los_Angeles") );

            mPlayerSignupList.clear();
            jArray = data.getJSONArray(PLAYERS_LIST_FIELD);

            for(int i=0, n=jArray.length(); i<n; i++)
            {
                JSONObject currPlayer = jArray.getJSONObject(i);

                Day currRequestDay = Day.valueOf( currPlayer.getString( PLAYER_REQUESTDAY_FIELD) );
                if(currRequestDay == _selectedDay)
                {
                	String displayDateTime;

                	try
            		{
                		String createDate = currPlayer.getString( PLAYER_CREATEDATE_FIELD );
                		Date dateTime = inParser.parse(createDate);
                		displayDateTime = outFormatter.format(dateTime);
            		}
            		catch(ParseException e)
            		{
            			//error on parsing time. Should never happen. Just use
            			//the current time to avoid ugly error messages
            			displayDateTime = outFormatter.format( new Date() );

            			//log it
            			Log.e(LOG_TAG, e.getMessage(), e);
            		}

                	PlayerSignupData rowData = new PlayerSignupData(
                		currPlayer.getString(PLAYER_ID_FIELD),
                		displayDateTime,
                		currPlayer.getString(PLAYER_NAME_FIELD),
                		currPlayer.getString(PLAYER_NUMCHUKKARS_FIELD) );

                	mPlayerSignupList.add(rowData);
                }
            }

            ( (SignupDayListSubadapter)mListAdapter.getCurrentSubadapter() ).refreshHeader();

//TODO:
//            if( data.has(res.getString(R.string.curr_player_persisted_field)) )
//            {
//            	//persist the Id of the newly persisted Player, ONLY if
//            	//it doesn't already exist in the DB
//            	JSONObject persistedPlayer = data.getJSONObject( res.getString(R.string.curr_player_persisted_field) );
//            	long id = persistedPlayer.getLong( res.getString(R.string.player_id_field) );
//            	SignupDbAdapter db = getDBHelper();
//
//            	if( !db.containsPlayer(id) )
//            	{
//            		String name = persistedPlayer.getString( res.getString(R.string.player_name_field) );
//            		db.createPlayer(id, name);
//            	}
//
//            	db.close();
//            }
	    }
	    catch(JSONException e)
	    {
	    	//JSON response string does not match what we are expecting
	    	//show error toast on GUI thread
			Message msg = _errHandler.obtainMessage(R.id.message_what_error);
	    	msg.arg1 = R.string.unexpected_json_error;
			_errHandler.sendMessage(msg);

			Log.e(LOG_TAG, e.getMessage() + "\n\nHTTP response:\n" + result, e);
	    }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);

    	setHasOptionsMenu(true);
    }

	@Override
	protected FancyScrollSignupDayListAdapter createListAdapter(LayoutInflater inflater, FrameLayout parent) {
		//set params for the fancy scroll adapter
		final Bundle args = new Bundle();
		//calculating values for Add Player button
        args.putString( FancyScrollSignupDayListAdapter.LOWER_LABEL_1_STRING, getString(R.string.game_day_loading) );
        args.putString( FancyScrollSignupDayListAdapter.LOWER_LABEL_2_STRING, getString(R.string.game_chukkars_loading) );


        FancyScrollListSubadapter subadapter = new SignupDayListSubadapter(mPlayerSignupList, getActivity(), this) {

        	@Override
        	protected void onHeaderClick(View v) {
                launchAddPlayerPage();
            }

        	@Override
        	protected void onItemClick(View v, int position, long id) {
        		closeActionMode();

                // Start the CAB using the ActionMode.Callback defined above
                mActionMode = getActivity().startActionMode(mActionModeCallback);
                v.setSelected(true);
                mPrevSelectedView = v;

                mSelectedPlayer = (PlayerSignupData)getListItem(position);
        	}
        };

        FancyScrollSignupDayListAdapter adapter = new FancyScrollSignupDayListAdapter(getActivity(), inflater, this, parent, args, subadapter) {

			@Override
		    protected Drawable getCoverArt() {
				if(mCoverArtId != -1) {
					return CoverArtUtil.getAssignedCoverArt(mRes, mCoverArtId);
				} else {
					CoverArtData data = CoverArtUtil.getRandomCoverArt(mRes);
					mCoverArtId = data.mCoverArtId;

					return data.mCoverArtDrawable;
				}
		    }

			@Override
			protected void onHeaderCircleButtonClick(View v) {
				closeActionMode();
				launchAddPlayerPage();
			}

		};

		return adapter;
	}

	private void closeActionMode() {
		if(mPrevSelectedView != null) {
			mPrevSelectedView.setSelected(false);
		}

		if (mActionMode != null) {
            mActionMode.finish();
        }

		mPrevSelectedView = null;
		mActionMode = null;
		mSelectedPlayer = null;
	}

	private void launchAddPlayerPage() {
		Intent i = new Intent(getActivity(), AddPlayerActivity.class);
		i.putExtra(SIGNUP_DAY_KEY, _selectedDay);
		i.putExtra( COVER_ART_KEY, CoverArtUtil.getAssignedCoverArtResourceId(mCoverArtId) );
		i.putExtra(TITLE_RES_KEY, R.string.menu_add);

		//prevent 2 activities from being displayed if the "add button" is accidentally pressed twice
		i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

		startActivityForResult(i, R.id.get_server_data_request);
	}

	private void launchEditPlayerPage() {
		Intent i = new Intent(getActivity(), AddPlayerActivity.class);
		i.putExtra(SIGNUP_DAY_KEY, _selectedDay);
		i.putExtra( COVER_ART_KEY, CoverArtUtil.getAssignedCoverArtResourceId(mCoverArtId) );
		i.putExtra(PLAYER_ID_KEY, mSelectedPlayer.mPlayerId);
		i.putExtra(PLAYER_NAME_KEY, mSelectedPlayer.mName);
		i.putExtra(NUM_CHUKKARS_KEY, mSelectedPlayer.mNumChukkars);
		i.putExtra(TITLE_RES_KEY, R.string.menu_edit);

		startActivityForResult(i, R.id.get_server_data_request);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == R.id.get_server_data_request) {
			if(resultCode == Activity.RESULT_CANCELED) {
				if(data == null) {
					mCallback.onPlayerModificationCancel();
				} else {
					Day selectedDay = (Day)data.getSerializableExtra(SELECTED_DAY_KEY);
					mCallback.onPlayerModificationCancelAndRequestRefresh(selectedDay);
				}
			} else if(resultCode == Activity.RESULT_OK) {
				Day selectedDay = (Day)data.getSerializableExtra(SELECTED_DAY_KEY);
				boolean isCreateNewPlayer = data.getBooleanExtra(IS_ADD_PLAYER_KEY, false);

				mCallback.onPlayerModificationSave(
					selectedDay,
					isCreateNewPlayer ? ModificationType.PLAYER_ADDED : ModificationType.PLAYER_EDITED);
			} else {
				throw new IllegalArgumentException("result code (" + resultCode + ") not recognized for request R.id.get_server_data_request");
			}
		}
	}

	@Override
	public String getDataId() {
		if(_selectedDay == null) {
			Bundle args = getArguments();
	        _selectedDay = Day.valueOf( args.getString(SIGNUP_DAY_KEY) );
		}

	    return _selectedDay.name();
	}


	//////////////////////////// INNER CLASSES /////////////////////////////////
	public interface OnPlayerModificationListener {
		public enum ModificationType {
			PLAYER_ADDED,
			PLAYER_EDITED
		}

		/**
		 * Notifies the listener that a player modification has been saved
		 * @param selectedDay game Day that the modified player was signed up for
		 * @param modType the type of modification that took place
		 */
		void onPlayerModificationSave(Day selectedDay, ModificationType modType);

		/** Notifies the listener that a player modification has been canceled */
		void onPlayerModificationCancel();

		/**
		 * Notifies the listener that a player modification has been canceled
		 * and that the listener should refresh its state
		 * @param selectedDay game Day that the modified player was signed up for
		 */
		void onPlayerModificationCancelAndRequestRefresh(Day selectedDay);
	}
}
