package com.defenestrate.chukkars.shared;

import java.util.Calendar;


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
	private Day()
	{
		//necessary for GWT serialization only
		_numRepresentation = 1;
		_isEnabled = false;
	}

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
	static final public Day valueOf(int numRepresentation)
	{
		switch(numRepresentation)
		{
		case 1:
			return MONDAY;
		case 2:
			return TUESDAY;
		case 3:
			return WEDNESDAY;
		case 4:
			return THURSDAY;
		case 5:
			return FRIDAY;
		case 6:
			return SATURDAY;
		case 7:
			return SUNDAY;
		default:
			throw new IllegalArgumentException(numRepresentation + " is not a valid number representation for the current list of defined Days.");
		}
	}

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

	public boolean isFirstGameDayOfTheWeek()
	{
		Day[] daysOfTheWeek = getAll();

		for(Day currDay : daysOfTheWeek) {
			if( currDay.isEnabled() ) {
				if(currDay.isBefore(this)) {
					return false;
				} else if(currDay == this) {
					return true;
				}
			}
		}

		return true;
	}

	private boolean isBefore(Day query)
	{
		return this._numRepresentation < query._numRepresentation;
	}

	private boolean isAfter(Day query)
	{
		return this._numRepresentation > query._numRepresentation;
	}

	public Day tomorrow()
	{
		switch(_numRepresentation)
		{
		case 1:
			return TUESDAY;
		case 2:
			return WEDNESDAY;
		case 3:
			return THURSDAY;
		case 4:
			return FRIDAY;
		case 5:
			return SATURDAY;
		case 6:
			return SUNDAY;
		case 7:
			return MONDAY;
		default:
			throw new IllegalArgumentException(_numRepresentation + " is not a valid number representation for the current list of defined Days.");
		}
	}

	public Day yesterday()
	{
		switch(_numRepresentation)
		{
		case 1:
			return SUNDAY;
		case 2:
			return MONDAY;
		case 3:
			return TUESDAY;
		case 4:
			return WEDNESDAY;
		case 5:
			return THURSDAY;
		case 6:
			return FRIDAY;
		case 7:
			return SATURDAY;
		default:
			throw new IllegalArgumentException(_numRepresentation + " is not a valid number representation for the current list of defined Days.");
		}
	}

	public int toJavaUtilCalendarDay() {
		switch(this)
		{
		case MONDAY:
			return Calendar.MONDAY;
		case TUESDAY:
			return Calendar.TUESDAY;
		case WEDNESDAY:
			return Calendar.WEDNESDAY;
		case THURSDAY:
			return Calendar.THURSDAY;
		case FRIDAY:
			return Calendar.FRIDAY;
		case SATURDAY:
			return Calendar.SATURDAY;
		case SUNDAY:
			return Calendar.SUNDAY;
		default:
			throw new IllegalArgumentException(this + " has not been defined yet in the current list of Days.");
		}
	}
}
