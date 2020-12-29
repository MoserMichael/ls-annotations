package lsann.asmtools;

import org.objectweb.asm.*;

public class TracingVisitors {
    private static int apiVersion;

    public static boolean isTraceOn() {
        return System.getProperty("lsann.visitclass.verbose") != null;
    }
    public static ClassVisitorImp makeClassVisitor(int api) {
        if (isTraceOn()) {
            apiVersion = api;
            return new ClassVisitorImp(api);
        }
        return null;
    }
    public static AnnotationVisitorImp makeAnnotationVisitorImp() {
        if (isTraceOn()) {
            return new AnnotationVisitorImp(apiVersion);
        }
        return null;
    }
    public static FieldVisitorImp makeFieldVisitor() {
        if (isTraceOn()) {
            return new FieldVisitorImp(apiVersion);
        }
        return null;
    }
    public static MethodVisitorImp makeMethodVisitor() {
        if (isTraceOn()) {
            return new MethodVisitorImp(apiVersion);
        }
        return null;
    }

    public static class ClassVisitorImp extends ClassVisitor {

        public ClassVisitorImp(int api) {
            super(api);
        }

        @Override
        public void visit(int version, int access, String name, String signature,
                          String superName, String[] interfaces) {

                System.out.printf(">visit :: version: %d access: 0x%x name: %s signature: %s super: %s interfaces: %s\n",
                        version, access, name, signature, superName, TracingVisitors.join(interfaces));
        }
        @Override
        public void visitOuterClass(String owner, String name, String desc) {
                System.out.printf(">visitOuterClass :: owner: %s name: %s desc: %s\n",
                        owner, name, desc);
        }

        @Override
        public org.objectweb.asm.AnnotationVisitor visitTypeAnnotation(int typeRef,
                                                                       TypePath typePath, String desc, boolean visible) {
                System.out.printf(">visitTypeAnnotation :: typeRef: %d typePath: %s desc: %s visible: %d\n",
                        typeRef, makeString(typePath), desc, visible);
            return null;
        }

        @Override
        public org.objectweb.asm.AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                System.out.printf(">visitAnnotation :: desc: %s visible: %b\n",
                        desc, visible);
            return null;
        }

        @Override
        public void visitInnerClass(String name, String outerName,
                                    String innerName, int access) {
                System.out.printf(">visitInnerClass :: name: %s outerName: %s innerName: %s access: 0x%x\n",
                        name, outerName, innerName, access);
        }
        @Override
        public FieldVisitor visitField(int access, String name, String desc,
                                       String signature, Object value) {
                System.out.printf(">visitField :: access: 0x%x name: %s desc: %s signature: %s value: %s valueType: %s\n",
                        access, name, desc, signature, makeString(value), makeTypeString(value));
            return null;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                                         String signature, String[] exceptions) {

            System.out.printf(">visitMethod :: access: 0x%x name: %s desc: %s signature: %s exceptions: %s\n",
                        access, name, desc, signature, join(exceptions));
            return null;
        }


