package com.company;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server____old {
    static final File[] fileToSend = new File[1];
    static String fileName = "question.zzzz";
    static ServerSocket serverSocket = null;

    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(6969);
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileToSend[0] = new File(fileName);
        while (true) {
            try {

                System.out.println("Waiting for connection");
                //Get connections
                Socket socket = serverSocket.accept();
                System.out.println("------->> Connection");
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                //Get File to send
                byte[] fileNameBytes = fileName.getBytes();
                byte[] fileBytes = new byte[(int) fileToSend[0].length()];

                //Send data to Client
                //Title
                dataOutputStream.writeInt(fileNameBytes.length);
                dataOutputStream.write(fileNameBytes);

                //File
                FileInputStream fileInputStream = new FileInputStream(fileToSend[0].getAbsolutePath());
                fileInputStream.read(fileBytes);
                dataOutputStream.writeInt(fileBytes.length);
                dataOutputStream.write(fileBytes);
                System.out.println("----->> Sent");

                //Close Connections
                dataOutputStream.close();
                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
