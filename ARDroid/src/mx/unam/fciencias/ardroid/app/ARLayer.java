package mx.unam.fciencias.ardroid.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import mx.unam.fciencias.ardroid.app.POISources.GeoNamesPOISource;
import mx.unam.fciencias.ardroid.app.POISources.POISource;
import mx.unam.fciencias.ardroid.app.POISources.TwitterPOISource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;

// TODO: Auto-generated Javadoc

/**
 * Clase que define la capa de realidad aumentada.
 *
 * @author Sebastián García Anderman
 */
public class ARLayer extends View {

    /**
     * Dirección a la que apunta el dispositivo.
     */
    private float direction;

    /**
     * Inclinación del dispositivo respecto al suelo.
     */
    private float inclination;

    /**
     * La ubicación actual del dispositivo.
     */
    public static Location currentLocation;

    /**
     * Indica cuando la ubicación ha cambiado.
     */
    private boolean locationChanged = false;

    public static int screenWidth;
    public static int screenHeight;

    private LocationManager locationManager;
    private SensorManager sensorManager;

    private float[] valuesMagneticField = {0, 0, 0};
    private float[] valuesAccelerometer = {0, 0, 0};

    /**
     * Lista de POI
     */
    public static CopyOnWriteArrayList<POI> poiList;
    private static Comparator poiListComparator;

    private static final float CAMERA_ANGLE_HORIZONTAL = 49.55f;
    private static final float CAMERA_ANGLE_VERTICAL = 34.2f;

    private static final float CAMERA_ANGLE_HORIZONTAL_HALF = CAMERA_ANGLE_HORIZONTAL / 2;
    private static final float CAMERA_ANGLE_VERTICAL_HALF = CAMERA_ANGLE_VERTICAL / 2;

    private static final float POI_RADIUS_IN_INCHES = 3f / 16f;

    public static int poiRadius;

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
     * Lo usamos para esperar a tener una buena precisión en la ubicación para empezar a bajar los datos de los POIs
     */
    private boolean poiDownloadStarted = false;
    private static final int MAXIMUM_COLLISIONS = 1;
    public static float xdpi;
    public static float ydpi;

