package bulman.daniel.bleattractions;

import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class OfflineExplore extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_explore);
        //read local json file for database and attempt to get websites that can be loaded
        try {
            String json="";//reads password from pass.json
            try {
                InputStream is = getAssets().open("offlineData.json");//reads from the password file to get password for database (only stored locally)
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                json = new String(buffer, StandardCharsets.UTF_8);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try {
                JSONObject obj = new JSONObject(json);
                JSONArray devices =obj.getJSONArray("bledevices");
                JSONArray organisations= obj.getJSONArray("organisations");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
    }
}