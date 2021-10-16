import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class IPServer extends Thread{
    public static ServerSocketManager ssm;
    static String currentWorkingDir;
    private int heapSpace = 5; //5mb
    private static boolean loud = false;
    private int port = 6969;

    IPServer(){}

    IPServer(int port, int heapSpace, boolean loud){
        this.port = port;
        this.heapSpace = heapSpace;
        IPServer.loud = loud;
    }

    public void run() {
        toActivity("");

        ssm = new ServerSocketManager(port, heapSpace, loud);

        boolean shutdown = false;
        while (!shutdown) {
            currentWorkingDir = System.getProperty("user.dir");


            //Thread a connection call
            AtomicBoolean connected = new AtomicBoolean(false);
            Thread connection = new Thread(() -> connected.set(ssm.openConnection()));
            connection.start();

            //Wait for connection and check for termination
            while (!connected.get()) {
                if (Thread.currentThread().isInterrupted()) {
                    connection.interrupt();
                    ssm.close();
                    return;
                }
            }

            verboseCommand("Accepting HandShake (Boolean, String, Int)");
            while(true) {

                //Thread boolean reply
                AtomicBoolean clientReply = new AtomicBoolean(false);
                AtomicBoolean clientBooleanPass = new AtomicBoolean(false);
                Thread booleanResponse = new Thread(() -> {
                    clientReply.set(ssm.readBoolean());
                    clientBooleanPass.set(true);
                });
                booleanResponse.start();

                //Wait for client reply and check for server termination
                while(!clientBooleanPass.get()) {
                    if (Thread.currentThread().isInterrupted()) {
                        booleanResponse.interrupt();
                        ssm.close();
                        return;
                    }
                }

                if(clientReply.get()) {
                    String clientInput = ssm.readString();
                    int index = ssm.readInt();

                    verboseCommand("User asks for: " + clientInput);
                    verboseCommand("At index: " + index);
                    switch (clientInput) {
                        case "Directory":
                            if (index != -1) {
                                currentWorkingDir = currentWorkingDir + "/" + files.get(index).filename;
                            }
                            SendDirectory(currentWorkingDir);
                            break;

                        case "File":
                            if (index < files.size()) {
                                SendFile(currentWorkingDir + "/" + files.get(index).filename);
                            } else {
                                verboseCommand("User Requested");
                            }
                            break;

                        case "ShutDown":
                            toActivity("Client Requested to ShutDown Server");
                            shutdown = true;
                            break;

                        default:
                            toActivity("Unknown request -> " + clientInput + " <- ");
                            toActivity("Closing Connection");
                            break;
                    }
                } else {
                    toActivity("\nConnection Closed -/-\n");
                    break;
                }

                toActivity("");
            }

        }
        ssm.close();
        toActivity("Server Shutdown");
    }

    public static void verboseCommand(String str){
        if(loud)
            toActivity(str);
    }

    public static void toActivity(String str){
        PcGui.activity.append(str + "\n");
        PcGui.activity.setCaretPosition(PcGui.activity.getText().length());
    }

    public static boolean isRoot(String path){ return new File(path).toPath().getNameCount() == 0; }

    private static ArrayList<FileData> files;
    public static void SendDirectory(String path){
        verboseCommand("Current Working Directory --> " + path);
        files = new ArrayList<>();
        files.add(new FileData( "..", false));


        //get file in new dir
        File f = new File(path);
        String[] pathNames;
        pathNames = f.list();

        //Fill array with files
        File dirOrFil;
        if(pathNames != null) {
            for (String pathName : pathNames) {
                dirOrFil = new File(path + "/" + pathName);
                files.add(new FileData(pathName, dirOrFil.isFile()));
            }

            ssm.send(files.size());
            for (FileData file : files) {
                ssm.send(file.isFile());
                ssm.send(file.getFilename());
            }

        } else {
            verboseCommand("NOT SENDING AS DIR " + path);
        }
    }

    // bytes ->
    public static boolean SendFile(String filePath){
        File file = new File(filePath);
        return ssm.send(file);
    }

    public static class FileData {
        private final String filename;
        private final String type;

        public FileData(String filename, boolean isFile){
            this.filename = filename;
            if(isFile){
                type = "File";
            } else {
                type = "Directory";
            }
        }

        public String getFilename(){ return filename; }
        public String isFile(){ return type; }
    }
}