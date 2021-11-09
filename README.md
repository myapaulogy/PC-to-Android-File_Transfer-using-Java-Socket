# PC-to-Android-File_Transfer-using-Java-Socket
This is an android application that allows the user to download files from a computer on the **local network** <br />

# Quick Start Guide
## For Android
Download the Apk located here -> [Android APK](https://github.com/PaulAntonescu/PC-to-Android-File_Transfer-using-Java-Socket/tree/main/For%20Android) <br />
The source code is located at the same location as the APK.<br />
The code was developed using [Android Studio](https://developer.android.com/studio)

## For PC
### Command Line interface
Download the jar located here -> [CLI](https://github.com/PaulAntonescu/PC-to-Android-File_Transfer-using-Java-Socket/tree/main/For%20PC/Command%20Line%20application)

### Graphical User interface
Download the jar located here -> [GUI](https://github.com/PaulAntonescu/PC-to-Android-File_Transfer-using-Java-Socket/tree/main/For%20PC/GUI%20application)

# Console Arguments for CLI
arguments | Default Values | Details
------------ | ------------- | -------------
arg1 = port number            |  DEFAULT: 6969   |  Port range depends on your router <br />
arg2 = transfer size (in mb)  |  DEFAULT: 5      |  If you set this to high you might get a heap space error <br />
arg3 = print more to console  |  DEFAULT: false  |  to set this to true type: "--loud" <br />

example: "java -jar IPserver.jar 420 2 --loud"

# ShowCase
## PC side (CLI)
![pc command line](https://raw.githubusercontent.com/PaulAntonescu/PC-to-Android-File_Transfer-using-Java-Socket/main/assets/pc_side_cli.png)

This is the expected output when running the command line client<br />

If you run the -loud command it will display more to the console<br />
![pc loud command](https://raw.githubusercontent.com/PaulAntonescu/PC-to-Android-File_Transfer-using-Java-Socket/main/assets/pc_side_cli_android_downloading.png)

## PC side (GUI)
The Graphical Interface will show Local Addresses

And you can type your preferred Port Address and the max transfer size.
![GUI interface](https://raw.githubusercontent.com/PaulAntonescu/PC-to-Android-File_Transfer-using-Java-Socket/main/assets/GUI_starting.png)

The Verbose command is similar to the loud flag for the CLI version.

When starting or when a user is connected the GUI will update and show information about the transactions happening
![User Connected](https://raw.githubusercontent.com/PaulAntonescu/PC-to-Android-File_Transfer-using-Java-Socket/main/assets/User_Connected.png)

## Android side
![When Connected](https://raw.githubusercontent.com/PaulAntonescu/PC-to-Android-File_Transfer-using-Java-Socket/main/assets/Android_First_State.png)

![Moved dir](https://raw.githubusercontent.com/PaulAntonescu/PC-to-Android-File_Transfer-using-Java-Socket/main/assets/Android_Moving_Dir.png)

When connected to the right IP and Port Address the pc will send the current working directory to the user.<br />
From here the user can move in and out of folders and download files.<br />
**Unfortunately it is set up to only download "mime type" files**<br />

![Download Progress](https://github.com/PaulAntonescu/PC-to-Android-File_Transfer-using-Java-Socket/blob/main/assets/Android_Downloading_Progress.png)

When downloading a big file the PC will send the file into chuncks (arg2 = transfer size)<br />
**developed this method for low memory computers (raspberryPi...)**<br />

![Hamburger](https://raw.githubusercontent.com/PaulAntonescu/PC-to-Android-File_Transfer-using-Java-Socket/main/assets/Android_Hamburger_Menu.png)

1 "To First" -> Will move list to top of displayed files/folders "file: .."<br />

2 "Sync Music Files" -> App will access Local Music Folder (on Android) and compare the CWD and download all music file from the PC<br />

3 "Shutdown" -> It will shutdown the PC Server program<br />
