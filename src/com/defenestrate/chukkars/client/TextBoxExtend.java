package com.defenestrate.chukkars.client;

import com.google.gwt.user.client.ui.TextBox;

public class TextBoxExtend extends TextBox
{
	//////////////////////////// MEMBER VARIABLES //////////////////////////////
	private int _row;
	private int _col;
	private String _prevTxt;
	

	/////////////////////////////// CONSTRUCTORS ///////////////////////////////
	public TextBoxExtend()
	{
		resetPreviousData();
	}
	
	
	///////////////////////////////// METHODS //////////////////////////////////
	public void resetPreviousData()
	{
		_row = -1;
		_col = -1;
		_prevTxt = null;
	}
	
	public int getRow()
	{
		return _row;
	}
	
	public void setRow(int row)
	{
		_row = row;
	}
	
	public int getColumn()
	{
		return _col;
	}
	
	public void setColumn(int col)
	{
		_col = col;
	}
	
	public String getPreviousText()
	{
		return _prevTxt;
	}
	
	public void setPreviousText(String prevTxt)
	{
		_prevTxt = prevTxt;
	}
}
