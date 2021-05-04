package com.example.ipserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    static byte[] fileContentBytes;
    private static final int CREATE_FILE = 101;
    public static ExecutorService executorService;

    private static int fileIndex = -1;

    Button connect, disconnect, sync, stop, goToFirst;
    EditText EditTextIpAddress, EditTextPort;
    ProgressBar progressBar;

    private ClientSocketManager csm;
    static RecyclerViewAdapter adapter;
    static ArrayList<FileDataStructure> serverMedia, localMedia;
    static RecyclerView recyclerView;

    private boolean SYNCFLAG;
    private boolean userAskToDisconnect;
    public static boolean STOP;
    public static Handler setProgressHandler, progressHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        localMedia = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        EditTextIpAddress = findViewById(R.id.ipAddress);
        EditTextPort = findViewById(R.id.port);

        connect = findViewById(R.id.connect);
        connect.setOnClickListener(v -> {
            if(executorService != null)
                executorService.shutdown();

            //create thread to connect
            executorService = Executors.newFixedThreadPool(2);

            userAskToDisconnect = false;
            String address = EditTextIpAddress.getText().toString();
            int port = Integer.parseInt(EditTextPort.getText().toString());
            executorService.execute(new ClientPi(address, port, getApplicationContext()));
        });

        disconnect = findViewById(R.id.disconnect);
        disconnect.setOnClickListener(v -> {
            userAskToDisconnect = true;
            executorService.shutdown();
        });

        sync = findViewById(R.id.sync);
        sync.setOnClickListener(v -> {
            //Get Local Storage MEDIA
            localMedia.clear();
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
            Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
            if (cursor != null) {
                if(cursor.moveToFirst()) {
                    do {
                        String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                        //String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                        //String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                        localMedia.add(new FileDataStructure(name, "File"));
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
            if(!executorService.isShutdown()) {
                executorService.execute(new SYNC(serverMedia, localMedia));
            } else
                Toast.makeText(this,"Something Went Wrong", Toast.LENGTH_SHORT).show();
        });

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        DisconnectedGUI();

        stop = findViewById(R.id.stop);
        stop.setOnClickListener(v -> {
            STOP = true;
        });
        stop.setEnabled(false);

        setProgressHandler = new Handler(Looper.myLooper()){

            @Override
            public void handleMessage(@NonNull Message msg) {
                serverMedia.get(msg.arg2).EnableProgress(true);
                serverMedia.get(msg.arg2).setMaxProgress(msg.arg1);
                serverMedia.get(msg.arg2).setSyncStatus((String) msg.obj);

                try {
                    adapter.notifyItemChanged(msg.arg2);
                } catch (java.lang.IllegalStateException e) {
                    //e.printStackTrace();
                    System.out.println("Recycler View Cannot keep up");
                }
            }
        };

        progressHandler = new Handler(Looper.myLooper()) {
            private int currentAdapterView = 0;
            @Override
            public void handleMessage(@NonNull Message msg) {
                if(msg.arg1 >= 0 ) {
                    serverMedia.get(msg.arg2).setProgress(msg.arg1);
                } else {
                    serverMedia.get(msg.arg2).EnableProgress(true);
                }
                serverMedia.get(msg.arg2).setSyncStatus((String) msg.obj);

                if(msg.arg2 >= currentAdapterView)
                    currentAdapterView = msg.arg2 + 3;
                try {
                    adapter.notifyItemChanged(msg.arg2);

                    if(serverMedia.size() > currentAdapterView) {
                        recyclerView.scrollToPosition(currentAdapterView);
                    } else
                        recyclerView.scrollToPosition(msg.arg2);

                } catch (java.lang.IllegalStateException e) {
                    //e.printStackTrace();
                    System.out.println("Recycler View Cannot keep up");
                }
            }
        };

        goToFirst = findViewById(R.id.up);
        goToFirst.setOnClickListener(v -> {
            if(recyclerView != null){
                recyclerView.scrollToPosition(0);
            }
        });

    }

    /**
     * The GUI Classes will handle what should be VISIBLE or INVISIBLE...
     */
    public void ConnectedGUI(){
        //Enable:   Sync Button, Disconnect Button
        sync.setEnabled(true);
        disconnect.setEnabled(true);

        //Disable:  IP, PORT, Connect Button
        EditTextIpAddress.setEnabled(false);
        EditTextPort.setEnabled(false);
        connect.setEnabled(false);
    }

    public void DisconnectedGUI(){
        //Enable:   IP, PORT, Connect Button
        EditTextIpAddress.setEnabled(true);
        EditTextPort.setEnabled(true);
        connect.setEnabled(true);

        //Disable:  (remove)RecyclerView, Sync Button, Disconnect Button
        if(serverMedia != null) {
            serverMedia.clear();
            adapter.notifyDataSetChanged();
        }
        sync.setEnabled(false);
        disconnect.setEnabled(false);
        fileIndex = -1;
    }

    public void DownloadInProgressGUI(){
        //Enable:   Progress Bar, Stop
        stop.setEnabled(true);
        STOP = false; //reset
        progressBar.setVisibility(View.VISIBLE);

        //Disable:  RecyclerView, Sync, Disconnect
        RecyclerViewAdapter.isClickable = false;
        sync.setEnabled(false);
        disconnect.setEnabled(false);
    }

    public void DownloadComplete(){
        //Enable:   RecyclerView, Sync, Disconnect
        RecyclerViewAdapter.isClickable = true;
        STOP = false; //reset
        sync.setEnabled(true);
        disconnect.setEnabled(true);

        //Disable:  Progress Bar, Stop
        progressBar.setVisibility(View.INVISIBLE);
        stop.setEnabled(false);
    }

    //More for ClientSocketManager
    public static void progress(int max, int progress, String status){
        serverMedia.get(fileIndex).EnableProgress(true);
        serverMedia.get(fileIndex).setMaxProgress(max);
        serverMedia.get(fileIndex).setProgress(progress);
        serverMedia.get(fileIndex).setSyncStatus(status);
        try {
            adapter.notifyItemChanged(fileIndex);
        } catch (java.lang.IllegalStateException e) {
            //e.printStackTrace();
            System.out.println("Recycler View Cannot keep up");
        }
    }

    /** This is bad */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        userAskToDisconnect = true;
        if(executorService.isShutdown())
            executorService.shutdown();
    }

    public class SYNC implements Runnable{
        private final ArrayList<FileDataStructure> SERVER, LOCAL;
        private final Object[] files;


        public SYNC(ArrayList<FileDataStructure> server, ArrayList<FileDataStructure> local){
            this.SERVER = server;
            this.LOCAL = local;

            files = MissingFiles();
            progressBar.setMax(files.length);
        }

        private Object[] MissingFiles(){
            ArrayList<Integer> filesToGet = new ArrayList<>();
            try {
                for(int i = 1; i < SERVER.size(); i++){
                    String filename = SERVER.get(i).getFileName();
                    String fileType = SERVER.get(i).getType();
                    if(fileType.equals("Directory")) {
                        SERVER.get(i).setSyncStatus("Directory: /"+filename);
                        SERVER.get(i).EnableProgress(false);
                        adapter.notifyItemChanged(i);
                        continue;
                    }

                    String Mime = SERVER.get(i).getMimeTypeMap();
                    if( Mime == null || !Mime.split("/")[0].equals("audio") ) {
                        if(Mime == null)
                            SERVER.get(i).setSyncStatus("Unknown File Extension");
                        else
                            SERVER.get(i).setSyncStatus("File Type: " + Mime + " (NOT audio/...)");


                        SERVER.get(i).EnableProgress(false);
                        adapter.notifyItemChanged(i);
                        continue;
                    }

                    boolean getFile = true;
                    for (int j = 0; j < LOCAL.size(); j++){
                        //Short Circuit Eval
                        if ( filename.equals(LOCAL.get(j).getFileName()) ) {
                            LOCAL.remove(j);
                            SERVER.get(i).setSyncStatus("Already Downloaded <-");
                            SERVER.get(i).EnableProgress(false);
                            adapter.notifyItemChanged(i);
                            getFile = false;
                            break;
                        }
                    }
                    if (getFile) {
                        filesToGet.add(i);
                    }
                }
            } catch (java.lang.IllegalStateException e) {
                System.out.println("Recycler View Cannot keep up");
            }
            return filesToGet.toArray();
        }

        @Override
        public void run() {
            if (Looper.myLooper() == null)
                Looper.prepare();

            if(files.length == 0) {
                Toast.makeText(MainActivity.this, "Music: UP-TO-DATE", Toast.LENGTH_SHORT).show();
                return;
            }

            runOnUiThread(MainActivity.this::DownloadInProgressGUI);

            progressBar.setProgress(0);
            for (Object file : files) {
                SYNCFLAG = true;
                while (fileIndex != -1);
                if(STOP)
                    break;
                fileIndex = (int) file;
                progressBar.setProgress(progressBar.getProgress() + 1, true);
            }

            SYNCFLAG = false;
            runOnUiThread(MainActivity.this::DownloadComplete);

            Toast.makeText(MainActivity.this, "Sync Complete", Toast.LENGTH_SHORT).show();
        }
    }

    //"192.168.1.73", 6969
    public class ClientPi implements Runnable{

        private final String address;
        private final int port;
        private final Context context;
        public ClientPi(String address, int port, Context context){
            this.address = address;
            this.port = port;
            this.context = context;
        }

        @Override
        public void run() {
            if (Looper.myLooper() == null)
                Looper.prepare();
            //check if connected
            try {
                csm = new ClientSocketManager(address, port, context);
            } catch (IOException e){
                Toast.makeText(MainActivity.this, "Error On Creating a connection", Toast.LENGTH_SHORT).show();
                return;
            }

            if( !csm.hasConnection() ){
                Toast.makeText(MainActivity.this, "Failed to Connect", Toast.LENGTH_SHORT).show();
                runOnUiThread(MainActivity.this::DisconnectedGUI);
                return;
            }

            runOnUiThread(MainActivity.this::ConnectedGUI);

            try {
                //StartUP
                csm.send(true);
                csm.send("Directory");
                csm.send(-1);
                RetrieveDir();
            } catch (IOException e){
                Toast.makeText(MainActivity.this, "Failed On StartUP", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return;
            }
            try {
                while (!userAskToDisconnect) {
                    if (fileIndex >= 0) {
                        //System.out.println("Asking for file " + fileIndex);
                        String type = serverMedia.get(fileIndex).getType();
                        if(STOP) {
                            fileIndex = -1;
                            continue;
                        }
                        csm.send(true);
                        csm.send(type);
                        csm.send(fileIndex);
                        if (type.equals("File")) {
                            if (serverMedia.get(fileIndex).getMimeTypeMap() != null) {
                                if(SYNCFLAG) {
                                    RetrieveFile();
                                } else {
                                    runOnUiThread(MainActivity.this::DownloadInProgressGUI);
                                    RetrieveFile();
                                    runOnUiThread(MainActivity.this::DownloadComplete);
                                }

                            } else {
                                Toast.makeText(MainActivity.this, "Unknown File Type", Toast.LENGTH_SHORT).show();
                            }
                        } else if (type.equals("Directory")) {
                            RetrieveDir();
                        }
                        fileIndex = -1;
                    }
                }
            } catch (IOException e) {
                runOnUiThread(MainActivity.this::DisconnectedGUI);
                Toast.makeText(MainActivity.this, "Failed: Sending/Receiving", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return;
            }
            try {
                csm.send(false);
                csm.close();
                System.out.println("DISCONNECTED");
            } catch (IOException e){
                runOnUiThread(MainActivity.this::DisconnectedGUI);
                Toast.makeText(MainActivity.this, "Failed: Disconnecting", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            runOnUiThread(MainActivity.this::DisconnectedGUI);
        }

        // int <-
        // LOOP:
        //      String <-
        //      String <-
        public void RetrieveDir(){
            runOnUiThread(MainActivity.this::DownloadInProgressGUI);

            try {
                int totalFiles = csm.readInt();


                progressBar.setMax(totalFiles);
                progressBar.setProgress(0);
                serverMedia = new ArrayList<>();

                for (int i = 0; i < totalFiles; i++) {
                    String type = csm.readUTF();
                    String name = csm.readUTF();
                    serverMedia.add(new FileDataStructure(name, type));
                    runOnUiThread(() -> progressBar.setProgress(progressBar.getProgress() + 1));
                }
            } catch (IOException e){
                runOnUiThread(MainActivity.this::DisconnectedGUI);
                Toast.makeText(MainActivity.this, "Error: Retrieve Directories", Toast.LENGTH_SHORT).show();
                return;
            }

            runOnUiThread(MainActivity.this::DownloadComplete);

            //update UI
            runOnUiThread(() -> {
                adapter = new RecyclerViewAdapter(serverMedia);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                recyclerView.setAdapter(adapter);
            });
        }

        // bytes <-
        public void RetrieveFile(){
            //Download File
            runOnUiThread(() -> recyclerView.scrollToPosition(fileIndex));
            fileContentBytes = csm.readFile(fileIndex);
            String name = serverMedia.get(fileIndex).getFileName();
            if (fileContentBytes != null) {
                sendFileToSharedStorage(name, SYNCFLAG);
            } else {
                Toast.makeText(context, "Didn't Downloaded: " + name, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void setFileIndex(int FP){ fileIndex = FP; }
    public static int getFileIndex(){ return fileIndex; }


    /** create text file */
    public void sendFileToSharedStorage(String title, boolean toMusic) {

        if (toMusic) {
            /* DOWNLOAD TO SHARED AUDIO FILE */

            // Add a specific media item.
            ContentResolver resolver = getApplicationContext().getContentResolver();

            // Find all audio files on the primary external storage device.
            Uri audioCollection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

            // Publish a new song.
            ContentValues newSongDetails = new ContentValues();
            newSongDetails.put(MediaStore.Audio.Media.DISPLAY_NAME, title);

            // Keeps a handle to the new song's URI in case we need to modify it
            // later.

            try {
                Uri uri = resolver.insert(audioCollection, newSongDetails);
                OutputStream outputStream = getContentResolver().openOutputStream(uri, "w");
                if(STOP){
                    Toast.makeText(MainActivity.this, "Stopped Downloaded: " + title, Toast.LENGTH_SHORT).show();
                } else {
                    outputStream.write(fileContentBytes);
                }
                outputStream.close();

            } catch (IllegalArgumentException e) {
                Message downloading = Message.obtain();
                downloading.arg1 = -1;
                downloading.arg2 = fileIndex;
                downloading.obj = "Error Type: " + serverMedia.get(fileIndex).getMimeTypeMap();
                progressHandler.sendMessage(downloading);

                Toast.makeText(MainActivity.this, "Error: (MIME Type) " + title, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            /* Allow User to pick Location */

            // when you create document, you need to add Intent.ACTION_CREATE_DOCUMENT
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

            // filter to only show openable items.
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            intent.setType(serverMedia.get(fileIndex).getMimeTypeMap());
            intent.putExtra(Intent.EXTRA_TITLE, title);
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, MediaStore.Audio.Media.INTERNAL_CONTENT_URI);

            startActivityForResult(intent, CREATE_FILE);
        }
    }

    /** Write to location */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_FILE && resultCode == Activity.RESULT_OK) {
            Uri uri;
            if ( data != null ) {
                uri = data.getData();
                try {
                    OutputStream outputStream = getContentResolver().openOutputStream(uri);
                    outputStream.write(fileContentBytes);
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}