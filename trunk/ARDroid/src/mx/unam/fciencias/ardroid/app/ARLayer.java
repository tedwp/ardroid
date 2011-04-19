package mx.unam.fciencias.ardroid.app;

import android.content.Context;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc

/**
 * Clase que define la capa de realidad aumentada.
 *
 * @author Sebastián García Anderman
 */
public class ARLayer extends View {

    /**
     * The direction.
     */
    private float direction;

    /**
     * The inclination.
     */
    private float inclination;

    /**
     * The current location.
     */
    private Location currentLocation;

    /**
     * The location changed.
     */
    private boolean locationChanged = false;

    public static int screenWidth;
    public static int screenHeight;

    private LocationManager locationManager;
    private SensorManager sensorManager;

    private float[] valuesMagneticField = {0,0,0};
    private float[] valuesAccelerometer = {0,0,0};

    /**
     * Lista de POI
     */
    public static List<POI> poiList;

    private static final float CAMERA_ANGLE_HORIZONTAL = 49.55f;
    private static final float CAMERA_ANGLE_VERTICAL = 34.2f;

    private static final float CAMERA_ANGLE_HORIZONTAL_HALF = CAMERA_ANGLE_HORIZONTAL / 2;
    private static final float CAMERA_ANGLE_VERTICAL_HALF = CAMERA_ANGLE_VERTICAL / 2;

    /**
     * Utilizamos esta vista para poder desplegar el POIPopup
     */
    public static View arView;

    public static POIPopup poiPopup;

    /**
     * Rango en el cual vamos a buscar POI a desplegar, fijado en metros
     */
    public static int range = 1000;

    public static AccuracyDisplay accuracyDisplay;

    /**
     * Constructor
     */
    public ARLayer() {
        super(Main.context);
        initLayout();
        initDrawComponents();
        poiList = java.util.Collections.synchronizedList(new ArrayList<POI>());
        // TODO: Checar si necesita ser synchronized o no hace falta.
        SensorAvgFilter.initAvgArrays();
        arView = this;
        accuracyDisplay = new AccuracyDisplay();
    }

    public void onStart() throws LocationProviderNullException {
        initGPS();
        initSensors();
    }

    public void onStop() {
        stopGPS();
        stopSensors();
    }

    private void initLayout() {
        Display display = ((WindowManager) Main.context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        screenHeight = display.getHeight();
        screenWidth = display.getWidth();
        poiPopup = new POIPopup(screenWidth, screenHeight);
    }

    /**
     * Iniciamos el servicio de GPS y su escucha, solicitamos actualización de
     * la ubicación cada 30 segundos o cuando el dispositivo se mueva más de 5
     * metros. En general importa actualizar el valor solo si se ha cambiado de
     * posición.
     * <p/>
     * Solicitamos que el proveedor de ubicación tenga una precisión fina, esto
     * significa utilizar el GPS pero no solicitamos ese proveedor directamente
     * para seguir las convenciones de Android.
     *
     * @throws LocationProviderNullException Se arroja en caso de que el GPS esté deshabilitado,
     *                                       <code>Main</code> hace el manejo de la excepción
     */
    private void initGPS() throws LocationProviderNullException {
        String context = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) Main.context
                .getSystemService(context);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setBearingRequired(true);
        criteria.setCostAllowed(true);
        String provider = locationManager.getBestProvider(criteria, true);
        if (provider == null) {
            throw new LocationProviderNullException();
        }
        Log.d("gps", "provider: " + provider);
        locationManager.requestLocationUpdates(provider, 30000, 2,
                locationListener);
        updatePOILocation(locationManager.getLastKnownLocation(provider));
    }

