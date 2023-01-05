package bulman.daniel.bleattractions;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Toast;

public class webViewer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {//load url if present
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_viewer);
        Intent intent=getIntent();
        if(intent.getStringExtra("urlToLoad")!=null) {
            WebView webBrowser = findViewById(R.id.webWindow);
            webBrowser.loadUrl(intent.getStringExtra("urlToLoad"));
        }
        else{//display error on failed load
            Toast.makeText(getApplicationContext(),"No Url was provided!",Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}