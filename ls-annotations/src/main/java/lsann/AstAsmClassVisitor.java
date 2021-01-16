package lsann;

import lsann.asmtools.SigParse;
import lsann.asmtools.TracingVisitors;
import org.objectweb.asm.*;

import java.util.*;
import java.util.stream.Collectors;

public class AstAsmClassVisitor extends ClassVisitor {
    private AstVisitorEvents astVisitor;
    private boolean traceApi = false;


    private int currentParamIndex;
    public LinkedList<AstDefinition.AnnotationCompoundRep>  annotationStack = new LinkedList<AstDefinition.AnnotationCompoundRep>();
    private LinkedList<AstDefinition.RepBase>  astStack = new LinkedList<AstDefinition.RepBase>();

    public AstAsmClassVisitor(int api, final ClassVisitor tracerVisitor, AstVisitorEvents astVisitor) {
        super(api, tracerVisitor);
        this.astVisitor = astVisitor;
        if (tracerVisitor != null) {
            traceApi = true;
        }
    }

    public void init() {
        astStack.clear();
        annotationStack.clear();
        currentParamIndex = 0;
    }


    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {

        super.visit(version, access, name, signature, superName, interfaces);

        String classType = name.replace('/','.');
        //ClassRep rep = mapClassNameToRep.get(name);
        List<String> interfacesOfClass =  Arrays.stream(interfaces)
                    .map(s -> s.replace('/','.'))
                       .collect(Collectors.toList());

        String superType = null;
        if (superName != null && !superName.equals("java/lang/Object")) {
            superType = superName.replace('/', '.');
        }
        AstDefinition.ClassRep rep = new AstDefinition.ClassRep(access, classType, superType, interfacesOfClass);
        astStack.addLast(rep);
    }

    @Override
    public void visitOuterClass(String owner, String name, String desc) {
        super.visitOuterClass(owner, name, desc);
    }

    @Override
    public org.objectweb.asm.AnnotationVisitor visitTypeAnnotation(int typeRef,
                                                                   TypePath typePath, String desc, boolean visible) {
        super.visitTypeAnnotation(typeRef, typePath, desc, visible);
        return getAnnotationVisitor();
    }

    @Override
    public org.objectweb.asm.AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        super.visitAnnotation(desc, visible);

