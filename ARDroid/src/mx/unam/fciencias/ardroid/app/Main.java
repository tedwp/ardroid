package mx.unam.fciencias.ardroid.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.FrameLayout;

/**
 * Actividad principal de la aplicación.
 *
 * @author Sebastián García Anderman
 */
public class Main extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * De esta manera siempre tenemos acceso al contexto de la aplicación, lo
     * hacemos de tipo volatile para que se guarde en el caché local y estático
     * para poder accederlo fácilmente desde cualquier clase. Fuente:
     * http://www.
     * hasemanonmobile.com/2009/10/05/quick-and-very-dirty-android-development
     * -trick/
     */
    public static volatile Context context;

    /**
     * Vista previa de la cámara.
     */
    private CameraPreview cameraPreview;

    /**
     * Capa que se encargará de manejar los POI.
     */
    private ARLayer arLayer;

    private static final int DIALOG_GPS_DISABLED = 0;
    private static final int DIALOG_MOVIL_DATA_DISABLED = 1;

    /**
     * Para acceder a las preferencias de la aplicación
     */
    private SharedPreferences preferences;

    /**
     * Se llama cuando la actividad se crea por primera vez.
     *
     * @param savedInstanceState the saved instance state
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        initWindowParameters();
        initComponents();
        initLayers();
        checkInternetConnection();
        TestPOIDrawing.testDrawPOI();
        getScreenSize();
        preferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void checkInternetConnection() {
        //Si no hay conexión a internet pedimos al usuario que la habilite.
        if (!HttpConnection.isOnline()) {
            showDialog(DIALOG_MOVIL_DATA_DISABLED);
        }
    }

    /**
     * Inicializamos las capas del programa.
     */
    private void initLayers() {
        setContentView(cameraPreview);
        addContentView(arLayer, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT));
        addContentView(new Radar(), new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        addContentView(new VerticalRangeSeekBar(), new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.RIGHT | Gravity.CENTER_VERTICAL));
        FrameLayout.LayoutParams accuracyDisplayLayoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.RIGHT | Gravity.TOP);
        accuracyDisplayLayoutParams.setMargins(0, 2, 2, 0);
        addContentView(ARLayer.accuracyDisplay, accuracyDisplayLayoutParams);
    }

    /**
     * Inicializamos los diferentes componentes del programa.
     */
    private void initComponents() {
        cameraPreview = new CameraPreview();
        arLayer = new ARLayer();
        //Para mantener la pantalla encendida
        arLayer.setKeepScreenOn(true);
        try {
            arLayer.onStart();
        } catch (LocationProviderNullException e) {
            showDialog(DIALOG_GPS_DISABLED);
        }
    }

    /**
     * Inicializamos los parámetros de la ventana, ponemos la orientación
     * horizontal (landscape), quitamos el título de la aplicación así como la
     * barra de estado, es decir, pantalla completa.
     */
    private void initWindowParameters() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_GPS_DISABLED:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.gps_disabled_dialog_text).setTitle(R.string.gps_disabled_dialog_title)
                        .setCancelable(false).setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dismissDialog(DIALOG_GPS_DISABLED);
                                startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                            }
                        }).setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                return builder.create();
            case DIALOG_MOVIL_DATA_DISABLED:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setMessage(R.string.movil_data_disabled_dialog_text).setTitle(R.string.movil_data_disabled_dialog_title)
                        .setCancelable(false).setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dismissDialog(DIALOG_MOVIL_DATA_DISABLED);
                                startActivityForResult(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS), 0);
                            }
                        }).setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                return builder2.create();
            default:
                return null;
        }
    }

    /*
      * (non-Javadoc)
      *
      * @see android.app.Activity#onResume()
      */

    @Override
    protected void onStart() {
        super.onStart();
        try {
            arLayer.onStart();
        } catch (LocationProviderNullException e) {
            showDialog(DIALOG_GPS_DISABLED);
        }
    }

    /*
      * (non-Javadoc)
      *
      * @see android.app.Activity#onStop()
      */

    @Override
    protected void onStop() {
        cameraPreview.onStop();
        arLayer.onStop();
        super.onStop();
    }

    private void getScreenSize() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        ARLayer.xdpi = metrics.xdpi;
        ARLayer.ydpi = metrics.ydpi;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent i = new Intent(this, Preferences.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        // TODO Check the shared preference and key parameters and change UI or // behavior as appropriate.
    }
}