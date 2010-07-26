package mx.unam.fciencias.ardroid.app;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

/**
 * Creado por: Sebasti‡n Garc’a Anderman
 * UNAM | Facultad de Ciencias | Ciencias de la Computaci—n
 * Fecha: 25-jul-2010
 */
public class Radar extends View {

    Paint radarPaint;
    Paint directionTextPaint;
    Paint poiInSightPaint;
    Paint poiNotInSightPaint;
    Paint radarLinesPaint;

    private int radius;
    private int xPosition;
    private int yPosition;

    public static float direction;

    private float range;
    private float scale;

    private String directionText;

    public Radar() {
        super(Main.context);
        initPaints();
        radius = 50;
        xPosition = 240;
        yPosition = 160;
        directionText = "0¼ N";

        direction = 0;

        range = 100;
        scale = (float) radius / range;
    }

    private void initPaints() {
        radarPaint = new Paint();
        radarPaint.setColor(Color.argb(180, 0, 0, 200));
        radarPaint.setAntiAlias(true);

        radarLinesPaint = new Paint();
        radarLinesPaint.setColor(Color.WHITE);

        directionTextPaint = new Paint();
        directionTextPaint.setColor(Color.WHITE);
        directionTextPaint.setAntiAlias(true);
        directionTextPaint.setFakeBoldText(true);
        directionTextPaint.setTextSize(20);

        poiInSightPaint = new Paint();
        poiInSightPaint.setColor(Color.GREEN);

        poiNotInSightPaint = new Paint();
        poiNotInSightPaint.setColor(Color.RED);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawCircle(xPosition, yPosition, radius, radarPaint);
        canvas.drawText(directionText, xPosition - 20, yPosition - radius + 10, directionTextPaint);
        canvas.drawLine(xPosition, yPosition, (int) (Math.cos(Math.toRadians(225)) * radius) + xPosition, (int) (Math.sin(Math.toRadians(225)) * radius) + yPosition, radarLinesPaint);
        canvas.drawLine(xPosition, yPosition, (int) (Math.cos(Math.toRadians(315)) * radius) + xPosition, (int) (Math.sin(Math.toRadians(315)) * radius) + yPosition, radarLinesPaint);
        for (POI poi : ARLayer.poiList) {
            canvas.drawCircle((float) (Math.cos(Math.toRadians(poi.getAzimuth() + 270 - direction)) * poi.getDistance() * scale) + xPosition, (float) (Math.sin(Math.toRadians(poi.getAzimuth() + 270 - direction)) * poi.getDistance() * scale) + yPosition, 2, poiNotInSightPaint);
            Log.d("radar", "dibujando " + poi.getName() + " en: " + (float) (Math.cos(Math.toRadians(poi.getAzimuth() + 270 + direction)) * poi.getDistance() * scale) + xPosition + ", " + (float) (Math.sin(Math.toRadians(poi.getAzimuth() + 270 + direction)) * poi.getDistance() * scale) + yPosition);
        }

        super.onDraw(canvas);
    }
}