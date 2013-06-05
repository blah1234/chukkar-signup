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

import com.flipthebird.gwthashcodeequals.EqualsBuilder;
import com.flipthebird.gwthashcodeequals.HashCodeBuilder;



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

	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}

		if(obj == this) {
			return true;
		}

		if( obj.getClass() != getClass() ) {
			return false;
		}

		Player rhs = (Player)obj;

		return new EqualsBuilder()
			.appendSuper(super.equals(obj))
	        .append(_id, rhs._id)
	        .append(_name, rhs._name)
	        .append(_numChukkars, rhs._numChukkars)
	        .append(_createDate, rhs._createDate)
	        .append(_requestDay, rhs._requestDay)
	        .isEquals();
	}

	public int hashCode() {
		// pick a hard-coded, randomly chosen, non-zero, odd number
	    // ideally different for each class
		return new HashCodeBuilder(17, 37).
			append(_id).
			append(_name).
			append(_numChukkars).
			append(_createDate).
			append(_requestDay).
			toHashCode();
	}
}