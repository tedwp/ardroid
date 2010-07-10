package mx.unam.fciencias;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;

/**
 * Clase que define la capa de realidad aumentada
 *
 * @author lander
 *
 */
public class ARLayer extends View {

	private float direction;
	private float inclination;

	public ARLayer(Context context) {
		super(context);
		initSensors();

	}

	private void initSensors() {
		SensorManager sm = (SensorManager) Main.context
				.getSystemService(Context.SENSOR_SERVICE);
		int sensorType = Sensor.TYPE_ORIENTATION;
		sm.registerListener(orientationListener,
				sm.getDefaultSensor(sensorType),
				SensorManager.SENSOR_DELAY_FASTEST);
		initDrawComponents();
	}

	private void initDrawComponents() {

	}

	final SensorEventListener orientationListener = new SensorEventListener() {

		private float tmpDirection;
		private float avgLocalDirection;
		private float avgSum;
		private ArrayList<Float> directions = new ArrayList<Float>();

		private float avgRollZSum;
		private float avgRollXSum;
		private ArrayList<Float> avgRollingZ = new ArrayList<Float>();
		private ArrayList<Float> avgRollingX = new ArrayList<Float>();
		private float avgInclination;

		// Usamos un filtro de promedios con las últimas <code>AVG_NUM</code>
		// lecturas de los sensores para determinar la nueva dirección e
		// inclinación del teléfono. Hacemos esto para filtrar el ruido de los
		// sensores.
		protected static final int AVG_NUM = 8;

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
			}

			postInvalidate();

		}

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			// No necesitamos hacer nada aquí
		}
	};

	/**
	 * Método que dibuja esta vista cuando se llama a <code>invalidate()</code>
	 * o <code>postInvalidate()</code>
	 */
	@Override
	protected void onDraw(Canvas canvas) {

		super.onDraw(canvas);
	}

	/**
	 * @return the direction
	 */
	public float getDirection() {
		return direction;
	}

	/**
	 * @return the inclination
	 */
	public float getInclination() {
		return inclination;
	}

}
