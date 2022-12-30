package bulman.daniel.bleattractions;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
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
import android.os.StrictMode;
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

public class AreaExplore extends AppCompatActivity {

    private TextView contentDisplay;
    private ArrayList<BluetoothDevice> mDeviceList;
    private ArrayList<String> mActiveDeviceNames;
    private ArrayList<String> mActiveDeviceSignals;
    private Connection mConnection;
    private ArrayList<String> mResults;
    private BluetoothLeScanner mScanner;
    private ScanCallback mScanCallback;
    private static final int REQUEST_PERMISSION_MODERN = 1;
    private static final int REQUEST_PERMISSION_CLASSIC = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_explore);
        mScanCallback=new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                if(!mDeviceList.contains(result.getDevice())) {
                    mDeviceList.add(result.getDevice());
                    updateDisplay();
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        };
        contentDisplay = findViewById(R.id.DeviceList);
        mDeviceList = new ArrayList<>();
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mScanner= mBluetoothAdapter.getBluetoothLeScanner();
        mActiveDeviceNames=new ArrayList<>();
        mActiveDeviceSignals=new ArrayList<>();
        mResults=new ArrayList<>();
        mConnection=generateConnection();
        if(mConnection!=null)
        {
            databaseHandler handle=new databaseHandler();
            handle.execute();
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Could not connect to database!",Toast.LENGTH_SHORT).show();
            finish();
        }
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this device!", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (mScanner == null) {
            Toast.makeText(this, "Bluetooth LE Scanning is not supported on this device!", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Toast.makeText(this, "Bluetooth is not supported on this device!", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Bluetooth low energy is not supported on this device!", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ActivityResultLauncher<Intent> initialiseBTRequest = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_MODERN);
                            } else{
                                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_CLASSIC);
                            }
                            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_MODERN);
                            }
                            mScanner.startScan(mScanCallback);
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Bluetooth is needed to detect devices!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
            initialiseBTRequest.launch(enableBT);
        }
        else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_MODERN);
            }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_CLASSIC);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Permissions needed to continue!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_MODERN);
            } else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_CLASSIC);
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_MODERN);
            }
            mScanner.startScan(mScanCallback);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_PERMISSION_MODERN);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, REQUEST_PERMISSION_MODERN);
        }
        mScanner.stopScan(mScanCallback);
    }

    private void updateDisplay() {
        StringBuilder toSet = new StringBuilder();
        for (int i = 0; i < mDeviceList.size(); i++) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.BLUETOOTH_CONNECT},REQUEST_PERMISSION_MODERN);
            }
            toSet.append("Name: ").append(mDeviceList.get(i).getName()).append(" MAC Address: ").append(mDeviceList.get(i).getAddress()).append("\n");
        }
        contentDisplay.setText(toSet.toString());
    }


    private Connection generateConnection()
    {
        StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection connection=null;
        try
        {
            String connectionUrl="jdbc:mysql://bledata.mysql.database.azure.com:3306/bledata?useSSL=true";//uses jdbc to get connection
            String json;//reads password from pass.json
            try {
                InputStream is = getAssets().open("pass.json");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                json = new String(buffer, StandardCharsets.UTF_8);
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }
            String password;
            try {
                JSONObject obj = new JSONObject(json);
                password=obj.getString("pass");
            }
            catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            connection= DriverManager.getConnection(connectionUrl,"DanielBulman",password);//forms connection and returns it
        }
        catch(SQLException e)
        {
            Log.e("SQL Exception",e.getMessage());
        }
        catch(Exception e)
        {
            Log.e("Exception",e.getMessage());
        }
        return connection;
    }

    public class databaseHandler extends AsyncTask<Void,Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            try {
                String query = "select * from bledevices";
                Statement stat = mConnection.createStatement();
                ResultSet results = stat.executeQuery(query);
                while (results.next()) {
                    mResults.add(results.getString("BleDeviceName") + "///" + results.getString("BleDeviceTransmitSignal"));
                }
                mConnection.close();
            }catch(SQLException e)
            {
                Log.e("Sql Error",e.getMessage());
            }
            return mResults;
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            for(String result : mResults)
            {
                mActiveDeviceNames.add(result.split("///")[0]);
                mActiveDeviceSignals.add(result.split("///")[1]);
            }
        }
    }

}