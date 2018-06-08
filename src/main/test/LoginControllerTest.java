import controller.LoginController;
import javafx.scene.Node;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

public class LoginControllerTest {
    private LoginController testObj;
    private Map<String,String> testconfig;
    private ByteArrayOutputStream out = new ByteArrayOutputStream();
    private ByteArrayOutputStream outErr = new ByteArrayOutputStream();
    @Before
    public void setup(){
        testObj = new LoginController();
        testconfig = new HashMap<>();
        testconfig.put("host","kali");
        testconfig.put("port","21");
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(outErr));

    }
    @Test
    public void testSavetoProperties(){
        testObj.saveToProperties("/home/kareeydev/IdeaProjects/ModosFTP/src/main/resources/properties","test1",testconfig);
    }

    @Test
    public void test_setupConfigFromInputFields(){
      Properties prop = testObj.loadLoginConfig("/home/kareeydev/IdeaProjects/ModosFTP/src/main/resources/properties/test1.properties");
      testObj.setupConfigFromInputFields(prop);
      assertEquals(prop.toString(),out.toString());

    }



}