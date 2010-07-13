package mx.unam.fciencias.ardroid.app;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/**
 * Actividad principal de la aplicación.
 *
 * @author Sebastián García Anderman
 */
public class Main extends Activity {

	/**
	 * De esta manera siempre tenemos acceso al contexto de la aplicación, lo
	 * hacemos de tipo volatile para que se guarde en el caché local y estático
	 * para poder accederlo fácilmente desde cualquier clase. Fuente:
	 * http://www.
	 * hasemanonmobile.com/2009/10/05/quick-and-very-dirty-android-development
	 * -trick/
	 */
	public static volatile Context context;

	private CameraPreview cameraPreview;
	private ARLayer arLayer;

	/** Se llama cuando la actividad se crea por primera vez. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getApplicationContext();
		initWindowParameters();
		initComponents();
		initLayers();
	}

	/**
	 * Inicializamos las capas del programa.
	 */
	private void initLayers() {
		setContentView(cameraPreview);
		addContentView(arLayer, new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT));
	}

	/**
	 * Inicializamos los diferentes componentes del programa.
	 */
	private void initComponents() {
		cameraPreview = new CameraPreview(context);
		arLayer = new ARLayer(context);
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