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

    private Paint circlePaint;
    private Paint textPaint;
    private int poiHalfWidth;
    private int poiHalfHeight;

    private boolean isVisibleInRange = false;
    private boolean isVisibleFromCollisions = true;

    private RadialGradient rg;

    private final int NAME_MAX_LENGTH = 23;

    private int leftTextBound;
    private int rightTextBound;
    private int textLengthHalf;

    public int l, r, t, b;
    private int x, y;

    public int collisionCounter = 0;

    private final int PIXELS_TO_MOVE_UP_IN_COLLISION = 90;

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
        if (name.length() > NAME_MAX_LENGTH) {
            this.name = name.substring(0, NAME_MAX_LENGTH) + "..";
        } else {
            this.name = name;
        }
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
        circlePaint = new Paint();
        circlePaint.setColor(Color.RED);
        circlePaint.setAntiAlias(true);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(6);
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);
        textPaint.setFakeBoldText(true);
        textPaint.setTextSize(18);
        Rect rect = new Rect();
        //Obtenemos la frontera del texto a la derecha e izquierda para poder posicionarlo
        //centrado respecto al círculo.
        textPaint.getTextBounds(name, 0, name.length(), rect);
        leftTextBound = rect.left;
        rightTextBound = rect.right;
        textLengthHalf = Math.abs(rightTextBound - leftTextBound) / 2;
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
        canvas.drawCircle(x, y, 35, circlePaint);
        canvas.drawText(name, l, y + 55, textPaint);
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
            //TODO: Se puede simplificar con valor absoluto
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
        l = left - textLengthHalf;
        r = left + textLengthHalf;
        t = top - 18;
        b = top + 55;
        x = left;
        y = top;
        this.layout(left - poiHalfWidth, top - poiHalfHeight, right + poiHalfWidth, +bottom + poiHalfHeight);
    }

    public void movePOIUp() {
        l = x - textLengthHalf;
        r = x + textLengthHalf;
        t = y - 18 - PIXELS_TO_MOVE_UP_IN_COLLISION;
        b = y + 55 - PIXELS_TO_MOVE_UP_IN_COLLISION;
        this.layout(x - poiHalfHeight, y - poiHalfHeight - PIXELS_TO_MOVE_UP_IN_COLLISION, x + poiHalfWidth, y + poiHalfHeight - PIXELS_TO_MOVE_UP_IN_COLLISION);
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

    public boolean isVisibleFromCollisions() {
        return isVisibleFromCollisions;
    }

    public void setIsVisibleFromCollisions(boolean b) {
        isVisibleFromCollisions = b;
    }
}
