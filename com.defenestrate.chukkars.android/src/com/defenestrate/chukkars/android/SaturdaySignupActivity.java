package com.defenestrate.chukkars.android;

import com.defenestrate.chukkars.android.entity.Day;

import android.os.Bundle;



public class SaturdaySignupActivity extends SignupActivity 
{
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        loadPlayers(Day.SATURDAY);
    }
}