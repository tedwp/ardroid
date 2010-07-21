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
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Iterator;
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

	private int screenWidth;
	private int screenHeight;

	private LocationManager locationManager;
	private SensorManager sensorManager;

	private SensorAvgFilter sensorAvgFilter;

	/**
	 * Lista de POI
	 */
	public static List<POI> poiList;

	private static final float CAMERA_ANGLE_HORIZONTAL = 49.55f;
	private static final float CAMERA_ANGLE_VERTICAL = 34.2f;

	private static final float CAMERA_ANGLE_HORIZONTAL_HALF = CAMERA_ANGLE_HORIZONTAL / 2;
	private static final float CAMERA_ANGLE_VERTICAL_HALF = CAMERA_ANGLE_VERTICAL / 2;

	/**
	 * Constructor
	 */
	public ARLayer() {
		super(Main.context);
		initLayout();
		initDrawComponents();
		poiList = java.util.Collections.synchronizedList(new ArrayList<POI>());
		// TODO: Checar si necesita ser synchronized o no hace falta.
		sensorAvgFilter = new SensorAvgFilter();
	}

	public void onStart() {
		initSensors();
		initGPS();
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
	 */
	private void initGPS() {
		String context = Context.LOCATION_SERVICE;
		locationManager = (LocationManager) Main.context
				.getSystemService(context);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setBearingRequired(true);
		criteria.setCostAllowed(true);
		String provider = locationManager.getBestProvider(criteria, true);
		Log.d("gps", "provider: " + provider);
		locationManager.requestLocationUpdates(provider, 30000, 5,
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
				sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
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
		locationManager.removeUpdates(locationListener);
	}

	private void stopSensors() {
		sensorManager.unregisterListener(orientationListener);
	}

	/**
	 * The orientation listener.
	 */
	final SensorEventListener orientationListener = new SensorEventListener() {

		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
				direction = SensorAvgFilter.orientationListener(event.values[0]);
			}

			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				inclination = SensorAvgFilter.accelerometerListener(event.values[0], event.values[2]);

			}

			if (SensorAvgFilter.directionChanged || SensorAvgFilter.inclinationChanged) {
				if (locationChanged) {
					Log.d("gps", "Location changed, updating");
					updatePOILayout(direction, inclination,
							currentLocation);
					locationChanged = false;
				} else {
					updatePOILayout(direction, inclination, null);
				}
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
			locationChanged = true;
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

	private void updatePOILocation(Location location) {
		POI.deviceLocation = location;
		Iterator<POI> poiIterator = poiList.iterator();
		while (poiIterator.hasNext()) {
			POI poi = poiIterator.next();
			poi.updateValues();
		}
	}

	/**
	 * Método para calcular la posición en pantalla de cada uno de los POI.
	 *
	 * @param direction Dirección del dispositivo
	 * @param inclination Inclinación del dispositivo
	 * @param location Ubicación del dispositivo
	 */
	private void updatePOILayout(float direction, float inclination,
			Location location) {
		if (location != null) {
			updatePOILocation(location);
		}
		Iterator<POI> poiIterator = poiList.iterator();
		poiIterator = poiList.iterator();
		while (poiIterator.hasNext()) {
			POI poi = poiIterator.next();
			// Calculamos la posición en x y en y de este POI y redondeamos al
			// entero
			// más cercano
			int x = Math.round(xPosition(poi.getAzimuth()));
			int y = Math.round(yPosition(poi.getInclination()));

			// TODO: Ajustar los valores que se suman y restan
			poi.layout(x, y, x + 15, y - 15);
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
		Iterator<POI> poiIterator = poiList.iterator();
		while (poiIterator.hasNext()) {
			POI poi = poiIterator.next();
			Log.d("dibujando", "intentando dibujar el poi en: " + poi.getLeft()
					+ " ," + poi.getTop());
			poi.draw(canvas);
		}
		super.onDraw(canvas);
	}

	/**
	 * Gets the direction.
	 *
	 * @return the direction
	 */
	public float getDirection() {
		return direction;
	}

	/**
	 * Gets the inclination.
	 *
	 * @return the inclination
	 */
	public float getInclination() {
		return inclination;
	}

}