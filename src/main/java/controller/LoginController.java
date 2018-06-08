package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;
import java.net.URL;
import java.util.*;

public class LoginController implements Initializable {
    private static final String CONFIG_PATH = "/home/kareeydev/IdeaProjects/ModosFTP/src/main/resources/properties/";
    private org.apache.commons.net.ftp.FTPClient client;
    private List<Properties> config;
    private ObservableList<String> sessionList;


    //Components
    @FXML
    private SplitPane loginWindow;
    @FXML
    private ListView listSession;
    @FXML
    private TextField tfHost;
    @FXML
    private TextField tfPort;
    @FXML
    private TextField tfUser;
    @FXML
    private PasswordField tfPassword;
    @FXML
    private Button btnSaveSession;
    @FXML
    private TextField tfSessionName;
    private   List<Node> children;

    private Stage primaryStage;
    private Scene actualScene;



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        client = new FTPClient();
        List<Properties> properties = loadAllConfig(CONFIG_PATH);
        sessionList = FXCollections.observableArrayList();
        for (Properties property : properties) {
            sessionList.add(property.getProperty("name"));
        }
        listSession.getItems().addAll(sessionList);

    }
    public boolean login() {
        return true;
    }

    /**
     * @param dir         the main properties container directory
     * @param sessionName the user defined name of the acatual session
     * @param config      list of key-value pairs of configuration data
     */
    public void saveToProperties(final String dir, String sessionName, Map<String, String> config) {
        StringBuilder sb = new StringBuilder();
        sb.append(dir + "/");
        sb.append(sessionName + ".properties");
        Properties prop = new Properties();
        String session = sb.toString();
        try (OutputStream out = new FileOutputStream((session))) {
            prop.setProperty("name",sessionName);
            for (String s : config.keySet()) {
                prop.setProperty(s, config.get(s));
            }
            prop.store(out, null);

        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    /**
     * @param filename the file which contains the desired configuration
     * @return the desidred configration
     */

    public Properties loadLoginConfig(String filename) {
        Properties result = new Properties();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename)))) {
            result.load(in);
        } catch (IOException io) {
            io.printStackTrace();
        }
        return result;
    }

    public List<Properties> loadAllConfig(final String DIR){
        List<Properties> allConfig = new ArrayList<>();
        File propertiDir = new File(DIR);
        if(propertiDir.isDirectory()){
            for (File file : propertiDir.listFiles()) {
                try(BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file)))){
                    Properties actual = new Properties();
                    actual.load(in);
                    allConfig.add(actual);
                }catch (IOException ex){
                    ex.printStackTrace();
                }
            }
        }
        return allConfig;
    }


    /**
     * loads the config, which the user clicked from the listView
     * then autocompletes the input fields with the session data
     *
     * @param mouseEvent the actual mouse event
     * @return
     */
    public String handleList(MouseEvent mouseEvent) {
        String actualSessionName = (String) listSession.getSelectionModel().getSelectedItem();
        System.out.println(actualSessionName);
        Properties prop = loadLoginConfig(CONFIG_PATH + actualSessionName + ".properties");
        System.out.println(prop);
        setupConfigFromInputFields(prop);
        setupLoginInputFields(prop);
        return actualSessionName;
    }

    private void setupLoginInputFields(Properties prop) {
    }


    public void openSessionDialog(ActionEvent actionEvent) throws IOException {
        Parent dialog =FXMLLoader.load(getClass().getClassLoader().getResource("view/session.fxml"));
        Scene sessionScene = new Scene(dialog,360,91);
        primaryStage = (Stage)loginWindow.getScene().getWindow();
        primaryStage.setScene(sessionScene);
        primaryStage.show();
    }

    public void confirmSessionName(ActionEvent actionEvent) {
       String sessionName = tfSessionName.getText();
//       List<Node> textFields = setupConfigFromInputFields();
        listSession.getItems().add(sessionName);
       
//       saveToProperties(CONFIG_PATH,sessionName,);
    }

    public void setupConfigFromInputFields(Properties prop) {
        System.out.print(prop);
        for (Object key : prop.keySet()) {
            String keyStr = key.toString();
            if(keyStr.equals("host")){
                tfHost.setText(prop.get(key).toString());
            }
            if(keyStr.equals("port")){
                tfPort.setText(prop.get(key).toString());
            }
            if(keyStr.equals("user")){
                tfUser.setText(prop.get(key).toString());
            }
            if(keyStr.equals("password")){
                tfPassword.setText(prop.get(key).toString());
            }
        }

    }
}
