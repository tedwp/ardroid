package mx.unam.fciencias.ardroid.app.POISources;

import android.location.Location;
import mx.unam.fciencias.ardroid.app.ARLayer;
import mx.unam.fciencias.ardroid.app.POI;

/**
 * Created by IntelliJ IDEA.
 * User: lander
 * Date: 19/04/11
 * Time: 22:09
 * To change this template use File | Settings | File Templates.
 */
public abstract class POISource {

    public abstract void retrievePOIs(double latitude, double longitude);

    protected void createPOIAndAddToLis(double latitude, double longitude, String provider, String title, String url) {
        Location location = new Location(provider);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        POI poi = new POI(location, ARLayer.currentLocation, title, url);
        ARLayer.poiList.add(poi);
    }
}
