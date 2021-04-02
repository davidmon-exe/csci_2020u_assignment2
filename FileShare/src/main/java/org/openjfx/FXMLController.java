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



public class FXMLController implements Initializable {


    @FXML public TreeView<File> clientTreeView;
    public File clientDir = new File("shareDir");
    @FXML public TreeItem<File> clientRoot = new TreeItem<>(clientDir);

    @FXML public TreeView<File> serverTreeView;
    public File serverDir = new File("newDir");
    @FXML public TreeItem<File> serverRoot = new TreeItem<>();

    public Socket socket;{
        try {
            socket = new Socket("127.0.0.1", 1234);
            System.out.println("Connected to server");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public ObjectInputStream ois;{
        try {
            ois = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public ObjectOutputStream oos;{
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BufferedReader in;{
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PrintWriter out;{
        try {
            out = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void initialize(URL url, ResourceBundle rb) throws NullPointerException {

        clientTreeView.setRoot(loadFiles(clientDir, clientRoot));
        clientTreeView.setShowRoot(false);

        try {
            getServerFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverDir = new File("newDir");
        serverTreeView.setRoot(loadFiles(serverDir, serverRoot));
        serverTreeView.setShowRoot(false);
    }
    public void getServerFiles() throws IOException {
        out.print("GET ");
        BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
        DataInputStream dis = new DataInputStream(bis);

        int filesCount = dis.readInt();
        File[] files = new File[filesCount];
        for(int i = 0; i < filesCount; i++) {
            long fileLength = dis.readLong();
            String fileName = dis.readUTF();

            files[i] = new File( "newDir/" + fileName);

            FileOutputStream fos = new FileOutputStream(files[i]);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            for(int j = 0; j < fileLength; j++) bos.write(bis.read());

            bos.close();
        }

        dis.close();
    }
    public TreeItem<File> loadFiles(File dir, TreeItem<File> parent ) {

        parent.setExpanded(true);
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                TreeItem<File> subDir = new TreeItem<>(file);
                parent.getChildren().add(subDir);
                loadFiles(file, subDir);
            } else {
                parent.getChildren().add(new TreeItem<>(file));
                System.out.println(parent);
                System.out.println("Reading " + file.getName());
            }
        }
        return(parent);
    }
    @FXML
    public void downloadHandler(ActionEvent actionEvent) throws IOException {
        TreeItem<File> temp = serverTreeView.getSelectionModel().getSelectedItem();
        File file = temp.getValue();
        System.out.println("Reading " + file.getName());
        Path tempPath = Paths.get(clientDir.getAbsolutePath());
        Files.move(Paths.get(file.getAbsolutePath()), tempPath.resolve(file.getName()), REPLACE_EXISTING);
        clientTreeView.setRoot(null);
        TreeItem<File> temp2 = new TreeItem<>(clientDir);
        clientTreeView.setRoot(loadFiles(new File("shareDir"), temp2));
        clientTreeView.setShowRoot(false);
        serverTreeView.setRoot(null);
        TreeItem<File> temp3 = new TreeItem<>(serverDir);
        serverTreeView.setRoot(loadFiles(new File("newDir"), temp3));
        serverTreeView.setShowRoot(false);
    }

    @FXML
    public void uploadHandler(ActionEvent actionEvent) throws IOException {
        TreeItem<File> temp = clientTreeView.getSelectionModel().getSelectedItem();
        File file = temp.getValue();
        System.out.println("Reading " + file.getName());
        Path tempDir = Paths.get(serverDir.getAbsolutePath());
        Files.move(Paths.get(file.getAbsolutePath()), tempDir.resolve(file.getName()), REPLACE_EXISTING);
        clientTreeView.setRoot(null);
        TreeItem<File> temp2 = new TreeItem<>(clientDir);
        clientTreeView.setRoot(loadFiles(new File("shareDir"), temp2));
        clientTreeView.setShowRoot(false);
        serverTreeView.setRoot(null);
        TreeItem<File> temp3 = new TreeItem<>(serverDir);
        serverTreeView.setRoot(loadFiles(new File("newDir"), temp3));
        serverTreeView.setShowRoot(false);
    }
}
