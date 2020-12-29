package lsann;
import java.io.IOException;
import java.util.*;
import java.io.File;

public class App {
    private boolean scanNestedJars = false;
    private LinkedList<String> inSpec = new LinkedList<String>();

    public static void main(String[] args) {
        new App(args);
    }

    private App(String [] args) {
        if (args.length == 0 ) {
            printHelp();
        }

        parseCommand(args);

        try {
            for(String arg : inSpec) {
                JarUtil jutil = new JarUtil(scanNestedJars);
                jutil.process(args[0], new VisitClassInJarImp());
            }
        } catch(IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private void parseCommand(String [] args) {
        for(String arg : args) {
            if (arg == "-h" || arg == "--help") {
                printHelp();
            }
            if (arg == "-r" || arg == "--scanRec") {
                scanNestedJars = true;
            } else {
                File f = new File(arg);
                if (!f.exists()) {
                    System.err.printf("Error: %s does not exist. should be file or directory");
                    System.exit(1);
                }
                if (!f.isFile() && !f.isDirectory()) {
                    System.err.printf("Error: %s should be file or directory");
                    System.exit(1 );
                }
                inSpec.add(arg);
            }
        }
    }

    private void printHelp() {
        System.err.println("java -jar ls-annotations [--scanRec|-r] [<directory>|<jar file>]*\n" +
                "\n" +
                "Display all definitions with annotationsx§\n" +
                "it works by scans by decompiling the bytecode of class files selectively to shows all definitions with annotations.\n" +
                "helps to decipher systems with interdependent annotations (like spring/grpc, etc)\n" +
                "\n" +
                "Arguments:\n" +
                "   --scanRec -r     scans jars contained in jars (default off)\n" +
                "   <directory>     scans directory recursively for jarsa nd object files to scan\n" +
                "   <jar file>      scans jar file\n");
        System.exit(1);
    }


}
