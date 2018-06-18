package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
import org.apache.commons.net.ftp.FTPFile;
import org.w3c.dom.Text;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginController implements Initializable {
    private static final String CONFIG_PATH = "/home/kareeydev/IdeaProjects/ModosFTP/src/main/resources/properties/";
    private org.apache.commons.net.ftp.FTPClient client;
    private List<Properties> config;
    private ObservableList<String> sessionList;



    //Components
    @FXML
    private Parent loginWindow;
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
    @FXML
    private ComboBox<String> comboProtocol;

    private Stage primaryStage;
    private Scene loginScene;
    public LoginController() throws IOException {
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loginScene = loginWindow.getScene();
        client = new FTPClient();
        List<Properties> properties = loadAllConfig(CONFIG_PATH);
        sessionList = FXCollections.observableArrayList();

        for (Properties property : properties) {
            sessionList.add(property.getProperty("name"));
        }
        listSession.getItems().addAll(sessionList);


        //Combobox setup with list of FTP-like protocols
        String[] ftpProts = new String[]{"FTP","SFTP","SCP"};
        comboProtocol.setItems(FXCollections.observableList(Arrays.asList(ftpProts)));

        List<TextField> inputFields = new ArrayList<>();
        inputFields.add(tfUser);
        inputFields.add(tfHost);
        inputFields.add(tfPassword);
        inputFields.add(tfPort);
        for (TextField inputField : inputFields) {
                inputField.textProperty().addListener((observable, oldValue, newValue) -> btnSaveSession.setDisable(false));
        }

    }

    private boolean checkInputFields() {
        return tfUser.getText()!=null &&
               tfPassword.getText()!=null &&
               tfHost.getText()!=null &&
               tfPassword.getText()!=null;
    }

    public boolean login() throws IOException {
        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    client.connect(tfHost.getText(),Integer.parseInt(tfPort.getText()));
                    System.out.println(client.getReplyString());
                    if(client.getReplyCode()>200 && client.getReplyCode()<300){
                      if(  client.login(tfUser.getText(),tfPassword.getText())){
                          System.out.println("Logged in");
                          for (FTPFile ftpFile : client.listFiles()) {
                              System.out.println(ftpFile.getName());
                          }
                      }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        executorService.shutdown();

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
            prop.setProperty("name", sessionName);
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

    public List<Properties> loadAllConfig(final String DIR) {
        List<Properties> allConfig = new ArrayList<>();
        File propertiDir = new File(DIR);
        if (propertiDir.isDirectory()) {
            for (File file : propertiDir.listFiles()) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
                    Properties actual = new Properties();
                    actual.load(in);
                    allConfig.add(actual);
                } catch (IOException ex) {
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
        return actualSessionName;
    }


    public void openSessionDialog(ActionEvent actionEvent) throws IOException {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(loginWindow.getScene().getWindow());
        TextField tfSession = null;
        try{
            DialogPane dialogPane = new DialogPane();
            Label lbSession = new Label("Név: ");
           tfSession = new TextField();
            tfSession.autosize();
            dialogPane.contentProperty().setValue(tfSession);
            dialogPane.setHeader(lbSession);
            dialog.getDialogPane().setContent(dialogPane);
        }catch(Exception e){
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        Optional<ButtonType> options = dialog.showAndWait();
        if(options.isPresent() && options.get() == ButtonType.OK){
            Map<String,String> config =  new HashMap<>();
            config.put("name",tfSession.getText());
            config.put("host",tfHost.getText());
            config.put("port",tfPort.getText());
            config.put("user",tfUser.getText());
            config.put("password",tfPassword.getText());
            saveToProperties(CONFIG_PATH,tfSession.getText(),config);
        }

    }



    public void setupConfigFromInputFields(Properties prop) {
        System.out.print(prop);
        for (Object key : prop.keySet()) {
            String keyStr = key.toString();
            if (keyStr.equals("host")) {
                tfHost.setText(prop.get(key).toString());
            }
            if (keyStr.equals("port")) {
                tfPort.setText(prop.get(key).toString());
            }
            if (keyStr.equals("user")) {
                tfUser.setText(prop.get(key).toString());
            }
            if (keyStr.equals("password")) {
                tfPassword.setText(prop.get(key).toString());
            }
        }
        btnSaveSession.setDisable(true);
    }

    public void setSaveSessionBtnEnabledOnInputChange() {

    }

    public Scene getLoginScene() {
        return loginScene;
    }

    public void setLoginScene(Scene loginScene) {
        this.loginScene = loginScene;
    }
//    public Task<Void> loginTask(){
//        System.out.println("Inside login task...");
//        return new Task<Void>() {
//            @Override
//            protected Void call() throws Exception {
//                String hostname = tfHost.getText();
//                int port = Integer.parseInt(tfHost.getText());
//                 client.connect(hostname,port);
//                System.out.println(client.getReplyString());
//                return null;
//            }
//        };
//    }
}
