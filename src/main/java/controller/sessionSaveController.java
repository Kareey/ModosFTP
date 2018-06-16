package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;

public class sessionSaveController {
    @FXML
    private LoginController loginController;
    private Scene loginScene = loginController.getLoginScene();

    public void confirmSessionName(ActionEvent actionEvent) {
        System.out.println("Hello");
        System.out.println(loginScene);
    }
}
