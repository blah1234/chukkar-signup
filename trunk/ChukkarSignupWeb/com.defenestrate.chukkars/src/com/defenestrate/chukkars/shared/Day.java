package com.defenestrate.chukkars.shared;


public enum Day
{
	/**
	 * Use {@link #Day(int, boolean)} constructor below to set active days
	 * when testing with local Java App Engine datastore.
	 */
	//////////////////////////////// CONSTANTS /////////////////////////////////
	MONDAY(1),
	TUESDAY(2),
	WEDNESDAY(3),
	THURSDAY(4),
	FRIDAY(5),
	SATURDAY(6),
	SUNDAY(7);


	//////////////////////////// MEMBER VARIABLES //////////////////////////////
	private int _numRepresentation;
	private boolean _isEnabled;


	/////////////////////////////// CONSTRUCTORS ///////////////////////////////
	private Day(int numRepresentation)
	{
		_numRepresentation = numRepresentation;
		_isEnabled = false;
	}

	private Day(int numRepresentation, boolean isEnabled)
	{
		_numRepresentation = numRepresentation;
		_isEnabled = isEnabled;
	}


	///////////////////////////////// METHODS //////////////////////////////////
	static final public Day[] getAll()
	{
		return new Day[]
		{
			//must be returned in lexicographically ascending order!
			MONDAY,
			TUESDAY,
			WEDNESDAY,
			THURSDAY,
			FRIDAY,
			SATURDAY,
			SUNDAY
		};
	}

	static final public void disableAll()
	{
		for( Day currDay : getAll() )
		{
			currDay.setEnabled(false);
		}
	}

	public boolean isEnabled()
	{
		return _isEnabled;
	}

	public void setEnabled(boolean isEnabled)
	{
		_isEnabled = isEnabled;
	}

	public int getNumber()
	{
		return _numRepresentation;
	}
}
