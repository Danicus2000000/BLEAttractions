package bulman.daniel.bleattractions;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class OfflineExplore extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_explore);
        Spinner organisationList=findViewById(R.id.organisationList);//loads json offline data to allow searching through corporations using the software
        JSONObject jsonOfflineData=connectToJSON();
        try {
            ArrayList<String> organisationsToSelectFrom=new ArrayList<>();
            JSONArray arrayToGet = jsonOfflineData.getJSONArray("organisations");
            for (int i = 0; i < arrayToGet.length(); i++) {
                organisationsToSelectFrom.add(arrayToGet.getJSONObject(i).getString("OrganisationName"));
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, organisationsToSelectFrom);
            organisationList.setAdapter(adapter);//stores offline data in a drop down box
        }catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        }
        organisationList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {//when an item is selected we show the beacon names that should be available in this place
                Spinner attractionList=findViewById(R.id.attractionList);
                Button loadPage=findViewById(R.id.loadPage);
                ArrayList<String> urlPointer=new ArrayList<>();
                ArrayList<String> attractionsToSelectFrom=new ArrayList<>();//load JSON
                try {
                    JSONArray arrayToGet = jsonOfflineData.getJSONArray("bledevices");
                    for (int j = 0; j < arrayToGet.length(); j++) {
                        if(arrayToGet.getJSONObject(j).getString("BleDeviceOrganisation").equals(organisationList.getSelectedItem().toString())) {
                            attractionsToSelectFrom.add(arrayToGet.getJSONObject(j).getString("BleDeviceName"));
                            urlPointer.add(arrayToGet.getJSONObject(j).getString("BleDeviceUrlToPointTo"));
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, attractionsToSelectFrom);
                    attractionList.setAdapter(adapter);
                }catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
                loadPage.setOnClickListener(view1 -> {//on selecting a page to view pass through the web page to view it
                    Intent webView=new Intent(getApplicationContext(),webViewer.class);
                    String[] addressBySlash=urlPointer.get(attractionsToSelectFrom.indexOf(attractionList.getSelectedItem().toString())).split("/");
                    webView.putExtra("urlToLoad",addressBySlash[addressBySlash.length-1]+".html");
                    webView.putExtra("isOffline",true);
                    startActivity(webView);
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
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