import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class FileUtilities {
    public static void writeToFile(Map<String, Object> data, String path) throws IOException {
        File file = new File(path);
        if(!file.exists()){
            file.createNewFile();
        }
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))){
            for (Object s : data.values()) {
                //TODO
            }
        }catch (IOException io){
            io.printStackTrace();
        }
    }
}
