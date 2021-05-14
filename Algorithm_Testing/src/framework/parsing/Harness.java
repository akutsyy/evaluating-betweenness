package framework.parsing;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;


public class Harness {

  public static double defaultEpsilon = 0.0001;
  public static double defaultPercent = 0.1;

  public static File getFile(HashMap<String, String> options) throws IOException {
    if (!options.containsKey("file")) {
      throw new IOException("USAGE: Harness --file <path>");
    }
    return new File(options.get("file"));
  }

  public static FileType getFileType(HashMap<String, String> options)
      throws IOException, FileTypeException {

    if (!options.containsKey("file")) {
      throw new IOException("USAGE: Harness --file <path>");
    }
    String fileName = options.get("file");

    if (options.containsKey("file_type")) {
      try {
        return new FileType(options.get("file_type"));
      } catch (NullPointerException | FileTypeException e) {
        System.out.println("Using inputted filetype \"" + options.get("file_type")
            + "\" failed, attempting to infer format");
        FileType t = inferType(fileName);
        System.out.println("Inferred type " + t);
        return t;
      }
    } else {
      System.out.println("No file format provided with --file_type <type>, attempting to infer");
      FileType t = inferType(fileName);
      System.out.println("Inferred type " + t);
      return t;
    }
  }

  public static FileType inferType(String fileName) throws FileTypeException {
    String[] split = fileName.split("\\.");
    String extension = split[split.length - 1];

    switch (extension) {
      case "mtx":
      case "edges":
        return new FileType("Directed_ID_ID_List");
      case "csv":
        return new FileType("Unweighted_CSV");
      default:
        throw new FileReadException("Cannot determine file type of input");
    }
  }

  public static HashMap<String, String> parseOpts(String args) {
    return parseOpts(args.split(" "));
  }

  public static HashMap<String, String> parseOpts(String[] args) {
    HashMap<String, String> optMap = new HashMap<>();
    for (int i = 0; i < args.length; i++) {
      if (args[i].charAt(0) == '-') {
        if (args[i].length() < 2) // Blank
        {
          throw new IllegalArgumentException("Not a valid argument: " + args[i]);
        }

        if (args[i].charAt(1) == '-') {
          if (args[i].length() < 3) // Blank
          {
            throw new IllegalArgumentException("Not a valid argument: " + args[i]);
          }
          // --opt
          if (args.length - 1 == i) {
            throw new IllegalArgumentException("Expected arg after: " + args[i]);
          }
          optMap.put(args[i].substring(2).toLowerCase(), args[i + 1]);
        } else {
          if (args.length - 1 == i) {
            throw new IllegalArgumentException("Expected arg after: " + args[i]);
          }
          // -opt
          optMap.put(args[i].substring(1).toLowerCase().strip(), args[i + 1].strip());
        }
        i++;
      }
    }
    return optMap;
  }
}
