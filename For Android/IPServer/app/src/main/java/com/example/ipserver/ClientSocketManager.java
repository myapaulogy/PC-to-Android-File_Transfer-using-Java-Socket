package com.example.ipserver;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientSocketManager {
    Socket clientSocket;
    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;

    String address;
    int port;
    Context context;

    public static boolean connectionFlag = false;

    ClientSocketManager (String address, int port, Context context) throws IOException {
        this.port = port;
        this.address = address;
        this.context = context;
        clientSocket = new Socket(address, port);
        dataInputStream = new DataInputStream(clientSocket.getInputStream());
        dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
        connectionFlag = true;
    }

    public boolean hasConnection(){
        return connectionFlag;
    }

    // Long ->
    public void send(long l) throws IOException { dataOutputStream.writeLong(l); }

    // Int ->
    public void send(int i) throws IOException { dataOutputStream.writeInt(i); }

    // String ->
    public void send(String s) throws IOException { dataOutputStream.writeUTF(s); }

    // Boolean ->
    public void send(boolean b) throws IOException { dataOutputStream.writeBoolean(b); }

    //File to bytes
    public byte[] convertFile(File f, long size) throws IOException {
        byte[] fileBytes = new byte[(int) size];
        FileInputStream fileInputStream = new FileInputStream(f.getAbsoluteFile());
        fileInputStream.read(fileBytes);
        return fileBytes;
    }

    // long <-
    // LOOP:
    //  Boolean <-
    //  Boolean ->
    //  int <-
    //  bytes <-
    public byte[] readFile(int index) {
        byte[] rawFile;
        try {
            int dataLength = (int)dataInputStream.readLong();
            rawFile = new byte[dataLength];

            Message loading = Message.obtain();
            loading.arg1 = dataLength;
            loading.arg2 = index;
            loading.obj = "Loading...";
            MainActivity.setProgressHandler.sendMessage(loading);
            while(MainActivity.setProgressHandler.hasMessages(loading.what));
            //MainActivity.progress(dataLength, 0, "Loading");
            boolean stop = false;
            int pageSize;
            int offset = 0;
            int percentage = 0;
            do{
                if (readBoolean()){
                    stop = MainActivity.STOP;
                    send(!stop);
                    if(stop)
                        break;

                    pageSize = dataInputStream.readInt();
                    dataInputStream.readFully(rawFile, offset, pageSize);
                    offset += pageSize;
                } else {
                    rawFile = null;
                }

                percentage = (int)(((double)offset/dataLength)*100);
                Message downloading = Message.obtain();
                downloading.arg1 = offset;
                downloading.arg2 = index;
                downloading.obj = "Downloading: " + percentage +"%";
                MainActivity.progressHandler.sendMessage(downloading);
                while(MainActivity.progressHandler.hasMessages(downloading.what));
                //MainActivity.progress(dataLength, offset, "Downloading: " + percentage +"%");
            }while(dataLength != offset);

            Message finished = Message.obtain();
            finished.arg1 = -1;
            finished.arg2 = index;

            if (stop) {

                finished.obj = "Stopped <-/- " + percentage + "%";
                MainActivity.progressHandler.sendMessage(finished);
                while(MainActivity.progressHandler.hasMessages(finished.what));
                //MainActivity.progress(dataLength, offset, "Stopped <-/-");
                return null;

            } else {

                finished.obj = "Downloaded <-";
                MainActivity.progressHandler.sendMessage(finished);
                while(MainActivity.progressHandler.hasMessages(finished.what));
                //MainActivity.progress(dataLength, offset, "Downloaded <-");
                return rawFile;
            }

        } catch (java.io.IOException e){
            e.printStackTrace();

        } catch (java.lang.OutOfMemoryError e){
            if (Looper.myLooper() == null)
                Looper.prepare();

            Toast.makeText(context, "Files is To big to transfer", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (java.lang.NullPointerException e) {
            if (Looper.myLooper() == null)
                Looper.prepare();

            Toast.makeText(context, "Server Might Be down", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        return null;
    }

    public boolean readBoolean() throws IOException { return dataInputStream.readBoolean(); }

    public String readUTF() throws IOException { return dataInputStream.readUTF(); }

    public int readInt() throws IOException { return dataInputStream.readInt(); }

    public void close() throws IOException {
            clientSocket.close();
            dataInputStream.close();
            dataOutputStream.close();
            connectionFlag = false;
    }
}
