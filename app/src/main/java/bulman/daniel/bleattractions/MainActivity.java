package bulman.daniel.bleattractions;


import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private boolean offlineMode=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button openExploreButton=findViewById(R.id.exploreArea);//open area explore
        openExploreButton.setOnClickListener(view -> {
            if(!offlineMode) {
                Intent openExploreIntent = new Intent(getApplicationContext(), AreaExplore.class);
                startActivity(openExploreIntent);
            }
            else{
                Intent openOfflineExplore=new Intent(getApplicationContext(),OfflineExplore.class);
                startActivity(openOfflineExplore);
            }
        });
        Button openTicketManager=findViewById(R.id.ticketManager);//open ticket manager
        openTicketManager.setOnClickListener(view -> {
            if(!offlineMode){
            Intent openTicketManagerIntent=new Intent(getApplicationContext(),TicketManager.class);
            startActivity(openTicketManagerIntent);
            }
            else{
                Toast.makeText(getApplicationContext(),"Ticket Manager is not currently supported whilst in offline mode!",Toast.LENGTH_SHORT).show();
            }
        });
        Button openQueueExplorer=findViewById(R.id.queueManager);//open queue manager
        openQueueExplorer.setOnClickListener(view -> {
            if(!offlineMode) {
                Intent openQueueManagerIntent = new Intent(getApplicationContext(), QueueExplorer.class);
                startActivity(openQueueManagerIntent);
            }
            else{
                Toast.makeText(getApplicationContext(),"Queue Manager is not currently supported whilst in offline mode!",Toast.LENGTH_SHORT).show();
            }
        });
        Button changeNetworkMode=findViewById(R.id.offlineMode);//enables/disables offline mode
        changeNetworkMode.setOnClickListener(view -> {
            if(offlineMode)
            {
                changeNetworkMode.setText(getResources().getString(R.string.enableOfflineMode));
                offlineMode=false;
                checkConnections();
            }
            else{
                changeNetworkMode.setText(getResources().getString(R.string.disableOfflineMode));
                offlineMode=true;
            }
        });
        checkConnections();//runs checks to determine whether or not the application has access to required network resources
        if(!offlineMode) {
            //download all urls data
            databaseHandler handle = new databaseHandler("select BleDeviceUrlToPointTo from bledevices");
            handle.execute();
        }
    }

    private void checkConnections(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        Button changeNetworkMode=findViewById(R.id.offlineMode);
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {//if the scanner is functional exists but is not enabled request it is enabled
            Toast.makeText(getApplicationContext(), "Bluetooth is not enabled setting mode to offline!", Toast.LENGTH_SHORT).show();
            changeNetworkMode.setText(getResources().getString(R.string.disableOfflineMode));
            offlineMode = true;
        }
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.DISCONNECTED && connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.DISCONNECTED)
        {
            Toast.makeText(getApplicationContext(),"Internet Access is not enabled setting mode to offline",Toast.LENGTH_SHORT).show();
            changeNetworkMode.setText(getResources().getString(R.string.disableOfflineMode));
            offlineMode=true;
        }
    }
    @SuppressLint("StaticFieldLeak")
    public class databaseHandler extends AsyncTask<Void,Void, ArrayList<String>> {//takes in database query and forms connection
        private final String mQuery;
        public databaseHandler(String pQuery) {
            mQuery = pQuery;
        }

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {//in the background connects to the database using connection string
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.DISCONNECTED && connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.DISCONNECTED) {
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "Internet Access is needed to connect to database\nPlease turn on your data or WIFI!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } else {
                String connectionUrl = "jdbc:mysql://bledata.mysql.database.azure.com:3306/bledata";//uses jdbc to get connection
                String password = "";
                String username = "";
                ArrayList<String> results = new ArrayList<>();
                try {
                    String json;//reads password from pass.json
                    try {
                        InputStream is = getAssets().open("pass.json");//reads from the password file to get password for database (only stored locally)
                        int size = is.available();
                        byte[] buffer = new byte[size];
                        is.read(buffer);
                        is.close();
                        json = new String(buffer, StandardCharsets.UTF_8);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        return null;
                    }
                    try {
                        JSONObject obj = new JSONObject(json);
                        username = obj.getString("username");
                        password = obj.getString("pass");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return null;
                    }
                } catch (Exception e) {
                    Log.e("Exception", e.getMessage());
                }
                try (Connection connection = DriverManager.getConnection(connectionUrl, username, password))//attempts to form connection and perform request
                {
                    if (connection != null) {
                        try {
                            Statement stat = connection.createStatement();
                            ResultSet resultSet = stat.executeQuery(mQuery);
                            while (resultSet.next()) {
                                results.add(resultSet.getString("BleDeviceUrlToPointTo"));
                            }
                            connection.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }//forms connection and returns it
                catch (Exception e) {
                    e.printStackTrace();
                }
                return results;//returns results to async event on complete
            }
            return null;//returns null if internet access is not present
        }

        @Override
        protected void onPostExecute(ArrayList<String> results) {//once executed offline data is updated
            if (results != null) {
                for (String result : results) {
                    htmlDownloader downloader = new htmlDownloader(result, getApplicationContext());
                    downloader.execute();
                }
                htmlDownloader downloader = new htmlDownloader("https://danicus2000000.github.io/BLEPages/default.css", getApplicationContext());
                downloader.execute();
            }
        }
    }
}