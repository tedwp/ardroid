package mx.unam.fciencias.ardroid.app;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Region;
import android.location.Location;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

// TODO: Auto-generated Javadoc

/**
 * Clase que representa un punto de interés (POI Point of Interest).
 *
 * @author Sebastián García Anderman
 */
public class POI extends View {

    /**
     * Ubicación del POI.
     */
    private Location poiLocation;

    /**
     * Ubicación del dispositivo.
     */
    public static Location deviceLocation;

    /**
     * distancia entre el dispositivo y el POI.
     */
    private float distance;

    /**
     * grados hacia el Este desde el Norte entre el dispositivo y el POI.
     */
    private float azimuth;

    /**
     * inclinación del dispositivo al POI, se obtiene con la altitud.
     */
    private float inclination = 0;

    /**
     * The name.
     */
    private String name;

    /**
     * The source.
     */
    private String source;

    private Paint paint1;
    private Paint paint2;
    private int poiHalfWidth;
    private int poiHalfHeight;

    /**
     * Instantiates a new pOI.
     *
     * @param poiLocation    La ubiciación del POI
     * @param deviceLocation La ubicación del dispositivo
     * @param name           Nombre de este POI
     */
    public POI(Location poiLocation, Location deviceLocation, String name) {
        super(Main.context);
        this.name = name;
        this.poiLocation = poiLocation;
        this.source = poiLocation.getProvider();
        if (POI.deviceLocation != null) {
            this.setDeviceLocation(deviceLocation);
        }
        updateValues();
        initPaint();
        calculateSize();
    }

    private void calculateSize() {
        //TODO: Change to final values
        poiHalfWidth = 25;
        poiHalfHeight = 25;
    }

    private void initPaint() {
        paint1 = new Paint();
        paint1.setColor(Color.RED);
        paint1.setAntiAlias(true);
        paint2 = new Paint();
        paint2.setColor(Color.WHITE);
        paint2.setAntiAlias(true);
        paint2.setFakeBoldText(true);
        paint2.setTextSize(24);
    }

    public void updateValues() {
        if (poiLocation != null && POI.deviceLocation != null) {
            azimuth = POI.deviceLocation.bearingTo(poiLocation);
            Log.d("gps", "poi: " + poiLocation.getLatitude() + " ," + poiLocation.getLongitude());
            Log.d("gps", "device: " + POI.deviceLocation.getLatitude() + " ," + POI.deviceLocation.getLongitude());
            Log.d("gps", "azumuth: " + name + " :" + azimuth);
            if (azimuth < 0) {
                azimuth += 360;
            }
            Log.d("gps", "azumuth: " + name + " :" + azimuth);
            distance = POI.deviceLocation.distanceTo(poiLocation);
            Log.d("gps", "distance: " + name + " :" + distance);
            setInclination();
        }
    }

    /**
     * Dibujamos este POI.
     *
     * @param canvas the canvas
     */
    @Override
    public void draw(Canvas canvas) {
        int x = getLeft()+poiHalfWidth;
        int y = getTop()+poiHalfHeight;
        canvas.drawCircle(x, y, 35, paint1);
        canvas.drawText(name, x - 35, y + 55, paint2);
        super.draw(canvas);
    }

    /**
     * Gets the azimuth.
     *
     * @return the azimuth
     */
    public float getAzimuth() {
        return azimuth;
    }

    /**
     * Gets the inclination.
     *
     * @return the inclination
     */
    public float getInclination() {
        return inclination;
    }

    /**
     * Calculamos la inclinación de este POI con respecto a la ubicación y
     * altitude del dispositivo
     */
    private void setInclination() {
        // Calculamos la inclinación a la que está el POI con respecto a nuestra
        // ubicación
        if (POI.deviceLocation.hasAltitude() && poiLocation.hasAltitude()) {
            double altitudeDiff;
            boolean negative = false;
            if (poiLocation.getAltitude() > POI.deviceLocation.getAltitude()) {
                altitudeDiff = poiLocation.getAltitude()
                        - POI.deviceLocation.getAltitude();
            } else {
                altitudeDiff = POI.deviceLocation.getAltitude()
                        - poiLocation.getAltitude();
                negative = true;
            }
            double coef = altitudeDiff / (double) distance;
            Log.d("gps2", "Coef: " + coef);
            if (negative) {
                inclination = (float) Math.toDegrees(Math.atan(coef)) * -1;
            } else {
                inclination = (float) Math.toDegrees(Math.atan(coef));
            }
            Log.d("gps2", "inc " + name + ": " + inclination);
        } else {
            // TODO: Checar si con esto la hago
            inclination = 0;
        }
    }

    /**
     * Sets the device location.
     *
     * @param deviceLocation the new device location
     */
    public void setDeviceLocation(Location deviceLocation) {
        POI.deviceLocation = deviceLocation;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("touch", "me pasaron el touchEvent: " + name);
        Log.d("touch", "region: " + getLeft() + ", " + getTop() + ", " + getRight() + ", " + getBottom());
        Log.d("touch", "event: " + (int) event.getX() + ", " + (int) event.getY());
        Log.d("touch", "Contiene: " + (new Region(getLeft(), getTop(), getRight(), getBottom()))
                .contains((int) event.getX(), (int) event.getY()));
        if ((new Region(getLeft(), getTop(), getRight(), getBottom()))
                .contains((int) event.getX(), (int) event.getY())) {
            Toast.makeText(Main.context, "me tocaste: " + name, Toast.LENGTH_SHORT).show();
            Log.d("touch", "tocado: " + name);
            return true;
        }
        return false;
    }

    public void poiLayout(int left, int top, int right, int bottom){
        this.layout(left-poiHalfWidth, top-poiHalfHeight, right+poiHalfWidth, +bottom+poiHalfHeight);
    }
}
