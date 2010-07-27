package mx.unam.fciencias.ardroid.app;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

/**
 * Clase que muestra un radar en pantalla con los POI en la dirección en que se encuentran
 *
 * @author Sebastián García Anderman
 */
public class Radar extends View {

    Paint radarPaint;
    Paint directionTextPaint;
    Paint poiInSightPaint;
    Paint poiNotInSightPaint;
    Paint radarLinesPaint;

    private static int radius;
    private int xPosition;
    private int yPosition;

    public static float direction;

    private float range;
    private static float scale;

    private static String directionText = "";

    private int directionTextXPos;
    private int directionTextYPos;

    private float radarLineLeftXEndPos;
    private float radarLineLeftYEndPos;
    private float radarLineRightXEndPos;
    private float radarLineRightYEndPos;
    private Paint semiCirclePaint;
    private RectF semiCircleRect;

    /**
     * Construimos el Radar
     */
    public Radar() {
        super(Main.context);
        calcDimensions(ARLayer.screenHeight);
        initPaints();
    }

    /**
     * Calculamos las dimensiones del radar, es 12.5% de la altura de la pantalla.
     * Calculamos las posiciones de los distintos elementos del radar.
     *
     * @param screenHeight Altura de la pantalla en modo landscape
     */
    private void calcDimensions(int screenHeight) {
        radius = screenHeight / 8;
        xPosition = radius + 10;
        yPosition = radius + 10;
        direction = 0;
        scale = (float) radius / range;

        directionTextXPos = xPosition;
        directionTextYPos = yPosition - radius + 10;
        radarLineLeftXEndPos = (float) ((Math.cos(Math.toRadians(225)) * radius) + xPosition);
        radarLineLeftYEndPos = (float) (Math.sin(Math.toRadians(225)) * radius) + yPosition;
        radarLineRightXEndPos = (float) (Math.cos(Math.toRadians(315)) * radius) + xPosition;
        radarLineRightYEndPos = (float) (Math.sin(Math.toRadians(315)) * radius) + yPosition;

        setRange();
    }

    /**
     * Iniciamos los elementos usados para pintar en <code>onDraw</code>
     */
    private void initPaints() {
        radarPaint = new Paint();
        radarPaint.setColor(Color.argb(180, 0, 0, 200));
        radarPaint.setAntiAlias(true);

        radarLinesPaint = new Paint();
        radarLinesPaint.setColor(Color.WHITE);
        radarLinesPaint.setAntiAlias(true);

        directionTextPaint = new Paint();
        directionTextPaint.setColor(Color.WHITE);
        directionTextPaint.setAntiAlias(true);
        directionTextPaint.setFakeBoldText(true);
        directionTextPaint.setTextSize(20);
        directionTextPaint.setTextAlign(Paint.Align.CENTER);

        poiInSightPaint = new Paint();
        poiInSightPaint.setColor(Color.GREEN);

        poiNotInSightPaint = new Paint();
        poiNotInSightPaint.setColor(Color.RED);

        semiCirclePaint = new Paint();
        semiCirclePaint.setColor(Color.argb(200, 0, 0, 140));

        semiCircleRect = new RectF(xPosition - radius, yPosition - radius, xPosition + radius, yPosition + radius);
    }

    /**
     * Ponemos la dirección en la que está viendo el telófono
     *
     * @param dir Dirección a la que apunta el telófono
     */
    public static void setDirection(float dir) {
        direction = dir;
        directionText = (int) dir + "º";
    }

    /**
     * Cambiamos el rango en el que estámos desplegando POIs, y recalculamos la escala
     *
     */
    public static void setRange() {
        scale = (float) radius / ARLayer.range;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(xPosition, yPosition, radius, radarPaint);
        canvas.drawArc(semiCircleRect, 225, 90, true, semiCirclePaint);
        canvas.drawText(directionText, directionTextXPos, directionTextYPos, directionTextPaint);
        canvas.drawLine(xPosition, yPosition, radarLineLeftXEndPos, radarLineLeftYEndPos, radarLinesPaint);
        canvas.drawLine(xPosition, yPosition, radarLineRightXEndPos, radarLineRightYEndPos, radarLinesPaint);
        for (POI poi : ARLayer.poiList) {
            if (poi.isVisibleInRange()) {
                canvas.drawCircle((float) (Math.cos(Math.toRadians(poi.getAzimuth() + 270 - direction)) * poi.getDistance() * scale) + xPosition,
                        (float) (Math.sin(Math.toRadians(poi.getAzimuth() + 270 - direction)) * poi.getDistance() * scale) + yPosition,
                        2, poiNotInSightPaint);
            }
        }
        super.onDraw(canvas);
    }
}