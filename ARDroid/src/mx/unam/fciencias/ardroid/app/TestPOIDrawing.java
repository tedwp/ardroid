package mx.unam.fciencias.ardroid.app;

import android.location.Location;

public class TestPOIDrawing {

	public static void testDrawPOI() {
		Location location = new Location("test");
		location.setLatitude(19.322339);
		location.setLongitude(-99.235229);
		POI p1 = new POI(location, null);
		ARLayer.poiList.add(p1);
	}

}
