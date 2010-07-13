package mx.unam.fciencias.ardroid.app;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Clase que implementa la vista previa de la cámara del dispositivo.
 *
 * @author Sebastián García Anderman
 */
public class CameraPreview extends SurfaceView implements
		SurfaceHolder.Callback {

	SurfaceHolder surfaceHolder;
	Camera camera;

	CameraPreview() {
		super(Main.context);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		camera = Camera.open();
		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException exception) {
			camera.release();
			camera = null;
			// TODO: add more exception handling logic here
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		camera.stopPreview();
		camera.release();
		camera = null;
	}

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.05;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Camera.Size size : sizes) {

			Log.d("Sizes", "W: " + size.width + ", H: " + size.height);
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Camera.Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.

		Log.d("Size", "w: " + w + ", h: " + h);

		Camera.Parameters parameters = camera.getParameters();

		// En el emulador truena con optimalSize, en el teléfono truena con
		// valores fijos
		if (android.os.Build.DEVICE.indexOf("generic") != -1) {
			parameters.setPreviewSize(w, h);
		} else {
			List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
			Camera.Size optimalSize = getOptimalPreviewSize(sizes, w, h);
			parameters.setPreviewSize(optimalSize.width, optimalSize.height);
			Log.d("optimalSize", "W: " + optimalSize.width + ", H: "
					+ optimalSize.height);
		}
		camera.setParameters(parameters);
		camera.startPreview();
	}
}
