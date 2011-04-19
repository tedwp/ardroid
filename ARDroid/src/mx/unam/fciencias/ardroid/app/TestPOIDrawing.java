package mx.unam.fciencias.ardroid.app;

import android.location.Location;
import android.util.Log;

public class TestPOIDrawing {

	public static void testDrawPOI() {
		Location location = new Location("test");
		location.setLatitude(19.322339);
		location.setLongitude(-103.234767);
		POI p1 = new POI(location, null, "Oeste", "http://es.wikipedia.org/wiki/Oeste");

		Location location2 = new Location("test2");
		location2.setLatitude(27.322967);
		location2.setLongitude(-99.234767);
		POI p2 = new POI(location2, null, "Norte", "http://es.wikipedia.org/wiki/Norte");

		Location location3 = new Location("test3");
		location3.setLatitude(19.322339);
		location3.setLongitude(-96.534767);
		POI p3 = new POI(location3, null, "Este", "http://es.wikipedia.org/wiki/Este");

		Location location4 = new Location("test4");
		location4.setLatitude(19.022967);
		location4.setLongitude(-99.234767);
		Log.d("gps", "Location 4: "+location4.getLatitude()+", "+location4.getLongitude());
		POI p4 = new POI(location4, null, "Sur", "http://es.wikipedia.org/wiki/Sur");

		Location location5 = new Location("test");
		location5.setLatitude(19.322339);
		location5.setLongitude(-99.235229);
		POI p5 = new POI(location5, null, "Casa de Seliks", "http://www.google.com");

		Location location6 = new Location("test");
		location6.setLatitude(19.322967);
		location6.setLongitude(-99.234767);
		POI p6 = new POI(location6, null, "Casa 10", "http://www.google.com");

		Location location7 = new Location("test");
		location7.setLatitude(19.321592);
		location7.setLongitude(-99.253535);
		location7.setAltitude(2754);
		POI p7 = new POI(location7, null, "Cerro del Judío", "http://www.google.com");


		ARLayer.poiList.add(p1);
		ARLayer.poiList.add(p2);
		ARLayer.poiList.add(p3);
		ARLayer.poiList.add(p4);
		ARLayer.poiList.add(p5);
//		ARLayer.poiList.add(p6);
		ARLayer.poiList.add(p7);
	}
}
