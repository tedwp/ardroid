package mx.unam.fciencias.ardroid.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Creado por: Sebastián García Anderman
 * UNAM | Facultad de Ciencias | Ciencias de la Computación
 * Fecha: 26-jul-2010
 */
public class RangeSeekBar extends LinearLayout {
    
    private SeekBar seekBar;
    private TextView textView;

    public RangeSeekBar() {
        super(Main.context);

        LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        li.inflate(R.layout.rangeseekbar, this, true);

        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        seekBar.setMax(200);
        seekBar.setProgress(ARLayer.range/100);
        seekBar.setOnSeekBarChangeListener(rangeSeekBarListener);

        textView = (TextView) findViewById(R.id.seek_bar_text);
    }

    final SeekBar.OnSeekBarChangeListener rangeSeekBarListener = new SeekBar.OnSeekBarChangeListener() {

        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            textView.setText(((float) i / 10) + " km");
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            textView.setVisibility(VISIBLE);
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            textView.setVisibility(INVISIBLE);
            ARLayer.range = seekBar.getProgress()*100;
            for (POI poi : ARLayer.poiList) {
                poi.updateValues();
            }
            Radar.setRange();
        }
    };
}
