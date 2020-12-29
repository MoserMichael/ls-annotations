package lsann;
import lsann.asmtools.TracingVisitors;
import lsann.fileio.JarClassVisitor;
import lsann.graph.HierarchyGraph;
import lsann.graph.HierarchyGraphVisitors;
import org.objectweb.asm.*;
import java.util.*;

public class AllJarClassVisitors {

    static public class ClassHierarchyJarClassVisitor extends JarClassVisitor {

        private ClassHierarchyAsmClassVisitor classVisitor = null;

        public ClassHierarchyJarClassVisitor() {
            int api = Opcodes.ASM7;
            this.classVisitor = new ClassHierarchyAsmClassVisitor(api, TracingVisitors.makeClassVisitor(api));
        }

        @Override
        public void visit(String displayPath, java.io.InputStream data) throws java.io.IOException {
            //this.classVisitor.init(displayPath);
            new ClassReader(data).accept(this.classVisitor, 0);
        }

        public ClassHierarchyAsmClassVisitor get() {
            return classVisitor;
        }
    }

    static public class LsAnnotationJarClassVisitor extends JarClassVisitor {

        private AstAsmClassVisitor classVisitor = null;
        private String displayPath;

        private class LsAstVisitorEvents extends AstVisitorEvents {
            public void visitClassWithAnnotation(AstDefinition.ClassRep cls) {
                System.out.println("File: " + displayPath + "\n");
                System.out.println(cls.show(0, null));
            }
            public void visitAnnotationDefinition(AstDefinition.ClassRep cls) {
                visitClassWithAnnotation(cls);
            }
        }

        public LsAnnotationJarClassVisitor() {
            int api = Opcodes.ASM7;
            //this.classVisitor = new LsAnnotationClassVisitor(api, TracingVisitors.makeClassVisitor(api));
            this.classVisitor = new AstAsmClassVisitor(api, TracingVisitors.makeClassVisitor(api), new LsAstVisitorEvents());

        }

        @Override
        public void visit(String displayPath, java.io.InputStream data) throws java.io.IOException {
            this.displayPath = displayPath;
            //this.classVisitor.init(displayPath);
            new ClassReader(data).accept(this.classVisitor, 0);
        }
    }

    static public class AnnoDeclGraphJarClassVisitor extends JarClassVisitor {

        private HierarchyGraph declarationGraph = new HierarchyGraph();
        private Map<String, AstDefinition.ClassRep> mapTypeToDefinition = new HashMap<String, AstDefinition.ClassRep>();
        private Map<String, List<AstDefinition.RepBase>> mapTypeToUsage = new HashMap<String, List<AstDefinition.RepBase>>();
        private AstAsmClassVisitor classVisitor = null;
        private String displayPath;

        private class AnnoDeclGraphAstVisitorEvents extends AstVisitorEvents {

            public void visitAnnotationDefinition(AstDefinition.ClassRep cls) {

                mapTypeToDefinition.put(cls.type, cls);
                declarationGraph.addNode(cls.type, cls);
                if (cls.annotations != null) {
                    for (AstDefinition.AnnotationRep anno : cls.annotations) {
                        declarationGraph.addTwoWayLink(cls.type, anno.type, null);
                    }
                }
            }

            public void visitClassWithAnnotation(AstDefinition.ClassRep cls) {
                addAnnotationUsage(cls);
            }

            public void visitFieldWithAnnotation(AstDefinition.FieldRep rep) {
                addAnnotationUsage(rep);
            }

            public void visitMethodParamWithAnnotation(AstDefinition.MethodParamRep rep) {
                addAnnotationUsage(rep);
            }

            private void addAnnotationUsage(AstDefinition.RepBase usage) {
                if (usage.annotations != null) {
                    for (AstDefinition.AnnotationRep anno : usage.annotations) {
                        addAnnotationUsage(anno, usage);
                    }
                }

            }

            private void addAnnotationUsage(AstDefinition.AnnotationRep annotation, AstDefinition.RepBase usage) {
                //System.out.printf("map %s -> %s:%s\n", annotation.type, usage.getClass().toString(), usage.type);

                List<AstDefinition.RepBase> usageList = mapTypeToUsage.get(annotation.type);
                if (usageList == null) {
                    usageList = new LinkedList<AstDefinition.RepBase>();
                    mapTypeToUsage.put(annotation.type, usageList);
                }
                usageList.add(usage);
            }
        }

        public  class ShowUsageRecursive implements HierarchyGraph.HierarchyGraphVisitor<AstDefinition.ClassRep> {
            public void visit(String entryName, AstDefinition.ClassRep entry, int nestingLevel) {
                //if (entry != null) {
                //    System.out.println(entry.show(0)); //nestingLevel)
                //}
            }
            public void nodeNotFound(String entryName, int nestingLevel) {
            }
        }

        public  class ShowAnnotationUsage implements HierarchyGraph.HierarchyGraphVisitor<AstDefinition.ClassRep> {
            public void visit(String entryName, AstDefinition.ClassRep entry, int nestingLevel) {
                if (entry != null) {
                    System.out.println(entry.show(0, null)); //nestingLevel)
                }
            }

            public void nodeNotFound(String entryName, int nestingLevel) {
                System.out.printf("definition of annotation: %s not found\n", entryName);
            }
        }


        public AnnoDeclGraphJarClassVisitor() {
            int api = Opcodes.ASM7;
            this.classVisitor = new AstAsmClassVisitor(api, TracingVisitors.makeClassVisitor(api), new AnnoDeclGraphAstVisitorEvents());
        }

        @Override
        public void visit(String displayPath, java.io.InputStream data) throws java.io.IOException {
            this.displayPath = displayPath;
            //this.classVisitor.init(displayPath);
            new ClassReader(data).accept(this.classVisitor, 0);
        }

        public void showAnnotationBasesOf(String annotationName) {
            annotationName = stripStrudel(annotationName);
            this.declarationGraph.walkDerived(new ShowAnnotationUsage(), annotationName);
        }

        public void showAnnotationExtensionOf(String annotationName) {
            annotationName = stripStrudel(annotationName);
            this.declarationGraph.walkBases(new ShowAnnotationUsage(), annotationName);
        }

        public void showAnnotationUsage(final String annotationTypeArg, boolean displayIfNotfound) {
            final String annotationType = stripStrudel(annotationTypeArg);
            List<AstDefinition.RepBase> usage = mapTypeToUsage.get(annotationType);
            if (usage == null) {
                if (displayIfNotfound) {
                    System.out.printf("Annotation @%s not in use\n", annotationType);
                }
                return;
            }
            usage.stream().forEach( rep -> {
                System.out.println(rep.getTopDecl().show(0, annotationType));
            });
        }

        public void showAnnotationUsageRecursive(String annotationTypeArg) {
            final String annotationType = stripStrudel(annotationTypeArg);
            this.declarationGraph.walkBases(new HierarchyGraph.HierarchyGraphVisitor<AstDefinition.ClassRep>(){
                public void visit(String entryName, AstDefinition.ClassRep entry, int nestingLevel) {
                    if (entry != null) {
                        System.out.println(entry.show(0, null)); //nestingLevel)
                        showAnnotationUsage(entryName, false);
                    }
                }

                public void nodeNotFound(String entryName, int nestingLevel) {
                    System.out.printf("definition of annotation: %s not found\n", entryName);
                }

            }, annotationType);
        }


        private String stripStrudel(String arg) {
            if (arg.startsWith("@")) {
                return arg.substring(1);
            }
            return arg;
        }
    }



}