        @Override
        public void visitEnd () {
                System.out.printf(">visitEnd\n");
        }

    }

    public static class AnnotationVisitorImp extends org.objectweb.asm.AnnotationVisitor {
        private boolean traceApi;

        private int annotationNestingLevel;

        AnnotationVisitorImp(int api) {
            super(api);
            this.annotationNestingLevel = 0;
        }

        public void incAnntationNestinglevel() {
            this.annotationNestingLevel = 0;
        }


        @Override
        public void visit(String name, Object value) {
                System.out.printf(">AnnotationVisitor.visit(%d) :: name: %s valueType: %s value: %s\n",
                        annotationNestingLevel, name, getTypeName(value), value);
        }

        String getTypeName(Object obj) {
            if (obj == null) {
                return "";
            }
            return obj.getClass().toString();
        }

        @Override
        public void visitEnum(String name, String desc, String value) {
            System.out.printf(">AnnotationVisitor.visitEnum(%d) :: name: %s value: %s desc: %s\n",
                    annotationNestingLevel, name, value, desc);

        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String desc) {
            System.out.printf(">AnnotationVisitor.visitAnnotation(%d) :: name: %s desc: %s\n",
                    annotationNestingLevel, name, desc);
            return null;
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            System.out.printf(">AnnotationVisitor.visitArray(%d) :: name: %s\n",
                    annotationNestingLevel,name);
            return null;
        }

        @Override
        public void visitEnd() {
            System.out.printf(">AnnotationVisitor.visitEnd(%d) \n",annotationNestingLevel);
        }
    }

    public static class FieldVisitorImp extends org.objectweb.asm.FieldVisitor {
         FieldVisitorImp(int api) {
            super(api, null);
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            System.out.printf(">FieldVisitor.visitAnnotation :: desc: %s visible: %b\n",
                desc, visible);
            return null;
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(int typeRef,
                                                     TypePath typePath, String desc, boolean visible) {
            System.out.printf(">FieldVisitor.visitTypeAnnotation :: typeRef: %d typePath: %s desc: %s visible: %d\n",
                    typeRef, makeString(typePath), desc, visible);

            return null;
        }

        @Override
        public void visitAttribute(Attribute attr) {
            System.out.printf(">FieldVisitor.visitAttribute :: attr: %s\n",
                    makeString(attr));

        }

        @Override
        public void visitEnd() {
            System.out.printf(">FieldVisitor.visitEnd\n");
        }

    } // FieldVisitorImp

    public static class MethodVisitorImp extends org.objectweb.asm.MethodVisitor {
        MethodVisitorImp(int api) {
            super(api, null);
        }

        @Override
        public void visitParameter(String name, int access) {
            System.out.printf(">MethodVisitor.visitParameter :: name: %s access: 0x%x\n",
                    name, access);

        }

        @Override
        public AnnotationVisitor visitAnnotationDefault() {
            System.out.printf(">MethodVisitor.visitAnnotationDefault\n");
            return null;
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            System.out.printf(">MethodVisitor.visitAnnotation desc: %s visible: %b\n",
                    desc, visible);
            return null;
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(int typeRef,
                                                     TypePath typePath, String desc, boolean visible) {
            System.out.printf(">MethodVisitor.visitTypeAnnotation :: typeRef: %d typePath: %s desc: %s visible: %d\n",
                    typeRef, makeString(typePath), desc, visible);
            return null;
        }

        public AnnotationVisitor visitParameterAnnotation(int parameter,
                                                          String desc, boolean visible) {
            System.out.printf(">MethodVisitor.visitParameterAnnotation :: parameter: %d desc: %s visible: %b\n",
                    parameter, desc, visible);
            return null;
        }

        @Override
        public void visitAttribute(Attribute attr) {
            System.out.printf(">MethodVisitor.visitAttribute :: attrib: %s\n",
                    makeString(attr));

        }

        @Override
        public void visitLocalVariable(String name, String desc, String signature,
                                       Label start, Label end, int index) {

            System.out.printf(">visitLocalVariable :: name: %s desc: %s signature: %s \n",
                    name, desc, signature);
        }

        @Override
        public AnnotationVisitor visitLocalVariableAnnotation(int typeRef,
                                                              TypePath typePath, Label[] start, Label[] end, int[] index,
                                                              String desc, boolean visible) {
            System.out.printf(">visitLocalVariableAnnotation :: typeRef: %d typePath: %s desc: %s visible: %b\n",
                    typeRef, makeString(typePath), desc, visible);
            return null;
        }

        @Override
        public void visitEnd() {
            System.out.printf(">MethodVisitor.visitEnd\n");
        }
    } // MethodVisitorImp

    protected static String join(String [] interfaces) {
        if (interfaces == null) {
            return "";
        }
        return String.join(", ", interfaces);
    }

    protected static String makeString(Object obj) {
        if (obj == null) {
            return "";
        }
        return obj.toString();
    }

    protected static String makeTypeString(Object obj) {
        if (obj == null) {
            return "";
        }
        return obj.getClass().toString();
    }

}
