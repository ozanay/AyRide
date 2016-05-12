package com.iride.ayride;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class GcmSender {

    private final static String API_KEY = "AIzaSyD58RSGPu5NBUL4O6Qy3SZH-dyUjcGAdyk";
    private final static String loggerTag = GcmSender.class.getSimpleName();

    public static void send(final String message, final String to, final Ride ride) {

        // Prepare JSON containing the GCM message content. What to send and where to send.
        if (message == null || message.trim().equals("")) {
            Log.d(loggerTag, "Message Is Null");
            return;
        }

        if (to == null || to.trim().equals("")) {
            Log.d(loggerTag, "TO is NULL!");
            return;
        }

        if (ride == null) {
            Log.d(loggerTag, "Ride is NULL");
            return;
        }


        new AsyncTask() {
            @Override
            protected Void doInBackground(Object... params) {
                try {
                    JSONObject jGcmData = new JSONObject();
                    JSONObject jData = new JSONObject();
                    jData.put("message", message);
                    jData.put("pedestrianId", ride.getPedestrianId());
                    jData.put("pedestrianInstanceId", ride.getPedestrianInstanceId());
                    jData.put("pedestrianName", ride.getPedestrianName());
                    jData.put("pedestrianSurname", ride.getPedestrianSurName());
                    // Where to send GCM message.
                    jGcmData.put("to", to);
                    // What to send in GCM message.
                    jGcmData.put("data", jData);

                    // Create connection to send GCM Message request.
                    URL url = new URL("https://android.googleapis.com/gcm/send");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("Authorization", "key=" + API_KEY);
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);

                    // Send GCM message content.
                    OutputStream outputStream = conn.getOutputStream();
                    outputStream.write(jGcmData.toString().getBytes());

                    // Read GCM response.
                    InputStream inputStream = conn.getInputStream();
                    String resp = IOUtils.toString(inputStream);
                    return null;
                } catch (MalformedURLException e) {
                    Log.e(loggerTag, e.getMessage());
                } catch (ProtocolException e) {
                    Log.e(loggerTag, e.getMessage());
                } catch (JSONException e) {
                    Log.e(loggerTag, e.getMessage());
                } catch (IOException e) {
                    Log.e(loggerTag, e.getMessage());
                }
                return null;
            }
        }.execute();
    }
}
