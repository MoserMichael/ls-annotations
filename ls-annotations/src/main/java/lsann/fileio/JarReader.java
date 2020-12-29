package lsann.fileio;

import java.io.*;
import java.util.zip.*;
import java.util.*;
import java.nio.file.Paths;
import java.nio.file.Files;

public class JarReader {
    static private class PathName {
        private LinkedList<String> names = new LinkedList<String>();
        private String displayPath = "";

        public void add(String name) {
            names.add(name);
            rebuild();
        }

        public void pop() {
            names.removeLast();
            rebuild();
        }

        private void rebuild() {
            displayPath = "";
            ListIterator<String> iter = names.listIterator();
            while(iter.hasNext()) {
                if (displayPath != "") {
                    displayPath += " - ";
                }
                displayPath += iter.next();
            }
        }

        public String getDisplayPath() {
            return displayPath;
        }
    }

    private PathName filePath = new PathName();
    boolean scanJarsRecursive;

    public JarReader(boolean scanJarsRecursive) {
        this.scanJarsRecursive = scanJarsRecursive;
    }

    public void process(ZipInputStream zip, JarClassVisitor visitor) throws java.io.IOException{
        for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
            if (!entry.isDirectory()) {
                if (entry.getName().endsWith(".class")) {
                    filePath.add(entry.getName());
                    visitor.visit( filePath.getDisplayPath(), zip);
                    filePath.pop();
                } else if (this.scanJarsRecursive && entry.getName().endsWith(".jar")) {
                    filePath.add(entry.getName());
                    ZipInputStream jarIn = new ZipInputStream(zip);
                    process(jarIn, visitor);
                    filePath.pop();
                }
            }
        }
    }

    private void processJar(String path, JarClassVisitor visitor) throws java.io.IOException{
        filePath.add(path);
        ZipInputStream zip = new ZipInputStream(new FileInputStream(path));
        process(zip, visitor);
        filePath.pop();
    }
    public void process(String fpath, JarClassVisitor visitor) throws java.io.IOException{
        File f = new File(fpath);

        if (!f.exists()) {
            System.err.println(fpath + " does not exist");
            System.err.println("current directory is: " + System.getProperty("user.dir"));
            return;
        }

        if (f.isFile()) {
            processJar(fpath, visitor);
        }
        else if (f.isDirectory()) {
            Files.walk(Paths.get(fpath)).forEach(path -> {
                try {
                    String name = path.toString();
                    if (name.endsWith(".jar")) {
                        processJar(name, visitor);
                    }
                    if (name.endsWith(".class")) {
                        FileInputStream in = new FileInputStream(name);
                        filePath.add(name);
                        visitor.visit( name, in);
                        filePath.pop();
                    }
                } catch(IOException ex) {
                    ex.printStackTrace();
                }
            });
        }
    }

    private String zipPathToClassName(String entryName) {
        // This ZipEntry represents a class. Now, what class does it represent?
        String className = entryName.replace('/', '.'); // including ".class"
        String strippedClassName = className.substring(0, className.length() - ".class".length());

        return strippedClassName;
    }
}
