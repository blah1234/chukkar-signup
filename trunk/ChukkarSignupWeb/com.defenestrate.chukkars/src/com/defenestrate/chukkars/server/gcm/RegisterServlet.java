package com.defenestrate.chukkars.server.gcm;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.defenestrate.chukkars.server.entity.DeviceDatastore;

/**
 * Servlet that registers a device, whose registration id is identified by
 * {@link #PARAMETER_REG_ID}.
 *
 * <p>
 * The client app should call this servlet everytime it receives a
 * {@code com.google.android.c2dm.intent.REGISTRATION C2DM} intent without an
 * error or {@code unregistered} extra.
 */
@SuppressWarnings("serial")
public class RegisterServlet extends BaseServlet {

	private static final String PARAMETER_REG_ID = "regId";

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException {
		String regId = getParameter(req, PARAMETER_REG_ID);
		DeviceDatastore.register(regId);
		setSuccess(resp);
	}

}
