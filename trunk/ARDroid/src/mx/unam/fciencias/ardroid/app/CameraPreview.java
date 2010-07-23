package mx.unam.fciencias.ardroid.app;

import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

// TODO: Auto-generated Javadoc

/**
 * Clase que implementa la vista previa de la cámara del dispositivo.
 *
 * @author Sebastián García Anderman
 */
public class CameraPreview extends SurfaceView implements
        SurfaceHolder.Callback {

    /**
     * The surface holder.
     */
    SurfaceHolder surfaceHolder;

    /**
     * The camera.
     */
    Camera camera;

    /**
     * Instantiates a new camera preview.
     */
    CameraPreview() {
        super(Main.context);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /* (non-Javadoc)
      * @see android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder)
      */

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

    /* (non-Javadoc)
      * @see android.view.SurfaceHolder.Callback#surfaceDestroyed(android.view.SurfaceHolder)
      */

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
        Log.d("stop", "onStop surfaceDestroyed");
        if (camera != null) {
            Log.d("stop", "onStop surfaceDestroyed no era nula");
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    /**
     * Gets the optimal preview size.
     *
     * @param sizes the sizes
     * @param w     the w
     * @param h     the h
     * @return the optimal preview size
     */
    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;
        if (sizes == null)
            return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {

            Log.d("Sizes", "W: " + size.width + ", H: " + size.height);
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - h);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - h);
                }
            }
        }
        return optimalSize;
    }

    /* (non-Javadoc)
      * @see android.view.SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder, int, int, int)
      */

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

    public void onStop() {
        Log.d("stop", "onStop CameraPreview");
        if (camera != null) {
            Log.d("stop", "no se había liberado");
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
}
