package mx.unam.fciencias.ardroid.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.PopupWindow;

/**
 * Creado por: Sebasti‡n Garc’a Anderman
 * UNAM | Facultad de Ciencias | Ciencias de la Computaci—n
 * Fecha: 22-jul-2010
 */
public class POIPopup extends PopupWindow {

    public POIPopup() {
        super(((LayoutInflater) Main.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popup, null, false));
        
    }
}
