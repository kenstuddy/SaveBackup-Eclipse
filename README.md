# SaveBackup-Eclipse
This Eclipse IDE plugin saves a backup copy of your current open file every time you save the file.

# Requirements
Eclipse 3.4 or newer


# How to Install
Note: Instructions may differ based on your operating system. These instructions are written for Linux.

Clone the repository

```git clone https://github.com/kenstuddy/SaveBackup-Eclipse```

Go to the repository directory

```cd SaveBackup-Eclipse```

Go to the dropins folder

```cd dropins/```

Drag, copy, or move the jar file from the repository dropins folder that corresponds to your OS version to the drops folder in your eclipse directory

```mv ./SaveBackup_*Linux.jar /opt/eclipse/dropins/```

Launch Eclipse, and save a file. You should notice a folder in your home directory called ```.SaveBackup``` with subfolders that contain a timestamped copy of any file you save in Eclipse.
