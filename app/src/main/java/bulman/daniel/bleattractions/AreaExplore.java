package bulman.daniel.bleattractions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
    private ArrayList<BluetoothDevice> deviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_explore);
        contentDisplay = findViewById(R.id.DeviceList);
        deviceList = new ArrayList<>();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Toast.makeText(this, "Bluetooth is not supported on this device!", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Bluetooth low energy is not supported on this device!", Toast.LENGTH_SHORT).show();
            finish();
        }
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, @NonNull Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                deviceList.add(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
                updateDisplay();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void updateDisplay() {
        StringBuilder toSet = new StringBuilder();
        for (int i = 0; i < deviceList.size(); i++) {
            toSet.append(" MAC: ").append(deviceList.get(i).getAddress()).append("\n");
        }
        contentDisplay.setText(toSet.toString());
    }
}