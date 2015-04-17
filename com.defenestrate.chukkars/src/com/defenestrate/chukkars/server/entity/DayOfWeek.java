package com.defenestrate.chukkars.server.entity;

import java.io.Serializable;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.defenestrate.chukkars.server.listener.ServerStartupListener;
import com.defenestrate.chukkars.shared.Day;

/**
 * Helper class that persists the Day enum.
 * @see {@link ServerStartupListener}
 * @see {@link Day}
 * @author shwang
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class DayOfWeek implements Serializable
{
	//////////////////////////// MEMBER VARIABLES //////////////////////////////
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long _id;
	
	@Column(name = "DayOfWeek_Name")
	private String _name;
	
	@Column(name = "DayOfWeek_Number")
	private int _numRepresentation;
	
	@Column(name = "DayOfWeek_Enabled")
	private boolean _isEnabled;
	

	/////////////////////////////// CONSTRUCTORS ///////////////////////////////
	public DayOfWeek(String name, int number, boolean isEnabled)
	{
		_name = name;
		_numRepresentation = number;
		_isEnabled = isEnabled;
	}
	

	///////////////////////////////// METHODS //////////////////////////////////
	public boolean isEnabled()
	{
		return _isEnabled;
	}
	
	public void setEnabled(boolean isEnabled)
	{
		_isEnabled = isEnabled;
	}

	public String getName()
	{
		return _name;
	}
}
