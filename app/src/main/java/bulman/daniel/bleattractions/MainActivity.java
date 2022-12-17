package bulman.daniel.bleattractions;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void openExplore(View view) {//opens explore window
        Intent openExploreIntent=new Intent(getApplicationContext(),AreaExplore.class);
        startActivity(openExploreIntent);
    }

    public void openTicketManager(View view) {//opens ticket manager
        Intent openTicketManagerIntent=new Intent(getApplicationContext(),TicketManager.class);
        startActivity(openTicketManagerIntent);
    }

    public void openQueueExplorer(View view) {//opens queue explorer
        Intent openQueueManagerIntent=new Intent(getApplicationContext(),QueueExplorer.class);
        startActivity(openQueueManagerIntent);
    }
}