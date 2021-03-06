package demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author Módos Károly
 */

public class Main  extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("FTP 1.0");
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/login.fxml"));
        Scene loginScene = new Scene(root,600,400);
        primaryStage.setScene(loginScene);
        primaryStage.show();

    }
}
