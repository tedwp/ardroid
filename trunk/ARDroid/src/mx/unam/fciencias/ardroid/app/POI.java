package mx.unam.fciencias.ardroid.app;

import android.graphics.*;
import android.location.Location;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

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

    private String infoUrl;

    private Paint paint1;
    private Paint paint2;
    private int poiHalfWidth;
    private int poiHalfHeight;

    private boolean isVisibleInRange = false;

    private RadialGradient rg;

    /**
     * Instantiates a new pOI.
     *
     * @param poiLocation    La ubiciación del POI
     * @param deviceLocation La ubicación del dispositivo
     * @param name           Nombre de este POI
     * @param infoUrl
     */
    public POI(Location poiLocation, Location deviceLocation, String name, String infoUrl) {
        super(Main.context);
        this.name = name;
        this.poiLocation = poiLocation;
        this.source = poiLocation.getProvider();
        this.infoUrl = infoUrl;
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
        paint1.setStyle(Paint.Style.STROKE);
        paint1.setStrokeWidth(6);
        paint2 = new Paint();
        paint2.setColor(Color.WHITE);
        paint2.setAntiAlias(true);
        paint2.setFakeBoldText(true);
        paint2.setTextSize(24);
    }

    public void updateValues() {
        if (poiLocation != null && POI.deviceLocation != null) {
            azimuth = POI.deviceLocation.bearingTo(poiLocation);
            if (azimuth < 0) {
                azimuth += 360;
            }
            distance = POI.deviceLocation.distanceTo(poiLocation);
            updateVisibilityInRange();
            Log.d("rang", name + " " + isVisibleInRange);
            setInclination();
        }
    }

    public void updateVisibilityInRange() {
        if (distance <= ARLayer.range) {
            isVisibleInRange = true;
        } else {
            isVisibleInRange = false;
        }
    }

    /**
     * Dibujamos este POI.
     *
     * @param canvas the canvas
     */
    @Override
    public void draw(Canvas canvas) {
        int x = getLeft() + poiHalfWidth;
        int y = getTop() + poiHalfHeight;
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
        if ((new Region(getLeft(), getTop(), getRight(), getBottom()))
                .contains((int) event.getX(), (int) event.getY())) {
            ARLayer.poiPopup.show(this);
            return true;
        }
        return false;
    }

    public void poiLayout(int left, int top, int right, int bottom) {
        this.layout(left - poiHalfWidth, top - poiHalfHeight, right + poiHalfWidth, +bottom + poiHalfHeight);
    }

    public float getDistance() {
        return distance;
    }

    public String getInfoUrl() {
        return infoUrl;
    }

    public boolean isVisibleInRange() {
        return isVisibleInRange;
    }
}
