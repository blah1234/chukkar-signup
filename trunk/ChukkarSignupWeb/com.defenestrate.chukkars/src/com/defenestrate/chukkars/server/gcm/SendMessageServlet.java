package com.defenestrate.chukkars.server.gcm;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.defenestrate.chukkars.server.entity.DeviceDatastore;
import com.defenestrate.chukkars.server.listener.ApiKeyInitializer;
import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.InvalidRequestException;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

/**
 * Servlet that sends a message to a device.
 * <p>
 * This servlet is invoked by AppEngine's Push Queue mechanism.
 */
@SuppressWarnings("serial")
public class SendMessageServlet extends BaseServlet {

	private static final String HEADER_QUEUE_COUNT = "X-AppEngine-TaskRetryCount";
	private static final String HEADER_QUEUE_NAME = "X-AppEngine-QueueName";
	private static final int MAX_RETRY = 3;

	public static final String PARAMETER_DEVICE = "device";
	public static final String PARAMETER_MULTICAST = "multicastKey";

	//used to build the payload of the message to client
	/** Name of the Queue to send tasks for this servlet */
	public static final String GCM_QUEUE_NAME = "gcm";

	/** URL path for this servlet */
	public static final String SEND_URL = "/gcm/send";

	/** Use to reference the parameter for the GCM message collapse key
	 * http://developer.android.com/google/gcm/adv.html#lifetime */
	public static final String PARAMETER_COLLAPSE_KEY = "collapseKey";

	/** Use as the value of the GCM message collapse key */
	public static final String COLLAPSE_KEY_VALUE = "signup";

	/** Use to reference the parameter for a "message type" key in the GCM message payload data */
	public static final String PARAMETER_MESSAGE_TYPE = PayloadDataKey.MESSAGE_TYPE.toString();

	/** Use to reference the parameter for a "day of the week" key in the GCM message payload data */
	public static final String PARAMETER_REMINDER_DAY_OF_WEEK = PayloadDataKey.REMINDER_DAY_OF_WEEK.toString();



