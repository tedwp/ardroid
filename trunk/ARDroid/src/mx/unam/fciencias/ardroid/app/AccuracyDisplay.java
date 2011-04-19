package mx.unam.fciencias.ardroid.app;

import android.graphics.Color;
import android.widget.TextView;

/**
 * Creado por: Sebastián García Anderman
 * UNAM | Facultad de Ciencias | Ciencias de la Computación
 * Fecha: 28-jul-2010
 */
public class AccuracyDisplay extends TextView{
    public AccuracyDisplay() {
        super(Main.context);
        setTextColor(Color.WHITE);
        setTextSize(14);
        setText(Main.context.getText(R.string.accuracy)+" ");
    }

    public void changeAccuracy(float accuracy) {
        setText(Main.context.getText(R.string.accuracy)+" " + accuracy);
    }
}
