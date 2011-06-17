package mx.unam.fciencias.ardroid.app.POISources;

import android.util.Log;
import mx.unam.fciencias.ardroid.app.HttpConnection;
import org.apache.http.client.HttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by IntelliJ IDEA.
 * User: lander
 * Date: 19/04/11
 * Time: 22:20
 * To change this template use File | Settings | File Templates.
 */
public class GeoNamesPOISource extends POISource {

    private final String GET_ADDRESS = "http://ws.geonames.org/findNearbyWikipediaJSON?lat={lat}&lng={lng}&lang=es&radius=20&maxRows=50";

    @Override
    public void retrievePOIs(double latitude, double longitude) {
        String getAddress = replaceLatLongInAddress(GET_ADDRESS, latitude, longitude);
        Log.d("poiData", "Get: " + getAddress);
        try {
            String response = HttpConnection.sendGet(getAddress);
            Log.d("poiData", "Response: " + response);
            if(response == null)
                return;
            Log.d("poiData", "Empezando a crear los objetos JSON");
            JSONObject responseJSON = new JSONObject(response);
            JSONArray poiJSONArray = responseJSON.getJSONArray("geonames");
            Log.d("poiData", "Se encontraron: " + poiJSONArray.length() + " POIs");
            for (int i = 0; i < poiJSONArray.length(); ++i) {
                Log.d("poiData", "Bajando poi #: " + i);
                JSONObject poiJSON = poiJSONArray.getJSONObject(i);
                createPOIAndAddToList(poiJSON.getDouble("lat"), poiJSON.getDouble("lng"), "geonames", poiJSON.getString("title"), "http://"+poiJSON.getString("wikipediaUrl"));
            }
            addPOIListToARLayer();
        } catch (HttpResponseException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
