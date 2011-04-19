package mx.unam.fciencias.ardroid.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import mx.unam.fciencias.ardroid.gui.verticalseekbar.VerticalSeekBar;

/**
 * Creado por: Sebastián García Anderman
 * UNAM | Facultad de Ciencias | Ciencias de la Computación
 * Fecha: 26-jul-2010
 */
public class VerticalRangeSeekBar extends LinearLayout {

    private VerticalSeekBar seekBar;
    private TextView textView;

    public VerticalRangeSeekBar() {
        super(Main.context);

        LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        li.inflate(R.layout.rangeseekbarvertical, this, true);

        seekBar = (VerticalSeekBar) findViewById(R.id.vertical_seek_bar);
        seekBar.setMax(1000);
        //Rango en el que se encontrará la barra por defecto
        seekBar.setProgress((int) (Math.sqrt(ARLayer.range)*10));
        seekBar.setOnSeekBarChangeListener(rangeSeekBarListener);

        textView = (TextView) findViewById(R.id.vertical_seek_bar_text);
    }

    final VerticalSeekBar.OnSeekBarChangeListener rangeSeekBarListener = new VerticalSeekBar.OnSeekBarChangeListener() {

        public void onProgressChanged(VerticalSeekBar seekBar, int progress, boolean fromUser) {
            textView.setText(((float)Math.round((float)Math.pow(progress, 2)/1000)/10f) + " km");
        }

        public void onStartTrackingTouch(VerticalSeekBar seekBar) {
            textView.setVisibility(VISIBLE);
        }

        public void onStopTrackingTouch(VerticalSeekBar seekBar) {
            textView.setVisibility(INVISIBLE);
            ARLayer.range = (int) (Math.pow(seekBar.getProgress(), 2)/10);
            for (POI poi : ARLayer.poiList) {
                poi.updateValues();
            }
            Radar.setScale();
        }
    };
}
