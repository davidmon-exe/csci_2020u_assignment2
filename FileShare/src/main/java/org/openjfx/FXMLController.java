package org.openjfx;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import static java.nio.file.StandardCopyOption.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;


/**
 * Controller and main source of code for the File Share Client.
 */
public class FXMLController implements Initializable {


    @FXML public  TreeView<File> clientTreeView;
    public File clientDir = new File("shareDir");
    @FXML public TreeItem<File> clientRoot = new TreeItem<>(clientDir);

    @FXML public TreeView<File> serverTreeView;
    @FXML public static TreeItem<File> serverRoot = new TreeItem<>();

    public Socket socket;
    @FXML
    public void initialize(URL url, ResourceBundle rb) throws NullPointerException {

        // set the client root to the returned TreeItem
        clientTreeView.setRoot(loadFiles(clientDir, clientRoot));
        clientTreeView.setShowRoot(false);

        try {
            getServerFiles(serverRoot);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    /** Connect to server and get a list of available files. It will connect using a new socket,
     * and will send the command "fetch". Method will read the ArrayList sent from the server into the parent tree item.
     * That item is then set as the new root, and the method will disconnect from the socket.
     *
     * @param parent        Root TreeItem that will be edited while method is running
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void getServerFiles(TreeItem<File> parent) throws IOException, ClassNotFoundException {
        try {
            socket = new Socket("127.0.0.1", 1234);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        // send command to server
        out.writeUTF("FETCH");
        out.flush();

        // read incoming ArrayList
        ArrayList<File> serverFiles = (ArrayList<File>) in.readObject();
        // iterate through the ArrayList and add each file to the tree item
        for (File file: serverFiles) {
            System.out.println("Reading " + file.getName());
            parent.getChildren().add(new TreeItem<>(file));
        }
        in.close();
        out.close();
        socket.close();

        // set TreeView of server
        serverTreeView.setRoot(parent);
        serverTreeView.setShowRoot(false);
        System.out.println("Disconnected from Server");
    }

    /** Method to load the files of the client onto a tree item. It will recursively call if a
     * subdirectory is detected as the current file, and will then browse through and
     * add each file as a child of the TreeItem
     *
     * @param dir    share directory on the client side
     * @param parent Root TreeItem that will be edited while method is running
     * @return       TreeItem that has the file list
     */
    private TreeItem<File> loadFiles(File dir, TreeItem<File> parent ) {
        parent.setExpanded(true);
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                loadFiles(file, parent);
            } else {
                parent.getChildren().add(new TreeItem<>(file));
                System.out.println("Reading " + file.getName());
            }
        }
        return(parent);
    }

    /**
     * Handle the downloading of a file from the server. It is meant to send the command "download",
     * then take the selected TreeItem and convert it to type File. It parses the name and sends it to server,
     * where it will find file. It is then supposed to read found size, create a new byteArray with that size,
     * and then get the bytes of the file on the server and save them to a file on the client shareDir
     *
     * This method does not work fully, and is broken after the dis.readLong() line
     * @param actionEvent
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @FXML
    private void downloadHandler(ActionEvent actionEvent) throws IOException, ClassNotFoundException {
        try {
            socket = new Socket("127.0.0.1", 1234);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //send command to server
        ObjectOutputStream command = new ObjectOutputStream(socket.getOutputStream());
        command.writeUTF("DOWNLOAD");
        command.flush();

        DataInputStream dis = new DataInputStream(socket.getInputStream());
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

        TreeItem<File> temp = serverTreeView.getSelectionModel().getSelectedItem();
        File file = temp.getValue();
        System.out.println(file.getName());
        dos.writeUTF(file.getName());

        FileOutputStream fos = new FileOutputStream("shareDir/" + file.getName());
        // input stream has problems starting on the next line
        int fileSize = (int) dis.readLong();
        byte[] fileBytes = new byte[fileSize];
        int read = 0;
        int totalRead = 0;
        int remaining = fileSize;

        // process incoming bytes into fos
        while((read = dis.read(fileBytes, 0, Math.min(fileBytes.length, remaining))) > 0) {
            totalRead += read;
            remaining -= read;
            System.out.println("read " + totalRead + " bytes.");
            fos.write(fileBytes, 0, read);
        }
        fos.close();
        dos.close();
        dis.close();
        socket.close();

        // reset TreeViews
        TreeItem<File> newClientRoot = new TreeItem<>(clientDir);
        clientTreeView.setRoot(loadFiles(new File("shareDir"), newClientRoot));
        clientTreeView.setShowRoot(false);

        TreeItem<File> newServerRoot = new TreeItem<>();
        serverTreeView.setRoot(null);
        getServerFiles(newServerRoot);
    }

    /** Method to upload a file from client to server. It will start a new socket and send the command "upload".
     * It then gets the selected TreeItem in client file list and will convert it to type File.
     * The name and size of the selected file will then be sent to server, and then will read the file into bytes and
     * send those over to the server. It  will then update both TreeViews to reflect the changed directories
     *
     * @param actionEvent
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @FXML
    private void uploadHandler(ActionEvent actionEvent) throws IOException, ClassNotFoundException {
        try {
            socket = new Socket("127.0.0.1", 1234);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //send command to server
        ObjectOutputStream command = new ObjectOutputStream(socket.getOutputStream());
        command.writeUTF("UPLOAD");
        command.flush();

        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        TreeItem<File> temp = clientTreeView.getSelectionModel().getSelectedItem();

        File file = temp.getValue();
        dos.writeUTF(file.getName());
        long fileSize =  file.length();
        dos.writeLong(fileSize);
        System.out.println("Uploading " + file.getName());

        // process file into bytes and send to server
        FileInputStream fis = new FileInputStream(file);
        byte [] fileBytes  = new byte[(int) fileSize];
        while (fis.read(fileBytes) > 0){
            dos.write(fileBytes);
        }
        fis.close();
        dos.close();
        socket.close();

        // reset TreeViews
        TreeItem<File> newClientRoot = new TreeItem<>(clientDir);
        clientTreeView.setRoot(loadFiles(new File("shareDir"), newClientRoot));
        clientTreeView.setShowRoot(false);

        TreeItem<File> newServerRoot = new TreeItem<>();
        serverTreeView.setRoot(null);
        getServerFiles(newServerRoot);
    }

    /**
     * Refresh the TreeViews in case that was skipped over while performing other tasks,
     * or if a directory is manually changed
     *
     * @param actionEvent
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @FXML
    private void refreshHandler(ActionEvent actionEvent) throws IOException, ClassNotFoundException {
        TreeItem<File> newClientRoot = new TreeItem<>(clientDir);
        clientTreeView.setRoot(loadFiles(new File("shareDir"), newClientRoot));
        clientTreeView.setShowRoot(false);

        TreeItem<File> newServerRoot = new TreeItem<>();
        serverTreeView.setRoot(null);
        getServerFiles(newServerRoot);
    }
}
