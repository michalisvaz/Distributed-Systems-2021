package e.master.updog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import e.master.updog.components.Broker;
import e.master.updog.components.Consumer;
import e.master.updog.components.Publisher;
import e.master.updog.utilities.Utilities;
import e.master.updog.utilities.VideoFile;
//TODO: Bill put comments
public class MainActivity extends AppCompatActivity {

    public String channelName = null, brokerInfo = null;
    public static String ip = null;
    public static int port;
    static ArrayList<Broker> brokers = null;
    public Publisher publisher;
    public Consumer consumer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle loginData = getIntent().getExtras();
        if (loginData != null) {
            channelName = (String) loginData.get("channelName");
            brokerInfo = (String) loginData.get("brokerInfo");
        } else {
            moveTaskToBack(true);
            finish();
        }
        brokers = initBrokerList(brokerInfo);
        if (brokers == null) {
            moveTaskToBack(true);
            finish();
        }
        Collections.sort(brokers);

        publisher = new Publisher(null, channelName, 0);
        publisher.init(brokers);

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
                consumer = new Consumer(null, 0, channelName);
                new SearchTask().execute(s);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private static ArrayList<Broker> initBrokerList(String brokerInfo) {
        if (Utilities.checkBrokerInfo(brokerInfo)) {
            ip = brokerInfo.split(";")[0];
            port = Integer.parseInt(brokerInfo.split(";")[2]);
            InitBrokerListTask initTask = new InitBrokerListTask();
            try {
                initTask.execute().get();
                return initTask.brokers;
            } catch (ExecutionException e) {
                e.printStackTrace();
                return null;
            } catch (InterruptedException e) {
                e.printStackTrace();
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

    private static class InitBrokerListTask extends AsyncTask<Void, Void, Void> {
        public ArrayList<Broker> brokers = null;

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Socket socket = new Socket(ip, port);
                ObjectOutputStream tempOutStream = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream tempInStream = new ObjectInputStream(socket.getInputStream());
                tempOutStream.writeUTF("GETBROKERLIST");
                tempOutStream.flush();
                brokers = new ArrayList<>();
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
            } catch (IOException e) {
                Log.d("Main Activity", "Error getting the brokers");
            }
            return null;
        }

    }

    private class SearchTask extends AsyncTask<String, Void, Void> {
        ProgressDialog progressDialog;
        TextView textHome = findViewById(R.id.text_home);
        boolean success;
        String searchMessage;
        VideoView videoView = findViewById(R.id.videoHome);

        @Override
        protected Void doInBackground(String... strings) {
            String searchword = strings[0];
            if (searchword.startsWith("#")) {
                consumer.setRandomBroker(brokers);
                success = consumer.getByHashtag(searchword);
                searchMessage = "No videos were found for this hashtag.";
            } else {
                if (!searchword.equals(channelName)) {
                    success = consumer.findBroker(brokers, searchword);
                    searchMessage = "There is no channel with this name.";
                    if (success) {
                        success = consumer.getByChannel(searchword);
                        searchMessage = "No videos were found for this channel name.";
                    }
                } else {
                    searchMessage = "Don't ask for your own videos. Search for another channel name.";
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MainActivity.this,
                    "Please wait...",
                    "Loading video...");
        }

        @Override
        protected void onPostExecute(Void unused) {
            if (success) {
                VideoFile searchedVideo = consumer.getTakenVideo();
                Uri uri = writeVideo(searchedVideo);

                if (uri != null) {
                    ShowVid(uri);
                }
            } else {
                videoView.setVisibility(View.GONE);
                textHome.setText(searchMessage);
            }
            progressDialog.dismiss();
        }

    }

    public Uri writeVideo(VideoFile video) {
        File dir = new File(getApplicationInfo().dataDir + "/" + channelName);
        try {
            if (!dir.exists()) {
                if (dir.mkdirs()) {
                    Log.d("CREATED DIR", "Success");
                } else {
                    Log.d("CREATED DIR", "Failed");
                }
            } else {
                Log.d("CREATED DIR", "Exists");
            }
        } catch (Exception e) {
            Log.d("CREATED DIR", "Exception" + e.toString());
        }
        String fileN = dir.getAbsolutePath() + "/" + "current.mp4";
        File filename = new File(fileN);//Environment.getExternalStorageDirectory().getAbsolutePath(), fileN);
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(filename);
            output.write(video.getData(), 0, video.getData().length);
            output.close();
            URI uri = filename.toURI();
            return Uri.parse(uri.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void ShowVid(Uri uri) {
        VideoView videoView = findViewById(R.id.videoHome);
        videoView.setVisibility(View.VISIBLE);
        videoView.setVideoURI(uri);
        videoView.start();
    }

}