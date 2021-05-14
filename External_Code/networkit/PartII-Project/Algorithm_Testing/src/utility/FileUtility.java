package utility;

import java.io.File;

public class FileUtility {
  public static boolean fileExists(String name){
    File f = new File(name);
    return f.exists();
  }
}
