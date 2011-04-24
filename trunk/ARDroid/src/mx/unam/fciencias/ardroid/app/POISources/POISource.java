package mx.unam.fciencias.ardroid.app.POISources;

import android.location.Location;
import mx.unam.fciencias.ardroid.app.ARLayer;
import mx.unam.fciencias.ardroid.app.POI;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: lander
 * Date: 19/04/11
 * Time: 22:09
 * To change this template use File | Settings | File Templates.
 */
public abstract class POISource {

    ArrayList<POI> poiList = new ArrayList<POI>();


    public abstract void retrievePOIs(double latitude, double longitude);

    protected void createPOIAndAddToList(double latitude, double longitude, String provider, String title, String url) {
        Location location = new Location(provider);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        POI poi = new POI(location, ARLayer.currentLocation, title, url);
        poiList.add(poi);
    }

    protected void addPOIListToARLayer() {
        if (poiList.size() > 0) {
            ARLayer.addPOIList(poiList);
        }
    }

    protected String replaceLatLongInAddress(String address, double latitude, double longitude) {
        String getAddress = address.replace("{lat}", new Double(latitude).toString());
        return getAddress.replace("{lng}", new Double(longitude).toString());
    }
}
