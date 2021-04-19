package com.example.ipserver;

import android.content.Context;
import android.os.Looper;
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

    ClientSocketManager(String address, int port, Context context){
        this.port = port;
        this.address = address;
        this.context = context;
        try {
            clientSocket = new Socket(address, port);
            dataInputStream = new DataInputStream(clientSocket.getInputStream());
            dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
            connectionFlag = true;
        } catch (java.io.IOException e){ e.printStackTrace(); }
    }

    public boolean hasConnection(){
        return connectionFlag;
    }

    public void send(int dataSize, Object b){
        byte[] byteData = new byte[dataSize];

        if (!clientSocket.isClosed()) {
            try {
                if (b instanceof java.io.File) {
                    //System.out.println("Packaging Type ---> (file)");
                    byteData = convertFile((File) b, dataSize);

                } else if (b instanceof String) {
                    //System.out.println("Packaging Type --> (String)");
                    byteData = convertString((String) b);

                } else if (b instanceof Integer){
                    //System.out.println("Packaging Type --> (Int)");
                    send(true);
                    dataOutputStream.writeInt((int)b);
                    return;
                }

                //System.out.println("Sending Data To --> client");
                dataOutputStream.writeInt(dataSize);
                dataOutputStream.write(byteData);
                dataOutputStream.flush();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void send(boolean b){
        try{
            dataOutputStream.writeBoolean(b);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] convertFile(File f, int size) throws IOException {
        byte[] fileBytes = new byte[size];
        FileInputStream fileInputStream = new FileInputStream(f.getAbsoluteFile());
        fileInputStream.read(fileBytes);
        return fileBytes;
    }

    public byte[] convertString(String s){ return s.getBytes(); }

    boolean isFile;
    public Object readData(boolean isFile){
        this.isFile = isFile;
        byte[] serverData;
        try {
            //Receive Total size
            //Loop
                //Receive page Size
                //Receive data

            int dataLength = dataInputStream.readInt();
            serverData = new byte[dataLength];

            //NEWWWW PROTOCOL

            //MainActivity.adapter.getItemViewType(0)
            if(isFile)
                MainActivity.progress(dataLength, 0);

            int pageSize;
            int offset = 0;
            do{
                pageSize = dataInputStream.readInt();
                //System.out.println("-------------->> "+ offset);
                //System.out.println("-------------->> "+ pageSize);
                dataInputStream.readFully(serverData, offset, pageSize);
                offset += pageSize;

                if(isFile)
                    MainActivity.progress(dataLength, offset);
                //System.out.println("-------------->>> " + pageSize + " <<<---------------");
            }while(dataLength != offset);

            //NEWWWW PROTOCOL


            //dataInputStream.readFully(serverData, 0, dataLength);
            if (isFile) {
                return serverData;
            } else if (serverData != null){
                //System.out.println(new String(serverData));
                return new String(serverData);
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
        }
        return null;
    }

    public boolean readBoolean() {
        boolean b = false;
        try { b = dataInputStream.readBoolean(); } catch (java.io.IOException e){ e.printStackTrace(); }
        return b;
    }

    public void close(){
        try {
            clientSocket.close();
            dataInputStream.close();
            dataOutputStream.close();
            connectionFlag = false;
        } catch (java.io.IOException e){ e.printStackTrace(); }
    }
}
