package org.openjfx;

import java.io.*;
import java.net.*;
import java.util.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;



public class FXMLController implements Initializable {

    @FXML private TableView clientList;
    @FXML private TableColumn clientCol;
    @FXML private TableView serverList;
    @FXML private TableColumn serverCol;

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            Socket socket = new Socket("127.0.0.1", 8080);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void downloadHandler(ActionEvent actionEvent) {
    }

    @FXML
    public void uploadHandler(ActionEvent actionEvent) {
    }

    public void listFiles(File dir, ){

    }
}
