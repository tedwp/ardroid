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
 * Date: 22/04/11
 * Time: 12:44
 * To change this template use File | Settings | File Templates.
 */
public class TwitterPOISource extends POISource {
    private final String GET_ADDRESS = "http://search.twitter.com/search.json?geocode={lat},{lng},15km&rpp=200";

    @Override
    public void retrievePOIs(double latitude, double longitude) {
        String getAddress = replaceLatLongInAddress(GET_ADDRESS, latitude, longitude);
        try {
            String response = HttpConnection.sendGet(getAddress);
            Log.d("poiData", "Address: " + getAddress);
            Log.d("poiData", "Response: " + response);
            if (response == null)
                return;
            JSONObject responseJSON = new JSONObject(response);
            JSONArray poiJSONArray = responseJSON.getJSONArray("results");
            Log.d("poiData", "Se encontraron t: " + poiJSONArray.length() + " POIs");
            for (int i = 0; i < poiJSONArray.length(); ++i) {
                Log.d("poiData", "Bajando poi t #: " + i);
                JSONObject poiJSON = poiJSONArray.getJSONObject(i);
                if (!poiJSON.isNull("geo")) {
                    JSONObject geo = poiJSON.getJSONObject("geo");
                    JSONArray coord = geo.getJSONArray("coordinates");
                    double lat = coord.getInt(0);
                    double lng = coord.getInt(1);
                    String text = poiJSON.getString("text");
                    String url = "http://twitter.com/#!/" + poiJSON.getString("from_user") + "/status/" + poiJSON.getString("id_str");
                    Log.d("poiData", "Se encontro un POI con información de GEO");
                    createPOIAndAddToList(lat, lng, "twiiter", text, url);
                }
            }
            addPOIListToARLayer();
        } catch (HttpResponseException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
