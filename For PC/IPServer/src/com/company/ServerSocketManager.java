package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketManager {
    ServerSocket serverSocket;
    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;
    Socket socket;

    int maxMemorySize;
    int port;
    ServerSocketManager(int port, int maxMemorySize){
        this.port = port;
        this.maxMemorySize = maxMemorySize * 1000000; //To bytes
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e){ e.printStackTrace(); }
    }

    public void openConnection(){
        System.out.println("Waiting for devices to connect...");
        try {
            socket = serverSocket.accept();
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e){ e.printStackTrace(); }
        System.out.println("Conncted To --->  " + socket.getLocalSocketAddress());
    }

    public void send(int dataSize, Object b){
        if (serverSocket.isClosed()){
            System.out.println("Connection Closed");
            //System.out.println("error");
        } else {
            int sendBytes = -1;
            try {
                //Send Total Size
                //Loop
                    //Send page Size
                    //Send data

		        //System.out.println("------dataSize----------->>>>>> " + dataSize);
                dataOutputStream.writeInt(dataSize);

                while(sendBytes != dataSize) {
                    //Only allow max bite size of transfer
                    if (dataSize >= maxMemorySize) {
                        sendBytes = maxMemorySize;
                        dataSize -= maxMemorySize;
                        System.out.println("-> File exceeds allowed size <-");
                        System.out.println("-> Sending in intervals of " + maxMemorySize);
                    } else {
                        sendBytes = dataSize; //last pie
                    }
                    //System.out.println("------SendBytes---------->>>>>> " + sendBytes);
                    dataOutputStream.writeInt(sendBytes);

                    byte[] byteData = new byte[sendBytes];

                    if (b instanceof File) {
                        System.out.println("Packaging Type ---> (file)");
                        byteData = convertFile((File) b, sendBytes);
                    } else if (b instanceof String) {
                        System.out.println("Packaging Type --> (String)");
                        byteData = convertString((String) b);
                    }

                    System.out.println("Sending Data To --> client");

                    dataOutputStream.write(byteData);
                    dataOutputStream.flush();

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void send(boolean b){
        try{
            dataOutputStream.writeBoolean(b);
        } catch (IOException e) {
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
        byte[] serverData = null;
        try {
            int dataLength = dataInputStream.readInt();
            serverData = new byte[dataLength];
            dataInputStream.readFully(serverData, 0, dataLength);
        } catch (IOException e){ e.printStackTrace(); }

        if (isFile) {
            return serverData;
        } else {
            return new String(serverData);
        }
    }

    public int readInt(){
        int integer = -1;
        try {integer = dataInputStream.readInt(); } catch (IOException e){ e.printStackTrace(); }
        System.out.println("Retrieved Data <-- " + integer);
        return integer;
    }

    public boolean readBoolean() {
        boolean b = false;
        try { b = dataInputStream.readBoolean();
        } catch (IOException e){
            //e.printStackTrace();
            System.out.println("\n\nConnection lost -> RESTARTING <-\n");
        }
        return b;
    }

    public void close() { try { serverSocket.close(); dataOutputStream.close(); } catch (IOException e ){ e.printStackTrace(); } }
}
