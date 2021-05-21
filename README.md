# PC-to-Android-File_Transfer-using-Java-Socket
This is an android application that allows the user to download files from a computer on the same local network

# Quick Start Guide
## For Android
Within the folder is the APK... Install it (on your android phone). 
You might get a warning saying google doesn't know this publisher and its not on the google play store because you need to pay $25 for a dev account

## For PC
within this folder is the JAR file... download it and run it. (double click the jar)

# Need More Help On Installation/Running
## Android 
I created the apk with android 11 (v30 & min version (28)) 
You might need to update you OS

## PC
I created the jar file with java 8 (To run jar files make sure you installed Java first)
When double Clicking you do not see the console show you IP or PORT or see any information (Completely Silent).
reminder for console "java -jar IPserver.jar"

### For Dummys console
press windows Key and type "command prompt"
*click the application*
go to where you saved the jar file (file explorer) and copy its location
should look somthing like this *C:\Users\broth\Downloads* Dont include the jar file

Go to the command prompt app and type "cd C:\Users\broth\Downloads" and press enter

##### if you type "dir" you should see IPserver.jar listed
(if its downloaded in a different drive type the drive into the command prompt "D:")

now type "java -jar IPserver.jar"

You should now see this in the console:

Waiting for devices to connect...
Address -> PC NAME / LOCAL IP ADDRESS
PORT    -> PORT NUMBER (DEFAULT: 6969)

Congrates... go hack The Pentagon you mad boy

# Console Arguments
arg1 = port number              DEFAULT: 6969     Port range depends on your router
arg2 = transfer size (in mb)    DEFAULT: 5        If you set this to high you might get a heap space error
arg3 = print more to console    DEFAULT: false    to set this to true type: "--loud"

example: "java -jar IPserver.jar 420 2 --loud"

I forgot about a help argument and I dont feel like adding it right now
