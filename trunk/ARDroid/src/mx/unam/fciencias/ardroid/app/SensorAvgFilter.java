package mx.unam.fciencias.ardroid.app;

import java.util.ArrayList;

public class SensorAvgFilter {

    private static ArrayList<Float> directions;
    private static ArrayList<Float> directionsB;

    private static ArrayList<Float> avgRollingZ;
    private static ArrayList<Float> avgRollingX;

    // Usamos un filtro de promedios con las últimas <code>AVG_NUM</code>
    // lecturas de los sensores para determinar la nueva dirección e
    // inclinación del teléfono. Hacemos esto para filtrar el ruido de los
    // sensores.
    private static final int AVG_NUM = 8;

    private static float tmpDirection;
    private static float avgLocalDirection;
    private static float avgLocalDirectionB;
    private static float prevAvgLocalDirection = 0;
    private static float prevAvgLocalDirectionB = 0;
    private static float avgSum;

    private static float avgRollZSum;
    private static float avgRollXSum;
    private static float avgInclination;
    private static float prevAvgInclination = 0;

    private static final float DIRECTION_THRESHOLD = 0.0f;
    private static final float INCLINATION_THRESHOLD = 0;

    public static boolean directionChanged = false;
    public static boolean inclinationChanged = false;

    /**
     * Inicializamos los <code>ArrayList</code> utilizados para llevar el promedio de los filtros
     */
    public static void initAvgArrays() {
        directions = new ArrayList<Float>();
        avgRollingZ = new ArrayList<Float>();
        avgRollingX = new ArrayList<Float>();

        directionsB = new ArrayList<Float>();
        for (int i = 0; i < (AVG_NUM - 1); i++) {
            directions.add(0f);
            avgRollingZ.add(0f);
            avgRollingX.add(0f);

            directionsB.add(0f);
        }
    }

    /**
     * Filtramos los valores del sensor de orientación.
     * Usamos dos filtros para resolver el problema de cuando la dirección cambia de 360 a 0 y viceversa.
     * Usamos un filtro para cuando la dirección es mayor de 180 y cambiamos a ese filtro cuando la dirección
     * menor a 90 o mayor a 270. Este filtro suma 360 a sus valores por lo que el cambio de 360 a 0 se vuelve
     * un cambio de 360 a 361 y entonces el filtro se vuelve efectivo.
     *
     * @param v0 Valor obtenido por sensor de orientación
     * @return Valor filtrado de orientación del dispositivo
     */
    public static float orientationListener(float v0) {
        directionChanged = false;
        tmpDirection = v0;
        if (tmpDirection < 0) {
            avgLocalDirection = 360 + tmpDirection;
            tmpDirection += 360;
        } else {
            avgLocalDirection = tmpDirection;
        }

        /**
         * Segundo filtro usado para los cambios de 360 a 0 y viceversa
         */
        if (avgLocalDirection > 180) {
            avgLocalDirectionB = avgLocalDirection;
        } else {
            avgLocalDirectionB = avgLocalDirection + 360;
        }
        avgSum = 0;
        for (int i = 0; i < (AVG_NUM - 1); i++) {
            avgSum += directionsB.get(i);
        }

        avgSum += avgLocalDirectionB;

        directionsB.add(avgLocalDirectionB);
        avgLocalDirectionB = avgSum / AVG_NUM;
        directionsB.remove(0);
        if (changeAboveThreshold(prevAvgLocalDirectionB, avgLocalDirectionB, DIRECTION_THRESHOLD)) {
            directionChanged = true;
            prevAvgLocalDirectionB = avgLocalDirectionB;
        }


        avgSum = 0;
        for (int i = 0; i < (AVG_NUM - 1); i++) {
            avgSum += directions.get(i);
        }

        avgSum += avgLocalDirection;

        directions.add(avgLocalDirection);
        avgLocalDirection = avgSum / AVG_NUM;
        directions.remove(0);
        if (changeAboveThreshold(prevAvgLocalDirection, avgLocalDirection, DIRECTION_THRESHOLD)) {
            directionChanged = true;
            prevAvgLocalDirection = avgLocalDirection;
        }


        /**
         * Si la dirección es menor que 90 o mayor que 270 regresamos el valor del segundo filtro.
         * Le restamos 360 antes de regresar el valor para que sea consistente, pero en su arreglo se
         * guarda antes de esta resta.
         */
        if (tmpDirection < 90) {
            avgLocalDirectionB -= 360;
            if (avgLocalDirectionB < 0) {
                return avgLocalDirectionB + 360;
            } else {
                return avgLocalDirectionB;
            }
        } else if (tmpDirection > 270) {
            if (avgLocalDirectionB < 0) {
                return avgLocalDirectionB + 360;
            } else {
                return avgLocalDirectionB;
            }
        }
        return avgLocalDirection;
    }

    public static float accelerometerListener(float v0, float v2) {
        inclinationChanged = false;
        avgRollZSum = 0;
        avgRollXSum = 0;
        for (int i = 0; i < (AVG_NUM - 1); i++) {
            avgRollZSum += avgRollingZ.get(i);
        }

        for (int i = 0; i < (AVG_NUM - 1); i++) {
            avgRollXSum += avgRollingX.get(i);
        }

        avgRollZSum += v2;
        avgRollXSum += v0;

        avgRollZSum = avgRollZSum / AVG_NUM;
        avgRollXSum = avgRollXSum / AVG_NUM;

        avgRollingZ.add(v2);
        avgRollingZ.remove(0);

        avgRollingX.add(v0);
        avgRollingX.remove(0);

        if (avgRollZSum != 0.0) {
            avgInclination = (float) Math.atan(avgRollXSum / avgRollZSum);
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
        }
        return avgInclination;
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
        return Math.abs(val1 - val2) > threshold;
    }

}
