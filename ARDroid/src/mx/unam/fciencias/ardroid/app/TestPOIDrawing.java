package mx.unam.fciencias.ardroid.app;

import android.location.Location;
import android.util.Log;

public class TestPOIDrawing {

    public static void testDrawPOI() {
        Location location;
        POI p;

        location = new Location("test");
        location.setLongitude(-99.18441742658615);
        location.setLatitude(19.314353010637802);
        p = new POI(location, null, "Sala Nezahualcóyotl", "");
        ARLayer.addPOI(p);

        location = new Location("test");
        location.setLongitude(-99.18540716171265);
        location.setLatitude(19.314616260349332);
        p = new POI(location, null, "MUAC", "");
        ARLayer.addPOI(p);

        location = new Location("test");
        location.setLongitude(-99.1826605796814);
        location.setLatitude(19.32608746097998);
        p = new POI(location, null, "Anexo de Ingeniería", "");
        ARLayer.addPOI(p);

        location = new Location("test");
        location.setLongitude(-99.1795814037323);
        location.setLatitude(19.325864726886373);
        p = new POI(location, null, "Instituto de Matamáticas", "");
        ARLayer.addPOI(p);

        location = new Location("test");
        location.setLongitude(-99.18085545301437);
        location.setLatitude(19.324490349568467);
        p = new POI(location, null, "Edificio P", "");
        ARLayer.addPOI(p);

        location = new Location("test");
        location.setLongitude(-99.18053895235062);
        location.setLatitude(19.324485287383858);
        p = new POI(location, null, "Edificio O", "");
        ARLayer.addPOI(p);

        location = new Location("test");
        location.setLongitude(-99.17996495962143);
        location.setLatitude(19.32499403615232);
        p = new POI(location, null, "Departamento de Física", "");
        ARLayer.addPOI(p);

        location = new Location("test");
        location.setLongitude(-99.17990058660507);
        location.setLatitude(19.32470549226914);
        p = new POI(location, null, "Departamento de Matemáticas", "");
        ARLayer.addPOI(p);

        location = new Location("test");
        location.setLongitude(-99.17903959751129);
        location.setLatitude(19.32379935991966);
        p = new POI(location, null, "Tlahuizcalpan", "");
        ARLayer.addPOI(p);

        location = new Location("test");
        location.setLongitude(-99.17901277542114);
        location.setLatitude(19.324847233187707);
        p = new POI(location, null, "Amoxcalli", "");
        ARLayer.addPOI(p);

        location = new Location("test");
        location.setLongitude(-99.17993545532227);
        location.setLatitude(19.33161521891654);
        p = new POI(location, null, "Factultad de Química", "");
        ARLayer.addPOI(p);

        location = new Location("test");
        location.setLongitude(-99.18532937765121);
        location.setLatitude(19.33015483584919);
        p = new POI(location, null, "Alberca Olímpica", "");
        ARLayer.addPOI(p);

        location = new Location("test");
        location.setLongitude(-99.18430745601654);
        location.setLatitude(19.334439770256417);
        p = new POI(location, null, "Factultad de Economía", "");
        ARLayer.addPOI(p);

        location = new Location("test");
        location.setLongitude(-99.192134141922);
        location.setLatitude(19.3319695559938);
        p = new POI(location, null, "Estadio Olímpico", "");
        ARLayer.addPOI(p);

        location = new Location("test");
        location.setLongitude(-99.18445765972137);
        location.setLatitude(19.33157978516653);
        p = new POI(location, null, "Factultad de Ingeniería", "");
        ARLayer.addPOI(p);

        location = new Location("test");
        location.setLongitude(-99.18601334095001);
        location.setLatitude(19.331893626684813);
        p = new POI(location, null, "Factultad de Arquitectura", "");
        ARLayer.addPOI(p);

        location = new Location("test");
        location.setLongitude(-99.18692529201508);
        location.setLatitude(19.331372244476388);
        p = new POI(location, null, "MUCA", "");
        ARLayer.addPOI(p);

        location = new Location("test");
        location.setLongitude(-99.18811082839966);
        location.setLatitude(19.33243525498334);
        p = new POI(location, null, "Rectoria", "");
        ARLayer.addPOI(p);

        location = new Location("test");
        location.setLongitude(-99.18732225894928);
        location.setLatitude(19.333422329972993);
        p = new POI(location, null, "Biblioteca Central", "");
        ARLayer.addPOI(p);

        location = new Location("test");
        location.setLongitude(-99.18561100959778);
        location.setLatitude(19.334272728256774);
        p = new POI(location, null, "Facultad de Derecho", "");
        ARLayer.addPOI(p);

        location = new Location("test");
        location.setLongitude(-99.18299853801727);
        location.setLatitude(19.33326541075809);
        p = new POI(location, null, "Torre de Humanidades", "");
        ARLayer.addPOI(p);


        /*	Location location = new Location("test");
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


          ARLayer.addPOI(p1);
          ARLayer.addPOI(p2);
          ARLayer.addPOI(p3);
          ARLayer.addPOI(p4);
          ARLayer.addPOI(p5);
  //		ARLayer.poiList.add(p6);
          ARLayer.addPOI(p7);*/
    }
}
