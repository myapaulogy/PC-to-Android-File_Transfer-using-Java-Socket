package com.example.serverdl;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    final private int USER_PERMISSIONS = 6969;
    private static final int CREATE_FILE = 101;

    private Button connectionButton;
    private TextView TV_IpAddress, TV_Port;
    private ProgressBar PB_main;
    private RecyclerView RV_serverFiles;
    private ArrayList<ServerFilesDataStructure> serverFiles;
    private RecyclerViewAdapter adapter;
    private ExecutorService executorService;

    public static List<Integer> filesRequested;
    public static Uri userUriLocation;

    SharedPreferences sharedPref;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
               if(isGranted){
                   //Permissions Granted
                   //Toast.makeText(this, "User gave permissions", Toast.LENGTH_SHORT).show();
               } else {
                   //Permission Denied... Exit application
                   Toast.makeText(this, "I need permissions", Toast.LENGTH_SHORT).show();
                   MainActivity.this.finish();
                   System.exit(0);
               }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* GET USER PERMISSION */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            requestPermissionLauncher.launch(Manifest.permission.INTERNET);
        }
        /* GET USER PERMISSION */

        /* UI links */
        connectionButton = findViewById(R.id.UI_Connect);

        TV_IpAddress = findViewById(R.id.UI_IPAddress);
        TV_Port = findViewById(R.id.UI_Port);
        sharedPref = getSharedPreferences("connectionData", MODE_PRIVATE);
        String userIP = sharedPref.getString("IP", "192.168.1.73");
        String userPORT = sharedPref.getString("PORT", "6969");
        TV_IpAddress.setText(userIP);
        TV_Port.setText(userPORT);

        PB_main = findViewById(R.id.UI_ProgressBar);
        RV_serverFiles = findViewById(R.id.UI_recyclerView);
        /* UI links */

        /* Create Recycler View */
        RV_serverFiles.setHasFixedSize(true);
        RV_serverFiles.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        RV_serverFiles.setAdapter(adapter);
        /* Create Recycler View */

        /* Buttons */
        connectionButton.setOnClickListener(v -> {
            CharSequence status = connectionButton.getText();
            if(status.equals("connect")) {
                executorService = Executors.newFixedThreadPool(2);
                String address = TV_IpAddress.getText().toString();
                int port = Integer.parseInt(TV_Port.getText().toString());
                executorService.execute(new NetworkThread(address, port, getApplicationContext()));

            } else if (status.equals("disconnect")){
                synchronized (filesRequested) {
                    filesRequested.clear();
                    filesRequested.add(-1);
                    filesRequested.notify();
                }

                serverFiles.clear();
                runOnUiThread(() -> adapter.notifyDataSetChanged());
                connectionButton.setText("connect");

            } else if (status.equals("stop")){
                STOP = true;
                SYNC = false;
                connectionButton.setText("disconnect");
                Toast.makeText(this,"Check your Files For incomplete downloads", Toast.LENGTH_SHORT).show();
            }
        });
        /* Buttons */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
    }

    public boolean STOP = false;
    public boolean SYNC = false;

    public class NetworkThread implements Runnable{
        private Socket client = new Socket();
        private DataInputStream from_Server;
        private DataOutput to_Server;

        private final String address;
        private final int port;
        private final Context context;
        public NetworkThread(String address, int port, Context context){
            this.address = address;
            this.port = port;
            this.context = context;

            filesRequested = new ArrayList<>();
            filesRequested = Collections.synchronizedList(filesRequested);
        }

        @Override
        public void run() {
            Looper.prepare();

            if (!ConnectToServer())
                return;

            runOnUiThread(() -> connectionButton.setText("disconnect"));
            sharedPref.edit().putString("IP", TV_IpAddress.getText().toString()).apply();
            sharedPref.edit().putString("PORT", TV_Port.getText().toString()).apply();

            // Get Starting Directory
            AskForDirectory(-1);

            boolean exit = false;
            while (!exit) {
                synchronized (filesRequested) {
                    try {
                        filesRequested.wait();

                        if (filesRequested.size() > 0) {

                            PB_main.setMax(filesRequested.size());
                            PB_main.setProgress(0);

                            invalidateOptionsMenu();
                            for (int i = 0; i < filesRequested.size(); i++) {
                                int fileIndex = filesRequested.get(i);

                                //Handle Neg values
                                if (fileIndex == -1) { //Disconnect
                                    to_Server.writeBoolean(false);
                                    exit = true;
                                    break;
                                }

                                if (fileIndex == -10) { //Ask to Shutdown
                                    to_Server.writeBoolean(true);
                                    to_Server.writeUTF("ShutDown");
                                    to_Server.writeInt(-1);
                                    to_Server.writeBoolean(false);
                                    exit = true;
                                    break;
                                }

                                if (serverFiles.get(fileIndex).getType().equals("File")) {
                                    if(!STOP)
                                        AskForFile(fileIndex);

                                } else if (serverFiles.get(fileIndex).getType().equals("Directory")) {
                                    AskForDirectory(fileIndex);
                                }
                                PB_main.setProgress(i);

                            }
                            SYNC = false;
                            STOP = false;
                            filesRequested.clear();
                        }
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                from_Server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public boolean ConnectToServer(){
            try{
                // Toast.makeText(context, "Looking for the Server", Toast.LENGTH_SHORT).show();
                client = new Socket(address, port);
                from_Server = new DataInputStream(client.getInputStream());
                to_Server = new DataOutputStream(client.getOutputStream());
            }catch (IOException e){
                Toast.makeText(context, "Connection Failed", Toast.LENGTH_LONG).show();
                return false;
            }
            // Toast.makeText(context, "Connection Established", Toast.LENGTH_SHORT).show();
            return true;
        }

        public void AskForDirectory(int index) {
            try {
                // ASK FOR CWD
                to_Server.writeBoolean(true);
                to_Server.writeUTF("Directory");
                to_Server.writeInt(index);

                // STORE CWD
                int totalFiles = from_Server.readInt();

                PB_main.setMax(totalFiles);
                PB_main.setProgress(0);
                serverFiles = new ArrayList<>();

                for (int i = 0; i < totalFiles; i++) {
                    String type = from_Server.readUTF();
                    String name = from_Server.readUTF();
                    serverFiles.add(new ServerFilesDataStructure(name, type));
                    runOnUiThread(() -> PB_main.setProgress(PB_main.getProgress() + 1));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            runOnUiThread(() -> {
                adapter = new RecyclerViewAdapter(serverFiles);
                RV_serverFiles.setHasFixedSize(true);
                RV_serverFiles.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                RV_serverFiles.setAdapter(adapter);

            });

        }

        public void AskForFile(int index){
            runOnUiThread(() -> connectionButton.setText("stop"));

            try {
                userUriLocation = emptyLocation;

                SendToSharedStorage(index);
                while (userUriLocation.equals(emptyLocation));

                //User Did not give a location to download the file to
                if (userUriLocation.equals(noLocation)){
                    System.out.println("No           location                given");
                    userUriLocation = null;
                    runOnUiThread(() -> connectionButton.setText("disconnect"));
                    return;
                }

                // ASK FOR FILE
                to_Server.writeBoolean(true);
                to_Server.writeUTF("File");
                to_Server.writeInt(index);

                try {
                    OutputStream outputStream = getContentResolver().openOutputStream(userUriLocation);

                    int pageSize;
                    int dataRetrieved = 0;
                    int percentage;
                    byte[] rawBytes;

                    long fileSize = from_Server.readLong();
                    serverFiles.get(index).setMaxProgress(100);
                    serverFiles.get(index).setEnableProgress(true);
                    serverFiles.get(index).setSyncStatus("Starting...");
                    runOnUiThread(() -> adapter.notifyItemChanged(index));
                    RV_serverFiles.smoothScrollToPosition(index);
                    do {

                        from_Server.readBoolean();
                        to_Server.writeBoolean(!STOP);
                        if(STOP) {
                            filesRequested.clear();
                            break;
                        }

                        //Store to temp
                        pageSize = from_Server.readInt();
                        rawBytes = new byte[pageSize];
                        from_Server.readFully(rawBytes);
                        outputStream.write(rawBytes);
                        dataRetrieved += pageSize;

                        //Update file UI
                        percentage = (int)(((double)dataRetrieved/fileSize)*100);
                        serverFiles.get(index).setProgress(percentage);
                        serverFiles.get(index).setSyncStatus("Downloading: " + percentage +"%");

                        runOnUiThread(() -> adapter.notifyItemChanged(index));

                    } while(fileSize != dataRetrieved);

                    outputStream.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                userUriLocation = emptyLocation;

                if(STOP) {
                    serverFiles.get(index).setEnableProgress(false);
                    serverFiles.get(index).setSyncStatus("Progress -> STOPPED");
                    runOnUiThread(() -> adapter.notifyItemChanged(index));
                } else {
                    serverFiles.get(index).setEnableProgress(false);
                    serverFiles.get(index).setSyncStatus("Completed");
                    runOnUiThread(() -> adapter.notifyItemChanged(index));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            runOnUiThread(() -> connectionButton.setText("disconnect"));
        }

        public void SendToSharedStorage(int index){
            String title = serverFiles.get(index).getFileName();
            if(SYNC){
                /* DOWNLOAD TO SHARED AUDIO FILE */

                // Add a specific media item.
                ContentResolver resolver = getApplicationContext().getContentResolver();

                // Find all audio files on the primary external storage device.
                Uri audioCollection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

                // Publish a new song.
                ContentValues newSongDetails = new ContentValues();
                newSongDetails.put(MediaStore.Audio.Media.DISPLAY_NAME, title);

                userUriLocation = resolver.insert(audioCollection, newSongDetails);
            } else {
                // when you create document, you need to add Intent.ACTION_CREATE_DOCUMENT
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

                // filter to only show openable items.
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                intent.setType(serverFiles.get(index).getMimeTypeMap());
                intent.putExtra(Intent.EXTRA_TITLE, title);

                //startActivityForResult(intent, CREATE_FILE);
                mGetContent.launch(intent);
            }
        }
    }
    //End of NETWORK THREAD//

    Uri noLocation = Uri.parse("NoLocation");
    Uri emptyLocation = Uri.parse("EmptyLocation");
    ActivityResultLauncher<Intent> mGetContent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getData() != null) {
                        userUriLocation = result.getData().getData();
                    } else {
                        userUriLocation = noLocation;
                    }
                }
            });

    /* Action bar Queue */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.toFirst:
                RV_serverFiles.smoothScrollToPosition(0);
                return(true);
            case R.id.sync:
                System.out.println("SYNC FILES");

                ArrayList<ServerFilesDataStructure> localMedia = new ArrayList<>();

                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
                Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
                if (cursor != null) {
                    if(cursor.moveToFirst()) {
                        do {
                            String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                            //String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                            //String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                            localMedia.add(new ServerFilesDataStructure(name, "File"));
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                }

                synchronized (filesRequested) {
                    try {
                        for (int i = 1; i < serverFiles.size(); i++) {
                            String filename = serverFiles.get(i).getFileName();
                            String fileType = serverFiles.get(i).getType();
                            if (fileType.equals("Directory")) {
                                serverFiles.get(i).setSyncStatus("Directory: /" + filename);
                                serverFiles.get(i).setEnableProgress(false);
                                adapter.notifyItemChanged(i);
                                continue;
                            }



                            String Mime = serverFiles.get(i).getMimeTypeMap();
                            if (Mime == null || !Mime.split("/")[0].equals("audio") || Mime.split("/")[1].equals("x-mpegurl")) {

                                if (Mime == null)
                                    serverFiles.get(i).setSyncStatus("Unknown File Extension");
                                else
                                    serverFiles.get(i).setSyncStatus("File Type: " + Mime + " is not audio");

                                serverFiles.get(i).setEnableProgress(false);
                                adapter.notifyItemChanged(i);
                                continue;
                            }

                            boolean getFile = true;
                            for (int j = 0; j < localMedia.size(); j++) {
                                //Short Circuit Eval
                                if (filename.equals(localMedia.get(j).getFileName())) {
                                    localMedia.remove(j);
                                    serverFiles.get(i).setSyncStatus("Already Downloaded <-");
                                    serverFiles.get(i).setEnableProgress(false);
                                    adapter.notifyItemChanged(i);
                                    getFile = false;
                                    break;
                                }
                            }
                            if (getFile) {
                                filesRequested.add(i);
                            }
                        }

                        SYNC = true;
                        filesRequested.notify();
                    } catch (java.lang.IllegalStateException e) {
                        System.out.println("Recycler View Cannot keep up");
                    }
                }

                return(true);

            case R.id.shutdown:
                synchronized (filesRequested) {
                    filesRequested.clear();
                    filesRequested.add(-10);
                    filesRequested.notify();
                }

                serverFiles.clear();
                runOnUiThread(() -> adapter.notifyDataSetChanged());
                connectionButton.setText("connect");
                return(true);
        }
        return(super.onOptionsItemSelected(item));
    }

}

