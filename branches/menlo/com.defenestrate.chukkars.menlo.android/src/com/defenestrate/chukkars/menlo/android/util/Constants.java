package com.defenestrate.chukkars.menlo.android.util;

public interface Constants {
	String SERVER_DATA_PREFS_NAME = "all-players.json";
	String SIGNUP_CLOSED = "!!!SIGNUP_CLOSED!!!";

	String PAGE_INDEX_KEY = "PAGE_INDEX_KEY";
	String SIGNUP_DAY_KEY = "SIGNUP_DAY_KEY";
	String SELECTED_DAY_KEY = "SELECTED_DAY_KEY";
	String COVER_ART_KEY = "COVER_ART_KEY";
	String CONTENT_KEY = "CONTENT_KEY";
    String LAST_MODIFIED_KEY = "LAST_MODIFIED_KEY";
    String RESET_DATE_KEY = "RESET_DATE_KEY";
    String ACTIVE_DAYS_KEY = "ACTIVE_DAYS_KEY";
    String PLAYER_ID_KEY = "PLAYER_ID_KEY";
	String PLAYER_NAME_KEY = "PLAYER_NAME_KEY";
	String NUM_CHUKKARS_KEY = "NUM_CHUKKARS_KEY";
	String TITLE_RES_KEY = "TITLE_RES_KEY";
	String HAS_NETWORK_CONNECTIVITY_KEY = "HAS_NETWORK_CONNECTIVITY_KEY";

	//keys for the server urls to query
	String ADD_PLAYER_URL_KEY = "add_player_url";
	String EDIT_CHUKKARS_URL_KEY = "edit_chukkars_url";
	String QUERY_RESET_URL_KEY = "query_reset_url";
	String GET_ACTIVE_DAYS_URL_KEY = "get_active_days_url";
	String GET_PLAYERS_URL_KEY = "get_players_url";

	//fields in the server-returned JSON reply
    String TOTALS_LIST_FIELD = "_totalsList";
    String PLAYERS_LIST_FIELD ="_playersList";
    String CURR_PLAYER_PERSISTED_FIELD = "_currPersisted";
    String TOTAL_DAY_FIELD = "_day";
    String TOTAL_NUM_CHUKKARS_FIELD = "_numGameChukkars";
    String PLAYER_ID_FIELD = "_id";
    String PLAYER_NAME_FIELD = "_name";
    String PLAYER_NUMCHUKKARS_FIELD = "_numChukkars";
    String PLAYER_CREATEDATE_FIELD = "_createDate";
    String PLAYER_REQUESTDAY_FIELD = "_requestDay";
}