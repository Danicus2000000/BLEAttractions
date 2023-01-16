package bulman.daniel.bleattractions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class offlineQueueExplorer extends AppCompatActivity {
    private Map<String,String> queueData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_queue_explorer);
        TextView display = findViewById(R.id.queueTimes);
        queueData=new HashMap<>();
        JSONObject doc=connectToJSON();
        try {
            JSONArray bledevices = doc.getJSONArray("bledevices");
            for (int i = 0; i < bledevices.length(); i++) {
                queueData.put(bledevices.getJSONObject(i).getString("BleDeviceTitle"), bledevices.getJSONObject(i).getString("BleDeviceQueueTime"));
            }
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(),"Offline data corrupted!",Toast.LENGTH_SHORT).show();
            finish();
        }
        StringBuilder toSet=new StringBuilder();
        for(Map.Entry<String,String> entry : queueData.entrySet()){
            toSet.append(entry.getKey()).append(":\n").append(entry.getValue()).append("\n");
        }
        display.setText(toSet.toString());
    }
    private JSONObject connectToJSON(){
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
                return new JSONObject(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
        return null;
    }
}