package mx.unam.fciencias.ardroid.app;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Clase para mostrar las preferencias
 */
public class Preferences extends PreferenceActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
