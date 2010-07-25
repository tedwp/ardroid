package mx.unam.fciencias.ardroid.app;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;

/**
 * Creado por: Sebasti‡n Garc’a Anderman
 * UNAM | Facultad de Ciencias | Ciencias de la Computaci—n
 * Fecha: 22-jul-2010
 */
public class POIPopup extends PopupWindow {

    WebView wv;
    private static boolean shown = false;
    private TextView poiTitle;
    private TextView poiDistance;
    private ProgressBar progressBar;
    private RelativeLayout progressBarLayout;


    public POIPopup(int screenWidth, int screenHeight) {
        super(Main.context);
        View v = LayoutInflater.from(Main.context).inflate(R.layout.popup, null);
        this.setWidth(screenWidth - (screenWidth / 20));
        this.setHeight(screenHeight - (screenHeight / 40));
        this.setAnimationStyle(R.style.Animation_Popup);
        this.setFocusable(true);
        this.update();
        this.setContentView(v);
        ImageButton b = (ImageButton) v.findViewById(R.id.closebutton);
        poiTitle = (TextView) v.findViewById(R.id.title);
        poiDistance = (TextView) v.findViewById(R.id.distance);
        progressBar = (ProgressBar) v.findViewById(R.id.progressbar_popup);
        progressBarLayout = (RelativeLayout) v.findViewById(R.id.progressbar_layout);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dismiss();
            }
        });

        wv = (WebView) v.findViewById(R.id.webView);
        wv.getSettings().setJavaScriptEnabled(true);

        wv.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                /*
                Activities and WebViews measure progress with different scales.
                The progress meter will automatically disappear when we reach 100%
                */
                progressBar.setProgress(progress);
                if(progress == 100){
                    progressBarLayout.setVisibility(ProgressBar.GONE);
                }
            }
        });
        wv.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(Main.context, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }
        });

    }

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

    public void show(POI poi) {
        poiTitle.setText(poi.getName());
        poiDistance.setText(Main.context.getText(R.string.distance_text_popup) + " " + Float.toString(poi.getDistance()) + "m");
        wv.loadData("", "text/html", "utf-8");
        if (poi.getInfoUrl() != null) {
            progressBarLayout.setVisibility(ProgressBar.VISIBLE);
            progressBar.setProgress(0);
            wv.loadUrl(poi.getInfoUrl());
        } else {
            wv.loadData("<h2>"+Main.context.getString(R.string.no_extra_data_popup)+"</h2>", "text/html", "utf-8");
        }
        showAtLocation(ARLayer.arView, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 0);
    }
}
