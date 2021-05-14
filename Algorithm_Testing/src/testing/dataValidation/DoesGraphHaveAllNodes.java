package testing.dataValidation;

import static framework.parsing.Harness.getFile;
import static framework.parsing.Harness.parseOpts;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import utility.Printing;

public class DoesGraphHaveAllNodes {

  public static void main(String[] args)
      throws IOException {
    Printing.set_debug_level(5);
    String separator = "\\s+";
    doesFileHaveAllNodes(args, separator);
  }

  public static boolean doesFileHaveAllNodes(String[] args, String separator) throws IOException {
    HashMap<String, String> options = parseOpts(args);

    boolean allNodes = true;
    File f = getFile(options);
    Scanner fileScanner = new Scanner(f);
    String line;
    TIntSet nodes = new TIntHashSet();
    while (fileScanner.hasNextLine()) {
      line = fileScanner.nextLine();
      line = line.strip();
      // Ignore empty or commented lines
      if (line.length() != 0 && line.charAt(0) != '#' && line.charAt(0) != '%') {
        int first = Integer.parseInt(line.split(separator)[0]);
        int second = Integer.parseInt(line.split(separator)[1]);
        nodes.add(first);
        nodes.add(second);
      }
    }

    TIntIterator it = nodes.iterator();
    int max = 0;
    while (it.hasNext()) {
      int i = it.next();
      max = Math.max(max, i);
    }
    for (int i = 0; i <= max; i++) {
      if (!nodes.contains(i)) {
        System.out.println("Missing " + i);
        allNodes = false;
      }
    }
    return allNodes;
  }

  public static boolean doesFileStartAtZero(String[] args, String separator) throws IOException {
    HashMap<String, String> options = parseOpts(args);

    File f = getFile(options);
    Scanner fileScanner = new Scanner(f);
    String line;
    while (fileScanner.hasNextLine()) {
      line = fileScanner.nextLine();
      line = line.strip();
      // Ignore empty or commented lines
      if (line.length() != 0 && line.charAt(0) != '#' && line.charAt(0) != '%') {
        int first = Integer.parseInt(line.split(separator)[0]);
        int second = Integer.parseInt(line.split(separator)[1]);
        if (first == 0 || second == 0) {
          return true;
        }
      }
    }
    return false;
  }

}
