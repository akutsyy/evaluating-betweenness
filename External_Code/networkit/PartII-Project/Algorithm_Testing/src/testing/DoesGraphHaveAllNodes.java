package testing;

import com.sun.jdi.InvalidTypeException;
import framework.main.FileTypeException;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import utility.Printing;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import static framework.main.Harness.*;

public class DoesGraphHaveAllNodes {
    public static void main(String[] args)
            throws IOException, FileTypeException, InvalidTypeException, InterruptedException {
        HashMap<String, String> options = parseOpts(args);

        Printing.set_debug_level(5);
        File f = getFile(options);
        String separator = "\\s+";
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
        while (it.hasNext()){
            int i = it.next();
            max = Math.max(max,i);
        }
        for(int i=0;i<=max;i++){
            if(!nodes.contains(i)){
                System.out.println("Missing "+i);
            }
        }

    }
}
