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

	/**
	 * Lista de POI
	 */
	public static List<POI> poiList;

	private ArrayList<Float> directions;

	private ArrayList<Float> avgRollingZ;
	private ArrayList<Float> avgRollingX;

	// Usamos un filtro de promedios con las últimas <code>AVG_NUM</code>
	// lecturas de los sensores para determinar la nueva dirección e
	// inclinación del teléfono. Hacemos esto para filtrar el ruido de los
	// sensores.
	private static final int AVG_NUM = 8;

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
		initSensors();
		initGPS();
		initDrawComponents();
		poiList = java.util.Collections.synchronizedList(new ArrayList<POI>());
		// TODO: Checar si necesita ser synchronized o no hace falta.
		initAvgArrays();
	}

	private void initAvgArrays() {
		directions = new ArrayList<Float>();
		avgRollingZ = new ArrayList<Float>();
		avgRollingX = new ArrayList<Float>();
		for (int i = 0; i < (AVG_NUM - 1); i++) {
			directions.add(0f);
			avgRollingZ.add(0f);
			avgRollingX.add(0f);
		}
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
		LocationManager locationManager;
		String context = Context.LOCATION_SERVICE;
		locationManager = (LocationManager) Main.context
				.getSystemService(context);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setBearingRequired(true);
		criteria.setCostAllowed(true);
		String provider = locationManager.getBestProvider(criteria, true);
		locationManager.requestLocationUpdates(provider, 30000, 5,
				locationListener);
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
		SensorManager sm = (SensorManager) Main.context
				.getSystemService(Context.SENSOR_SERVICE);
		sm.registerListener(orientationListener,
				sm.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_GAME);
		sm.registerListener(orientationListener,
				sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_GAME);
	}

	/**
	 * Inits the draw components.
	 */
	private void initDrawComponents() {

	}

	/**
	 * The orientation listener.
	 */
	final SensorEventListener orientationListener = new SensorEventListener() {

		private float tmpDirection;
		private float avgLocalDirection;
		private float prevAvgLocalDirection = 0;
		private float avgSum;

		private float avgRollZSum;
		private float avgRollXSum;
		private float avgInclination;
		private float prevAvgInclination = 0;

		private boolean directionChanged = false;
		private boolean inclinationChanged = false;

		private static final float DIRECTION_THRESHOLD = 0.0f;
		private static final float INCLINATION_THRESHOLD = 0;

		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {

				tmpDirection = event.values[0];
				if (tmpDirection < 0) {
					tmpDirection += 360;
				}
				if (tmpDirection < 0) {
					avgLocalDirection = 360 + tmpDirection;
				} else {
					avgLocalDirection = tmpDirection;
				}
				avgSum = 0;
				for (int i = 0; i < (AVG_NUM - 1); i++) {
					avgSum += directions.get(i);
				}
				avgSum += avgLocalDirection;

				directions.add(avgLocalDirection);
				avgLocalDirection = avgSum / AVG_NUM;
				directions.remove(0);
				if (changeAboveThreshold(prevAvgLocalDirection,
						avgLocalDirection, DIRECTION_THRESHOLD)) {
					directionChanged = true;
					prevAvgLocalDirection = avgLocalDirection;
					direction = avgLocalDirection;
				}
			}

			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				avgRollZSum = 0;
				avgRollXSum = 0;
				for (int i = 0; i < (AVG_NUM - 1); i++) {
					avgRollZSum += avgRollingZ.get(i);

				}

				for (int i = 0; i < (AVG_NUM - 1); i++) {
					avgRollXSum += avgRollingX.get(i);

				}

				avgRollZSum += event.values[2];
				avgRollXSum += event.values[0];

				avgRollZSum = avgRollZSum / AVG_NUM;
				avgRollXSum = avgRollXSum / AVG_NUM;

				avgRollingZ.add(event.values[2]);
				avgRollingZ.remove(0);

				avgRollingX.add(event.values[0]);
				avgRollingX.remove(0);

				if (avgRollZSum != 0.0) {
					avgInclination = (float) Math.atan(avgRollXSum
							/ avgRollZSum);
				} else if (avgRollXSum < 0) {
					avgInclination = (float) (Math.PI / 2.0);
				} else if (avgRollXSum >= 0) {
					avgInclination = (float) (3 * Math.PI / 2.0);
				}

				// convert to degress
				avgInclination = (float) (avgInclination * (360 / (2 * Math.PI)));

				// flip!
				if (avgInclination < 0) {
					avgInclination = avgInclination + 90;
				} else {
					avgInclination = avgInclination - 90;
				}
				// avgInclination es la inclinación final

				if (changeAboveThreshold(prevAvgInclination, avgInclination,
						INCLINATION_THRESHOLD)) {
					inclinationChanged = true;
					prevAvgInclination = avgInclination;
					inclination = avgInclination;
				}

			}

			// definir el threshold
			if (directionChanged || inclinationChanged) {
				if (locationChanged) {
					Log.d("location", "Location changed, updating");
					updatePOILayout(avgLocalDirection, avgInclination,
							currentLocation);
					locationChanged = false;
				} else {
					updatePOILayout(avgLocalDirection, avgInclination, null);
				}
				directionChanged = false;
				inclinationChanged = false;
				postInvalidate();
			}

		}

		public void onAccuracyChanged(Sensor arg0, int arg1) {
			// No necesitamos hacer nada aquí
		}
	};

	/**
	 * Calculamos si la diferencia entre ambos valores está arriba de cierto
	 * umbral. Usamos este método ya que a veces el cambio de dirección o
	 * inclinación es mínimo y no necesitamos recalcular la posición de los POI.
	 *
	 * @param val1 Primer valor
	 * @param val2 Segundo valor
	 * @param threshold Umbral
	 * @return Verdadero si la diferencia entre <code>val1</code> y
	 *         <code>val2</code> es mayor que <code>threshold</code>
	 */
	private boolean changeAboveThreshold(float val1, float val2, float threshold) {
		return Math.abs(val1 - val2) > threshold;
	}

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

	/**
	 * Método para calcular la posición en pantalla de cada uno de los POI.
	 *
	 * @param direction Dirección del dispositivo
	 * @param inclination Inclinación del dispositivo
	 * @param location Ubicación del dispositivo
	 */
	private void updatePOILayout(float direction, float inclination,
			Location location) {
		Iterator<POI> poiIterator = poiList.iterator();
		if (location != null) {
			POI.deviceLocation = location;
			while (poiIterator.hasNext()) {
				POI poi = poiIterator.next();
				poi.updateValues();
			}
		}

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