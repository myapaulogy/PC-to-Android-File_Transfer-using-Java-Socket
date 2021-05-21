package com.company;

import java.io.File;
import java.util.ArrayList;

public class IPServer {
    public static ServerSocketManager ssm;
    static String currentWorkingDir;
    private static int heapSpace = 5; //5mb
    private static boolean loud = false;
    private static int port = 6969;
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

    // Open connection
    // LOOP: Boolean <-
    //      String <-
    //      Int <-
    public static void main(String[] args) {
        System.out.println("\n");
        //Set up connection
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);

            if(args.length > 1)
                heapSpace = Integer.parseInt(args[1]);

            if(args.length > 2 && args[2].equals("--loud")) {
                System.out.println("Printing More to The Console\n\n");
                loud = true;
            }

        }

        ssm = new ServerSocketManager(port, heapSpace, loud);

        boolean shutdown = false;
        while (!shutdown) {
            currentWorkingDir = System.getProperty("user.dir");

	        //wait for connection
            ssm.openConnection();

            toSystemConsole("Accepting HandShake (Boolean, String, Int)");
            while(true) {
                if(ssm.readBoolean()) {
                    String clientInput = ssm.readString();
                    int index = ssm.readInt();
                    toSystemConsole("User asks for: " + clientInput);
                    toSystemConsole("At index: " + index);
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
                                toSystemConsole("User Requested");
                            }
                            break;

                        case "ShutDown":
                            System.out.println("Client Requested to ShutDown Server");
                            shutdown = true;
                            break;

                        default:
                            System.out.println("Unknown request -> " + clientInput + " <- ");
                            System.out.println("Closing Connection");
                            break;
                    }
                } else {
                    System.out.println("\nConnection Closed -/-");
                    break;
                }
            }

        }
        ssm.close();
        System.out.println("Server Shutdown");
    }

    public static void toSystemConsole(String str){
        if(loud)
            System.out.println(str);
    }

    public static boolean isRoot(String path){ return new File(path).toPath().getNameCount() == 0; }

    // int ->
    // LOOP:
    //      String ->
    //      String ->
    private static ArrayList<FileData> files;
    public static void SendDirectory(String path){
        toSystemConsole("Current Working Directory --> " + path);
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
            for (int i = 0; i < files.size(); i++) {
                ssm.send(files.get(i).isFile());
                ssm.send(files.get(i).getFilename());
            }

        } else {
            toSystemConsole("NOT SENDING AS DIR " + path);
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
