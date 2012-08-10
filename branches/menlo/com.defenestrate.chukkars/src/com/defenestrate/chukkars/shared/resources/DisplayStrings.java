package com.defenestrate.chukkars.shared.resources;

import com.google.gwt.i18n.client.Constants;

public interface DisplayStrings extends Constants {
	/**
	 * Returns the abbreviation for the polo club (e.g., HPPC or MPC)
	 */
	String clubAbbreviation();

	/**
	 * Returns the polo club's email list address
	 */
  	String clubListEmail();

  	/**
  	 * Returns the number of players that make up a chukkar.
  	 */
  	int playersPerChukkar();

  	/**
  	 * Returns the minimum number of players that make up a chukkar.
  	 */
  	int minPlayersPerChukkar();

  	/**
  	 * Indicates whether or not the application should enforce a cutoff
  	 * time for signups.
  	 * @return <code>true</code> if the application should enforce a cutoff
  	 * time for signups; <code>false</code> otherwise.
  	 */
  	boolean enableSignupCutoff();
}
