package com.defenestrate.chukkars.server.entity;

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
public class CronTask implements Serializable
{
	//////////////////////////////// CONSTANTS /////////////////////////////////
	static public final String RESET = "RESET";
	

	//////////////////////////// MEMBER VARIABLES //////////////////////////////
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long _id;
	
	@Column(name = "CronTask_Name")
	private String _name;
	
	@Column(name = "CronTask_RunDate")
	@Temporal(TemporalType.TIMESTAMP)
	private Date _runDate;
	
	
	/////////////////////////////// CONSTRUCTORS ///////////////////////////////
	public CronTask() 
	{
		_runDate = new Date();
	}

	public CronTask(String name) 
	{
		this();
		_name = name;
	}

	public String getName() 
	{
		return _name;
	}

	public Date getRunDate() 	
	{
		return _runDate;
	}
	
	public void setName(String name) 
	{
		_name = name;
	}
	
	public void setRunDateToNow()
	{
		_runDate = new Date();
	}
}