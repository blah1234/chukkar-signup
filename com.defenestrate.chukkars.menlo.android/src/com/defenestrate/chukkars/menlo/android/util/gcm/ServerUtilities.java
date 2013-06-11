package com.defenestrate.chukkars.menlo.android.util.gcm;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import android.content.Context;
import android.util.Log;

import com.defenestrate.chukkars.menlo.android.util.Constants;
import com.defenestrate.chukkars.menlo.android.util.PropertiesUtil;
import com.google.android.gcm.GCMRegistrar;

/**
 * Helper class used to communicate Google Cloud Messaging credentials with the server.
 */
public final class ServerUtilities implements Constants {

	/////////////////////////////// CONSTANTS //////////////////////////////////
    private static final int MAX_ATTEMPTS = 5;
    private static final int BACKOFF_MILLI_SECONDS = 2000;

    private static final String LOG_TAG = ServerUtilities.class.getSimpleName();


	/////////////////////////// MEMBER VARIABLES ///////////////////////////////
    private static final Random random = new Random();


	//////////////////////////////// METHODS ///////////////////////////////////
    /**
     * Register this account/device pair within the server.
     *
     * @return whether the registration succeeded or not.
     */
    public static boolean register(final Context context, final String regId) {
        Log.i(LOG_TAG, "registering device (regId = " + regId + ")");

        String serverUrl = PropertiesUtil.getURLProperty(context.getResources(), GCM_REGISTER_URL);
        Map<String, String> params = new HashMap<String, String>();
        params.put(GCM_REG_ID_PARAMETER, regId);
        long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);

        // Once GCM returns a registration id, we need to register it in the
        // demo server. As the server might be down, we will retry it a couple
        // times.
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            Log.d(LOG_TAG, "Attempt #" + i + " to register");

            try {
                post(serverUrl, params);
                GCMRegistrar.setRegisteredOnServer(context, true);
                return true;
            } catch (IOException e) {
                // Here we are simplifying and retrying on any error; in a real
                // application, it should retry only on unrecoverable errors
                // (like HTTP error code 503).
                Log.e(LOG_TAG, "Failed to register on attempt " + i, e);

                if (i == MAX_ATTEMPTS) {
                    break;
                }

                try {
                    Log.d(LOG_TAG, "Sleeping for " + backoff + " ms before retry");
                    Thread.sleep(backoff);
                } catch (InterruptedException e1) {
                    // Activity finished before we complete - exit.
                    Log.d(LOG_TAG, "Thread interrupted: abort remaining retries!");
                    Thread.currentThread().interrupt();
                    return false;
                }

                // increase backoff exponentially
                backoff *= 2;
            }
        }

        return false;
    }

    /**
     * Unregister this account/device pair within the server.
     */
    static void unregister(final Context context, final String regId) {
        Log.i(LOG_TAG, "unregistering device (regId = " + regId + ")");

        String serverUrl = PropertiesUtil.getURLProperty(context.getResources(), GCM_UNREGISTER_URL);
        Map<String, String> params = new HashMap<String, String>();
        params.put(GCM_REG_ID_PARAMETER, regId);

        try {
            post(serverUrl, params);
            GCMRegistrar.setRegisteredOnServer(context, false);
        } catch (IOException e) {
            // At this point the device is unregistered from GCM, but still
            // registered in the server.
            // We could try to unregister again, but it is not necessary:
            // if the server tries to send a message to the device, it will get
            // a "NotRegistered" error message and should unregister the device.
        }
    }

    /**
     * Issue a POST request to the server.
     *
     * @param endpoint POST address.
     * @param params request parameters.
     *
     * @throws IOException propagated from POST.
     */
    private static void post(String endpoint, Map<String, String> params) throws IOException {
        URL url;

        try {
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }

        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Entry<String, String>> iterator = params.entrySet().iterator();

        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=').append(param.getValue());

            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }

        String body = bodyBuilder.toString();
        Log.v(LOG_TAG, "Posting '" + body + "' to " + url);
        byte[] bytes = body.getBytes();
        HttpURLConnection conn = null;

        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

            // post the request
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();

            // handle the response
            int status = conn.getResponseCode();

            if (status != HttpURLConnection.HTTP_OK) {
              throw new IOException("Post failed with error code " + status);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
