package mx.unam.fciencias.ardroid.app;

import java.util.ArrayList;

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
import android.view.View;

// TODO: Auto-generated Javadoc
/**
 * Clase que define la capa de realidad aumentada.
 *
 * @author Sebastián García Anderman
 */
public class ARLayer extends View {

	/** The direction. */
	private float direction;

	/** The inclination. */
	private float inclination;

	/** The current location. */
	private Location currentLocation;

	/** The location changed. */
	private boolean locationChanged = false;;

	/**
	 * Instantiates a new aR layer.
	 */
	public ARLayer() {
		super(Main.context);
		initSensors();
		initGPS();
		initDrawComponents();
	}

	/**
	 * Iniciamos el servicio de GPS y su escucha, solicitamos actualización de
	 * la ubicación cada 30 segundos o cuando el dispositivo se mueva más de 5
	 * metros. En general importa actualizar el valor solo si se ha cambiado de
	 * posición.
	 *
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
	 *
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

	/** The orientation listener. */
	final SensorEventListener orientationListener = new SensorEventListener() {

		private float tmpDirection;
		private float avgLocalDirection;
		private float prevAvgLocalDirection = 0;
		private float avgSum;
		private ArrayList<Float> directions = new ArrayList<Float>();

		private float avgRollZSum;
		private float avgRollXSum;
		private ArrayList<Float> avgRollingZ = new ArrayList<Float>();
		private ArrayList<Float> avgRollingX = new ArrayList<Float>();
		private float avgInclination;
		private float prevAvgInclination = 0;

		private boolean directionChanged = false;
		private boolean inclinationChanged = false;

		// Usamos un filtro de promedios con las últimas <code>AVG_NUM</code>
		// lecturas de los sensores para determinar la nueva dirección e
		// inclinación del teléfono. Hacemos esto para filtrar el ruido de los
		// sensores.
		private static final int AVG_NUM = 8;

		private static final float DIRECTION_THRESHOLD = 0;
		private static final float LOCATION_THRESHOLD = 0;

		@Override
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
				if (Math.abs(prevAvgLocalDirection - avgLocalDirection) > DIRECTION_THRESHOLD) {
					directionChanged = true;
				}
				prevAvgLocalDirection = avgLocalDirection;
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

				if (Math.abs(prevAvgInclination - avgInclination) > LOCATION_THRESHOLD) {
					inclinationChanged = true;
				}
				prevAvgInclination = avgInclination;
			}

			// definir el threshold
			if (directionChanged || inclinationChanged) {
				if (locationChanged) {
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

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			// No necesitamos hacer nada aquí
		}
	};

	/** Escucha para el cambio de ubicación. */
	private final LocationListener locationListener = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			currentLocation = location;
			locationChanged = true;
		}

		@Override
		public void onProviderDisabled(String provider) {

		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}
	};

	/**
	 * Método para calcular la posición en pantalla de cada uno de los POI.
	 *
	 * @param direction Dirección del dispositivo
	 * @param inclination Inclinación del dispositivo
	 * @param location Ubicación del dispositivo
	 */
	private void updatePOILayout(float direction, float inclination,
			Location location) {

	}

	/**
	 * Método que dibuja esta vista cuando se llama a <code>invalidate()</code>
	 * o <code>postInvalidate()</code>.
	 *
	 * @param canvas the canvas
	 */
	@Override
	protected void onDraw(Canvas canvas) {

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
