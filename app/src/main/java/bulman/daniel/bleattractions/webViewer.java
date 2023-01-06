package bulman.daniel.bleattractions;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class webViewer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {//load url if present
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_viewer);
        Intent intent=getIntent();
        if(intent.getStringExtra("urlToLoad")!=null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);//web settings to make browser full fat and add progress message for loading
            progressDialog.setMessage("Loading Web Page...");
            progressDialog.setCancelable(false);
            WebView webBrowser = findViewById(R.id.webWindow);
            webBrowser.requestFocus();
            webBrowser.getSettings().setLightTouchEnabled(true);
            webBrowser.getSettings().setJavaScriptEnabled(true);
            webBrowser.getSettings().setGeolocationEnabled(true);
            webBrowser.setSoundEffectsEnabled(true);
            webBrowser.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
            webBrowser.getSettings().setUseWideViewPort(true);
            webBrowser.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
            webBrowser.setWebChromeClient(new WebChromeClient(){
                public void onProgressChanged(WebView view, int progress) {
                    if (progress < 100) {
                        progressDialog.show();
                    }
                    if (progress == 100) {
                        progressDialog.dismiss();
                    }
                }
            });
            webBrowser.setWebViewClient(new WebViewClient(){
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    if(!request.getUrl().toString().startsWith("http://") && !request.getUrl().toString().startsWith("https://")){
                        Toast.makeText(getApplicationContext(),"Cannot Load in web view!",Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    view.loadUrl(request.getUrl().toString());
                    return super.shouldOverrideUrlLoading(view, request);
                }
            });
            if(intent.getBooleanExtra("isOffline",false)){//attempt load from local file if present
                try {
                    InputStream is = getApplicationContext().openFileInput(intent.getStringExtra("urlToLoad"));//load json object
                    int size = is.available();
                    byte[] buffer = new byte[size];
                    is.read(buffer);
                    is.close();
                    String webInput = new String(buffer, StandardCharsets.UTF_8);
                    webBrowser.loadData(webInput,"text/html; charset=UTF-8", null);
                } catch (IOException e) {//on error warn user the file was not accessible
                    Toast.makeText(getApplicationContext(),"File "+ intent.getStringExtra("urlToLoad")+" does not exist!",Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            else {//load webpage from online if we are using online web viewer
                webBrowser.loadUrl(intent.getStringExtra("urlToLoad"));
            }
        }
        else{//display error on failed load
            Toast.makeText(getApplicationContext(),"No Url was provided!",Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}