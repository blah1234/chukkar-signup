package com.defenestrate.chukkars.server.gcm;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.defenestrate.chukkars.server.entity.DeviceDatastore;
import com.defenestrate.chukkars.server.gcm.SendMessageServlet.MessageType;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

/**
 * Servlet that adds a new message to all registered devices.
 * <p>
 * This servlet is used just by the browser (i.e., not device).
 */
@SuppressWarnings("serial")
public class SendAllMessagesServlet extends BaseServlet {

	/** The kind of key for the Datastore Key: Test */
	private static final String TEST_ENTITY_KIND = "Test";
	private static final String TEST_GCM_SEND_MESSAGE_TYPE_ENTITY_KEY = "GCMSendMessageType";
	private static final String TEST_ENTITY_VALUE_FIELD = "TestValue";



	/**
	 * Processes the request to add a new message.
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		//get the test message type from the datastore
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key key = KeyFactory.createKey(TEST_ENTITY_KIND, TEST_GCM_SEND_MESSAGE_TYPE_ENTITY_KEY);
		Entity entity;

		try {
			entity = datastore.get(key);
		} catch (EntityNotFoundException e) {
			entity = new Entity(key);

			//put it in the datastore
		    entity.setProperty(TEST_ENTITY_VALUE_FIELD, MessageType.SIGNUP_NOTICE.toString());
			datastore.put(entity);
		}

		MessageType sendMessageType = MessageType.valueOf( (String)entity.getProperty(TEST_ENTITY_VALUE_FIELD) );

		//----------

		List<String> devices = DeviceDatastore.getDevices();
		String status;

		if (devices.isEmpty()) {
			status = "Message ignored as there is no device registered!";
		} else {
			Queue queue = QueueFactory.getQueue(SendMessageServlet.GCM_QUEUE_NAME);
			// NOTE: check below is for demonstration purposes; a real
			// application
			// could always send a multicast, even for just one recipient
			if (devices.size() == 1) {
				String device = devices.get(0);

				// send a single message using plain post
				TaskOptions opts = TaskOptions.Builder.withUrl(SendMessageServlet.SEND_URL)
					.param(SendMessageServlet.PARAMETER_DEVICE, device)
					.param(SendMessageServlet.PARAMETER_COLLAPSE_KEY, SendMessageServlet.COLLAPSE_KEY_VALUE)
					.method(Method.POST);

				if(sendMessageType == MessageType.SIGNUP_NOTICE) {
					opts.param(SendMessageServlet.PARAMETER_MESSAGE_TYPE, MessageType.SIGNUP_NOTICE.toString());
					opts.param(SendMessageServlet.PARAMETER_REMINDER_DAY_OF_WEEK, Integer.toString(Calendar.TUESDAY));
				} else if(sendMessageType == MessageType.SIGNUP_REMINDER) {
					opts.param(SendMessageServlet.PARAMETER_MESSAGE_TYPE, MessageType.SIGNUP_REMINDER.toString());
				}

				queue.add(opts);
				status = "Single message queued for registration id " + device;
			} else {
				// send a multicast message using JSON
				// must split in chunks of 1000 devices (GCM limit)
				int total = devices.size();
				List<String> partialDevices = new ArrayList<String>(total);
				int counter = 0;
				int tasks = 0;

				for (String device : devices) {
					counter++;
					partialDevices.add(device);
					int partialSize = partialDevices.size();

					if(partialSize == DeviceDatastore.MULTICAST_SIZE || counter == total) {
						String multicastKey = DeviceDatastore.createMulticast(partialDevices);

						logger.fine("Queuing " + partialSize + " devices on multicast " + multicastKey);

						TaskOptions taskOptions = TaskOptions.Builder.withUrl(SendMessageServlet.SEND_URL)
							.param(SendMessageServlet.PARAMETER_MULTICAST, multicastKey)
							.param(SendMessageServlet.PARAMETER_COLLAPSE_KEY, SendMessageServlet.COLLAPSE_KEY_VALUE)
							.method(Method.POST);

						if(sendMessageType == MessageType.SIGNUP_NOTICE) {
							taskOptions.param(SendMessageServlet.PARAMETER_MESSAGE_TYPE, MessageType.SIGNUP_NOTICE.toString());
							taskOptions.param(SendMessageServlet.PARAMETER_REMINDER_DAY_OF_WEEK, Integer.toString(Calendar.TUESDAY));
						} else if(sendMessageType == MessageType.SIGNUP_REMINDER) {
							taskOptions.param(SendMessageServlet.PARAMETER_MESSAGE_TYPE, MessageType.SIGNUP_REMINDER.toString());
						}

						queue.add(taskOptions);
						partialDevices.clear();
						tasks++;
					}
				}

				status = "Queued tasks to send " + tasks
						+ " multicast messages to " + total + " devices";
			}
		}

		//------------------

		resp.setContentType("text/plain;charset=UTF-8");
		PrintWriter charWriter = null;

		try
		{
			charWriter = resp.getWriter();
			charWriter.write(status);

			//commit the response
			charWriter.flush();
		}
		catch (IOException e)
		{
			logger.log(
				Level.SEVERE,
				"Error encountered trying to write to the ServletResponse:\n" + status + "\n\n" + e.getMessage(),
				e);

			//display stack trace to browser
			throw new ServletException(e);
		}
		finally
		{
			if(charWriter != null)
			{
				charWriter.close();
			}
		}
	}
}