        if (desc.indexOf("Lscala/reflect/ScalaSignature;") != -1) {
            return null;
        }
        addAnno(desc, visible);
        return getAnnotationVisitor();
    }

    private void addAnno(String desc, boolean visible) {
        String typeDesc = SigParse.parseTypeSig(desc);
        AstDefinition.AnnotationRep rep = new AstDefinition.AnnotationRep(typeDesc, visible);
        this.annotationStack.addLast(rep);
        //System.out.println("annotationStack++");

    }

    @Override
    public void visitInnerClass(String name, String outerName,
                                String innerName, int access) {
        super.visitInnerClass(name, outerName, innerName, access);
    }

    class AnnotationVisitorImp extends org.objectweb.asm.AnnotationVisitor {
        private int numCall;
        private boolean isArray;
        int parameterIndex;

        AnnotationVisitorImp(int api, AnnotationVisitor anno) {
            super(api, anno);

            this.numCall = 0;
            this.isArray = false;
            this.parameterIndex = -1;
        }

        public void setParamIndex(int  parameter) {
            this.parameterIndex = parameter;
        }


        @Override
        public void visit(String name, Object value) {
            super.visit(name, value);

            AstDefinition.AnnotationValueRep rep = new AstDefinition.AnnotationValueRep(name, value.getClass(), value);
            annotationStack.getLast().add(rep);

        }

        @Override
        public void visitEnum(String name, String desc, String value) {
            super.visitEnum(name, desc, value);
            AstDefinition.AnnotationEnumValRep rep = new AstDefinition.AnnotationEnumValRep(name, SigParse.parseTypeSig(desc), value);
            annotationStack.getLast().add(rep);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String desc) {
            super.visitAnnotation(name, desc);

            AstDefinition.AnnotationRep rep = new AstDefinition.AnnotationNestedRep(name, SigParse.parseTypeSig(desc), true);
            annotationStack.getLast().add(rep);
            annotationStack.addLast(rep);
            //System.out.println("annotationStack++");

            return getAnnotationVisitor();
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            super.visitArray(name);

            AstDefinition.AnnotationArrayRep rep = new AstDefinition.AnnotationArrayRep(name);
            annotationStack.getLast().add(rep);
            annotationStack.addLast(rep);
            //System.out.println("annotationStack++");

            return getAnnotationVisitor();
        }

        @Override
        public void visitEnd() {
            super.visitEnd();
            //System.out.println("annotationStack--");

            AstDefinition.AnnotationCompoundRep currentAnnotation = annotationStack.removeLast();
            if (annotationStack.isEmpty()) {
                astStack.getLast().addAnnotation((AstDefinition.AnnotationRep) currentAnnotation);
                if (astStack.getLast().getClass() == AstDefinition.MethodParamRep.class) {
                    astStack.removeLast();
                }
            }
        }
    } // AnnotationVisitorImp

    TracingVisitors.AnnotationVisitorImp tracingAnnotationVisitor;

    private AstAsmClassVisitor.AnnotationVisitorImp getAnnotationVisitor() {

        if (traceApi) {
            if (tracingAnnotationVisitor == null) {
                tracingAnnotationVisitor = TracingVisitors.makeAnnotationVisitorImp();
            }
            tracingAnnotationVisitor.incAnntationNestinglevel();
        }

        return new AstAsmClassVisitor.AnnotationVisitorImp(api, tracingAnnotationVisitor);
    }



    AstAsmClassVisitor.FieldVisitorImp fieldVisitor = null;

    @Override
    public FieldVisitor visitField(int access, String name, String desc,
                                   String signature, Object value) {
        super.visitField(access, name, desc, signature, value);

        AstDefinition.FieldRep rep = new AstDefinition.FieldRep(access, name, SigParse.parseTypeSig(desc), value);
        astStack.addLast(rep);

        if (fieldVisitor == null) {
            fieldVisitor = new AstAsmClassVisitor.FieldVisitorImp(api);
        }
        return fieldVisitor;
    }

    class FieldVisitorImp extends org.objectweb.asm.FieldVisitor {
        FieldVisitorImp(int api) {
            super(api, TracingVisitors.makeFieldVisitor());
        }

        private AnnotationVisitor getAnnotationVisitor() {
            return new AnnotationVisitorImp(api, null);
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            super.visitAnnotation(desc,visible);
            addAnno(desc, visible);
            return getAnnotationVisitor();
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(int typeRef,
                                                     TypePath typePath, String desc, boolean visible) {
            super.visitTypeAnnotation(typeRef,typePath, desc, visible);
            return getAnnotationVisitor();
        }

        @Override
        public void visitAttribute(Attribute attr) {
            super.visitAttribute(attr);
        }

        @Override
        public void visitEnd() {
            super.visitEnd();
            AstDefinition.FieldRep field = (AstDefinition.FieldRep) onVisitEnd();
            if (field != null) {
                astVisitor.visitFieldWithAnnotation(field);
            }
        }

    } // FieldVisitorImp


    class MethodVisitorImp extends org.objectweb.asm.MethodVisitor {
        MethodVisitorImp(int api) {
            super(api, TracingVisitors.makeMethodVisitor());
        }

        @Override
        public void visitParameter(String name, int access) {
            super.visitParameter(name, access);

            AstDefinition.MethodRep method = (AstDefinition.MethodRep) astStack.getLast();
            AstDefinition.MethodParamRep param = method.paramRep.get(currentParamIndex);
            param.access = access;
            param.name = name;
            currentParamIndex+=1;
        }

        @Override
        public AnnotationVisitor visitAnnotationDefault() {
            super.visitAnnotationDefault();
            return null; //getAnnotationVisitor();
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            super.visitAnnotation(desc,visible);
            //startAnnotation(desc);
            addAnno(desc, visible);
            return getAnnotationVisitor();
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(int typeRef,
                                                     TypePath typePath, String desc, boolean visible) {
            super.visitTypeAnnotation(typeRef, typePath, desc, visible);
            return getAnnotationVisitor();
        }

        public AnnotationVisitor visitParameterAnnotation(int parameter,
                                                          String desc, boolean visible) {
            super.visitParameterAnnotation(parameter, desc, visible);

            AstDefinition.MethodRep method = (AstDefinition.MethodRep) astStack.getLast();
            AstDefinition.MethodParamRep methodRep = method.paramRep.get(parameter);

            astStack.addLast(methodRep);
            addAnno(desc,visible);

            //AnnotationCompoundRep rep = annotationStack.getLast();
            //AnnotationParamRep param = new AnnotationParamRep(sigParse.parseTypeSig(desc), visible);
            //rep.add(param);

            return getAnnotationVisitor();
        }

        @Override
        public void visitAttribute(Attribute attr) {
            super.visitAttribute(attr);
        }

        @Override
        public void visitLocalVariable(String name, String desc, String signature,
                                       Label start, Label end, int index) {

            super.visitLocalVariable(name, desc, signature, start, end, index);
        }

        @Override
        public AnnotationVisitor visitLocalVariableAnnotation(int typeRef,
                                                              TypePath typePath, Label[] start, Label[] end, int[] index,
                                                              String desc, boolean visible) {
            super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible);
            return getAnnotationVisitor();
        }

        @Override
        public void visitEnd() {
            super.visitEnd();
            AstDefinition.MethodRep method = (AstDefinition.MethodRep) onVisitEnd();
            if (method != null) {
                astVisitor.visitMethodWithAnnotation(method);
            }
        }
    } // MethodVisitorImp

    AstAsmClassVisitor.MethodVisitorImp methodVisitor = null;

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {

        super.visitMethod(access, name, desc, signature, exceptions);

        LinkedList<String> params = new LinkedList<String>();
        SigParse.parseFuncSigToList(params, desc);

        AstDefinition.MethodRep rep = new AstDefinition.MethodRep(access, name, params.removeFirst());
        for(String paramType : params) {
            rep.paramRep.add(new AstDefinition.MethodParamRep(paramType, rep));
        }

        astStack.addLast(rep);
        currentParamIndex = 0;

        if (methodVisitor == null) {
            methodVisitor = new MethodVisitorImp(api);
        }

        return methodVisitor;
    }


    @Override
    public void visitEnd () {
        super.visitEnd();

        AstDefinition.ClassRep classRep = (AstDefinition.ClassRep) astStack.removeLast();
        if (classRep.definesAnnotation()) {
            astVisitor.visitAnnotationDefinition(classRep);
        } else if (classRep.mustDisplay()){
            astVisitor.visitClassWithAnnotation(classRep);
        }
    }

    private AstDefinition.RepBase onVisitEnd() {
        AstDefinition.RepBase base = astStack.removeLast();
        AstDefinition.ClassRep classRep = ((AstDefinition.ClassRep)astStack.getFirst());
        if (base.mustDisplay() || classRep.definesAnnotation() ||
                (isCtor(base) && classRep.annotations != null && showCtorOnClassWithAnnotation())) {
            base.parentDecl = astStack.getFirst();
            classRep.add(base);
            return base;
        }
        return null;
    }

    private boolean isCtor(AstDefinition.RepBase base) {
        if (base.getClass() == AstDefinition.MethodRep.class) {
            AstDefinition.MethodRep method = (AstDefinition.MethodRep) base;
            return method.name.equals("<init>");
        }
        return false;
    }

    private boolean showCtorOnClassWithAnnotation() {
        return System.getProperty("lsann.showctor") != null;
    }



}


