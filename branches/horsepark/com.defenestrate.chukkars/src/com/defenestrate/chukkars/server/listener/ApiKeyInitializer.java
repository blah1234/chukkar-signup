package com.defenestrate.chukkars.server.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * Context initializer that loads the Google Play Services API key from the App Engine datastore.
 */
public class ApiKeyInitializer implements ServletContextListener {

	/////////////////////////////// CONSTANTS //////////////////////////////////
	static public final String ATTRIBUTE_ACCESS_KEY = "googlePlayServicesAPIKey";
	static private final String API_KEY_VALUE = "AIzaSyAn7EjT1zO2Cpo3puep4G8GJuV3N8z7Fh8";

	/** The kind of key for the Datastore Key: Settings */
	private static final String SETTINGS_ENTITY_KIND = "Settings";
	private static final String ENTITY_KEY = "SenderAuthToken";
	private static final String ACCESS_KEY_FIELD = "ApiKey";


	//////////////////////////////// METHODS ///////////////////////////////////
	@Override
	public void contextInitialized(ServletContextEvent event) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key key = KeyFactory.createKey(SETTINGS_ENTITY_KIND, ENTITY_KEY);
		Entity entity;

		try {
			entity = datastore.get(key);
		} catch (EntityNotFoundException e) {
			entity = new Entity(key);

			//put it in the datastore
		    entity.setProperty(ACCESS_KEY_FIELD, API_KEY_VALUE);
			datastore.put(entity);
		}

		String accessKey = (String) entity.getProperty(ACCESS_KEY_FIELD);
		event.getServletContext().setAttribute(ATTRIBUTE_ACCESS_KEY, accessKey);
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {}
}
