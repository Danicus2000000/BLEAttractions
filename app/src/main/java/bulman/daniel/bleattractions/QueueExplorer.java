package bulman.daniel.bleattractions;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
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
import java.util.HashMap;
import java.util.Map;

public class QueueExplorer extends AppCompatActivity {
    private TextView display;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue_explorer);
        display=findViewById(R.id.queueTimes);
        databaseHandler handle=new databaseHandler("select BleDeviceTitle,BleDeviceQueueTime from bledevices");
        handle.execute();
    }
    @SuppressLint("StaticFieldLeak")
    public class databaseHandler extends AsyncTask<Void,Void, Map<String,String>> {//takes in database query and forms connection
        private final String mQuery;
        public databaseHandler(String pQuery)
        {
            mQuery=pQuery;
        }
        @Override
        protected Map<String,String> doInBackground(Void... voids) {//in the background connects to the database using connection string
            ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.DISCONNECTED && connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.DISCONNECTED)
            {
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(),"Internet Access is needed to connect to database\nPlease turn on your data or WIFI!",Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
            else {
                String connectionUrl = "jdbc:mysql://bledata.mysql.database.azure.com:3306/bledata";//uses jdbc to get connection
                String password = "";
                String username = "";
                Map<String,String> results=new HashMap<>();
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
                                results.put(resultSet.getString("BleDeviceTitle"),resultSet.getString("BleDeviceQueueTime"));
                            }
                            connection.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                            runOnUiThread(() -> {
                                Toast.makeText(getApplicationContext(),"Could not connect to database!",Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        }
                    }
                }//forms connection and returns it
                catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(),"Could not connect to database!",Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
                return results;//returns results to async event on complete
            }
            return new HashMap<>();//returns empty array if internet access is not present
        }

        @Override
        protected void onPostExecute(Map<String,String> results) {//once executed the data from the results is passed into the main program
            StringBuilder times=new StringBuilder();
            for(Map.Entry<String,String> obj : results.entrySet()){
                times.append(obj.getKey()).append(":\n").append(obj.getValue()).append("\n");
            }
            runOnUiThread(() -> display.setText(times.toString()));
        }
    }
}