    /**
     * Constructor
     */
    public ARLayer() {
        super(Main.context);
        initLayout();
        initDrawComponents();
        //Utilizamos este tipo de dato para que pueda haber acceso concurrente a la lista
        //Ya que mientras estamos recorriéndola para mostrar los POI, se agregan más
        //Desde el servicio
        poiList = new CopyOnWriteArrayList<POI>();
        poiListComparator = new POIDistanceComparator();
        //SensorAvgFilter.initAvgArrays();
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
        float xLength = screenWidth / xdpi;
        float yLength = screenHeight / ydpi;
        float xPOI = xLength / POI_RADIUS_IN_INCHES;
        float yPOI = yLength / POI_RADIUS_IN_INCHES;
        float xRadius = screenWidth / xPOI;
        float yRadius = screenHeight / yPOI;
        poiRadius = (int) Math.max(xRadius, yRadius);
        Log.d("radius", "Radio: " + poiRadius);
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

    /**
     * Detenemos las actualizaciones del GPS
     */
    private void stopGPS() {
        if (locationManager != null)
            locationManager.removeUpdates(locationListener);
    }

    /**
     * Detenemos las actualizaciones de los sensores
     */
    private void stopSensors() {
        if (sensorManager != null)
            sensorManager.unregisterListener(orientationListener);
    }

    /**
     * Calculamos la dirección en la que apunta el teléfono de acuerdo al norte magnético
     * Hacemos esto utilizando los valores del acelerómetro, el magnetómetro y usando una
     * matriz de rotación, después reasignamos los ejes para que concuerden con la posición
     * del dispositivo.
     *
     * @return Dirección de 0º a 359º
     */
    private float computeDirection() {
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
        direction = (float) Math.toDegrees(valores[0]);
        Log.d("filter2", "Dir: " + direction);
        if (direction < 0) {
            direction = 360 + direction;
        } else if (direction >= 360) {
            direction = direction - 360;
        }
        return direction;
    }

    /**
     * Calculamos la inclinación del dispositivo usando los valores del acelerómetro.
     *
     * @param rollingX Valor del acelerómetro en el eje X
     * @param rollingZ Valor del acelerómetro en el eje Z
     * @return Inclinación del dispositivo de -90 a 90
     */
    private float computeInclination(float rollingX, float rollingZ) {
        //Si uno de los ejes i.e. Z es cero, entonces es porque el dispositivo
        //está paralelo a ese eje, entonces la inclinación debería ser 0 o 180,
        //dependiendo del eje x
        if (rollingZ != 0.0) {
            inclination = (float) Math.atan(rollingX / rollingZ);
        } else if (rollingX <= 0) {
            inclination = (float) (Math.PI / 2.0);
        } else if (rollingX > 0) {
            inclination = (float) (3 * Math.PI / 2.0);
        }

        // convert to degress
        inclination = (float) (inclination * (360 / (2 * Math.PI)));

        // flip!
        if (inclination < 0) {
            inclination = inclination + 90;
        } else {
            inclination = inclination - 90;
        }
        return inclination;
    }

    /**
     * Escucha para los sensores
     */
    final SensorEventListener orientationListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent event) {
            //En caso de que el sensor sea el magnetómetro, guardamos sus valores, calculamos la
            //dirección del dispositivo y pasamos ese valor al filtro óptimo
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                valuesMagneticField = event.values;
                //direction = SensorOptimalFilter.filterDirection(computeDirection());
                Log.d("cambio", "Dir1: " + direction);
                direction = SensorOptimalFilter.filterDirection(computeDirection());
                Log.d("cambio", "Dir2: " + direction);
            }
            //En caso de que el sensor sea el acelerómetro, guardamos sus valores, calculamos la
            //inclinación del dispositivo y pasamos ese valor al filtro ópitmo
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                valuesAccelerometer = event.values;
                inclination = SensorOptimalFilter.filterInclination(computeInclination(event.values[0], event.values[2]));
            }
            //Si cambio la dirección o la inclinación entonces debemos actualizar los POI
            if (SensorOptimalFilter.directionChanged
                    || SensorOptimalFilter.inclinationChanged || SensorAvgFilter.directionChanged || SensorAvgFilter.inclinationChanged) {
                //Si cambio nuestra ubicación, la pasamos a updatePOILayout
                if (locationChanged) {
                    Log.d("gps", "Location changed, updating");
                    updatePOILayout(currentLocation);
                    locationChanged = false;
                } else {
                    updatePOILayout(null);
                }
                //Ponemos la dirección en la que está el dispositivo en el radar
                Radar.setDirection(direction);
                //Con esta llamada se redibujan todos los elementos de la pantalla
                postInvalidate();
            }

        }

        public void onAccuracyChanged(Sensor arg0, int arg1) {
            // No necesitamos hacer nada aquí
        }
    };

    private static final int GPS_ACCURACY_TO_DOWNLOAD_DATA = 100;
    //TODO: Cambiar a 50
    /**
     * Escucha para el cambio de ubicación.
     */
    private final LocationListener locationListener = new LocationListener() {

        public void onLocationChanged(Location location) {
            currentLocation = location;
            Log.d("gps2", "cambio la ubicacion: " + location.getLatitude()
                    + ", " + location.getLongitude() + ", altitude: " + location.getAltitude());
            locationChanged = true;
            float accuracy = location.getAccuracy();
            if (accuracy < GPS_ACCURACY_TO_DOWNLOAD_DATA && !poiDownloadStarted) {
                poiDownloadStarted = true;
                new DownloadPOIData().execute(null, null, null);
            }
            accuracyDisplay.changeAccuracy(accuracy);
        }

        public void onProviderDisabled(String provider) {

        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    };

    /**
     * Calculamos el brazo izquierdo de vista de la cámara
     *
     * @return Ángulo el brazo izquierdo
     */
    private float leftArm() {
        float la = direction - CAMERA_ANGLE_HORIZONTAL_HALF;
        if (la < 0) {
            la += 360;
        }
        return la;
    }

    /**
     * Calculamos el brazo derecho de vista de la cámara
     *
     * @return
     */
    private float rightArm() {
        float ra = direction + CAMERA_ANGLE_HORIZONTAL_HALF;
        if (ra > 360) {
            ra -= 360;
        }
        return ra;
    }

    /**
     * Calcula la posición en x de un POI
     * Calculamos un x en grados relativo a la dirección del dispositivo
     * usando el brazo izquierdo y derecho, después multiplicamos ese x por el ancho
     * de la pantalla para obtener su posición en pixeles. Y dividimos ese valor entre
     * el ángulo horizontal de visión de la cámara.
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

    /**
     * Calculamos el brazo superior del ángulo de vista de la cámara
     *
     * @return
     */
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
    private void updatePOILayout(Location location) {
        if (location != null) {
            updatePOILocation(location);
        }

        for (POI poi : poiList) {
            poi.collisionCounter = 0;
            poi.setIsVisibleFromCollisions(true);
        }
        int i = 0;
        for (POI poi : poiList) {
            //Si el POI no es visible no hace falta calcular nada pues no se dibujará
            if (poi.isVisibleInRange()) {
                // Calculamos la posición en x y en y de este POI y redondeamos al
                // entero más cercano
                int x = Math.round(xPosition(poi.getAzimuth()));
                int y = Math.round(yPosition(poi.getInclination()));
                poi.poiLayout(x, y, x, y);
                checkForCollisions(poi, i);
                ++i;
            }
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
            if (poi.isVisibleInRange() && poi.isVisibleFromCollisions()) {
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

    /**
     * Clase para descargar los POI de las fuentes de información
     * en un thread separado
     */
    class DownloadPOIData extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            double latitude = currentLocation.getLatitude();
            double longitude = currentLocation.getLongitude();
            SharedPreferences preferences =
                    PreferenceManager.getDefaultSharedPreferences(Main.context);
            if (preferences.getBoolean("PREF_PI_SOURCE_GEONAMES", true)) {
                POISource geo = new GeoNamesPOISource();
                geo.retrievePOIs(latitude, longitude);
            }
            if (preferences.getBoolean("PREF_PI_SOURCE_TWITTER", false)) {
                POISource twitter = new TwitterPOISource();
                twitter.retrievePOIs(latitude, longitude);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void s) {
            Log.d("poiData", "Se termino de bajar los POI");
        }
    }

    /**
     * Clase para comparar los POI en base a su distancia al dispositivo
     * y así poder ordenarlos de esta manera
     */
    class POIDistanceComparator implements Comparator<POI> {
        public int compare(POI p1, POI p2) {
            float d1 = p1.getDistance();
            float d2 = p2.getDistance();
            return (int) (d1 - d2);
        }
    }

    /**
     * Método auxiliar para mantener ordenado <code>poiList</code> al agregarle un elemento.
     * Como <code>poiList</code> es <code>CopyOnWriteArrayList</code> que es "thread safe"
     * no podemos usar sort sobre ella, pues sort no es "thread safe", entonce,
     * tenemos que copiar la lista en otra para poder entonces ordenarla,
     * luego vaciamos la lista original y copiamos la nueva lista ordenada a ella.
     *
     * @param poi POI a agregar
     */
    public static void addPOI(POI poi) {
        poiList.add(poi);
        POI[] aList = poiList.toArray(new POI[0]);
        Arrays.sort(aList, poiListComparator);
        poiList.clear();
        poiList.addAll(Arrays.asList(aList));
    }

    /**
     * Método auxiliar para mantener ordenado <code>poiList</code> al agregarle una lista de elementos.
     * Como <code>poiList</code> es <code>CopyOnWriteArrayList</code> que es "thread safe"
     * no podemos usar sort sobre ella, pues sort no es "thread safe", entonce,
     * tenemos que copiar la lista en otra para poder entonces ordenarla,
     * luego vaciamos la lista original y copiamos la nueva lista ordenada a ella.
     *
     * @param poiL Lista de POI a agregar
     */
    public static void addPOIList(ArrayList<POI> poiL) {
        poiList.addAll(poiL);
        POI[] aList = poiList.toArray(new POI[0]);
        Arrays.sort(aList, poiListComparator);
        poiList.clear();
        poiList.addAll(Arrays.asList(aList));
        int i = 1;
        for (POI p : poiList) {
            Log.d("comparator", "POI: " + i + ", dist: " + p.getDistance());
            ++i;
        }
    }

    public static void checkForCollisions(POI poi, int n) {
        for (int i = 0; i < n; ++i) {
            POI p = poiList.get(i);
            int collision = checkPOICollision(poi, p);
            switch (collision) {
                case -1:
                    //No debe dibujarse y ya no hace falta checar nada más
                    poi.setIsVisibleFromCollisions(false);
                    return;
                case 0:
                    //No colisiona, no hay que hacer nada
                    poi.setIsVisibleFromCollisions(true);
                    break;

                case 1:
                    //Colisiona, hay que moverlo y seguir checando
                    poi.movePOIUp();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Checa si el POI p2 colisiona con el POI p1
     *
     * @param p2
     * @param p1
     * @return
     */
    private static int checkPOICollision(POI p2, POI p1) {
        boolean hCollision = false;
        boolean vCollision = false;

        //Si alguno de los lados izquierdo o derecho de la frontera de ambos POI
        //son iguales entonces colisionan horizontalmente
        if (p1.l == p2.l || p1.r == p2.r || p1.l == p2.r || p1.r == p2.l) {
            //Chocan horizontalmente
            hCollision = true;
        } else {
            if (p2.l > p1.l) {
                if (p2.l < p1.r) {
                    //Chocan horizontalmente
                    hCollision = true;
                }
            } else {
                if (p1.l < p2.r) {
                    //Chocan horizontalmente
                    hCollision = true;
                }
            }
        }

        //Si alguno de los lados arriba o abajo de la frontera de ambos POI
        //son iguales entonces colisionan verticalmente
        if (p1.t == p2.t || p1.b == p2.b || p1.t == p2.b || p1.b == p2.t) {
            //Chocan verticalmente
            vCollision = true;
        } else {
            if (p2.t > p1.t) {
                if (p2.t < p1.b) {
                    //Chocan verticalmente
                    vCollision = true;
                }
            } else {
                if (p1.t < p2.b) {
                    //Chocan verticalmente
                    vCollision = true;
                }
            }
        }

        if (hCollision && vCollision) {

            //Si se alcanza el número máximo de colisiones sobre un POI ya no se dibuja el que está colisionando
            if (p1.collisionCounter >= MAXIMUM_COLLISIONS) {
                return -1;
            } else {
                p1.collisionCounter++;
                return 1;
            }
        } else {
            return 0;
        }
    }
}