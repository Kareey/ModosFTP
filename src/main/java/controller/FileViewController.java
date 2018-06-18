package controller;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import org.apache.commons.net.ftp.FTPFile;

public class FileViewController{
    @FXML
    Parent fileWindow;
    @FXML
    ListView<FTPFile> ftpList1;
    @FXML
    ListView<FTPFile> ftpList2;
    private LoginController loginController;
    public FileViewController() {

    }

    public void init(LoginController loginController) {
        System.out.println("Hello World");
        this.loginController = loginController;
        System.out.println(loginController.getClient());
    }

    @Override
    public String toString() {
        return "FileViewController{" +
                "fileWindow=" + fileWindow +
                ", loginController=" + loginController +
                '}';
    }
}
