package mx.unam.fciencias.ardroid.app;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Creado por: Sebastián García Anderman
 * UNAM | Facultad de Ciencias | Ciencias de la Computación
 * Fecha: 19/10/10
 */
public class HttpConnection {

    /**
     * Sends a post request to the specified address with the corresponding message in the body
     *
     * @param address Where to send the post request
     * @param message Body of the post request
     * @return Server body response
     * @throws HttpResponseException In case the response status is other than 200
     */
    public static String sendPost(String address, String message) throws HttpResponseException {
        BufferedReader in = null;
        try {
            HttpClient client = new DefaultHttpClient();
            HttpPost request = new HttpPost(address);
            if (message != null) {
                StringEntity stringEntity = new StringEntity(message, "US-ASCII");
                request.setEntity(stringEntity);
            }
            HttpResponse response = client.execute(request);

            int responseStatus = response.getStatusLine().getStatusCode();

            Log.e("sendPost", "post response: " + responseStatus);

            switch (responseStatus) {
                case HttpStatus.SC_OK:
                    in = new BufferedReader(new InputStreamReader(response.getEntity()
                            .getContent()));
                    String result = in.readLine();
                    Log.d("sendPost", "post result: " + result);
                    return result;
                default:
                    throw new HttpResponseException(responseStatus, null);
            }
        } catch (ClientProtocolException e) {
            Log.d("sendPost", "exception in post clientprotocol:\n" + e);
            return null;
        } catch (UnsupportedEncodingException e) {
            Log.d("sendPost", "exception in post encoding:\n" + e);
            return null;
        } catch (IOException e) {
            Log.d("sendPost", "exception in post io:\n" + e);
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static String sendGet(String address) throws HttpResponseException {
        BufferedReader in = null;
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(address);
            HttpResponse response = client.execute(request);

            int responseStatus = response.getStatusLine().getStatusCode();
            Log.d("sendGet", "get response: " + responseStatus);

            switch (responseStatus) {
                case HttpStatus.SC_OK:
                    in = new BufferedReader(new InputStreamReader(response.getEntity()
                            .getContent()));
                    StringBuffer sb = new StringBuffer("");
                    String line;
                    String NL = System.getProperty("line.separator");
                    while ((line = in.readLine()) != null) {
                        sb.append(line).append(NL);
                    }
                    in.close();
                    String result = sb.toString();
                    Log.d("sendGet", "get result: " + result);
                    return result;
                default:
                    throw new HttpResponseException(responseStatus, null);
            }
        } catch (ClientProtocolException e) {
            Log.d("sendGet", "exception in get clientprotocol:\n" + e);
            return null;
        } catch (UnsupportedEncodingException e) {
            Log.d("sendGet", "exception in get encoding:\n" + e);
            return null;
        } catch (IOException e) {
            Log.d("sendGet", "exception in get io:\n" + e);
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static String sendDelete(String address) throws HttpResponseException {
        BufferedReader in = null;
        try {
            HttpClient client = new DefaultHttpClient();
            HttpDelete request = new HttpDelete(address);
            HttpResponse response = client.execute(request);

            int responseStatus = response.getStatusLine().getStatusCode();
            Log.e("sendDelete", "delete response: " + responseStatus);

            switch (responseStatus) {
                case HttpStatus.SC_OK:
                    in = new BufferedReader(new InputStreamReader(response.getEntity()
                            .getContent()));
                    String result = in.readLine();
                    Log.d("sendDelete", "delete result: " + result);
                    return result;
                default:
                    throw new HttpResponseException(responseStatus, null);
            }
        } catch (ClientProtocolException e) {
            Log.d("sendDelete", "exception in delete clientprotocol:\n" + e);
            return null;
        } catch (UnsupportedEncodingException e) {
            Log.d("sendDelete", "exception in delete encoding:\n" + e);
            return null;
        } catch (IOException e) {
            Log.d("sendDelete", "exception in delete io:\n" + e);
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Check if the device is online
     *
     * @return
     */
    public static boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) Main.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
