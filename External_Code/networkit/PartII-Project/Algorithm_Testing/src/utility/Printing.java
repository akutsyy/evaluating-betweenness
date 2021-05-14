package utility;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Printing {

  public static void printToFile(String s, String filename) {
    try {
      File myObj = new File(filename);
      if (myObj.createNewFile()) {
        System.out.println("File created: " + myObj.getName());
      } else {
        System.out.println("File " + filename + " already exists.");
      }
    } catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }

    try {
      FileWriter myWriter = new FileWriter(filename);
      myWriter.write(s);
      myWriter.close();
      System.out.println("Successfully wrote to the file " + filename);
    } catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
  }

  public static int debug_level = 2;

  public static void set_debug_level(int level) {
    debug_level = level;
  }

  public static void print_debug(int level, String s) {
    if (level <= debug_level) {
      System.out.println(s);
    }
  }

  public static void print_debug_nobreak(int level, String s) {
    if (level <= debug_level) {
      System.out.print(s);
    }
  }

  public static void printProgress(int num, int howOftenToPrint) {
    if (howOftenToPrint == 0) {
      return;
    }
    if (Printing.debug_level > 0 && num % howOftenToPrint == 0) {
      System.out.print("Progress: [");
      for (int i = 0; i < num / howOftenToPrint; i++) {
        System.out.print("=");
      }
      for (int i = num / howOftenToPrint; i < 100; i++) {
        System.out.print(" ");
      }
      System.out.print("] " + (num / howOftenToPrint) + "%\r");
    }
  }
}
