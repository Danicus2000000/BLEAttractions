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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
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
import java.util.ArrayList;
import java.util.List;
//Note pass.json file should be placed at \app\src\main\assets
public class AreaExplore extends AppCompatActivity {

    private TextView contentDisplay;//contains the text display that all found devices are written to
    private ArrayList<BluetoothDevice> mDeviceList;//the list of all devices discovered during bluetooth search
    private ArrayList<String> mActiveDeviceNames;//the name of Active devices pulled from the database
    private ArrayList<String> mActiveDeviceSignals;//the signals that need to be matched from these devices
    private BluetoothLeScanner mScanner;//the bluetooth low energy scanner that is in use
    private ScanCallback mScanCallback;//the callback that handles device discovery
    private static final int REQUEST_DEFAULT_CODE = 1;//the request code used to handle simple permission requests
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_explore);
        mScanCallback=new ScanCallback() {//initialise callback
            @Override
            public void onScanResult(int callbackType, ScanResult result) {//if result is found and has not already been discovered add to list
                super.onScanResult(callbackType, result);
                if(!mDeviceList.contains(result.getDevice())) {
                    mDeviceList.add(result.getDevice());
                    updateDisplay();
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {//if multiple results are found check they have not been added and then add them
                super.onBatchScanResults(results);
                for(ScanResult result : results)
                {
                    if(!mDeviceList.contains(result.getDevice())) {
                        mDeviceList.add(result.getDevice());
                        updateDisplay();
                    }
                }
            }

            @Override
            public void onScanFailed(int errorCode) {//if a scan fails for any reason output its error code in the log
                super.onScanFailed(errorCode);
                Log.e("Scan failed","Error Code: "+errorCode);
            }
        };
        contentDisplay = findViewById(R.id.DeviceList);//initialise basic variables
        mDeviceList = new ArrayList<>();
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mScanner= bluetoothAdapter.getBluetoothLeScanner();
        mActiveDeviceNames=new ArrayList<>();
        mActiveDeviceSignals=new ArrayList<>();
        databaseHandler handle=new databaseHandler("select * from bledevices");//starts a threaded task to fetch database data
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
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_DEFAULT_CODE);
            } else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_DEFAULT_CODE);
            }
            mScanner.startScan(mScanCallback);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);//if permissions are not granted close page otherwise allow runtime to continue
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Permissions needed to continue!", Toast.LENGTH_SHORT).show();
            finish();
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
        for (int i = 0; i < mDeviceList.size(); i++) {//check we have permission and then add name and mac address to list
            toSet.append("Name: ").append(mDeviceList.get(i).getName()).append(" MAC Address: ").append(mDeviceList.get(i).getAddress()).append("\n");
        }
        contentDisplay.setText(toSet.toString());
    }
    @SuppressLint("StaticFieldLeak")
    public class databaseHandler extends AsyncTask<Void,Void, ArrayList<String>> {//takes in database query and forms connection
        private final String mQuery;
        public databaseHandler(String pQuery)
        {
            mQuery=pQuery;
        }
        @Override
        protected ArrayList<String> doInBackground(Void... voids) {//in the background connects to the database using connection string
            String connectionUrl="jdbc:mysql://bledata.mysql.database.azure.com:3306/bledata?useSSL=true";//uses jdbc to get connection
            String password="";
            ArrayList<String> results=new ArrayList<>();
            try
            {
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
                    password=obj.getString("pass");
                }
                catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            catch(Exception e)
            {
                Log.e("Exception",e.getMessage());
            }
            try (Connection connection= DriverManager.getConnection(connectionUrl,"DanielBulman",password))//attempts to form connection and perform request
            {
                if (connection != null) {
                    try {
                        Statement stat = connection.createStatement();
                        ResultSet resultSet = stat.executeQuery(mQuery);
                        while (resultSet.next()) {
                            results.add(resultSet.getString("BleDeviceName") + "///" + resultSet.getString("BleDeviceTransmitSignal"));
                        }
                        connection.close();
                    } catch (SQLException e) {
                        Log.e("Sql Error", e.getMessage());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "A connection to the database could not be made!", Toast.LENGTH_SHORT).show());
                }
            }//forms connection and returns it
            catch (Exception e){
                Log.e("Exception Occurred: ",e.getMessage());
                runOnUiThread(() -> Toast.makeText(getApplicationContext(),"A connection to the database could not be made!",Toast.LENGTH_SHORT).show());
            }
            return results;//returns results to async event on complete
        }

        @Override
        protected void onPostExecute(ArrayList<String> results) {//once executed the data from the results is passed into the main program
            if(!results.isEmpty()) {
                for (String result : results) {
                    mActiveDeviceNames.add(result.split("///")[0]);
                    mActiveDeviceSignals.add(result.split("///")[1]);
                }
            }
        }
    }

}