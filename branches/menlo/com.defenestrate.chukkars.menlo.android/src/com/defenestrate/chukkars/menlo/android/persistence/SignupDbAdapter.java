package com.defenestrate.chukkars.menlo.android.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple signup database access helper class. Defines the basic CRUD operations
 * for storing, querying, and deleting player ids and names.
 */
public class SignupDbAdapter 
{
    //////////////////////////////// CONSTANTS /////////////////////////////////
	private static final String DATABASE_TABLE = "Player";
    public static final String KEY_WEBAPP_ID = "WebApp_Player_Id";
    public static final String KEY_NAME = "Player_Name";


    //////////////////////////// MEMBER VARIABLES //////////////////////////////
    private DatabaseHelper _dbHelper;
    private SQLiteDatabase _db;
    private final Context _ctx;


	/////////////////////////////// CONSTRUCTORS ///////////////////////////////
    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public SignupDbAdapter(Context ctx) 
    {
        this._ctx = ctx;
    }

    
	///////////////////////////////// METHODS //////////////////////////////////
    /**
     * Open the signup database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public SignupDbAdapter open() throws SQLException 
    {
        _dbHelper = new DatabaseHelper(_ctx);
        _db = _dbHelper.getWritableDatabase();
        return this;
    }

    public void close() 
    {
        _dbHelper.close();
    }


    /**
     * Adds a new player id entry using the id and name provided. If the player is
     * successfully created return the new rowId for that player, otherwise return
     * a -1 to indicate failure.
     * 
     * @param id the id assigned to the player in the webapp persistent storage
     * @param name the name of the player
     * @return rowId or -1 if failed
     */
    public long createPlayer(long id, String name) 
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_WEBAPP_ID, id);
        initialValues.put(KEY_NAME, name);

        return _db.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the player id entry with the given player webapp Id
     * 
     * @param id the id assigned to the player in the webapp persistent storage
     * @return true if deleted, false otherwise
     */
    public boolean deletePlayer(long id) 
    {
        return _db.delete(DATABASE_TABLE, KEY_WEBAPP_ID + "=" + id, null) > 0;
    }

    /**
     * Deletes all players from persistent storage
     * 
     * @return <code>true</code> if deletion was successful, <code>false</code>
     * otherwise
     */
    public boolean deleteAllPlayers() 
    {
    	try
    	{
    		_db.execSQL("delete from " + DATABASE_TABLE);
    		return true;
    	}
    	catch(SQLException e)
    	{
    		return false;
    	}
    }

    /**
     * Indicates whether or not a player id entry is contained within the DB.
     * 
     * @param id the id assigned to the player in the webapp persistent storage
     * @return <code>true</code> if the player id entry is contained within the 
     * DB; <code>false</code> otherwise
     */
    public boolean containsPlayer(long id) 
    {
        Cursor cursor = _db.query(
        	DATABASE_TABLE, 
        	new String[] { KEY_WEBAPP_ID }, 
        	KEY_WEBAPP_ID + "=" + id, 
        	null,
        	null, 
        	null, 
        	null, 
        	null);
        
        boolean retVal = false;
        if(cursor != null)
        {
        	retVal = (cursor.getCount() > 0);
        	cursor.close();
        }
        
        return retVal;
    }
    
    /**
     * Return a Cursor over the list of all player names in the database
     * @return Cursor over all player names
     */
    public Cursor fetchAllPlayerNames() 
    {
        return _db.query(
        	DATABASE_TABLE, 
        	new String[] { KEY_NAME }, 
        	null, 
        	null, 
        	null, 
        	null, 
        	null);
    }
    
    
    /**
     * Returns the number of players in the database
     * @return the number of players in the database
     */
    public int getPlayerCount()
    {
    	Cursor cursor = fetchAllPlayerNames();
    	
    	int count = 0;
    	if(cursor != null)
    	{
    		count = cursor.getCount();
    		cursor.close();
    	}
    	
    	return count;
    }

    
    //////////////////////////////INNER CLASSES ///////////////////////////////
    private static class DatabaseHelper extends SQLiteOpenHelper 
    {
    	////////////////////////////// CONSTANTS ///////////////////////////////
    	private static final String DATABASE_NAME = "signup";
    	private static final int DATABASE_VERSION = 3;
        private static final String DATABASE_CREATE =
            "CREATE TABLE " + DATABASE_TABLE + " (" +
            KEY_WEBAPP_ID + " LONG, " +
            KEY_NAME + " TEXT);";
        private static final String TAG = "SignupDbAdapter";

    	
		///////////////////////////// CONSTRUCTORS /////////////////////////////
        DatabaseHelper(Context context) 
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }


		/////////////////////////////// METHODS ////////////////////////////////
        @Override
        public void onCreate(SQLiteDatabase db) 
        {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
        {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }
}
