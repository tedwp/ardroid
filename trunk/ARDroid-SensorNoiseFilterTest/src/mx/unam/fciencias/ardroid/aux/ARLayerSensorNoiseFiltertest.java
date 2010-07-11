package mx.unam.fciencias.ardroid.aux;

import java.io.PrintWriter;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

/**
 * Clase para probar dos tipos de filtros (Kalman y promedios) sobre los datos
 * de los sensores del teléfono
 *
 * @author lander
 *
 */
public class ARLayerSensorNoiseFiltertest extends View {

	private float kalDirection = 0f;
	private float kalLocalDirection;
	private float kalInclination;
	private float rollingZ = 0f;
	private float rollingX = 0f;

	private float avgLocalDirection;
	private float avgInclination;
	private float avgSum;
	private float avgRollXSum;
	private float avgRollZSum;

	private float tmpDirection;

	// Constante para filtro de Kalmann, se debe cambiar dependiendo de la
	// velocidad a la que se pidan
	// las lecturas de los sensores, debe ser más pequeña cuanto más rápida sea
	// esta.
	private static final float K_FILTERING_FACTOR = 0.1f;
	protected static final int AVG_NUM = 8;

	Paint paint;

	String textKdirection = "null";
	String textKinclination = "null";
	String textAdirection = "null";
	String textAInclination = "null";
	String textRDirection = "null";
	String textRInclination = "null";

	String textSRPS = "null";

	ArrayList<Float> directions;
	ArrayList<Float> avgRollingZ;
	ArrayList<Float> avgRollingX;

	boolean kalman, average;

	PrintWriter kDirWriter;
	PrintWriter aDirWriter;
	PrintWriter rDirWriter;
	PrintWriter kIncWriter;
	PrintWriter aIncWriter;
	PrintWriter rIncWriter;

	public ARLayerSensorNoiseFiltertest(Context context,
			PrintWriter kDirPW, PrintWriter aDirPW,
			PrintWriter rDirPW, PrintWriter kIncPW,
			PrintWriter aIncPW, PrintWriter rIncPW) {
		super(context);

		average = true;
		kalman = true;

		SensorManager sm = (SensorManager) ARDroidSensorNoiseFilterTest.context
				.getSystemService(Context.SENSOR_SERVICE);
		int sensorType = Sensor.TYPE_ORIENTATION;
		sm.registerListener(orientationListener,
				sm.getDefaultSensor(sensorType),
				SensorManager.SENSOR_DELAY_GAME);
		sm.registerListener(orientationListener,
				sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_GAME);
		initDrawComponents();
		if (average) {
			directions = new ArrayList<Float>();
			for (int i = 0; i < (AVG_NUM - 1); i++) {
				directions.add(0f);
			}
			avgRollingZ = new ArrayList<Float>();
			for (int i = 0; i < (AVG_NUM - 1); i++) {
				avgRollingZ.add(0f);
			}
			avgRollingX = new ArrayList<Float>();
			for (int i = 0; i < (AVG_NUM - 1); i++) {
				avgRollingX.add(0f);
			}
		}
		kDirWriter = kDirPW;
		aDirWriter =aDirPW;
		rDirWriter = rDirPW;
		kIncWriter = kIncPW;
		aIncWriter = aIncPW;
		rIncWriter = rIncPW;

	}

	private void initDrawComponents() {
		// Solo para probar los métodos de los sensores
		paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.WHITE);
		paint.setTextSize(38f);
		paint.setAntiAlias(true);
		paint.setFakeBoldText(true);
		// probar sensores
	}

	final SensorEventListener orientationListener = new SensorEventListener() {

		int cont = 0;

		long time, pTime = 0;

		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
				if (kalman) {
					tmpDirection = event.values[0];
					if (tmpDirection < 0) {
						tmpDirection += 360;
					}
					kalDirection = ((tmpDirection * K_FILTERING_FACTOR) + (kalDirection * (1.0f - K_FILTERING_FACTOR)));
					if (kalDirection < 0) {
						kalLocalDirection = 360 + kalDirection;
					} else {
						kalLocalDirection = kalDirection;
					}
					textKdirection = "dir K: " + (int) kalLocalDirection;
					kDirWriter.print(kalLocalDirection);
					kDirWriter.flush();
				}
				// Valores reales de los sensores
				textRDirection = "realD: " + event.values[0];
				textRInclination = "realI: " + (90 - event.values[2]);

				rDirWriter.print(event.values[0]);
				rIncWriter.print((90 - event.values[2]));
				rDirWriter.flush();
				rIncWriter.flush();

				if (average) {
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
					textAdirection = "dir A: " + (int) avgLocalDirection;
					aDirWriter.print(avgLocalDirection);
				}
			}

			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				if (kalman) {
					rollingZ = (event.values[2] * K_FILTERING_FACTOR)
							+ (rollingZ * (1.0f - K_FILTERING_FACTOR));
					rollingX = (event.values[0] * K_FILTERING_FACTOR)
							+ (rollingX * (1.0f - K_FILTERING_FACTOR));

					if (rollingZ != 0.0) {
						kalInclination = (float) Math.atan(rollingX / rollingZ);
					} else if (rollingX < 0) {
						kalInclination = (float) (Math.PI / 2.0);
					} else if (rollingX >= 0) {
						kalInclination = (float) (3 * Math.PI / 2.0);
					}

					// convert to degress
					kalInclination = (float) (kalInclination * (360 / (2 * Math.PI)));

					// flip!
					if (kalInclination < 0) {
						kalInclination = kalInclination + 90;
					} else {
						kalInclination = kalInclination - 90;
					}
					textKinclination = "inc K: " + (int) kalInclination;
					kIncWriter.print(kalInclination);
					kIncWriter.flush();
				}

				if (average) {

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
					textAInclination = "inc A: " + (int) avgInclination;
					aIncWriter.print(avgInclination);

					{
						++cont;

						time = SystemClock.uptimeMillis();
						if ((time - pTime) > 1000) {
							Log.d("SensorRate", "SensorReadings per second: "
									+ cont);
							textSRPS = "SR/s: " + cont;
							cont = 0;
							pTime = time;
						}
					}
				}
			}
			postInvalidate();

		}

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			// TODO Auto-generated method stub

		}
	};

	/*
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawText(textKdirection, 40, 60, paint);
		canvas.drawText(textAdirection, 40, 120, paint);
		canvas.drawText(textRDirection, 40, 180, paint);
		canvas.drawText(textKinclination, 270, 60, paint);
		canvas.drawText(textAInclination, 270, 120, paint);
		canvas.drawText(textRInclination, 270, 180, paint);
		canvas.drawText(textSRPS, 180, 240, paint);
		super.onDraw(canvas);
	}

}
