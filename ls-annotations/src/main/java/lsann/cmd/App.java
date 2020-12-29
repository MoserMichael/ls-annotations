package lsann.cmd;
import java.io.IOException;
import java.util.*;
import java.io.File;

import lsann.AllJarClassVisitors;
import lsann.fileio.*;

public class App {
    private boolean scanNestedJars = false;
    private LinkedList<String> inSpec = new LinkedList<String>();
    private int command = CMD_NONE;
    private String queryClassName;

    private static final int CMD_NONE = -1;
    private static final int CMD_SHOW_NAMES = 1;
    private static final int CMD_ANNOTATIONS_LS = 2;
    private static final int CMD_CLASSES_FIND_BASES = 3;
    private static final int CMD_CLASSES_FIND_DERIVED = 4;
    private static final int CMD_ANNOTATIONS_FIND_BASES = 5;
    private static final int CMD_ANNOTATIONS_FIND_DERIVED = 6;
    private static final int CMD_ANNOTATIONS_USAGE = 7;
    private static final int CMD_ANNOTATIONS_USAGE_RECURSIVE = 8;

    //private static final int CMD_SPRING_BOOT_ANALYSER = 3;

    public static void main(String[] args) {
        new App(args);
    }

    private App(String [] args) {
        if (args.length == 0) {
            printHelp();
        }

        parseCommand(args);
        runCommand();
    }

