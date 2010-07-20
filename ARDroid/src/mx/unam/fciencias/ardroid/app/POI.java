package mx.unam.fciencias.ardroid.app;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.view.View;

// TODO: Auto-generated Javadoc
/**
 * Clase que representa un punto de interés (POI Point of Interest).
 *
 * @author Sebastián García Anderman
 *
 */
public class POI extends View {

	/** Ubicación del POI. */
	private Location poiLocation;

	/** Ubicación del dispositivo. */
	public static Location deviceLocation;

	/** distancia entre el dispositivo y el POI. */
	private float distance;

	/** grados hacia el Este desde el Norte entre el dispositivo y el POI. */
	private float azimuth;

	/** inclinación del dispositivo al POI, se obtiene con la altitud. */
	private float inclination = 0;

	/** The name. */
	private String name;

	/** The source. */
	private String source;

	private Paint paint1;

	/**
	 * Instantiates a new pOI.
	 *
	 * @param location the location
	 * @param deviceLocation the device location
	 */
	public POI(Location location, Location deviceLocation) {
		super(Main.context);
		this.poiLocation = location;
		if (POI.deviceLocation == null && poiLocation != null) {
			this.setDeviceLocation(deviceLocation);
		}
		updateValues();
		initPaint();
	}

	private void initPaint() {
		paint1 = new Paint();
		paint1.setColor(Color.RED);
        paint1.setAntiAlias(true);
	}

	public void updateValues() {
		if(poiLocation != null && POI.deviceLocation != null) {
			azimuth = deviceLocation.bearingTo(poiLocation);
			distance = deviceLocation.distanceTo(poiLocation);
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
		int x = getLeft();
		int y = getTop();
		canvas.drawCircle(x, y, 20, paint1);
		super.draw(canvas);
	}

	/**
	 * Gets the location.
	 *
	 * @return the location
	 */
	public Location getLocation() {
		return poiLocation;
	}

	/**
	 * Sets the location.
	 *
	 * @param location the location to set
	 */
	public void setLocation(Location location) {
		this.poiLocation = location;
	}

	/**
	 * Gets the distance.
	 *
	 * @return the distance
	 */
	public float getDistance() {
		return distance;
	}

	/**
	 * Sets the distance.
	 *
	 * @param distance the distance to set
	 */
	public void setDistance(float distance) {
		this.distance = distance;
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
	 * Sets the azimuth.
	 *
	 * @param azimuth the azimuth to set
	 */
	public void setAzimuth(float azimuth) {
		this.azimuth = azimuth;
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
	 * Sets the inclination.
	 *
	 * @param inclination the inclination to set
	 */
	public void setInclination(float inclination) {
		this.inclination = inclination;
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
			if (negative) {
				inclination = (float) (Math
						.atan(((double) altitudeDiff / distance)) * -1);
			} else {
				inclination = (float) Math.atan((double) altitudeDiff
						/ distance);
			}
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
	 * Gets the device location.
	 *
	 * @return the device location
	 */
	public Location getDeviceLocation() {
		return deviceLocation;
	}

	/**
	 * Sets the source.
	 *
	 * @param source the new source
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * Gets the source.
	 *
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

}
