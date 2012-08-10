package com.defenestrate.chukkars.client;

import com.google.gwt.user.client.ui.Button;

public class ButtonExtend extends Button
{
	//////////////////////////// MEMBER VARIABLES //////////////////////////////
	private int _row;
	private long _playerId;
	

	/////////////////////////////// CONSTRUCTORS ///////////////////////////////
	public ButtonExtend(String text, int row, long playerId)
	{
		super(text);
		_row = row;
		_playerId = playerId;
	}
	
	
	///////////////////////////////// METHODS //////////////////////////////////
	public int getRow()
	{
		return _row;
	}
	
	public void setRow(int row)
	{
		_row = row;
	}
	
	public long getPlayerId()
	{
		return _playerId;
	}
}
