package com.company;

import java.io.File;
import java.util.ArrayList; //192.168.1.73

public class IPServer {
    public static ServerSocketManager ssm;
    private static ArrayList<String> files;
    static String currentWorkingDir;
    private static int heapSpace = 1000; //1gb
    /**
    * Communication Structure To Client:
    * Loop Server:
    *   Loop Dir:
    *       send: "dir"
    *       Send: boolean
    *       Send: FileName
    *   loop Get CLIENT data:
    *       Get: boolean
    *       Get: index
    *       if "dir":
    *           Loop Dir
    *       if "fil":
    *           send: "fil"
    *           Send: boolean
    *           Send: file
    *
    *       SEND    1. dir or fil
    *               2. boolean
    *               3. type
     * */

    public static void main(String[] args) {
        //Set up connection

        if (args.length > 0) {
            heapSpace = Integer.parseInt(args[0]);
        }

        ssm = new ServerSocketManager(6969, heapSpace);

        while (true) {
            currentWorkingDir = System.getProperty("user.dir");

	    //wait for connection
            ssm.openConnection();
            //send dir to Client
            SendDirectory("");

            //Get next
            while (true) {
                if (ssm.readBoolean()) {
                    int filePos = ssm.readInt();
                    /* Go into Dir */
                    File file = new File(currentWorkingDir + "/" + files.get(filePos));

                    if (file.isDirectory()) {
                        /* Send "dir" */
                        System.out.println("Sending  DIR ---> " + files.get(filePos));
                        SendDirectory(files.get(filePos));
                    } else {
                        /* Send "fil" */
                        System.out.println("Sending FIL ---> " + files.get(filePos));
                        ssm.send(3, "fil");
                        ssm.send((int) file.length(), file);
                        //SendDirectory("");
                    }

                } else {
                    break;
                }
            }
        }
    }

    public static boolean isRoot(String path){ return new File(path).toPath().getNameCount() == 0; }

    public static void SendDirectory(String fname){
        files = new ArrayList<>();
        files.add("..");

        currentWorkingDir = currentWorkingDir +"/"+ fname;



        /* //fix? dont append \..


        //Dont want to send root dir if at root
        //Makes this pointless
        if (isRoot(currentWorkingDir)) {
            return;
        }
        */

        //get file in new dir
        File f = new File(currentWorkingDir);
        String[] pathNames;
        pathNames = f.list();

        //Fill array with files
        for (String pathname : pathNames) {
            System.out.println("Storing Files ---> "+pathname);
            files.add(pathname);
        }

        /* Send Client to get ready for Dir */
        ssm.send(3,"dir");

        //send files to client
        for (String title : files){

	    //remove Special chars
	    title = title.replaceAll("[^a-zA-Z0-9-_\\]\\[?><!@#$%^&*()+=`~{}| .]", "");

            ssm.send(true);
	    ssm.send(title.length(), title);
        }
        ssm.send(false);
    }
}
