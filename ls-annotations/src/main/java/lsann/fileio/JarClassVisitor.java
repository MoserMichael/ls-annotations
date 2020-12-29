package lsann.fileio;

public abstract class JarClassVisitor {
    JarClassVisitor visitor;

    public void setNextVisitor(JarClassVisitor next) {
        this.visitor = next;
    }
    public void visit(String displayPath, java.io.InputStream data ) throws java.io.IOException {
        if (this.visitor != null) {
            this.visitor.visit(displayPath, data);
        }
    }


}
