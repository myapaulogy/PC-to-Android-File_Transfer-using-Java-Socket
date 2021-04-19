package com.example.ipserver;

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
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    static byte[] fileContentBytes;
    ClientSocketManager CSM;
    private static final int CREATE_FILE = 101;
    public static ExecutorService executorService;
    private static int filePosition = -1;

    Button connect, disconnect, sync;
    EditText EditTextIpAddress, EditTextPort;
    ProgressBar progressBar;

    static RecyclerViewAdapter adapter;
    static ArrayList<FileDataStructure> serverMedia;
    static ArrayList<FileDataStructure> localMedia;
    static RecyclerView recyclerView;

    boolean SYNCFLAG;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        localMedia = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        EditTextIpAddress = findViewById(R.id.ipAddress);
        EditTextPort = findViewById(R.id.port);

        //create thread to connect
        executorService = Executors.newFixedThreadPool(2);
        connect = findViewById(R.id.connect);

        connect.setOnClickListener(v -> {

            String address = EditTextIpAddress.getText().toString();
            int port = Integer.parseInt(EditTextPort.getText().toString());

            executorService.execute(new ClientPi(address, port, getApplicationContext()));

            ConnectedGUI();
        });

        disconnect = findViewById(R.id.disconnect);
        disconnect.setOnClickListener(v -> {
            if(CSM != null) {
                CSM.close();
            }

            DisconnectedGUI();
        });

        sync = findViewById(R.id.sync);
        sync.setOnClickListener(v -> {
            // Toast.makeText(this, "NOT IMPLEMENTED", Toast.LENGTH_SHORT).show();

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
                        localMedia.add(new FileDataStructure(name));
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }

            executorService.execute(new SYNC(serverMedia, localMedia));
        });

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        DisconnectedGUI();
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
    }

    public void DownloadInProgressGUI(){
        //Enable:   Progress Bar
        progressBar.setVisibility(View.VISIBLE);

        //Disable:  RecyclerView, Sync, Disconnect
        RecyclerViewAdapter.isClickable = false;
        sync.setEnabled(false);
        disconnect.setEnabled(false);
    }

    public void DownloadComplete(){
        //Enable:   RecyclerView, Sync, Disconnect
        RecyclerViewAdapter.isClickable = true;
        sync.setEnabled(true);
        disconnect.setEnabled(true);

        //Disable:  Progress Bar
        progressBar.setVisibility(View.INVISIBLE);
    }

    public static void progress(int max, int progress){
        serverMedia.get(filePosition).EnableProgress(true);
        serverMedia.get(filePosition).setMaxProgress(max);
        serverMedia.get(filePosition).setProgress(progress);
        try {
            adapter.notifyItemChanged(filePosition);
        } catch (java.lang.IllegalStateException e){
            System.out.println("Recycler View Cannot keep up");
        }
    }

    /** This is bad */
    @Override
    protected void onDestroy() {
        super.onDestroy();
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
            //Compair server files and local music files
            //Looper.prepare();
            for(int i = 1; i < SERVER.size(); i++){
                int j;

                for (j = 0; j < LOCAL.size(); j++){
                    //System.out.println("-----------SERVER------------>>>>>>>> "+ SERVER.get(i));
                    //System.out.println("-----------LOCAL------------->>>>>>>> "+ LOCAL.get(j));
                    if ( SERVER.get(i).getFileName().equals(LOCAL.get(j).getFileName()) ) {
                        System.out.println("TRUEE");
                        LOCAL.remove(j);
                        break;
                    }
                }

                if (j == LOCAL.size()) {
                    System.out.println("---------ADDDDDDDD------->>>>>>>> " + SERVER.get(i));
                    filesToGet.add(i);
                }
            }
            return filesToGet.toArray();
        }

        @Override
        public void run() {
            if (Looper.myLooper() == null)
                Looper.prepare();

            if(files.length == 0) {
                Toast.makeText(MainActivity.this, "Music is up-to-date", Toast.LENGTH_SHORT).show();
                return;
            }

            runOnUiThread(MainActivity.this::DownloadInProgressGUI);

            progressBar.setProgress(0);
            for (Object file : files) {
                //SET fileposition so ClientPi thread can DOWNLOAD MUSIC
                //WAIT UNTIL CLientPI THREAD IS DONE DOWNLOADING FILE
                while (filePosition != -1);
                SYNCFLAG = true;
                filePosition = (int) file;

                //Update GUI After Download
                while (filePosition != -1);
                progressBar.setProgress(progressBar.getProgress() + 1, true);
            }

            runOnUiThread(MainActivity.this::DownloadComplete);

            Toast.makeText(MainActivity.this, "Sync Complete", Toast.LENGTH_SHORT).show();
        }
    }

    //"192.168.1.73", 6969
    public class ClientPi implements Runnable{

        /**
         * ACCEPT:  1. dir or fil
         *          2. boolean
         *          3. data
         *
         * SEND:    1. boolean
         *          2. file index
         * */
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
            //check if connected
            CSM = new ClientSocketManager(address, port, context);

            if( !CSM.hasConnection() ){
                if (Looper.myLooper() == null)
                    Looper.prepare();
                Toast.makeText(MainActivity.this, "Failed to Connect", Toast.LENGTH_SHORT).show();
                connect.setEnabled(true);
                return;
            }

            //Throw away "dir" for Start up
            CSM.readData(false);
            RetrieveDir();

            String control;
            //Send files wanted
            while( ! CSM.clientSocket.isClosed()) {
                //User clicked a file or Sync requested a file
                if(filePosition >= 0) {
                    runOnUiThread(MainActivity.this::DownloadInProgressGUI);

                    CSM.send(0, filePosition);
                    control = (String)CSM.readData(false);
                    //Check if dir or fil
                    if(control == null){
                        if (Looper.myLooper() == null)
                            Looper.prepare();
                        Toast.makeText(getApplicationContext(),"Server Might Be Down", Toast.LENGTH_SHORT).show();
                        runOnUiThread(MainActivity.this::DisconnectedGUI);
                        break;
                    }
                    if (control.equals("dir")) {
                        RetrieveDir();
                    } else if (control.equals("fil")){
                        RetrieveFile();
                    }
                    filePosition = -1;

                    runOnUiThread(MainActivity.this::DownloadComplete);
                }
            }
        }

        public void RetrieveDir(){
            //get new file list
            serverMedia = new ArrayList<>();
            while (CSM.readBoolean()) {
                serverMedia.add(new FileDataStructure((String) CSM.readData(false)));
            }

            //update UI
            runOnUiThread(() -> {
                adapter = new RecyclerViewAdapter(serverMedia);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                recyclerView.setAdapter(adapter);
            });
        }

        public void RetrieveFile(){
            if(!SYNCFLAG)
                runOnUiThread(MainActivity.this::DownloadInProgressGUI);

            //Download File
            runOnUiThread(() -> recyclerView.scrollToPosition(filePosition));
            Object check = CSM.readData(true);
            if (check != null)
                fileContentBytes = (byte[])check;
            else
                Toast.makeText(context,"Error when retrieving File", Toast.LENGTH_SHORT).show();

            sendFileToSharedStorage(serverMedia.get(filePosition).toString(), SYNCFLAG);

            if(!SYNCFLAG)
                runOnUiThread(MainActivity.this::DownloadComplete);

        }
    }

    /** Get RecyclerOnClick index */
    public static void setFilePosition(int FP){
        filePosition = FP;
    }

    /** create text file */
    public void sendFileToSharedStorage(String title, boolean syncRequest) {

        // Create a file with the requested Mime type
        String[] str = title.split("\\.");
        //System.out.println("------->> "+str[str.length-1]);
        String fileExtension = fileType(str[str.length - 1]);

        if (syncRequest) {
            /* DOWNLOAD TO SHARED AUDIO FILE */

            //System.out.println(fileExtension);
            if( fileExtension == null || !fileExtension.split("/")[0].equals("audio") ) {
                System.out.println("NOT DOWNLAOINDG");
                return;
            }

            System.out.println("-------------->> "+fileExtension.split("/")[0]);

            // Add a specific media item.
            ContentResolver resolver = getApplicationContext().getContentResolver();

            // Find all audio files on the primary external storage device.
            Uri audioCollection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

            // Publish a new song.
            ContentValues newSongDetails = new ContentValues();
            newSongDetails.put(MediaStore.Audio.Media.DISPLAY_NAME, title);

            // Keeps a handle to the new song's URI in case we need to modify it
            // later.
            Uri uri = resolver.insert(audioCollection, newSongDetails);

            //Uri uri = Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + title);
            try {
                OutputStream outputStream = getContentResolver().openOutputStream(uri, "w");
                outputStream.write(fileContentBytes);
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            SYNCFLAG = false;
        } else {
            /* Allow User to pick Location */

            // when you create document, you need to add Intent.ACTION_CREATE_DOCUMENT
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

            // filter to only show openable items.
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            if(fileExtension == null) {
                if (Looper.myLooper() == null)
                    Looper.prepare();
                Toast.makeText(getApplicationContext(), "Unknown File Extension", Toast.LENGTH_SHORT).show();
                return;
            }
            intent.setType(fileExtension);
            intent.putExtra(Intent.EXTRA_TITLE, title);
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, MediaStore.Audio.Media.INTERNAL_CONTENT_URI);

            startActivityForResult(intent, CREATE_FILE);
        }
    }

    public String fileType(String extension){
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getMimeTypeFromExtension(extension);
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