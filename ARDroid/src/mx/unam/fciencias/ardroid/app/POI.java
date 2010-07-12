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
	private Location deviceLocation;// Ubicación del dispositivo

	// distancia entre el dispositivo y el POI
	private float distance;

	// grados hacia el Este desde el Norte entre el dispositivo y el POI
	private float azimuth;

	// inclinación del dispositivo al POI, se obtiene con la altitud
	private float inclination = 0;

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
		if (deviceLocation.hasAccuracy() && location.hasAltitude()) {
			double opposite;
			boolean neg = false;
			if (location.getAltitude() > deviceLocation.getAltitude()) {
				opposite = location.getAltitude()
						- deviceLocation.getAltitude();
			} else {
				opposite = deviceLocation.getAltitude()
						- location.getAltitude();
				neg = true;
			}
			setInclination((float) Math
					.atan(((double) opposite / getDistance())));
			if (neg)
				setInclination(getInclination() * -1);
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
		this.deviceLocation = deviceLocation;
	}

	public Location getDeviceLocation() {
		return deviceLocation;
	}

}