	private Sender sender;


	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		sender = newSender(config);
	}

	/**
	 * Creates the {@link Sender} based on the servlet settings.
	 */
	protected Sender newSender(ServletConfig config) {
		String key = (String) config.getServletContext().getAttribute(
				ApiKeyInitializer.ATTRIBUTE_ACCESS_KEY);
		return new Sender(key);
	}

	/**
	 * Indicates to App Engine that this task should be retried.
	 */
	private void retryTask(HttpServletResponse resp) {
		resp.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
	}

	/**
	 * Indicates to App Engine that this task is done.
	 */
	private void taskDone(HttpServletResponse resp) {
		resp.setStatus(HttpURLConnection.HTTP_OK);
	}

	/**
	 * Processes the request to add a new message.
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		if (req.getHeader(HEADER_QUEUE_NAME) == null) {
			throw new IOException("Missing header " + HEADER_QUEUE_NAME);
		}

		String retryCountHeader = req.getHeader(HEADER_QUEUE_COUNT);
		logger.fine("retry count: " + retryCountHeader);

		if (retryCountHeader != null) {
			int retryCount = Integer.parseInt(retryCountHeader);
			if (retryCount > MAX_RETRY) {
				logger.severe("Too many retries, dropping task");
				taskDone(resp);
				return;
			}
		}

		String regId = req.getParameter(PARAMETER_DEVICE);
		if (regId != null) {
			Map<PayloadDataKey, Object> payloadData = buildPayload(req);
			String collapseKey = extractCollapseKey(req);
			sendSingleMessage(regId, payloadData, collapseKey, resp);
			return;
		}

		String multicastKey = req.getParameter(PARAMETER_MULTICAST);
		if (multicastKey != null) {
			Map<PayloadDataKey, Object> payloadData = buildPayload(req);
			String collapseKey = extractCollapseKey(req);
			sendMulticastMessage(multicastKey, payloadData, collapseKey, resp);
			return;
		}

		logger.severe("Invalid request!");
		taskDone(resp);
		return;
	}

	private Map<PayloadDataKey, Object> buildPayload(HttpServletRequest req) {
		Map<PayloadDataKey, Object> ret = null;
		String msgType = req.getParameter(PARAMETER_MESSAGE_TYPE);

		if(msgType != null) {
			ret = new HashMap<PayloadDataKey, Object>();
			ret.put(PayloadDataKey.MESSAGE_TYPE, msgType);

			String reminderDay = req.getParameter(PARAMETER_REMINDER_DAY_OF_WEEK);

			if(reminderDay != null) {
				ret.put(PayloadDataKey.REMINDER_DAY_OF_WEEK, reminderDay);
			}
		} else {
			logger.fine("No param: " + PARAMETER_MESSAGE_TYPE + ". No message payload data created.");
		}

		return ret;
	}

	private String extractCollapseKey(HttpServletRequest req) {
		String key = req.getParameter(PARAMETER_COLLAPSE_KEY);
		return key;
	}

	private void sendSingleMessage(String regId,
								   Map<PayloadDataKey, Object> payloadData,
								   String collapseKey,
								   HttpServletResponse resp) {
		logger.info("Sending message to device " + regId);

		Message.Builder b = new Message.Builder();

		if(payloadData != null) {
			for( Entry<PayloadDataKey, Object> currEntry : payloadData.entrySet() ) {
				b.addData( currEntry.getKey().toString(), currEntry.getValue().toString() );
			}
		}

		if(collapseKey != null) {
			b.collapseKey(collapseKey);
		}

		Message message = b.build();

		Result result;

		try {
			result = sender.sendNoRetry(message, regId);
		} catch(InvalidRequestException e) {
			int statusCode = e.getHttpStatusCode();

			//Can retry if error in 500-599 range:
			//http://developer.android.com/google/gcm/gcm.html#response
			if(statusCode >= HttpURLConnection.HTTP_INTERNAL_ERROR && statusCode < 600) {
				result = null;
				logger.fine("Received from server error code: " + statusCode + ". Retrying...");
			} else {
				logger.log(Level.SEVERE, "Exception posting " + message, e);
				taskDone(resp);
				return;
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Exception posting " + message, e);
			taskDone(resp);
			return;
		}


		if (result == null) {
			retryTask(resp);
			return;
		}

		if (result.getMessageId() != null) {
			logger.info("Succesfully sent message to device " + regId);
			String canonicalRegId = result.getCanonicalRegistrationId();
			if (canonicalRegId != null) {
				// same device has more than on registration id: update it
				logger.finest("canonicalRegId " + canonicalRegId);
				DeviceDatastore.updateRegistration(regId, canonicalRegId);
			}
		} else {
			String error = result.getErrorCodeName();
			if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
				// application has been removed from device - unregister it
				DeviceDatastore.unregister(regId);
			} else if(error.equals(Constants.ERROR_MISMATCH_SENDER_ID)) {
				//If (usually during development) you switch to a different
				//sender, the existing registration IDs won't work. Unregister
				//http://developer.android.com/google/gcm/gcm.html#response
				DeviceDatastore.unregister(regId);
			} else {
				logger.severe("Error sending message to device " + regId + ": "
						+ error);
			}
		}
	}

	private void sendMulticastMessage(String multicastKey,
									  Map<PayloadDataKey, Object> payloadData,
									  String collapseKey,
									  HttpServletResponse resp) {
		// Recover registration ids from datastore
		List<String> regIds = DeviceDatastore.getMulticast(multicastKey);

		Message.Builder b = new Message.Builder();

		if(payloadData != null) {
			for( Entry<PayloadDataKey, Object> currEntry : payloadData.entrySet() ) {
				b.addData( currEntry.getKey().toString(), currEntry.getValue().toString() );
			}
		}

		if(collapseKey != null) {
			b.collapseKey(collapseKey);
		}

		Message message = b.build();

		MulticastResult multicastResult;
		boolean allDone = true;

		try {
			multicastResult = sender.sendNoRetry(message, regIds);
		} catch(InvalidRequestException e) {
			int statusCode = e.getHttpStatusCode();

			//Can retry if error in 500-599 range:
			//http://developer.android.com/google/gcm/gcm.html#response
			if(statusCode >= HttpURLConnection.HTTP_INTERNAL_ERROR && statusCode < 600) {
				allDone = false;
				multicastResult = null;
				logger.fine("Received from server error code: " + statusCode + ". Retrying...");
			} else {
				logger.log(Level.SEVERE, "Exception posting " + message, e);
				multicastDone(resp, multicastKey);
				return;
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Exception posting " + message, e);
			multicastDone(resp, multicastKey);
			return;
		}


		// check if any registration id must be updated
		if (multicastResult != null && multicastResult.getCanonicalIds() != 0) {
			List<Result> results = multicastResult.getResults();
			for (int i = 0; i < results.size(); i++) {
				String canonicalRegId = results.get(i)
						.getCanonicalRegistrationId();
				if (canonicalRegId != null) {
					String regId = regIds.get(i);
					DeviceDatastore.updateRegistration(regId, canonicalRegId);
				}
			}
		}

		if (multicastResult != null && multicastResult.getFailure() != 0) {
			// there were failures, check if any could be retried
			List<Result> results = multicastResult.getResults();
			List<String> retriableRegIds = new ArrayList<String>();
			for (int i = 0; i < results.size(); i++) {
				String error = results.get(i).getErrorCodeName();
				if (error != null) {
					String regId = regIds.get(i);
					logger.warning("Got error (" + error + ") for regId "
							+ regId);
					if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
						// application has been removed from device - unregister
						// it
						DeviceDatastore.unregister(regId);
					} else if(error.equals(Constants.ERROR_MISMATCH_SENDER_ID)) {
						//If (usually during development) you switch to a different
						//sender, the existing registration IDs won't work. Unregister
						//http://developer.android.com/google/gcm/gcm.html#response
						DeviceDatastore.unregister(regId);
					} else if (error.equals(Constants.ERROR_UNAVAILABLE)) {
						retriableRegIds.add(regId);
					}
				}
			}

			if (!retriableRegIds.isEmpty()) {
				// update task
				DeviceDatastore.updateMulticast(multicastKey, retriableRegIds);
				allDone = false;
				retryTask(resp);
			}
		}


		if (allDone) {
			multicastDone(resp, multicastKey);
		} else {
			retryTask(resp);
		}
	}

	private void multicastDone(HttpServletResponse resp, String encodedKey) {
		DeviceDatastore.deleteMulticast(encodedKey);
		taskDone(resp);
	}


	//////////////////////////// INNER CLASSES /////////////////////////////////
	private enum PayloadDataKey {
		MESSAGE_TYPE,
		/** Day of the week the signup reminder is for */
		REMINDER_DAY_OF_WEEK
	}

	static public enum MessageType {
		/** The signup notice that goes out at the start of the week */
		SIGNUP_NOTICE,
		/** The signup reminder that goes out on the cutoff day */
		SIGNUP_REMINDER
	}
}
