package bulman.daniel.bleattractions;


import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

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

}