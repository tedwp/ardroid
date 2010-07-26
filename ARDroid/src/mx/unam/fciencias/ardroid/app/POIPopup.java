package mx.unam.fciencias.ardroid.app;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;

/**
 * Clase que despliega informaci�n adicional de un POI cargandola desde una p�gina web.
 *
 * @author Sebasti�n Garc�a Anderman
 */
public class POIPopup extends PopupWindow {

    WebView webView;
    private static boolean shown = false;
    private TextView poiTitle;
    private TextView poiDistance;
    private ProgressBar progressBar;
    private RelativeLayout progressBarLayout;


    /**
     * Constructor de POIPopup
     *
     * @param screenWidth  Ancho de la pantalla
     * @param screenHeight Alto de la pantalla
     */
    public POIPopup(int screenWidth, int screenHeight) {
        super(Main.context);
        View view = LayoutInflater.from(Main.context).inflate(R.layout.popup, null);
        this.setWidth(screenWidth - (screenWidth / 20));
        this.setHeight(screenHeight - (screenHeight / 40));

        /**
         * Cambiamos la animaci�n de esta <code>PopupWindow</code> para que
         * aparezca desliz�ndose desde abajo y desaparezca desliz�ndose hacia abajo
         */
        this.setAnimationStyle(R.style.Animation_Popup);
        this.setFocusable(true);
        this.update();
        this.setContentView(view);
        ImageButton b = (ImageButton) view.findViewById(R.id.closebutton);
        poiTitle = (TextView) view.findViewById(R.id.title);
        poiDistance = (TextView) view.findViewById(R.id.distance);
        progressBar = (ProgressBar) view.findViewById(R.id.progressbar_popup);
        progressBarLayout = (RelativeLayout) view.findViewById(R.id.progressbar_layout);

        /**
         * Cuando se aprieta este bot�n cerramos el <code>POIPopup</code>
         */
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dismiss();
            }
        });

        initWebView(view);
    }

    /**
     * Configuramos la <code>WebView</code> para que tenga javascript
     * y reporte el progreso de carga
     *
     * @param view View donde se encuentra el WebView a configurar
     */
    private void initWebView(View view) {
        webView = (WebView) view.findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebChromeClient(new WebChromeClient() {

            /**
             * Desplegamos el progreso de carga de esta p�gina, cuando llega al 100%
             * desaparecemos la <code>ProgressBar</code>
             * @param view
             * @param progress
             */
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(progress);
                if (progress == 100) {
                    progressBarLayout.setVisibility(ProgressBar.GONE);
                }
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(Main.context, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Es igual que el m�todo padre, simplemente prendemos una bandera
     * para evitar que se despliegue dos veces
     *
     * @param parent
     * @param gravity
     * @param x
     * @param y
     */
    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        if (!shown) {
            super.showAtLocation(parent, gravity, x, y);
            shown = true;
        }
    }

    @Override
    public void dismiss() {
        shown = false;
        super.dismiss();
    }

    /**
     * Mostramos esta <code>POIPopup</code> de acuerdo al <code>POI</code> que recibamos como par�metro
     *
     * @param poi POI sobre el cual debemos desplegar informaci�n extra
     */
    public void show(POI poi) {
        poiTitle.setText(poi.getName());
        poiDistance.setText(Main.context.getText(R.string.distance_text_popup) + " " + Float.toString(poi.getDistance()) + "m");
        /**
         * Antes de cargar la nueva p�gina ponemos el WebView en blanco
         * para borrar la p�gina cargada en otra llamada a este m�todo
         */
        webView.loadData("", "text/html", "utf-8");
        /**
         * En caso de que el POI no tenga un URL con informaci�n extra notificamos al usuario de esto.
         */
        if (poi.getInfoUrl() != null) {
            progressBarLayout.setVisibility(ProgressBar.VISIBLE);
            progressBar.setProgress(0);
            webView.loadUrl(poi.getInfoUrl());
        } else {
            webView.loadData("<h2>" + Main.context.getString(R.string.no_extra_data_popup) + "</h2>", "text/html", "utf-8");
        }
        showAtLocation(ARLayer.arView, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 0);
    }
}