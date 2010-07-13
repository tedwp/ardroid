package mx.unam.fciencias.ardroid.app;

import android.location.Location;

/**
 * Clase que representa un punto de interés (POI Point of Interest)
 *
 * @author Sebastián García Anderman
 *
 */
public class POI {

	private Location location;// Ubicación del POI
	public static Location deviceLocation;// Ubicación del dispositivo

	// distancia entre el dispositivo y el POI
	private float distance;

	// grados hacia el Este desde el Norte entre el dispositivo y el POI
	private float azimuth;

	// inclinación del dispositivo al POI, se obtiene con la altitud
	private float inclination = 0;

	private String name;
	private String source;



	/**
	 *
	 * @param location
	 * @param deviceLocation
	 */
	public POI(Location location, Location deviceLocation) {
		this.location = location;
		this.setDeviceLocation(deviceLocation);
		azimuth = deviceLocation.bearingTo(location);
		distance = deviceLocation.distanceTo(location);

		// Calculamos la inclinación a la que está el POI con respecto a nuestra
		// ubicación
		if (deviceLocation.hasAltitude() && location.hasAltitude()) {
			double altitudeDiff;
			boolean negative = false;
			if (location.getAltitude() > deviceLocation.getAltitude()) {
				altitudeDiff = location.getAltitude()
						- deviceLocation.getAltitude();
			} else {
				altitudeDiff = deviceLocation.getAltitude()
						- location.getAltitude();
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
	 * @return the location
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * @param location
	 *            the location to set
	 */
	public void setLocation(Location location) {
		this.location = location;
	}

	/**
	 * @return the distance
	 */
	public float getDistance() {
		return distance;
	}

	/**
	 * @param distance
	 *            the distance to set
	 */
	public void setDistance(float distance) {
		this.distance = distance;
	}

	/**
	 * @return the azimuth
	 */
	public float getAzimuth() {
		return azimuth;
	}

	/**
	 * @param azimuth
	 *            the azimuth to set
	 */
	public void setAzimuth(float azimuth) {
		this.azimuth = azimuth;
	}

	/**
	 * @return the inclination
	 */
	public float getInclination() {
		return inclination;
	}

	/**
	 * @param inclination
	 *            the inclination to set
	 */
	public void setInclination(float inclination) {
		this.inclination = inclination;
	}

	public void setDeviceLocation(Location deviceLocation) {
		POI.deviceLocation = deviceLocation;
	}

	public Location getDeviceLocation() {
		return deviceLocation;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSource() {
		return source;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
