package e.master.updog;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

import e.master.updog.components.Broker;
import e.master.updog.utilities.Utilities;

public class MainActivity extends AppCompatActivity {

    public String channelName = null, brokerInfo = null;
    static ArrayList<Broker> brokers = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle loginData = getIntent().getExtras();
        if (loginData != null) {
            channelName = (String) loginData.get("channelName");
            Log.d("Main Activity", "onCreate: " + channelName);
            brokerInfo = (String) loginData.get("brokerInfo");
            Log.d("Main Activity", "onCreate: " + brokerInfo);
        }else{
            moveTaskToBack(true);
            finish();
        }
        brokers = initBrokerList(brokerInfo);
        if (brokers == null) {
            moveTaskToBack(true);
            finish();
        }
        Collections.sort(brokers);
        for (Broker b : brokers){
            Log.d("BROKER", b.getString());
        }
        moveTaskToBack(true);
        finish();
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_upload, R.id.navigation_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.searchbarmenu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_icon);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Add '#' for hashtags (e.g. #memes)");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //TODO:redirect to home fragment
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //TODO:here we add the context for the search of the videos
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private static ArrayList<Broker> initBrokerList(String brokerInfo) {
        if (Utilities.checkBrokerInfo(brokerInfo)) {
            String ip = brokerInfo.split(";")[0];
            int port = Integer.parseInt(brokerInfo.split(";")[2]);
            try {
                Socket socket = new Socket(ip, port);
                ObjectOutputStream tempOutStream = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream tempInStream = new ObjectInputStream(socket.getInputStream());
                tempOutStream.writeUTF("GETBROKERLIST");
                tempOutStream.flush();
                ArrayList<Broker> brokers = new ArrayList<>();
                boolean endFound = false;
                while (!endFound) {
                    String current = tempInStream.readUTF();
                    if (current.equals("FINISHED")) {
                        endFound = true;
                    } else {
                        brokers.add(Utilities.toBroker(current));
                    }
                }
                tempInStream.close();
                tempOutStream.close();
                socket.close();
                Log.d("Main Activity", "Successfully got the brokers");
                return brokers;
            } catch (IOException e) {
                Log.d("Main Activity", "Error getting the brokers");
                return null;
            }
        } else {
            return null;
        }
    }

    public void signOut() {
        Intent login = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(login);
    }
}