package bulman.daniel.bleattractions;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Note pass.json file should be placed at \app\src\main\assets
@SuppressLint("MissingPermission")
public class AreaExplore extends AppCompatActivity {

    private TextView contentDisplay;//contains the text display that all found devices are written to
    private Map<BluetoothDevice,Integer> mDeviceList;//the list of all devices discovered during bluetooth search
    private Map<String,String> mDeviceCodesToSearch;
    private BluetoothLeScanner mScanner;//the bluetooth low energy scanner that is in use
    private ScanCallback mScanCallback;//the callback that handles device discovery
    private static final int REQUEST_ENABLE_SCANNER_CODE=0;
    private static final int REQUEST_DEFAULT_CODE = 1;//the request code used to handle simple permission requests
    private Button mLoadWeb;
    private Button mLoadWeb2;
    private Button mLoadWeb3;
    private String mWebUrl="";
    private String mWebUrl2="";
    private String mWebUrl3="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_explore);
        mLoadWeb=findViewById(R.id.loadWebPage);//when button is clicked load webpage
        mLoadWeb2=findViewById(R.id.loadWebPage2);
        mLoadWeb3=findViewById(R.id.loadWebPage3);
        mLoadWeb.setOnClickListener(view -> {
            Intent loadWeb=new Intent(getApplicationContext(),webViewer.class);
            loadWeb.putExtra("urlToLoad",mWebUrl);
            startActivity(loadWeb);
        });
        mLoadWeb2.setOnClickListener(view -> {
            Intent loadWeb=new Intent(getApplicationContext(),webViewer.class);
            loadWeb.putExtra("urlToLoad",mWebUrl2);
            startActivity(loadWeb);
        });
        mLoadWeb3.setOnClickListener(view -> {
            Intent loadWeb=new Intent(getApplicationContext(),webViewer.class);
            loadWeb.putExtra("urlToLoad",mWebUrl3);
            startActivity(loadWeb);
        });
        mScanCallback=new ScanCallback() {//initialise callback
            @Override
            public void onScanResult(int callbackType, ScanResult result) {//if result is found and has not already been discovered add to list
                super.onScanResult(callbackType, result);
                for (Map.Entry<String, String> possibleDevice : mDeviceCodesToSearch.entrySet()) {
                    if (possibleDevice.getKey().equals(result.getDevice().getName())) {
                        if (mLoadWeb.getText().equals("")) {
                            mLoadWeb.setText("Attraction " + possibleDevice.getKey());
                            mWebUrl = possibleDevice.getValue();
                            mLoadWeb.setVisibility(View.VISIBLE);
                        } else if (!mLoadWeb.getText().equals("Attraction " +possibleDevice.getKey()) && mLoadWeb2.getText().equals("")) {
                            mLoadWeb2.setText("Attraction " + possibleDevice.getKey());
                            mWebUrl2 = possibleDevice.getValue();
                            mLoadWeb2.setVisibility(View.VISIBLE);
                        } else if (!mLoadWeb2.getText().equals("Attraction " +possibleDevice.getKey()) && !mLoadWeb.getText().equals("Attraction " +possibleDevice.getKey()) && mLoadWeb3.getText().equals("")) {
                            mLoadWeb3.setText("Attraction " + possibleDevice.getKey());
                            mWebUrl3 = possibleDevice.getValue();
                            mLoadWeb3.setVisibility(View.VISIBLE);
                        }
                        break;
                    }
                }
                if (!mDeviceList.containsKey(result.getDevice()) && result.getRssi() >= -70) {
                    mDeviceList.put(result.getDevice(), result.getRssi());
                    updateDisplay();
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {//if multiple results are found check they have not been added and then add them
                super.onBatchScanResults(results);
                ArrayList<ScanResult> resultMatch=new ArrayList<>();
                for(ScanResult result : results)
                {
                    if(result.getDevice().getName()!=null) {
                        for (Map.Entry<String, String> possibleDevice : mDeviceCodesToSearch.entrySet()) {
                            if (possibleDevice.getKey().equals(result.getDevice().getName())){
                                resultMatch.add(result);
                            }
                        }
                    }
                    if(!mDeviceList.containsKey(result.getDevice()) && result.getRssi()>=-70) {
                        mDeviceList.put(result.getDevice(),result.getRssi());
                        updateDisplay();
                    }
                }
                if(resultMatch.size()!=0) {
                    int closestVal = Integer.MAX_VALUE;
                    ScanResult closest = null;
                    Map.Entry<String,String> urlToGet=new AbstractMap.SimpleEntry<>("","");
                    for (ScanResult result : resultMatch) {
                        if (result.getRssi() < closestVal) {
                            closestVal = result.getRssi();
                            closest = result;
                        }
                    }
                    for (Map.Entry<String, String> possibleDevice : mDeviceCodesToSearch.entrySet()) {
                        if (closest!=null &&possibleDevice.getKey().equals(closest.getDevice().getName())){
                            urlToGet=possibleDevice;
                            break;
                        }
                    }
                    mLoadWeb.setText("Attraction " + urlToGet.getKey());
                    mWebUrl = urlToGet.getValue();
                    mLoadWeb.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onScanFailed(int errorCode) {//if a scan fails for any reason output its error code in the log
                super.onScanFailed(errorCode);
                Log.e("Scan failed","Error Code: "+errorCode);
            }
        };
        contentDisplay = findViewById(R.id.DeviceList);//initialise basic variables
        mDeviceList = new HashMap<>();
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mScanner= bluetoothAdapter.getBluetoothLeScanner();
        mDeviceCodesToSearch=new HashMap<>();
        databaseHandler handle=new databaseHandler("select BleDeviceName,BleDeviceUrlToPointTo from bledevices");//starts a threaded task to fetch database data
        handle.execute();
        if (mScanner == null || !getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) || !getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {//if the bluetooth scanner is out of action error code
            Toast.makeText(this, "A required bluetooth feature is not enabled on this device!", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (!bluetoothAdapter.isEnabled()) {//if the scanner is functional exists but is not enabled request it is enabled
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ActivityResultLauncher<Intent> initialiseBTRequest = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {//if the device is enabled perform permission checks to see we have valid permissions to use the devices
                            if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_DEFAULT_CODE);
                            } else{
                                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_DEFAULT_CODE);
                            }
                            mScanner.startScan(mScanCallback);//start scanning
                        }
                        else {//if we are rejected bluetooth access warn user and close page
                            Toast.makeText(getApplicationContext(), "Bluetooth is needed to detect devices!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
            initialiseBTRequest.launch(enableBT);//launch request shown above
        }
        else//if bluetooth is enabled check we have permissions then launch the scanner
        {
            if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ENABLE_SCANNER_CODE);
            } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT < Build.VERSION_CODES.S){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ENABLE_SCANNER_CODE);
            }
            else {
                mScanner.startScan(mScanCallback);//start scanning
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);//if permissions are not granted close page otherwise allow runtime to continue
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Permissions needed to continue!", Toast.LENGTH_SHORT).show();
            finish();
        }
        else if(requestCode==REQUEST_ENABLE_SCANNER_CODE){
            if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ENABLE_SCANNER_CODE);
            } else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ENABLE_SCANNER_CODE);
            }
            mScanner.startScan(mScanCallback);//start scanning
        }
    }
    @Override
    protected void onDestroy() {//when the page is closed check we have permission and stop ongoing scan
        super.onDestroy();
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_DEFAULT_CODE);
        } else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_DEFAULT_CODE);
        }
        mScanner.stopScan(mScanCallback);
        finish();
    }

    private void updateDisplay() {//when a new device is discovered reset display with new device
        StringBuilder toSet = new StringBuilder();
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_DEFAULT_CODE);
        } else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_DEFAULT_CODE);
        }
        for (Map.Entry<BluetoothDevice,Integer> entry : mDeviceList.entrySet()) {//check we have permission and then add name and mac address to list
            if(entry.getKey().getName()!=null)
            {
                toSet.append(getString(R.string.nameList)).append(" ").append(entry.getKey().getName()).append(" ");
            }
            toSet.append(getString(R.string.MACList)).append(" ").append(entry.getKey().getAddress()).append(" Signal Noise: ").append(entry.getValue()).append("dBm").append("\n");
        }
        contentDisplay.setText(toSet.toString());
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
                                results.put(resultSet.getString("BleDeviceName"),resultSet.getString("BleDeviceUrlToPointTo"));
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
            return new HashMap<>();//returns empty array if internet access is not present
        }

        @Override
        protected void onPostExecute(Map<String,String> results) {//once executed the data from the results is passed into the main program
            if(results!=null) {
                mDeviceCodesToSearch.putAll(results);
            }
        }
    }
}