    private void runCommand() {
        try {
            JarReader jutil = new JarReader(scanNestedJars);
            if (command == CMD_ANNOTATIONS_LS) {
                for(String arg : inSpec) {
                    jutil.process(arg, new AllJarClassVisitors.LsAnnotationJarClassVisitor());
                }
            }
            if (command == CMD_CLASSES_FIND_BASES || command == CMD_CLASSES_FIND_DERIVED) {
                AllJarClassVisitors.ClassHierarchyJarClassVisitor visit = new AllJarClassVisitors.ClassHierarchyJarClassVisitor();
                for (String arg : inSpec) {
                    jutil.process(arg, visit);
                }
                if (command == CMD_CLASSES_FIND_BASES) {
                    visit.get().showBasesOf(queryClassName);
                }
                if (command == CMD_CLASSES_FIND_DERIVED) {
                    visit.get().showDerivationsOf(queryClassName);
                }
            }

            if (command >=CMD_ANNOTATIONS_FIND_BASES  && command <= CMD_ANNOTATIONS_USAGE_RECURSIVE) {

                AllJarClassVisitors.AnnoDeclGraphJarClassVisitor visit = new AllJarClassVisitors.AnnoDeclGraphJarClassVisitor();
                for (String arg : inSpec) {
                    jutil.process(arg, visit);
                }
                if (command == CMD_CLASSES_FIND_BASES) {
                    visit.showAnnotationBasesOf(queryClassName);
                }

                if (command == CMD_CLASSES_FIND_DERIVED) {
                    visit.showAnnotationExtensionOf(queryClassName);
                }

                if (command == CMD_ANNOTATIONS_FIND_DERIVED) {
                    visit.showAnnotationExtensionOf(queryClassName);
                }

                if (command == CMD_ANNOTATIONS_FIND_BASES) {
                    visit.showAnnotationBasesOf(queryClassName);
                }
                if (command == CMD_ANNOTATIONS_USAGE) {
                    visit.showAnnotationUsage(queryClassName, true);
                }
                if (command == CMD_ANNOTATIONS_USAGE_RECURSIVE) {
                    visit.showAnnotationUsageRecursive(queryClassName);
                }
            }
            if (command == CMD_SHOW_NAMES) {
                for(String arg : inSpec) {
                    jutil.process(arg, new lsann.fileio.JarClassVisitor() {
                        @Override
                        public void visit(String displayPath, java.io.InputStream data) throws java.io.IOException {
                            System.out.println(displayPath);
                        }
                    });
                }
            }
            /*
            if (command == CMD_SPRING_BOOT_ANALYSER) {
                VisitClasses.ShowVisitClasses visit = new VisitClasses.ShowVisitClasses();
                VisitClasses.TypeInfoVisitor visitTinfo = new VisitClasses.TypeInfoVisitor();
                visitTinfo.setNextVisitor(visit);

                for(String arg : inSpec) {
                    jutil.process(arg, visit);
                }
                SpringBootAutowireAnalyser analyser = new SpringBootAutowireAnalyser(visit.get().getTypeHierarchyGraph(), visitTinfo.get().getMapTypeNameToTypeInfo() );
                analyser.scan();
            }
             */
        } catch(IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private void parseCommand(String [] args) {
        for(int i = 0; i < args.length; ++i) {
            String arg = args[i];
            if (arg.equals("-h") || arg.equals("--help")) {
                printHelp();
            }

            /* common properties */
            if (arg.equals("-v") || arg.equals("--verbose")) {
                System.setProperty("lsann.visitclass.verbose", "1");
            }
            else
            if (arg.equals("-r") || arg.equals("--scanRec")) {
                scanNestedJars = true;
            }
            else
            /* specific commands */
            if (arg.equals("-l") || arg.equals("--list")) {
                command = CMD_ANNOTATIONS_LS;
            }
            else if (arg.equals("-n") || arg.equals("--names")) {
                command = CMD_SHOW_NAMES;
            }
            else if (arg.equals("-b") || arg.equals("--baseOf")) {
                command = CMD_CLASSES_FIND_BASES;
                if (i+1 == args.length) {
                    printHelp();
                }
                queryClassName = args[i+1];
                ++i;
            }
            else if (arg.equals("-d") || arg.equals("--derivedOf")) {
                command = CMD_CLASSES_FIND_DERIVED;
                if (i + 1 == args.length) {
                    printHelp();
                }
                queryClassName = args[i + 1];
                ++i;
            }
            else if (arg.equals("-a") || arg.equals("--derivedAnnoOf")) {
                command = CMD_ANNOTATIONS_FIND_DERIVED;
                if (i + 1 == args.length) {
                    printHelp();
                }
                queryClassName = args[i + 1];
                ++i;
            }
            else if (arg.equals("-e") || arg.equals("--baseAnnoOf")) {
                command = CMD_ANNOTATIONS_FIND_BASES;
                if (i + 1 == args.length) {
                    printHelp();
                }
                queryClassName = args[i + 1];
                ++i;
            } else if (arg.equals("-u") || arg.equals("--annoUsage")) {
                command = CMD_ANNOTATIONS_USAGE;
                if (i+1 == args.length) {
                    printHelp();
                }
                queryClassName = args[i+1];
                ++i;
            } else if (arg.equals("-w") || arg.equals("--annoRec")) {
                command = CMD_ANNOTATIONS_USAGE_RECURSIVE;
                if (i+1 == args.length) {
                    printHelp();
                }
                queryClassName = args[i+1];
                ++i;
            } else {
                File f = new File(arg);
                if (!f.exists()) {
                    System.err.printf("Error: %s does not exist. should be file or directory\n", arg);
                    System.exit(1);
                }
                if (!f.isFile() && !f.isDirectory()) {
                    System.err.printf("Error: %s should be file or directory\n", arg);
                    System.exit(1 );
                }
                inSpec.add(arg);
            }
        }
        if (command == CMD_NONE) {
            System.out.printf("Error: no command given\n");
            printHelp();
        }
    }

    private void printHelp() {
        System.err.println("java -jar ls-annotations [[-l|--list] className|[-b|--baseOf] className]|[[-d|--derivedOf]] [--scanRec|-r] [<directory>|<jar file>]*\n" +
                "\n" +
                "Display all definitions with annotations\n" +
                "it works by scans by decompiling the bytecode of class files selectively to shows all definitions with annotations.\n" +
                "helps to decipher systems with interdependent annotations (like spring/grpc, etc)\n" +
                "\n" +
                "Common Arguments:\n" +
                " <directory>       scans directory recursively for jarsa nd object files to scan\n" +
                " <jar file>        scans jar file\n" +
                " --scanRec -r      scans jars contained in jars (default off) (optional)\n" +
                " -v                (debug) very verbose, trace objectweb events\n" +
                "\n" +
                "Commands:\n" +
                " --l               List all annotated classes or methods and show the annotations\n" +
                " --list" +
                "\n" +
                 " -d <n>            List all derived classes or interfaces of class/interface <n>\n" +
                 " --derivedClassOf <n>\n" +
                "\n" +
                " -b <n>            List all base classes or interfaces of class/interface <n>\n" +
                " --baseClassOf <n>\n" +
                "\n" +
                " -a <n>            List all derived annotations of annotation <n>\n" +
                " --derivedAnnoOf <n>\n" +
                " -e <n>            List all base annotations of annotation <n>\n" +
                " --baseAnnoOf <n>\n" +
                "\n" +
                " -u <n>            Show all uses of annotation <n>\n" +
                " --annoUsage <n>\n" +
                "\n" +
                " -w <n>            Show really all uses of annotation <n>\n" +
                " --annoRec <n>     Including annotations that extend the given one and their usage\n" +
                "\n"

        );
        System.exit(1);
    }
}
