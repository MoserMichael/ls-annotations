package lsann;

import lsann.graph.HierarchyGraph;
import lsann.graph.HierarchyGraphVisitors;
import lsann.asmtools.AsmAccessNames;
import org.objectweb.asm.ClassVisitor;

public class ClassHierarchyAsmClassVisitor extends ClassVisitor  {

    private HierarchyGraph<ClassEntryData, Object> hierarchy = new HierarchyGraph<ClassEntryData, Object>();

    public static class ClassEntryData {
        private boolean isInterface;
        private int access;

        ClassEntryData(int access) {
            this.isInterface = isInterface;
            this.access = access;
        }
    }

    public ClassHierarchyAsmClassVisitor(int api, final ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {

        super.visit(version, access, name, signature, superName, interfaces);

        String className = name.replace('/','.');
        hierarchy.addNode(className, new ClassEntryData(access));

        if (superName != null && superName != "java/lang/Object") {
            String superClassName = superName.replace('/','.');
            hierarchy.addTwoWayLink(superClassName, className, null);
        }

        for(String in : interfaces) {
            String interfaceName = in.replace('/','.');
            hierarchy.addTwoWayLink(interfaceName, className, null);
        }
    }



    public void showBasesOf(String className) {
        this.hierarchy.walkBases(new HierarchyGraphVisitors.ShowHierarchyVisitor(), className);
    }

    public void showDerivationsOf(String className) {
        this.hierarchy.walkDerived(new HierarchyGraphVisitors.ShowHierarchyVisitor(), className);
    }

    public HierarchyGraph getTypeHierarchyGraph() {
        return hierarchy;
    }



}
