package com.company;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLOutput;

public class ServerSocketManager {
    ServerSocket serverSocket;
    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;
    Socket socket;

    /**
     *
     *
     * Communication Structure:
     *
     * Wait for Connection:
     *  Loop:
     *      Wait for Boolean:
     *      True Continue <--> False Close connection
     *          Get user input (Directory or File)
     *          Get user input (index)
     *              Send Dir or Fil
     *
     * How Send File:
     *  Send File Size
     *  Loop:
     *      Send boolean TRUE
     *      Get boolean
     *      True Continue <--> False Stop
     *      Send sending size
     *      Send byte data
     *  Send boolean False
     *
     * How Send Directory:
     *  Send How many objects are in Dir
     *  Loop:
     *      Send boolean TRUE
     *      Get boolean
     *      True Continue <--> False Stop
     *      Send size
     *      Send File or Send Directory
     *      Send String
     *  Send boolean False
     *
     *
     * */

    int maxMemorySize;
    int port;
    boolean quite;
    ServerSocketManager(int port, int maxMemorySize, boolean quite){
        this.port = port;
        this.maxMemorySize = maxMemorySize * 1000000; //To bytes
        this.quite = quite;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e){ e.printStackTrace(); }
    }

    public void openConnection() {
        System.out.println("Waiting for devices to connect...");
        try {
            System.out.println("Address -> " + InetAddress.getLocalHost());
            System.out.println("PORT    -> " + serverSocket.getLocalPort() +"\n");
            socket = serverSocket.accept();
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("Conncted To --->  " + socket.getLocalSocketAddress());
    }

    // long ->
    // LOOP:
    //  Boolean ->
    //  Boolean <-
    //  int ->
    //  bytes ->
    public boolean send(File b){
        boolean success = true;

        long dataSize = b.length();
        if (serverSocket.isClosed()){
            toSystemConsole("Connection Closed");
        } else {
            int sendBytes = -1;
            try {
                send(dataSize);

                while(sendBytes != dataSize) {
                    send(true);
                    if(!readBoolean()) {
                        toSystemConsole("Client Asked to Stop transfer -/->");
                        success = false;
                        break;
                    }

                    //Only allow max bite size of transfer
                    if (dataSize >= maxMemorySize) {
                        sendBytes = maxMemorySize;
                        dataSize -= maxMemorySize;
                        toSystemConsole("-> File exceeds allowed size <-");
                        toSystemConsole("-> Sending in intervals of " + maxMemorySize);
                    } else {
                        sendBytes = (int)dataSize; //last part of the pie
                    }

                    send(sendBytes);

                    byte[] byteData = convertFile(b, sendBytes);
                    toSystemConsole("Sending File To --> client");
                    dataOutputStream.write(byteData, 0, (int)sendBytes);
                    dataOutputStream.flush();

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return success;
    }

    // Long ->
    public void send(long l){
        try{
            dataOutputStream.writeLong(l);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Int ->
    public void send(int i){
        try{
            dataOutputStream.writeInt(i);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // String ->
    public void send(String s){
        try{
            dataOutputStream.writeUTF(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Boolean ->
    public void send(boolean b){
        try{
            dataOutputStream.writeBoolean(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //File to bytes
    public byte[] convertFile(File f, long size) throws IOException {
        byte[] fileBytes = new byte[(int) size];
        FileInputStream fileInputStream = new FileInputStream(f.getAbsoluteFile());
        fileInputStream.read(fileBytes);
        return fileBytes;
    }

    //OLD
    public byte[] readFile(){
        byte[] serverData = null;
        try {
            int dataLength = dataInputStream.readInt();
            serverData = new byte[dataLength];
            dataInputStream.readFully(serverData, 0, dataLength);
        } catch (IOException e){ e.printStackTrace(); }

        return serverData;
    }

    // String <-
    public String readString(){
        String str = null;
        try {
            str = dataInputStream.readUTF();
        } catch (IOException e){ e.printStackTrace(); }
        return str;
    }

    // int <-
    public int readInt(){
        int integer = -1;
        try { integer = dataInputStream.readInt(); } catch (IOException e){ e.printStackTrace(); }
        return integer;
    }

    // Boolean <-
    public boolean readBoolean() {
        boolean b = false;
        try {
            b = dataInputStream.readBoolean();
        } catch (IOException e){
            toSystemConsole("ERROR: Connection Interrupted");
            e.printStackTrace();
        }
        return b;
    }

    public void toSystemConsole(String str){
        if(quite)
            System.out.println(str);
    }

    // Close Connection
    public void close() { try { serverSocket.close(); dataOutputStream.close(); } catch (IOException e ){ e.printStackTrace(); } }
}
