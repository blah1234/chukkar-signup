package com.defenestrate.chukkars.android.entity;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class Player
{
	//////////////////////////// MEMBER VARIABLES //////////////////////////////
	private long _id;
	private String _name;
	private int _numChukkars;
	private Date _createDate;
	private Day _requestDay;

	
	/////////////////////////////// CONSTRUCTORS ///////////////////////////////
	public Player(long id, String name, int numChukkars, String createDate, String requestDay) 
	{
		_id = id;
		_name = name;
		_numChukkars = numChukkars;
		_requestDay = Day.valueOf(requestDay);
		
		try
		{
			_createDate = DateFormat.getDateTimeInstance().parse(createDate);
		}
		catch(ParseException e)
		{
			throw new RuntimeException(e);
		}
	}

	public long getId() 
	{
		return _id;
	}

	public String getName() 
	{
		return _name;
	}

	public int getChukkarCount() 
	{
		return _numChukkars;
	}

	public Date getCreateDate() 	
	{
		return _createDate;
	}
	
	public Day getRequestDay()
	{
		return _requestDay;
	}
}