package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import org.apache.commons.net.ftp.FTPFile;

import java.net.URL;
import java.util.ResourceBundle;

public class FileViewController implements Initializable {
    @FXML
    Parent fileWindow;
    @FXML
    ListView<FTPFile> ftpList1;
    @FXML
    ListView<FTPFile> ftpList2;
    private LoginController loginController;
    private ResourceBundle myBundle;

    //default constructor
    public FileViewController() {
    }



    @Override
    public String toString() {
        return "FileViewController{" +
                "fileWindow=" + fileWindow +
                ", loginController=" + loginController +
                '}';
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("HELLO WORLD!");
        myBundle = resources;
    }
}
