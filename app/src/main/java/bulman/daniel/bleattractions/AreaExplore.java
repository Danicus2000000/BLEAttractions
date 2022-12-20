package bulman.daniel.bleattractions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class AreaExplore extends AppCompatActivity {

    private TextView contentDisplay;
    private ArrayList<BluetoothDevice> mDeviceList;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mReceiverIsRegistered;
    private static final int REQUEST_ENABLE_BT=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_explore);
        contentDisplay = findViewById(R.id.DeviceList);
        mDeviceList = new ArrayList<>();
        mReceiverIsRegistered=false;
        mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter==null)
        {
            Toast.makeText(this,"Bluetooth is not supported on this device!",Toast.LENGTH_SHORT).show();
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
        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBT=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT,REQUEST_ENABLE_BT);
        }
        else
        {
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(receiver, filter);
            mReceiverIsRegistered=true;
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, @NonNull Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                mDeviceList.add(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
                updateDisplay();
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==REQUEST_ENABLE_BT)
        {
            if(resultCode==RESULT_OK)
            {
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(receiver, filter);
                mReceiverIsRegistered=true;
            }
            else
            {
                Toast.makeText(getApplicationContext(),"Bluetooth is needed to detect devices!",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mReceiverIsRegistered) {
            unregisterReceiver(receiver);
        }
    }

    private void updateDisplay() {
        String toSet="";
        for (int i = 0; i < mDeviceList.size(); i++) {
            toSet+="Name: "+mDeviceList.get(i).getName()+"MAC Address: "+mDeviceList.get(i).getAddress()+"\n";
        }
        contentDisplay.setText(toSet);
    }
}