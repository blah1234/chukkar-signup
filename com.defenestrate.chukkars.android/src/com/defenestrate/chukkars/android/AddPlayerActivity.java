package com.defenestrate.chukkars.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.defenestrate.chukkars.android.entity.Day;
import com.defenestrate.chukkars.android.exception.PlayerNotFoundException;
import com.defenestrate.chukkars.android.exception.SignupClosedException;
import com.defenestrate.chukkars.android.util.Constants;
import com.defenestrate.chukkars.android.util.HttpUtil;
import com.defenestrate.chukkars.android.util.PropertiesUtil;

public class AddPlayerActivity extends ChukkarsActivity
							   implements Constants {

	/////////////////////////////// CONSTANTS //////////////////////////////////
	private static final String LOG_TAG = AddPlayerActivity.class.getSimpleName();
	private static final int DEFAULT_INIT_NUM_CHUKKARS = 2;


	/////////////////////////// MEMBER VARIABLES ///////////////////////////////
	private Day mSelectedDay;
	private EditText mNameEdit;
	private TextView mChukkarsLabel;
	private ImageView mSliderThumb;
	private View mSliderTrack;
	private int mSliderCenterX, mSliderCenterY, mSliderRadius;
	private boolean mInitialized = false;
	private AsyncTask<String, Void, Day> mTask;
	private final Handler _errHandler;
	private boolean mActivityDestroyed = false;
	private int mInitNumChukkars = DEFAULT_INIT_NUM_CHUKKARS;
	private boolean mIsCreateNewPlayer;
	private String mEditPlayerId;
	private Vibrator mVibrator;

	static private int sSysStatusBarHeight = -1;


	///////////////////////////// CONSTRUCTORS /////////////////////////////////
	public AddPlayerActivity() {
		_errHandler = new Handler( Looper.getMainLooper() )
        {
            @Override
            public void handleMessage(Message msg)
            {
            	if(!mActivityDestroyed) {
	                if( (msg.what == R.id.message_what_error) && (msg.arg1 != 0) )
	                {
	            		ErrorToast.show( AddPlayerActivity.this,
	            						 getResources().getString(msg.arg1) );
	                }
	                else if( (msg.what == R.id.message_what_info) && (msg.arg1 != 0) )
	                {
	                	CharSequence text = getResources().getString(msg.arg1);
	                	int duration = Toast.LENGTH_LONG;

	                	Toast toast = Toast.makeText(AddPlayerActivity.this, text, duration);
	                	toast.show();

	                	if(msg.arg1 == R.string.player_not_found) {
	                		Intent i = null;

	                		if(msg.obj != null) {
	                			Day signupDay = Day.valueOf( (String)msg.obj );

	                			i = new Intent();
	                			i.putExtra(SELECTED_DAY_KEY, signupDay);
	                		}

	                		cancel(i);
	                	}
	                }
            	}
            }
        };
	}


	//////////////////////////////// METHODS ///////////////////////////////////
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.drop_in, R.anim.hold);

        mVibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        this.setTitle( getIntent().getIntExtra(TITLE_RES_KEY, R.string.menu_add) );

        setContentView(R.layout.signup_add_player);

        mSelectedDay = (Day)getIntent().getSerializableExtra(SIGNUP_DAY_KEY);

        int coverArtRes = getIntent().getIntExtra(COVER_ART_KEY, R.drawable.cover10);
        ImageView backgroundImage = (ImageView)findViewById(R.id.background);
        backgroundImage.setImageDrawable( getResources().getDrawable(coverArtRes) );

        initNameWidgets();
        initNumChukkarWidgets();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mActivityDestroyed = true;

		if(mTask != null) {
    		mTask.cancel(true);
    		mTask = null;
    	}
	}

	private void initNameWidgets() {
		View headingContainer = findViewById(R.id.name_heading);
        headingContainer.findViewById(R.id.divider1).setAlpha(.6f);

        TextView nameHeader = (TextView)headingContainer.findViewById(R.id.label1);
        nameHeader.setText(R.string.name_col_header);
        nameHeader.setAlpha(.6f);

        mNameEdit = (EditText)findViewById(R.id.name_field);
        mNameEdit.setTypeface( Typeface.createFromAsset(getAssets(), "fonts/roboto_thin.ttf") );

        String playerName = getIntent().getStringExtra(PLAYER_NAME_KEY);

        if(playerName != null) {
        	//editing an existing player
        	mIsCreateNewPlayer = false;
        	mNameEdit.setText(playerName);
        	mNameEdit.setKeyListener(null);

        	mEditPlayerId = getIntent().getStringExtra(PLAYER_ID_KEY);
        } else {
        	//adding a new player
        	mIsCreateNewPlayer = true;
	        mNameEdit.setOnTouchListener(new View.OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					int action = event.getAction();

					switch (action) {
					case MotionEvent.ACTION_DOWN:
						mNameEdit.setCursorVisible(true);

					default:
						return false;
					}
				}
			});

	        mNameEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {

				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					switch (actionId) {
					case EditorInfo.IME_ACTION_DONE:
						mNameEdit.setCursorVisible(false);

					default:
						return false;
					}
				}
			});
        }
	}

	private void initNumChukkarWidgets() {
		View headingContainer = findViewById(R.id.chukkars_heading);
        headingContainer.findViewById(R.id.divider1).setAlpha(.6f);

        TextView chukkarHeader = (TextView)headingContainer.findViewById(R.id.label1);
        chukkarHeader.setText(R.string.chukkars_col_header);
        chukkarHeader.setAlpha(.6f);

        mChukkarsLabel = (TextView)findViewById(R.id.chukkars_label);
        mChukkarsLabel.setTypeface( Typeface.createFromAsset(getAssets(), "fonts/roboto_thin.ttf") );

        //editing an existing player
        String numChukkars = getIntent().getStringExtra(NUM_CHUKKARS_KEY);

        if(numChukkars == null) {
        	//adding a new player
        	numChukkars = Integer.toString(DEFAULT_INIT_NUM_CHUKKARS);
        }

        mInitNumChukkars = Integer.parseInt(numChukkars);
    	mChukkarsLabel.setText(numChukkars);


        mSliderTrack = findViewById(R.id.chukkars_slider_track);
        mSliderThumb = (ImageView)findViewById(R.id.chukkars_slider_thumb);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        	initJellyBeanThumb();
        } else {
        	mSliderThumb.setImageResource(R.drawable.chukkars_slider_thumb);
        }

        mSliderThumb.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
	            case MotionEvent.ACTION_DOWN:
                    v.setPressed(true);
	                v.invalidate();
	                break;

	            case MotionEvent.ACTION_MOVE:
                    v.setPressed(true);
	                v.invalidate();

	                int x = (int) event.getRawX();
					int y = (int) event.getRawY() - getActionBar().getHeight() - getStatusBarHeight();

					// clamp values to be on the slider track

					// calculate offsets of touchpoint
					int xOffset = x - mSliderCenterX;
					int yOffset = y - mSliderCenterY;

					// calculate closest point on the slider track to the touchpoint
					double distFromCenter = Math.sqrt(xOffset*xOffset + yOffset*yOffset);
					int aX = (int) (mSliderCenterX + xOffset / distFromCenter * mSliderRadius);
					int aY = (int) (mSliderCenterY + yOffset / distFromCenter * mSliderRadius);

					// update UI accordingly adjusting so thumb is on the center of that point
					v.setTranslationX(aX - v.getWidth()/2f);
					v.setTranslationY(aY - v.getHeight()/2f);

					updateDigits(aX - mSliderCenterX, aY - mSliderCenterY);
	                break;

	            case MotionEvent.ACTION_UP:
	                v.setPressed(false);

	                Animation a = AnimationUtils.loadAnimation(AddPlayerActivity.this, R.anim.bounce);
	                mChukkarsLabel.startAnimation(a);

	                // ProgressBar doesn't know to repaint the thumb drawable
	                // in its inactive state when the touch stops (because the
	                // value has not apparently changed)
	                v.invalidate();
	                break;

	            case MotionEvent.ACTION_CANCEL:
	                v.setPressed(false);
	                v.invalidate(); // see above explanation
	                break;
	        }
	        return true;
			}
		});
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void initJellyBeanThumb() {
		LayoutInflater inflator = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        SeekBar raidForParts = (SeekBar)inflator.inflate(R.layout.seek_bar, (ViewGroup)findViewById(R.id.controls_container), false);

    	mSliderThumb.setImageDrawable( raidForParts.getThumb() );
	}

	@Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if(hasFocus && !mInitialized) {
        	// initialize some calculations we will need
            mSliderCenterX = mSliderTrack.getLeft() + mSliderTrack.getWidth() / 2;
			mSliderCenterY = mSliderTrack.getTop() + mSliderTrack.getHeight() / 2;

			Resources res = getResources();
			mSliderRadius = Math.round(
				(mSliderTrack.getWidth() / (float)res.getInteger(R.integer.chukkar_slider_track_inner_radius_ratio)) +
				(res.getDimension(R.dimen.chukkar_slider_track_thickness) / 2) );

        	mInitialized = true;

        	setInitialThumbPosition();
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//for the action bar options menu
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.add_player_action_bar_menu, menu);
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
        case R.id.menu_cancel:
        	cancel();
            return true;

        case R.id.menu_save:
        	closeSoftKeyboard();

        	if(mIsCreateNewPlayer) {
        		addPlayer( mSelectedDay, mNameEdit.getText().toString(), mChukkarsLabel.getText().toString() );
        	} else {
        		editNumChukkars( mSelectedDay, mEditPlayerId, mChukkarsLabel.getText().toString() );
        	}

        	return true;

        case android.R.id.home:
            // app icon in action bar clicked; navigate up to the home view pager
            Intent intent = new Intent(this, Main.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.hold, R.anim.rise_up);
            return true;

        default:
            return super.onOptionsItemSelected(item);
		}
	}

	private void cancel() {
		cancel(null);
	}

	private void cancel(Intent i) {
		if(i == null) {
			setResult(RESULT_CANCELED);
		} else {
			setResult(RESULT_CANCELED, i);
		}

        finish();
	}

	private void setInitialThumbPosition() {
		//init chukkar count is 2
		float percentAroundCircle = mInitNumChukkars / 12f;
//			Log.i(TAG, "Percent around circle: " + percentAroundCircle);
		double angle = 0;
		if(percentAroundCircle <= 0.25f)
		{
			angle = Math.PI * -2 * percentAroundCircle + Math.PI / 2;
		}
		else
		{
			angle = Math.PI * -2 * percentAroundCircle + Math.PI * 2.5f;
		}
//			Log.i(TAG, "Angle is: " + angle);
		int offsetX = (int) (mSliderRadius * Math.cos(angle));
		int offsetY = (int) (mSliderRadius * Math.sin(angle));
//			Log.i(TAG, "OffsetX,OffsetY: " + offsetX + "," + offsetY);
		int x = mSliderCenterX + offsetX;
		int y = mSliderCenterY - offsetY;
//			Log.i(TAG, "X,Y: " + x + "," + y);

		// update translation values
		mSliderThumb.setTranslationX(x - mSliderThumb.getWidth() / 2);
		mSliderThumb.setTranslationY(y - mSliderThumb.getHeight() / 2);
	}

	private void updateDigits(int xOffset, int yOffset)	{
//		Log.i(TAG, "Updating digits for xOffset,yOffset: " + xOffset + "," + yOffset);
		// Determine corresponding time to new selector position
		double angle = 0d;
		if(Math.abs(xOffset) < Math.abs(yOffset))
		{
			angle = Math.acos(((float) xOffset) / mSliderRadius);
			if(yOffset > 0)
			{
				angle = Math.PI * 2 - angle;
			}
		}
		else
		{
			angle = Math.asin(-1f * yOffset / mSliderRadius);
			if(xOffset < 0)
			{
				angle = Math.PI - angle;
			}
		}
//		Log.i(TAG, "Angle is: " + angle);
		double percentage = 1.25d - angle / 2d / Math.PI;
		if(percentage >= 1d)
		{
			percentage -= 1d;
		}
//		Log.i(TAG, "Percentage is " + percentage);

		double hoursExact = percentage * 12d;
		if(hoursExact < 1d)
		{
			hoursExact += 12d;
		}
		else if(hoursExact >= 12d)
		{
			hoursExact -= 12d;
		}
//		Log.i(TAG, "Hours exact: " + hoursExact);
		int hour = (int) hoursExact;
		int minute = (int) ((hoursExact - hour) * 60d);
//		Log.i(TAG, "Unclamped " + hour + ":" + minute);
		if(minute < 0)
		{
			// clamp minute
			minute = 0;
		}
		else if(minute >= 60)
		{
			minute = 59;
		}


		String hourStr = Integer.toString(hour % 12);

		if( !mChukkarsLabel.getText().equals(hourStr) ) {
			if( mVibrator.hasVibrator() ) {
				mVibrator.vibrate( getResources().getInteger(R.integer.medium_vibrate_time) );
			}

			mChukkarsLabel.setText(hourStr);
		}
	}

	private int getStatusBarHeight() {
		if(sSysStatusBarHeight == -1) {
			sSysStatusBarHeight = 0;
			int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");

			if(resourceId > 0) {
				sSysStatusBarHeight = getResources().getDimensionPixelSize(resourceId);
			}
		}

		return sSysStatusBarHeight;
	}

	private void addPlayer(Day selectedDay, String name, String numChukkars)
	{
		if( (name == null) || (name.trim().length() == 0) )
		{
			//show error toast
			Message msg = _errHandler.obtainMessage(R.id.message_what_info);
	    	msg.arg1 = R.string.blank_name;
			_errHandler.sendMessage(msg);

			return;
		}

		if(mTask != null) {
    		mTask.cancel(true);
    	}

		AsyncTask<String, Void, Day> task = new AsyncTask<String, Void, Day>()
		{
			@Override
			protected void onPreExecute()
			{
				//show the "busy" dialog
				showLoading(true);
			}

		    @Override
		    protected Day doInBackground(String... params)
		    {
		    	String selectedDayArg = params[0];

				try
				{
					String nameArg = params[1];
					String numChukkarsArg = params[2];

					//http post
					HttpClient httpclient = new DefaultHttpClient();
					HttpPost post = new HttpPost( PropertiesUtil.getURLProperty(getResources(), ADD_PLAYER_URL_KEY) );

					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
					nameValuePairs.add( new BasicNameValuePair(PLAYER_REQUESTDAY_FIELD, selectedDayArg) );
			        nameValuePairs.add( new BasicNameValuePair(PLAYER_NAME_FIELD, nameArg) );
			        nameValuePairs.add( new BasicNameValuePair(PLAYER_NUMCHUKKARS_FIELD, numChukkarsArg) );
			        post.setEntity( new UrlEncodedFormEntity(nameValuePairs) );

					HttpResponse response = httpclient.execute(post);

					if( !isCancelled() ) {
						HttpUtil.writeServerData(response, AddPlayerActivity.this);
					}

					return Day.valueOf(selectedDayArg);
			    }
				catch(SignupClosedException e)
				{
					//show error toast on GUI thread
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
			    	msg.obj = selectedDayArg;
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

					Log.e(LOG_TAG, e.getMessage(), e);
					return null;
				}
		    }

		    @Override
		    protected void onPostExecute(Day result)
		    {
		    	onAsyncTaskComplete(result);
		    }
		};

		mTask = task;
        task.execute(selectedDay.toString(), name, numChukkars);
	}

	private void editNumChukkars(Day selectedDay, String playerId, String numChukkars)
	{
		if(mTask != null) {
    		mTask.cancel(true);
    	}

		AsyncTask<String, Void, Day> task = new AsyncTask<String, Void, Day>()
		{
			@Override
			protected void onPreExecute()
			{
				//show the "busy" dialog
				showLoading(true);
			}

		    @Override
		    protected Day doInBackground(String... params)
		    {
		    	String selectedDayArg = params[0];

				try
				{
					String playerIdArg = params[1];
					String numChukkarsArg = params[2];

					//http post
					HttpClient httpclient = new DefaultHttpClient();
					HttpPost post = new HttpPost( PropertiesUtil.getURLProperty(getResources(), EDIT_CHUKKARS_URL_KEY) );

					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
					nameValuePairs.add( new BasicNameValuePair(PLAYER_ID_FIELD, playerIdArg) );
			        nameValuePairs.add( new BasicNameValuePair(PLAYER_NUMCHUKKARS_FIELD, numChukkarsArg) );
			        post.setEntity( new UrlEncodedFormEntity(nameValuePairs) );

					HttpResponse response = httpclient.execute(post);

					if( !isCancelled() ) {
						HttpUtil.writeServerData(response, AddPlayerActivity.this);
					}

					return Day.valueOf(selectedDayArg);
			    }
				catch(SignupClosedException e)
				{
					//show error toast on GUI thread
					Message msg = _errHandler.obtainMessage(R.id.message_what_info);
			    	msg.arg1 = R.string.signup_closed;
					_errHandler.sendMessage(msg);

					return null;
				}
				catch(PlayerNotFoundException e)
				{
					//show error toast on GUI thread
					Message msg = _errHandler.obtainMessage(R.id.message_what_info);
			    	msg.arg1 = R.string.player_not_found;
			    	msg.obj = selectedDayArg;
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

					Log.e(LOG_TAG, e.getMessage(), e);
					return null;
				}
		    }

		    @Override
		    protected void onPostExecute(Day result)
		    {
		    	onAsyncTaskComplete(result);
		    }
		};

		mTask = task;
        task.execute(selectedDay.toString(), playerId, numChukkars);
	}

	private void onAsyncTaskComplete(Day signupDay) {
		mTask = null;
    	showLoading(false);

    	if(signupDay != null) {
    		Intent i = new Intent();
    		i.putExtra(SELECTED_DAY_KEY, signupDay);
    		i.putExtra(IS_ADD_PLAYER_KEY, mIsCreateNewPlayer);
	    	setResult(RESULT_OK, i);

	    	finish();
    	}
	}

	@Override
	public void finish() {
		super.finish();

		closeSoftKeyboard();
		overridePendingTransition(R.anim.hold, R.anim.rise_up);
	}

	private void closeSoftKeyboard() {
		InputMethodManager immNameText = (InputMethodManager)
		   getSystemService(Context.INPUT_METHOD_SERVICE);

		if(immNameText != null) {
			immNameText.hideSoftInputFromWindow(mNameEdit.getWindowToken(), 0);
		}
	}
}
