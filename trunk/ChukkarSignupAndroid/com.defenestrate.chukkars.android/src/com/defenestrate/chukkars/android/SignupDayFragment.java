package com.defenestrate.chukkars.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.defenestrate.chukkars.android.entity.Day;
import com.defenestrate.chukkars.android.exception.SignupClosedException;
import com.defenestrate.chukkars.android.util.PlayerSignupData;
import com.defenestrate.chukkars.android.util.PropertiesUtil;
import com.defenestrate.chukkars.android.widget.FancyScrollListAdapter.FancyScrollListSubadapter;
import com.defenestrate.chukkars.android.widget.FancyScrollListAdapter.FancyScrollListSubadapter.FancyScrollListSubadapterCallback;
import com.defenestrate.chukkars.android.widget.FancyScrollSignupDayListAdapter;
import com.defenestrate.chukkars.android.widget.SignupDayListSubadapter;


public class SignupDayFragment extends FancyScrollListFragment
							   implements FancyScrollListSubadapterCallback {

	/////////////////////////////// CONSTANTS //////////////////////////////////
	static private final String SERVER_DATA_PREFS_NAME = "all-players.json";
	private static final String LOG_TAG = "SignupDayFragment";
	static final String PAGE_INDEX_KEY = "PAGE_INDEX_KEY";
	static final String SIGNUP_DAY_KEY = "SIGNUP_DAY_KEY";
	static private final String SIGNUP_CLOSED = "!!!SIGNUP_CLOSED!!!";
	static private final int MAX_NUM_COVER_ART = 12;


	/////////////////////////// MEMBER VARIABLES ///////////////////////////////
    /** The default cover art image. */
    private int mCoverArtId = -1;
    private long _dataLastModified = -1;
    private Day _selectedDay;
    private List<PlayerSignupData> mPlayerSignupList;
    private int mTotalGameChukkars;

    static private final Random mRand = new Random();
    static private Handler _errHandler;
    static private final Set<Integer> sUsedCoverArtIds = new TreeSet<Integer>();


	//////////////////////////////// METHODS ///////////////////////////////////
    /** {@inheritDoc} */
	@Override protected int getLayoutId() {
        return R.layout.fancy_scroll_signup_list;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPlayerSignupList = new ArrayList<PlayerSignupData>();

        if(_errHandler == null) {
        	_errHandler = new Handler( Looper.getMainLooper() )
            {
                @Override
                public void handleMessage(Message msg)
                {
                    if( (msg.what == R.id.message_what_error) && (msg.arg1 != 0) )
                    {
                		ErrorToast.show( getActivity(),
                						 getResources().getString(msg.arg1) );
                    }
                    else if( (msg.what == R.id.message_what_info) && (msg.arg1 != 0) )
                    {
                    	CharSequence text = getResources().getString(msg.arg1);
                    	int duration = Toast.LENGTH_LONG;

                    	Toast toast = Toast.makeText(getActivity(), text, duration);
                    	toast.show();
                    }
                }
            };
        }
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View ret = super.onCreateView(inflater, container, savedInstanceState);

        Resources res = getResources();
    	SharedPreferences settings = getActivity().getSharedPreferences(SERVER_DATA_PREFS_NAME, Context.MODE_PRIVATE);
        String data = settings.getString(res.getString(R.string.content_key), null);
        long lastModified = settings.getLong(res.getString(R.string.last_modified_key), 0);

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

    @Override public void onDestroyView() {
    	super.onDestroyView();

    	sUsedCoverArtIds.remove(mCoverArtId);
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

    private void getServerData(int tabIndex)
	{
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
		    	Resources res = getResources();

				try
				{
					//http get
					HttpClient httpclient = new DefaultHttpClient();
					HttpGet get = new HttpGet( PropertiesUtil.getURLProperty(getResources(), "get_players_url") );
					HttpResponse response = httpclient.execute(get);

					writeServerData(response);

					Integer tabIndexArg = params[0];
					return tabIndexArg;
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
		    	showLoading(false);

		    	if(result != null)
		    	{
		    		loadPlayersImpl(result);
		    	}
		    }
		};

		task.execute(tabIndex);
	}

    private void writeServerData(HttpResponse response) throws SignupClosedException, IOException
	{
		InputStream is = null;

		try
		{
			HttpEntity entity = response.getEntity();
			is = entity.getContent();

			//convert response to string
	    	BufferedReader reader = new BufferedReader( new InputStreamReader(is,"utf-8") );
	    	StringWriter strWrite = new StringWriter();

	    	String line = null;
	    	while( (line = reader.readLine()) != null )
	    	{
	    		strWrite.append(line + "\n");
	    	}

	    	String result = strWrite.toString();

	    	if( SIGNUP_CLOSED.equals(result.trim()) )
	    	{
	    		throw new SignupClosedException();
	    	}

	    	//write json data to preferences
	    	Resources res = getResources();
			SharedPreferences settings = getActivity().getSharedPreferences(SERVER_DATA_PREFS_NAME, Context.MODE_PRIVATE);
		    SharedPreferences.Editor editor = settings.edit();
		    editor.putString(res.getString(R.string.content_key), result);

		    _dataLastModified = new Date().getTime();
		    editor.putLong(res.getString(R.string.last_modified_key), _dataLastModified);

		    // Commit the edits!
		    editor.commit();
		}
	    finally
	    {
	    	try
	    	{
	    		if(is != null)
	    		{
	    			is.close();
	    		}
	    	}
	    	catch(IOException e) {}
	    }
	}

    private void loadPlayersImpl(int pageIndex)
    {
    	Resources res = getResources();
    	SharedPreferences settings = getActivity().getSharedPreferences(SERVER_DATA_PREFS_NAME, Context.MODE_PRIVATE);
        String data = settings.getString(res.getString(R.string.content_key), null);

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
	    	Resources res = getResources();
	    	JSONObject data = new JSONObject(result);
	    	JSONArray jArray = data.getJSONArray( res.getString(R.string.totals_list_field) );

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

	    	String dayStr = jArray.getJSONObject(pageIndex).getString( res.getString(R.string.total_day_field) );
	    	_selectedDay = Day.valueOf(dayStr);
	    	mTotalGameChukkars = jArray.getJSONObject(pageIndex).getInt( res.getString(R.string.total_num_chukkars_field) );

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
            jArray = data.getJSONArray( res.getString(R.string.players_list_field) );

            for(int i=0, n=jArray.length(); i<n; i++)
            {
                JSONObject currPlayer = jArray.getJSONObject(i);

                Day currRequestDay = Day.valueOf( currPlayer.getString( res.getString(R.string.player_requestDay_field)) );
                if(currRequestDay == _selectedDay)
                {
                	String displayDateTime;

                	try
            		{
                		String createDate = currPlayer.getString( res.getString(R.string.player_createDate_field) );
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
                		currPlayer.getString(res.getString(R.string.player_id_field)),
                		displayDateTime,
                		currPlayer.getString(res.getString(R.string.player_name_field)),
                		currPlayer.getString(res.getString(R.string.player_numChukkars_field)) );

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

    private Drawable getRandomCoverArt() {
    	mCoverArtId = getRandomWithExclusion(
    		mRand,
    		1,
    		MAX_NUM_COVER_ART,
    		sUsedCoverArtIds.toArray(new Integer[sUsedCoverArtIds.size()]) );

		sUsedCoverArtIds.add(mCoverArtId);

		return getAssignedCoverArt();
    }

    private Drawable getAssignedCoverArt() {
		String fieldName = "cover" + mCoverArtId;
		int id;

		try {
			Field coverArtField = R.drawable.class.getField(fieldName);
			id = coverArtField.getInt(null);
		} catch (NoSuchFieldException e) {
			//should never happen
			Log.e(LOG_TAG, e.getMessage(), e);
			id = R.drawable.cover1;
		} catch (IllegalArgumentException e) {
			//should never happen because the field is static
			Log.e(LOG_TAG, e.getMessage(), e);
			id = R.drawable.cover1;
		} catch (IllegalAccessException e) {
			//should never happen because the field is public
			Log.e(LOG_TAG, e.getMessage(), e);
			id = R.drawable.cover1;
		}

    	return getResources().getDrawable(id);
    }

    /**
     * Generates a random number (int) between start and end (both inclusive) and
     * does not return any number which is contained in the array exclude. All
     * other numbers occur with equal probability. Note, that the following
     * constrains must hold: exclude is sorted in ascending order and all numbers
     * are within the range provided and all of them are mutually exclusive.
     */
    private int getRandomWithExclusion(Random rnd, int start, int end, Integer... exclude) {
        int random = start + rnd.nextInt(end - start + 1 - exclude.length);
        for (int ex : exclude) {
            if (random < ex) {
                break;
            }
            random++;
        }

        return random;
    }

    static public void resetUsedCoverArtIds() {
    	sUsedCoverArtIds.clear();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);

    	setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflator) {
    	/*@todo
    	int menuLayoutId = -1;

    	switch (mPlaylist.source) {
		case AriaPlaylist.SOURCE_MOST_PLAYED:
		case AriaPlaylist.SOURCE_RECENTLY_ADDED:
			menuLayoutId = R.menu.playlist_recently_added_menu;
			break;
		case AriaPlaylist.SOURCE_ON_MY_PHONE:
			menuLayoutId = R.menu.playlist_on_my_device_menu;
			break;

		default:
			if(mPlaylist.isSmart()){
				menuLayoutId = R.menu.playlist_favorite_songs_menu;
			}else if( mPlaylist.isEditable() ) {
				menuLayoutId = R.menu.playlist_songs_menu;
			}
			break;
		}

    	if(menuLayoutId != -1) {
    		inflator.inflate(menuLayoutId, menu);
    	}*/
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
    	/**@todo
    	boolean isEditVisible;

    	if( (mListAdapter != null) && (mListAdapter instanceof FancyScrollSongsListAdapter) ) {
    		isEditVisible = !( (FancyScrollSongsListAdapter)mListAdapter ).isListRowDragAndDropEnabled() && mPlaylist.isEditable();
    	} else {
    		isEditVisible = false;
    	}
    	mAppResources.optionsMenuHelper.preparePlaylistSongsOptionsMenu(isEditVisible, menu, mPlaylist);*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	/**@todo
    	boolean itemHandled = false;

    	if( (mListAdapter != null) &&
    		(mListAdapter instanceof FancyScrollSongsListAdapter) &&
    		(mListAdapter.getCurrentSubadapter() instanceof ISongCollectionCreator) ) {

    		try {
	    		itemHandled = mAppResources.optionsMenuHelper.onPlaylistSongsOptionsItemSelected(
	    			mPlaylist,
	    			(FancyScrollSongsListAdapter)mListAdapter,
	    			getMusicHubActivity(),
	    			item,
	    			(ISongCollectionCreator)mListAdapter.getCurrentSubadapter() );
    		} catch(MusicDbException e) {
    			mAppResources.launcher.onDbError("onOptionsItemSelected", e);
    			itemHandled = true;
    		}
    	}

		return itemHandled ? itemHandled : super.onOptionsItemSelected(item);*/
    	return false;
    }

	@Override
	protected FancyScrollSignupDayListAdapter createListAdapter(LayoutInflater inflater, FrameLayout parent) {
		//set params for the fancy scroll adapter
		final Bundle args = new Bundle();
		//calculating values for Add Player button
		args.putInt(FancyScrollSignupDayListAdapter.CIRCLE_BUTTON_BACKGROUND_ID, R.drawable.circle_button_add_player);
        args.putString( FancyScrollSignupDayListAdapter.CIRCLE_BUTTON_TOP_STRING, getString(R.string.circle_button_top_text) );
        args.putString( FancyScrollSignupDayListAdapter.CIRCLE_BUTTON_BOTTOM_STRING, getString(R.string.circle_button_bottom_text) );

        args.putString( FancyScrollSignupDayListAdapter.LOWER_LABEL_1_STRING, "placeholder: eventually this will be the game day");
        args.putString( FancyScrollSignupDayListAdapter.LOWER_LABEL_2_STRING, "placeholder: eventually this will be the number of game chukkars");


        FancyScrollListSubadapter subadapter = new SignupDayListSubadapter(mPlayerSignupList, getActivity(), this);

        FancyScrollSignupDayListAdapter adapter = new FancyScrollSignupDayListAdapter(getActivity(), inflater, this, parent, args, subadapter) {

			@Override
		    protected Drawable getCoverArt() {
				if(mCoverArtId != -1) {
					return getAssignedCoverArt();
				} else {
					return getRandomCoverArt();
				}
		    }

		    @Override
			protected void onHeaderButton1Click(View v) {
			}

		    @Override
			protected void onHeaderButton2Click(View v) {
		    }

			@Override
			protected void onHeaderButton3Click(View v) {
			}

			@Override
			protected void onHeaderCircleButtonClick(View v) {
			}

		};

		return adapter;
	}

	@Override
	public String getDataId() {
		if(_selectedDay == null) {
			Bundle args = getArguments();
	        _selectedDay = Day.valueOf( args.getString(SIGNUP_DAY_KEY) );
		}

	    return _selectedDay.name();
	}
}
