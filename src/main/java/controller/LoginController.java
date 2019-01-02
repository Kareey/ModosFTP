package controller;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class LoginController {
    public final static FXMLLoader APPLOADER = new FXMLLoader();
    public static final String PROPERTIES = "properties/";
    static boolean logged_in = false;
    private static String CONFIG_PATH;
    @FXML
    public FileViewController fileViewController;
    @FXML
    public CheckBox anonymousCB;
    public ResourceBundle dataFromLogin;
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
    @FXML
    private Button btnLogin;
    private Stage primaryStage;
    private Scene loginScene;

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
        if (getOS().startsWith("Linux")) {
            CONFIG_PATH = System.getenv("HOME") + "/properties";
        } else if (getOS().startsWith("Windows")) {
            CONFIG_PATH = "C:/properties";
        }
        loginScene = loginWindow.getScene();
        setClient(new FTPClient());
        btnLogin.setOnAction(event -> {
            try {
                System.out.println("In setOnAction! ");
                login();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
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
                    deleteFile(cell.getItem()+".properties");
                    listSession.getItems().remove(cell.getItem());
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
        comboProtocol.getSelectionModel().selectFirst();

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
        Task loginTask = new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    try {
                        if (tfPort.getText().equals("")) {
                            client.connect(tfHost.getText());
                        } else {
                            client.connect(tfHost.getText().trim(), Integer.valueOf(tfPort.getText()));
                        }

                    } catch (IOException io) {
                        System.out.println(client.getReplyString());
                        showError("Connection error to " + tfHost.getText());
                    } catch (NumberFormatException nu) {
                        showError("Wrong port number, please enter a valid port number");
                    }
                    System.out.println(client.getReplyString());
                    if (client.getReplyCode() > 200 && client.getReplyCode() < 300) {
                        client.enterLocalPassiveMode();
                        logged_in = client.login(tfUser.getText().trim(), tfPassword.getText().trim());
                        if (logged_in) {
                            setupNewScene(new Object[]{client});
                            return true;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
        };
        loginTask.run();


        return logged_in;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setupNewScene(Object[] additionalData) throws IOException {
        File dataFile = new File(PROPERTIES + "datafile.properties");
        Map<String, Object> dataConfig = new HashMap<>();
        dataConfig.put("additionalData", additionalData);
        saveToProperties(dataFile.toPath().toString(), dataConfig);
        Parent fileWindow = APPLOADER.load(getClass().getClassLoader().getResource("fxml/fileView.fxml"));
        fileWindow.setUserData(additionalData);
        Stage currentStage = (Stage) loginWindow.getScene().getWindow();
        Scene fileView = new Scene(fileWindow);
        currentStage.setScene(fileView);
        currentStage.show();
    }

    /**
     * @param filePath the user defined name of the actual session
     * @param config   list of key-value pairs of configuration data
     */
    public void saveToProperties(String filePath, Map<String, Object> config) throws IOException {
        String fileName = filePath;
        Properties prop = new Properties();
        File file = new File(fileName);
        createPropertyFile(null, config, prop, file);

    }

    private void createPropertyFile(String sessionName, Map<String, Object> config, Properties prop, File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (OutputStream out = new FileOutputStream((file))) {
            if (sessionName != null) {
                prop.setProperty("name", sessionName);
            }
            if (config != null) {
                for (String s : config.keySet()) {
                    prop.setProperty(s, (String) config.get(s));
                }
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
            Map<String, Object> config = new HashMap<>();
            config.put("name", tfSession.getText());
            config.put("host", tfHost.getText());
            config.put("port", tfPort.getText());
            config.put("user", tfUser.getText());
            config.put("password", tfPassword.getText());
            saveToProperties(PROPERTIES + tfSession.getText(), config);
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
            if (file.getName().contains(name)) {
                try {
                    Files.delete(Paths.get(name));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public org.apache.commons.net.ftp.FTPClient getClient() {
        return client;
    }

    public void setClient(FTPClient client) {
        this.client = client;
    }

    public void isAnonymousLogin(ActionEvent actionEvent) {
        if (anonymousCB.isSelected()) {
            tfUser.setText("anonymous");
        } else {
            tfUser.setText("");
        }
    }
}
