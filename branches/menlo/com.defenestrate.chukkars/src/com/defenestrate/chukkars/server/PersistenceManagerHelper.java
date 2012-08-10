package com.defenestrate.chukkars.server;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

public class PersistenceManagerHelper
{
	//////////////////////////////// CONSTANTS /////////////////////////////////
	private static final PersistenceManagerFactory PMF =
		JDOHelper.getPersistenceManagerFactory("transactions-optional");


	///////////////////////////////// METHODS //////////////////////////////////
	static public PersistenceManager getPersistenceManager() 
	{
		return PMF.getPersistenceManager();
	}
}
