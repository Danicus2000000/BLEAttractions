package bulman.daniel.bleattractions;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button openExploreButton=findViewById(R.id.exploreArea);
        openExploreButton.setOnClickListener(view -> {
            Intent openExploreIntent=new Intent(getApplicationContext(),AreaExplore.class);
            startActivity(openExploreIntent);
        });
        Button openTicketManager=findViewById(R.id.ticketManager);
        openTicketManager.setOnClickListener(view -> {
            Intent openTicketManagerIntent=new Intent(getApplicationContext(),TicketManager.class);
            startActivity(openTicketManagerIntent);
        });
        Button openQueueExplorer=findViewById(R.id.queueManager);
        openQueueExplorer.setOnClickListener(view -> {
            Intent openQueueManagerIntent=new Intent(getApplicationContext(),QueueExplorer.class);
            startActivity(openQueueManagerIntent);
        });

    }
}