    /**
     * Iniciamos los sensores, de orientación y el acelerómetro. Pedimos que nos
     * dé actualizaciones con <code>SensorManager.SENSOR_DELAY_GAME</code> que
     * es el segundo más rápido y promedia 60 actualizaciones por segundo en un
     * Droid Eris.
     * <p/>
     * Sería suficiente tener 30 actualizaciones por segundo, pero como estamos
     * a la escucha de dos sensores diferentes, así nos da en promedio 30
     * actualizaciones de cada sensor por segundo.
     */
    private void initSensors() {
        sensorManager = (SensorManager) Main.context
                .getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(orientationListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(orientationListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);
    }

    /**
     * Inits the draw components.
     */
    private void initDrawComponents() {

    }

    private void stopGPS() {
        if (locationManager != null)
            locationManager.removeUpdates(locationListener);
    }

    private void stopSensors() {
        if (sensorManager != null)
            sensorManager.unregisterListener(orientationListener);
    }

    /**
     * The orientation listener.
     */
    final SensorEventListener orientationListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

                valuesMagneticField = event.values;
                float[] valores = new float[3];
                float[] matrizDeRotacion = new float[9];
                SensorManager.getRotationMatrix(matrizDeRotacion, null,
                        valuesAccelerometer, valuesMagneticField);

                float[] matrizDeRotacion2 = new float[9];
                SensorManager.remapCoordinateSystem(matrizDeRotacion,
                        SensorManager.AXIS_X, SensorManager.AXIS_Z,
                        matrizDeRotacion2);

                SensorManager.getOrientation(matrizDeRotacion2, valores);
                //Convertimos de radianes a grados
                float orientacion = (float) Math.toDegrees(valores[0]);


                direction = orientacion; //+ 90;
                if (direction < 0) {
                    direction = 360 + direction;
                }
                Log.d("filter", "dirprev: "+direction);



                direction = SensorAvgFilter
                        .orientationListener(direction);
                if (direction > 360) {
                    direction -= 360;
                }
                Log.d("filter", Float.toString(direction));
            }

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                valuesAccelerometer = event.values;
                inclination = SensorAvgFilter.accelerometerListener(
                        event.values[0], event.values[2]);

            }

            if (SensorAvgFilter.directionChanged
                    || SensorAvgFilter.inclinationChanged) {
                if (locationChanged) {
                    Log.d("gps", "Location changed, updating");
                    updatePOILayout(currentLocation);
                    locationChanged = false;
                } else {
                    updatePOILayout(null);
                }
                Radar.setDirection(direction);
                postInvalidate();
            }

        }

        public void onAccuracyChanged(Sensor arg0, int arg1) {
            // No necesitamos hacer nada aquí
        }
    };

    /**
     * Escucha para el cambio de ubicación.
     */
    private final LocationListener locationListener = new LocationListener() {

        public void onLocationChanged(Location location) {
            currentLocation = location;
            Log.d("gps2", "cambio la ubicacion: " + location.getLatitude()
                    + ", " + location.getLongitude() + ", altitude: " + location.getAltitude());
            locationChanged = true;
            accuracyDisplay.changeAccuracy(location.getAccuracy());
        }

        public void onProviderDisabled(String provider) {

        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    };

    private float leftArm() {
        float la = direction - CAMERA_ANGLE_HORIZONTAL_HALF;
        if (la < 0) {
            la += 360;
        }
        return la;
    }

    private float rightArm() {
        float ra = direction + CAMERA_ANGLE_HORIZONTAL_HALF;
        if (ra > 360) {
            ra -= 360;
        }
        return ra;
    }

    /**
     * Calcula la posición en x de un POI
     *
     * @param dir Dirección del POI
     * @return Posición en x del POI
     */
    private float xPosition(float dir) {
        float x;
        float la = leftArm();
        float ra = rightArm();
        if (la > ra) {
            if (dir >= la) {
                x = dir - la;
            } else {
                x = 360 - la + dir;
            }
        } else {
            x = dir - la;
        }
        return (x * screenWidth) / CAMERA_ANGLE_HORIZONTAL;
    }

    private float upperArm() {
        return inclination + CAMERA_ANGLE_VERTICAL_HALF;
    }

    /**
     * Calcula la posición en y de un POI
     *
     * @param inc Inclinación
     * @return Posición en y del POI
     */
    private float yPosition(float inc) {
        float y;
        float ua = upperArm();
        if (ua >= 0) {
            y = ua - inc;
        } else {
            if (ua < inc) {
                y = ua - inc;
            } else {
                y = -ua - inc;
            }
        }
        return (y * screenHeight) / CAMERA_ANGLE_VERTICAL;
    }

    /**
     * Actualizamos la ubicación de los POI, cambiamos deviceLocation,
     * y volvemos a calcular sus distancias y azimuth
     *
     * @param location Ubicación del dispositivo
     */
    private void updatePOILocation(Location location) {
        if (location != null) {
            Log.d("gps",
                    "Loc: " + location.getLatitude() + ", "
                            + location.getLongitude());
            POI.deviceLocation = location;
            for (POI poi : poiList) {
                poi.updateValues();
            }
            Radar.setScale();
        } else {
            Log.d("gps", "Actualizando con location null");
        }
    }

    /**
     * Método para calcular la posición en pantalla de cada uno de los POI.
     *
     * @param location Ubicación del dispositivo
     */
    private void updatePOILayout(
            Location location) {
        if (location != null) {
            updatePOILocation(location);
        }
        for (POI poi : poiList) {
            // Calculamos la posición en x y en y de este POI y redondeamos al
            // entero
            // más cercano
            int x = Math.round(xPosition(poi.getAzimuth()));
            int y = Math.round(yPosition(poi.getInclination()));

            poi.poiLayout(x, y, x, y);
        }
    }

    /**
     * Método que dibuja esta vista cuando se llama a <code>invalidate()</code>
     * o <code>postInvalidate()</code>.
     *
     * @param canvas The canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        for (POI poi : poiList) {
            if (poi.isVisibleInRange()) {
                poi.draw(canvas);
            }
        }
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = false;
        for (POI poi : poiList) {
            ret = poi.dispatchTouchEvent(event);
        }
        return ret;
    }
}