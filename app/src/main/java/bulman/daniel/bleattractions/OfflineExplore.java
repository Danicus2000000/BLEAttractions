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
import java.util.ArrayList;

public class OfflineExplore extends AppCompatActivity {

    private ArrayList<String> organisationsToSelectFrom;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_explore);
        organisationsToSelectFrom=connectToJSON("organisations","OrganisationName");
    }
    private ArrayList<String> connectToJSON(String arrayToSearch, String rowToGet){
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
                JSONArray arrayToGet =obj.getJSONArray(arrayToSearch);
                ArrayList<String> result=new ArrayList<>();
                for(int i=0;i<arrayToGet.length();i++){
                    result.add(arrayToGet.getJSONObject(i).getString(rowToGet));
                }
                return result;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
        return new ArrayList<>();
    }
}