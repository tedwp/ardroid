package mx.unam.fciencias.ardroid.aux;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

/**
 * Clase para principal para la prueba de filtros de reducción de ruido (Kalman
 * y promedios)
 *
 * @author Sebastián García Anderman
 *
 */
public class ARDroidSensorNoiseFilterTest extends Activity {

	public static volatile Context context;
	public ARLayerSensorNoiseFiltertest arLayer;

	String kDirFileString = "kDirFile";
	String aDirFileString = "aDirFile";
	String rDirFileString = "rDirFile";
	String kIncFileString = "kIncFile";
	String aIncFileString = "aIncFile";
	String rIncFileString = "rIncFile";

	PrintWriter kDirPW;
	PrintWriter aDirPW;
	PrintWriter rDirPW;
	PrintWriter kIncPW;
	PrintWriter aIncPW;
	PrintWriter rIncPW;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getApplicationContext();
		initWindowParameters();
		try {

			File kDirFile =new
			File(Environment.getExternalStorageDirectory()+"/"+kDirFileString);
			kDirFile.createNewFile();
			FileOutputStream kDirfileios=new FileOutputStream(kDirFile);
			kDirPW = new PrintWriter(kDirfileios);

			File aDirFile =new
			File(Environment.getExternalStorageDirectory()+"/"+aDirFileString);
			aDirFile.createNewFile();
			FileOutputStream aDirfileios=new FileOutputStream(aDirFile);
			aDirPW = new PrintWriter(aDirfileios);

			File rDirFile =new
			File(Environment.getExternalStorageDirectory()+"/"+rDirFileString);
			rDirFile.createNewFile();
			FileOutputStream iDirfileios=new FileOutputStream(rDirFile);
			rDirPW = new PrintWriter(iDirfileios);

			File kIncFile =new
			File(Environment.getExternalStorageDirectory()+"/"+kIncFileString);
			kIncFile.createNewFile();
			FileOutputStream kIncfileios=new FileOutputStream(kIncFile);
			kIncPW = new PrintWriter(kIncfileios);

			File aIncFile =new
			File(Environment.getExternalStorageDirectory()+"/"+aIncFileString);
			aIncFile.createNewFile();
			FileOutputStream aIncfileios=new FileOutputStream(aIncFile);
			aIncPW = new PrintWriter(aIncfileios);

			File rIncFile =new
			File(Environment.getExternalStorageDirectory()+"/"+rIncFileString);
			rIncFile.createNewFile();
			FileOutputStream rIncfileios=new FileOutputStream(rIncFile);
			rIncPW = new PrintWriter(rIncfileios);

			arLayer = new ARLayerSensorNoiseFiltertest(context, kDirPW,
					aDirPW, rDirPW, kIncPW, aIncPW, rIncPW);
			setContentView(arLayer);
		} catch (FileNotFoundException e) {
			Log.e("FileOutputStream", "No se pudieron abrir los archivos");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		 kDirPW.close();
		 aDirPW.close();
		 rDirPW.close();
		 kIncPW.close();
		 aIncPW.close();
		 rIncPW.close();
		super.onPause();
	}


}