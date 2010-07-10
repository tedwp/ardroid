package mx.unam.fciencias;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class ARDroidSensorNoiseFilterTest extends Activity {

	public static volatile Context context;
	public ARLayerSensorNoiseFiltertest arLayer;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getApplicationContext();
		initWindowParameters();
		arLayer = new ARLayerSensorNoiseFiltertest(context);
		setContentView(arLayer);
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
}