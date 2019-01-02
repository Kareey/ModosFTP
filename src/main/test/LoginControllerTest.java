import controller.LoginController;
import org.junit.*;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class LoginControllerTest {
    private LoginController testObj;
    private ByteArrayOutputStream out = new ByteArrayOutputStream();
    private ByteArrayOutputStream err = new ByteArrayOutputStream();
    @Before
    public void setup() throws IOException {
        testObj = new LoginController();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
    }

    @Test
    public void testOS() throws IOException {

            String expected = "Linux";
            String result = testObj.getOS();
            assertEquals(expected,result);
    }

    @Test
    public void testCurrentDir(){
      testObj.printCurrendDir();
      assertEquals("/home/kareeydev/IdeaProjects/ModosFTP\n",out.toString());
    }
    @Test
    public void testSaveToProperties() throws IOException {
        String path =LoginController.PROPERTIES+"test.properties";
        Map<String,Object> testConfig = new HashMap<>();
        testConfig.put("name","lol");
        testObj.saveToProperties(path,testConfig);

    }


}
