package mx.unam.fciencias.ardroid.app;

import android.util.Log;

/**
 * Created by IntelliJ IDEA.
 * User: lander
 * Date: 19/04/11
 * Time: 18:32
 * To change this template use File | Settings | File Templates.
 */
public class SensorOptimalFilter {

    private static float direction;
    private static float directionB;
    private static float prevDirection = 0;
    private static float prevDirectionB = 0;

    private static float inclination;
    private static float prevInclination;

    private static final float LOW_PASS_FILTER_COEF = 0.15f;
    private static final float NOISE_MAX_AMPLITUDE_DIRECTION = 8;
    private static final float NOISE_MAX_AMPLITUDE_INCLINATION = 3;

    private static final float DIRECTION_THRESHOLD = 1f;
    private static final float INCLINATION_THRESHOLD = 0.5f;

    public static boolean directionChanged = false;
    public static boolean inclinationChanged = false;


    private static float optimalFilter(float newInputValue, float priorOutputValue, float noiseMaxAmplitude) {
        float newOutputValue = newInputValue;
        if (Math.abs(newInputValue - priorOutputValue) <= noiseMaxAmplitude) { // Simple low-pass filter
            newOutputValue = priorOutputValue + LOW_PASS_FILTER_COEF * (newInputValue - priorOutputValue);
        }
        return newOutputValue;
    }

    public static float filterDirection(float dir) {
        directionChanged = false;
        direction = optimalFilter(dir, prevDirection, NOISE_MAX_AMPLITUDE_DIRECTION);
        //Log.d("filtro", "Dir: " + direction);

        //Si la dirección es menor que 180 sumamos 360 para que haya continuidad en el cambio de 359º a 0º
        if (direction <= 180) {
            directionB = direction + 360;
        } else {
            directionB = direction;
        }

        directionB = optimalFilter(directionB, prevDirectionB, NOISE_MAX_AMPLITUDE_DIRECTION);

        //if (changeAboveThreshold(prevDirection, direction, DIRECTION_THRESHOLD)) {
//            directionChanged = true;
//            prevDirection = direction;
//            prevDirectionB = directionB;
        //}

        directionChanged = true;
        prevDirection = direction;
        prevDirectionB = directionB;

        /**
         * Si la dirección es menor que 90 o mayor que 270 regresamos el valor del segundo filtro.
         * Le restamos 360 antes de regresar el valor para que sea consistente, pero en su arreglo se
         * guarda antes de esta resta.
         */
        if (direction < 90) {
            direction = directionB - 360;
        }
        if (direction > 270) {
            direction = directionB;
        }

        if (direction < 0)
            direction += 360;
        if (direction >= 360)
            direction -= 360;

        return direction;
    }

    public static float filterInclination(float inc) {
        inclinationChanged = false;
        inclination = optimalFilter(inc, prevInclination, NOISE_MAX_AMPLITUDE_INCLINATION);
        //if (changeAboveThreshold(prevInclination, inclination, INCLINATION_THRESHOLD)) {
        inclinationChanged = true;
        prevInclination = inclination;
        //}
        return inclination;
    }


    /**
     * Calculamos si la diferencia entre ambos valores está arriba de cierto
     * umbral. Usamos este método ya que a veces el cambio de dirección o
     * inclinación es mínimo y no necesitamos recalcular la posición de los POI.
     *
     * @param val1      Primer valor
     * @param val2      Segundo valor
     * @param threshold Umbral
     * @return Verdadero si la diferencia entre <code>val1</code> y
     *         <code>val2</code> es mayor que <code>threshold</code>
     */
    private static boolean changeAboveThreshold(float val1, float val2, float threshold) {
        return Math.abs(val1 - val2) >= threshold;
    }
}
