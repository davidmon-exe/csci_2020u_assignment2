# CSCI 2020u Assignment 2: File Sharing Server and Client

![running_program](https://github.com/davidmon-exe/csci_2020u_assignment1/blob/main/running_program.png)

## Desription:
* File Share is a program that allows a user to create a file sharing server, and connect as a client which can upload or download files to or from the server respectively.
* To Note: download does not work completely. This is a problem to do with the inputstream in FXMLController for the downloadHandler that I was never able to solve with any solution 


## How to use:
* This program is a gradle project, therefore gradle build and gradle run can be used from the command line to start either of these programs. Start FileShareServer first as otherwise the client will have difficulty connecting and will have to be re-runned in order to work properly. If using Intellij to start these make sure to open FileShare and FileShareServer in seperate workspaces. 

## Improvements:
* Added a refresh button to reset the TreeViews incase they didn't update during either the download or upload, or if a directory is manually changed

## Resources:
* https://docs.oracle.com/javase/tutorial/essential/io/index.html
* https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/TreeView.html
* https://docs.oracle.com/javase/7/docs/api/java/net/Socket.html
* https://plugins.gradle.org/plugin/org.openjfx.javafxplugin
* https://plugins.gradle.org/plugin/org.beryx.jlink
