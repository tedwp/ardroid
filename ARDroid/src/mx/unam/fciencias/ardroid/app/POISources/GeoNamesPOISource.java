package mx.unam.fciencias.ardroid.app.POISources;

import android.location.Location;
import android.util.Log;
import mx.unam.fciencias.ardroid.app.ARLayer;
import mx.unam.fciencias.ardroid.app.HttpConnection;
import mx.unam.fciencias.ardroid.app.POI;
import org.apache.http.client.HttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lander
 * Date: 19/04/11
 * Time: 22:20
 * To change this template use File | Settings | File Templates.
 */
public class GeoNamesPOISource extends POISource {

    private final String GET_ADDRESS = "http://ws.geonames.org/findNearbyWikipediaJSON?lat={lat}&lng={lng}&lang=es&radius=20&maxRows=100";

    @Override
    public void retrievePOIs(double latitude, double longitude) {
        String getAddress = GET_ADDRESS.replace("{lat}", new Double(latitude).toString());
        getAddress = getAddress.replace("{lng}", new Double(longitude).toString());
        Log.d("poiData", "Get: " + getAddress);
        try {
            String response = HttpConnection.sendGet(getAddress);
            Log.d("poiData", "Response: " + response);
            JSONObject responseJSON = new JSONObject(response);
            JSONArray poiJSONArray = responseJSON.getJSONArray("geonames");
            Log.d("poiData", "Se encontraron: " + poiJSONArray.length() + " POIs");
            for (int i = 0; i < poiJSONArray.length(); ++i) {
                Log.d("poiData", "Bajando poi #: " + i);
                JSONObject poiJSON = poiJSONArray.getJSONObject(i);
                createPOIAndAddToLis(poiJSON.getDouble("lat"), poiJSON.getDouble("lng"), "geonames", poiJSON.getString("title"), poiJSON.getString("wikipediaUrl"));
            }
        } catch (HttpResponseException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
