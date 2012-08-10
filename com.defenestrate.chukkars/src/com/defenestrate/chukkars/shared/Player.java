package com.defenestrate.chukkars.shared;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;



@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Player implements Serializable
{
	//////////////////////////// MEMBER VARIABLES //////////////////////////////
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long _id;
	
	@Column(name = "Player_Name")
	private String _name;
	
	@Column(name = "Player_Chukkars")
	private Integer _numChukkars;
	
	@Column(name = "Player_CreateDate")
	@Temporal(TemporalType.TIMESTAMP)
	private Date _createDate;
	
	@Column(sqlType = "varchar(15)", name = "Player_RequestDay")
	private Day _requestDay;

	
	/////////////////////////////// CONSTRUCTORS ///////////////////////////////
	public Player() 
	{
		_createDate = new Date();
	}

	public Player(String name, Integer numChukkars, Day requestDay) 
	{
		this();
		_name = name;
		_numChukkars = numChukkars;
		_requestDay = requestDay;
	}

	public Long getId() 
	{
		return _id;
	}

	public String getName() 
	{
		return _name;
	}

	public Integer getChukkarCount() 
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

	public void setName(String name) 
	{
		_name = name;
	}

	public void setChukkarCount(Integer numChukkars) 
	{
		_numChukkars = numChukkars;
	}
	
	public void setRequestDay(Day requestDay)
	{
		_requestDay = requestDay;
	}
}