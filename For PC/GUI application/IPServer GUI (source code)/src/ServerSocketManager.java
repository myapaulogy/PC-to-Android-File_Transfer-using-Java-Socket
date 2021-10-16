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
    boolean loud;
    ServerSocketManager(int port, int maxMemorySize, boolean loud){
        this.port = port;
        this.maxMemorySize = maxMemorySize * 1000000; //To bytes
        this.loud = loud;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e){ e.printStackTrace(); }
    }

    public boolean openConnection() {
        try {
            socket = serverSocket.accept();
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());
            toActivity("Conncted To --->  " + socket.getRemoteSocketAddress());
        } catch (IOException e){
            System.out.println("\n IF \"Socket Close\" Error: It is expected when forcing accept call to terminate\n");
            e.printStackTrace();
        }
        return true;
    }

    // long ->
    // LOOP:
    //  Boolean ->
    //  Boolean <-
    //  int ->
    //  bytes ->
    public boolean send(File b)  {
        boolean success = true;

        long dataSize = b.length();
        if (serverSocket.isClosed()){
            verboseCommand("Connection Closed");
        } else {
            int sendBytes = -1;
            byte[] byteData;
            try {
                FileInputStream fileInputStream = new FileInputStream(b.getAbsoluteFile());
                send(dataSize);

                while(sendBytes != dataSize) {
                    send(true);
                    if(!readBoolean()) {
                        verboseCommand("Client Asked to Stop transfer -/->");
                        success = false;
                        break;
                    }

                    //Only allow max bite size of transfer
                    if (dataSize >= maxMemorySize) {
                        sendBytes = maxMemorySize;
                        dataSize -= maxMemorySize;
                        verboseCommand("-> File exceeds allowed size <-");
                        verboseCommand("-> Sending in intervals of " + maxMemorySize);
                    } else {
                        sendBytes = (int)dataSize; //last part of the pie
                    }

                    send(sendBytes);

                    byteData = new byte[sendBytes];
                    verboseCommand("Sending File To --> client");
                    fileInputStream.read(byteData);
                    dataOutputStream.write(byteData);
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
            verboseCommand("ERROR: Connection Interrupted");
            e.printStackTrace();
        }
        return b;
    }

    public void verboseCommand(String str){
        if(loud)
            toActivity(str);
    }

    public static void toActivity(String str){
        PcGui.activity.append(str + "\n");
        PcGui.activity.setCaretPosition(PcGui.activity.getText().length());
    }

    // Close Connection
    public void close() {
        try {
            if(serverSocket != null) serverSocket.close();
            if(socket != null) socket.close();
            if(dataOutputStream != null) dataOutputStream.close();
            if(dataInputStream != null) dataInputStream.close();

        } catch (IOException e ){ e.printStackTrace(); } }
}