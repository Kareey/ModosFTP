package controller;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;
import java.util.*;

public class LoginController {
    private static String CONFIG_PATH;

    @FXML
    public FileViewController fileViewController;
    private org.apache.commons.net.ftp.FTPClient client;


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
    static boolean logged_in = false;

    public LoginController() {

    }


    public String getOS() {
        return System.getProperty("os.name");
    }

    public void printCurrendDir() {
        System.out.println(System.getProperty("user.dir"));
    }

    @FXML
    public void initialize() {
        CONFIG_PATH = System.getenv("HOME") + "/properties";

//        fileViewController.init(this);

        loginScene = loginWindow.getScene();
        setClient(new FTPClient());
        List<Properties> properties = loadAllConfig(CONFIG_PATH);
        ObservableList<String> sessionList = FXCollections.observableArrayList();
        for (Properties property : properties) {
            sessionList.add(property.getProperty("name"));
        }
        listSession.getItems().addAll(sessionList);

        listSession.setCellFactory(param -> {
            ListCell<String> cell = new ListCell<>();
            ContextMenu menu = new ContextMenu();
            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("Delete \"%s\"", cell.itemProperty()));
            deleteItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    listSession.getItems().remove(cell.getItem());
                    deleteFile(cell.getItem());
                }
            });
            cell.textProperty().bind(cell.itemProperty());
            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(menu);
                }
            });
            menu.getItems().add(deleteItem);
            return cell;
        });

        //Combobox setup with list of FTP-like protocols
        String[] ftpProts = new String[]{"FTP", "SFTP", "SCP"};
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

    public boolean login() throws IOException {
        Platform.runLater(() -> {
            try {
                try {
                    client.connect(tfHost.getText(), Integer.parseInt(tfPort.getText()));
                } catch (IOException io) {
                    System.out.println(client.getReplyString());
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Connection error to" + tfHost.getText());
                    alert.showAndWait();
                }
                System.out.println(client.getReplyString());
                if (client.getReplyCode() > 200 && client.getReplyCode() < 300) {
                    if (client.login(tfUser.getText(), tfPassword.getText())) {
                        logged_in = true;
                        Parent fileWindow = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/fileView.fxml"));
                        Stage currentStage = (Stage) loginWindow.getScene().getWindow();
                        currentStage.setScene(new Scene(fileWindow));
                        currentStage.show();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return logged_in;
    }

    /**
     * @param dir         the main properties container directory
     * @param sessionName the user defined name of the acatual session
     * @param config      list of key-value pairs of configuration data
     */
    public void saveToProperties(final String dir, String sessionName, Map<String, String> config) throws IOException {
        String fileName = sessionName + ".properties";
        Properties prop = new Properties();
        File propertyDir = new File(dir);
        if (!propertyDir.exists()) {
            propertyDir.mkdir();
            File file = new File(propertyDir.getAbsolutePath() + "/" + fileName);
            createPropertyFile(sessionName, config, prop, file);


        }else{
            File file = new File(propertyDir.getAbsolutePath() + "/" + fileName);
            createPropertyFile(sessionName, config, prop, file);
        }

    }

    private void createPropertyFile(String sessionName, Map<String, String> config, Properties prop, File file) {
        try {
            file.createNewFile();
            try (OutputStream out = new FileOutputStream((file))) {
                prop.setProperty("name", sessionName);
                if (config != null) {
                    for (String s : config.keySet()) {
                        prop.setProperty(s, config.get(s));
                    }
                }
                prop.store(out, null);

            } catch (IOException io) {
                io.printStackTrace();
            }
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
        Properties prop = loadLoginConfig(CONFIG_PATH + "/" + actualSessionName + ".properties");
        System.out.println(prop);
        setupConfigFromInputFields(prop);
        return actualSessionName;
    }


    public void openSessionDialog(ActionEvent actionEvent) throws IOException {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(loginWindow.getScene().getWindow());
        TextField tfSession = null;
        try {
            DialogPane dialogPane = new DialogPane();
            Label lbSession = new Label("Név: ");
            tfSession = new TextField();
            tfSession.autosize();
            dialogPane.contentProperty().setValue(tfSession);
            dialogPane.setHeader(lbSession);
            dialog.getDialogPane().setContent(dialogPane);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        Optional<ButtonType> options = dialog.showAndWait();
        if (options.isPresent() && options.get() == ButtonType.OK) {
            Map<String, String> config = new HashMap<>();
            config.put("name", tfSession.getText());
            config.put("host", tfHost.getText());
            config.put("port", tfPort.getText());
            config.put("user", tfUser.getText());
            config.put("password", tfPassword.getText());
            saveToProperties(CONFIG_PATH, tfSession.getText(), config);
            listSession.getItems().add(tfSession.getText());
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

    public void deleteFile(String name) {
        File confDir = new File(CONFIG_PATH);
        for (File file : confDir.listFiles()) {
            if (file.getName().startsWith(name)) {
                file.delete();

            }
        }
    }

    public org.apache.commons.net.ftp.FTPClient getClient() {
        return client;
    }

    public void setClient(FTPClient client) {
        this.client = client;
    }
}
