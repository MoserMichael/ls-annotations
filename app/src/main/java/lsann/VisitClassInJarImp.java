package lsann;
import org.objectweb.asm.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class VisitClassInJarImp implements VisitClassinJar {

    private TraceClassVisitor tracer = null;


    @Override
    public void visit(String displayPath, java.io.InputStream data ) throws java.io.IOException {

        if (tracer == null) {
            tracer = new TraceClassVisitor(Opcodes.ASM7);
        }

        //System.out.println(displayPath);
        tracer.init(displayPath);
        new ClassReader(data).accept(tracer,0);
    }

    static class TraceClassVisitor extends ClassVisitor {
        private boolean traceApi = false;
        private SigParse sigParse = new SigParse();
        private int annotationNestingLevel = 0;
        private String currentAnnotationDesc = "";
        private LinkedList<Entry> parsedEntries = new LinkedList<Entry>();
        private String currentClassName = "";

        public TraceClassVisitor(int api) {
            super(api);
        }

        void init(String displayPath) {
            this.annotationNestingLevel = 0;
            parsedEntries.clear();
            pushParsedEntries(new Entry( "File: " + displayPath + "\n"));
            currentAnnotationDesc = "";
        }

        @Override
        public void visit(int version, int access, String name, String signature,
                          String superName, String[] interfaces) {

            if (traceApi) {
                System.out.printf(">visit :: verion: %d access: %d name: %s signature: %s super: %s interfaces: %s\n",
                        version, access, name, signature, superName, join(interfaces));
            }

            this.currentClassName = name;

            String displayName = AsmAccessNames.get(access,AsmAccessNames.SCOPE_CLASS) + " class " + name.replace('/','.');
            if (superName != "java/lang/Object") {
                displayName += "\n  extends " + superName.replace( '/', '.' );
            }
            if (interfaces != null && interfaces.length != 0) {
                String impl = Arrays.stream(interfaces)
                        .map(s -> "\n  implements " + s.replace('/','.'))
                        .collect(Collectors.toList()).toString();
                displayName += impl.substring(1, impl.length() - 1);
            }
            displayName += " {";
            pushParsedEntries(new Entry(displayName));

        }

        @Override
        public void visitOuterClass(String owner, String name, String desc) {
            if (traceApi) {
                System.out.printf(">visitOuterClass :: owner: %s name: %s desc: %s\n",
                        owner, name, desc);
            }
        }

        @Override
        public org.objectweb.asm.AnnotationVisitor visitTypeAnnotation(int typeRef,
                                                                       TypePath typePath, String desc, boolean visible) {
            if (traceApi) {
                System.out.printf(">visitTypeAnnotation :: typeRef: %d typePath: %s desc: %s visible: %d\n",
                        typeRef, makeString(typePath), desc, visible);
            }

            return getAnnotationVisitor();
        }

        @Override
        public org.objectweb.asm.AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            if (traceApi) {
                System.out.printf(">visitAnnotation :: desc: %s visible: %b\n",
                        desc, visible);
            }
            if (desc.indexOf("Lscala/reflect/ScalaSignature;") != -1) {
                if (traceApi) {
                    System.out.printf("><scala signature annotation>\n");
                }
                return null;
            }

            startAnnotation(desc);
            return getAnnotationVisitor();
        }

        private void setAnnotationDesc(String desc) {
            currentAnnotationDesc += "@" + sigParse.parseTypeSig(desc) + "(";
        }

        private void startAnnotation(String desc) {
            if (annotationNestingLevel == 0) {
                currentAnnotationDesc = "";
            }
            setAnnotationDesc(desc);
        }


        //private AnnotationVisitorImp annotationVisitor = null;

        class AnnotationVisitorImp extends org.objectweb.asm.AnnotationVisitor {
            private int numCall;
            private boolean isArray;
            int parameterIndex;

            AnnotationVisitorImp(int api) {
                super(api, null);

                this.numCall = 0;
                this.isArray = false;
                this.parameterIndex = -1;
            }

            public void setParamIndex(int  parameter) {
                this.parameterIndex = parameter;
            }


            @Override
            public void visit(String name, Object value) {
                if (traceApi) {
                    System.out.printf(">AnnotationVisitor.visit(" + annotationNestingLevel + ") :: name: %s valueType: %s value: %s\n",
                            name, getTypeName(value), value);
                }

                String valueDesc;
                if (value.getClass() ==  org.objectweb.asm.Type.class) {
                    valueDesc = sigParse.parseTypeSig(value.toString()) + ".class";
                } else if (value.getClass() == java.lang.String.class) {
                    valueDesc = '"' + value.toString() + '"';
                } else {
                    valueDesc = value.toString();
                }

                addToDesc(valueDesc);
            }

            String getTypeName(Object obj) {
                if (obj == null) {
                    return "";
                }
                return obj.getClass().toString();
            }

            @Override
            public void visitEnum(String name, String desc, String value) {
                if (traceApi) {
                    System.out.printf(">AnnotationVisitor.visitEnum(" + annotationNestingLevel + ") :: name: %s value: %s\n",
                            name, value);
                }
                addToDesc(name + "=" + value);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String name, String desc) {
                if (traceApi) {
                    System.out.printf(">AnnotationVisitor.visitAnnotation(" + annotationNestingLevel + ") :: name: %s desc: %s\n",
                            name, desc);
                }
                setAnnotationDesc(desc);
                //currentAnnotationDesc += name + "=" + desc;
                return getAnnotationVisitor();
            }

            @Override
            public AnnotationVisitor visitArray(String name) {
                if (traceApi) {
                    System.out.printf(">AnnotationVisitor.visitArray(" + annotationNestingLevel + ") :: name: %s\n",
                            name);
                }

                addToDesc(name + "={");
                AnnotationVisitorImp ret = getAnnotationVisitor();
                ret.isArray = true;
                return ret;
             }

            @Override
            public void visitEnd() {
                if (traceApi) {
                    System.out.printf(">AnnotationVisitor.visitEnd(" + annotationNestingLevel + ") \n");
                }
                if (isArray) {
                    currentAnnotationDesc += "}";
                }

                --annotationNestingLevel;
                if (annotationNestingLevel == 0) {
                    currentAnnotationDesc += ")";
                    if (this.parameterIndex == -1) {
                        parsedEntries.peekLast().setAnnotation(currentAnnotationDesc);
                    } else {
                        FunctionEntry entry = (FunctionEntry) parsedEntries.peekLast();
                        entry.setAnnotation(this.parameterIndex, currentAnnotationDesc);
                    }
                } else {
                    if (!currentAnnotationDesc.endsWith(" ")) {
                        addToDesc(" ");
                    }
                }

                if (traceApi && annotationNestingLevel == 0) {
                    System.out.printf("\nAnnotation: %s\n\n", currentAnnotationDesc);
                }
            }

            private void addToDesc(String val) {
                currentAnnotationDesc += val + " ";
                /*
                if (numCall > 0) {
                    currentAnnotationDesc += ",";
                }
                ++numCall;
                 */

            }
        } // AnnotationVisitorImp

        AnnotationVisitorImp getAnnotationVisitor() {

            ++annotationNestingLevel;
            return new AnnotationVisitorImp(api);
        }

        @Override
        public void visitInnerClass(String name, String outerName,
                                    String innerName, int access) {
            if (traceApi) {
                System.out.printf(">visitInnerClass :: name: %s outerName: %s innerName: %s access: %d\n",
                        name, outerName, innerName, access);
            }
            /*
            Current objectweb seems to have a bug with inner classes -
            sometimes an inner class gets its own visit call (if it extends another class, or level two nesting);
            but otherwise it doesn't. in this case it is impossible to know when parsing of inner class ends !!!
            That's a bit of an ambiguity.

            if (this.currentClassName == outerName) {
                String nestedName;
                if (name.startsWith(outerName + '$')) {
                    nestedName = name.substring(outerName.length() + 1);
                } else {
                    nestedName = name;
                }
            }
             */
        }

        class FieldVisitorImp extends org.objectweb.asm.FieldVisitor {
            FieldVisitorImp(int api) {
                super(api, null);
            }

            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if (traceApi) {
                    System.out.printf(">FieldVisitor.visitAnnotation :: desc: %s visible: %b\n",
                            desc, visible);
                }
                return getAnnotationVisitor();
            }

            @Override
            public AnnotationVisitor visitTypeAnnotation(int typeRef,
                                                         TypePath typePath, String desc, boolean visible) {
                if (traceApi) {
                    System.out.printf(">FieldVisitor.visitTypeAnnotation :: typeRef: %d typePath: %s desc: %s visible: %d\n",
                            typeRef, makeString(typePath), desc, visible);
                }

                return getAnnotationVisitor();
            }

            @Override
            public void visitAttribute(Attribute attr) {
                if (traceApi) {
                    System.out.printf(">FieldVisitor.visitAttribute :: attr: %s\n",
                            makeString(attr));
                }
                setAnnotationDesc(attr.toString());

            }

            @Override
            public void visitEnd() {
                if (traceApi) {
                    System.out.printf(">FieldVisitor.visitEnd\n");
                }
                popParsedEntries();
            }

        } // FieldVisitorImp

        FieldVisitorImp fieldVisitor = null;

        @Override
        public FieldVisitor visitField(int access, String name, String desc,
                                       String signature, Object value) {
            if (traceApi) {
                System.out.printf(">visitField :: access: %d name: %s desc: %s signature: %s value: %s valueType: %s\n",
                        access, name, desc, signature, makeString(value), makeTypeString(value));
            }

            if (fieldVisitor == null) {
                fieldVisitor = new FieldVisitorImp(api);
            }

            String fieldDesc = AsmAccessNames.get(access,AsmAccessNames.SCOPE_FIELD) + " " + sigParse.parseTypeSig(desc)  + " " + name;
            if (value != null) {
                fieldDesc += " = ";
                if (value.getClass() == java.lang.String.class) {
                   fieldDesc += '"' + value.toString() + '"';
                } else {
                    fieldDesc += value.toString();
                }
            }
            fieldDesc += ";";
            pushParsedEntries(new Entry( fieldDesc ));

            if (fieldVisitor == null) {
                fieldVisitor = new FieldVisitorImp(api);
            }
            return fieldVisitor;
        }

        class MethodVisitorImp extends org.objectweb.asm.MethodVisitor {
            MethodVisitorImp(int api) {
                super(api, null);
            }

            @Override
            public void visitParameter(String name, int access) {
                if (traceApi) {
                    System.out.printf(">MethodVisitor.visitParameter :: name: %s access: %d\n",
                            name, access);
                }

            }

            @Override
            public AnnotationVisitor visitAnnotationDefault() {
                if (traceApi) {
                    System.out.printf(">MethodVisitor.visitAnnotationDefault\n");
                }
                return getAnnotationVisitor();
            }

            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if (traceApi) {
                    System.out.printf(">MethodVisitor.visitAnnotation desc: %s visible: %b\n",
                            desc, visible);
                }
                startAnnotation(desc);
                return getAnnotationVisitor();
            }

            @Override
            public AnnotationVisitor visitTypeAnnotation(int typeRef,
                                                         TypePath typePath, String desc, boolean visible) {
                if (traceApi) {
                    System.out.printf(">MethodVisitor.visitTypeAnnotation :: typeRef: %d typePath: %s desc: %s visible: %d\n",
                            typeRef, makeString(typePath), desc, visible);
                }
                return getAnnotationVisitor();
            }

            public AnnotationVisitor visitParameterAnnotation(int parameter,
                                                              String desc, boolean visible) {
                if (traceApi) {
                    System.out.printf(">MethodVisitor.visitParameterAnnotation :: parameter: %d desc: %s visible: %b\n",
                            parameter, desc, visible);
                }

                AnnotationVisitorImp ret = getAnnotationVisitor();
                ret.setParamIndex(parameter);
                currentAnnotationDesc = "";
                setAnnotationDesc(desc);
                return ret;
            }

            @Override
            public void visitAttribute(Attribute attr) {
                if (traceApi) {
                    System.out.printf(">MethodVisitor.visitAttribute :: attrib: %s\n",
                            makeString(attr));
                }

            }

            @Override
            public void visitLocalVariable(String name, String desc, String signature,
                                           Label start, Label end, int index) {

                if (traceApi) {
                    System.out.printf(">visitLocalVariable :: name: %s desc: %s signature: %s \n",
                            name, desc, signature);
                }
            }

            @Override
            public AnnotationVisitor visitLocalVariableAnnotation(int typeRef,
                                                                  TypePath typePath, Label[] start, Label[] end, int[] index,
                                                                  String desc, boolean visible) {
                if (traceApi) {
                    System.out.printf(">visitLocalVariableAnnotation :: typeRef: %d typePath: %s desc: %s visible: %b\n",
                            typeRef, makeString(typePath), desc, visible);
                }
                setAnnotationDesc(desc);

                return getAnnotationVisitor();
            }

            @Override
            public void visitEnd() {
                if (traceApi) {
                    System.out.printf(">MethodVisitor.visitEnd\n");
                }
                popParsedEntries();
            }
        } // MethodVisitorImp

        MethodVisitorImp methodVisitor = null;

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                                         String signature, String[] exceptions) {

            if (traceApi) {
                System.out.printf(">visitMethod :: access: %d name: %s desc: %s signature: %s exceptions: %s\n",
                        access, name, desc, signature, join(exceptions));
            }

            //String displayedName = AsmAccessNames.get(access,AsmAccessNames.SCOPE_METHOD) + " " + sigParse.parseFuncSig(desc, name) + "{";
            //pushParsedEntries( new Entry( displayedName ) );

            FunctionEntry entry = new FunctionEntry(name, access);
            sigParse.parseFuncSigToList(entry.params, desc);
            pushParsedEntries(entry);

            if (methodVisitor == null) {
                methodVisitor = new MethodVisitorImp(api);
            }

            return methodVisitor;
        }


        @Override
        public void visitEnd () {
            if (traceApi) {
                System.out.printf(">visitEnd\n");
            }
            popParsedEntries();
        }

        private String join(String [] interfaces) {
            if (interfaces == null) {
                return "";
            }
            return String.join(", ", interfaces);
        }

        private String makeString(Object obj) {
            if (obj == null) {
                return "";
            }
            return obj.toString();
        }

        private String makeTypeString(Object obj) {
            if (obj == null) {
                return "";
            }
            return obj.getClass().toString();
        }

        static class Entry {
            String description;
            protected enum State { Init, HasAnnotation, HasBeenDisplayed, }
            protected State entryState;

            Entry(String desc) {
                this.description = desc;
                this.entryState = State.Init;
            }

            void setAnnotation(String annotation) {
                this.description = annotation  + "\n" + this.description;
                this.entryState = State.HasAnnotation;
            }

            public String toString() {
                return this.description;
            }

            boolean needEofScope() {
                //return true;
                return this.entryState == State.HasBeenDisplayed && this.description != null && this.description.endsWith("{");
            }

            boolean needDisplay() {
                return this.entryState != State.HasBeenDisplayed;
            }

            void setDisplayed() {
                this.entryState = State.HasBeenDisplayed;
            }
            boolean hasAnnotation() {
                return this.entryState == State.HasAnnotation;
            }

        }

        static class FunctionEntry extends Entry {
            LinkedList<String> params = new LinkedList<String>();
            int access;
            String annotation;

            FunctionEntry(String desc, int access) {
                super(desc);
                this.access = access;
                this.annotation = "";
            }

            void setAnnotation(String annotation) {
                this.annotation = annotation;
                this.entryState = State.HasAnnotation;
            }

            void setAnnotation(int pos, String annotation) {
                this.params.set(pos+1, annotation + "\n  " + this.params.get(pos+1));
                this.entryState = State.HasAnnotation;
            }

            public void setDescription() {
                String retVal = this.params.removeFirst();
                this.description = this.annotation + " " +
                        AsmAccessNames.get(this.access,AsmAccessNames.SCOPE_METHOD) + " " +
                        retVal + " " +
                        this.description +
                        "(\n" + makeParamDescription() + ") {";
                this.params.addFirst(retVal);
            }

            private String makeParamDescription() {
                //return String.join(",\n", this.params);
                List<String> tmpList = this.params.stream().map(s -> "  " + s).collect(Collectors.toList());
                return String.join(",\n", tmpList);
            }

            public String toString() {
                this.setDescription();
                return this.description;
            }

        }

        private void pushParsedEntries(Entry entry) {
            parsedEntries.addLast(entry);

        }

        private void popParsedEntries() {

            assert(this.parsedEntries != null);
            Entry last = this.parsedEntries.peekLast();

            if (last.hasAnnotation()) {
                showStackEntries();
            }

            if (last != null && last.needEofScope()) {
                System.out.println(nestingPrefix( this.parsedEntries.size()-1 ) + "}");
            }
            this.parsedEntries.removeLast();
        }


        private void showStackEntries() {
            int nesting = 0;

            ListIterator<Entry> iter = parsedEntries.listIterator();
            while(iter.hasNext()) {
                Entry entry = iter.next();
                if (entry.needDisplay()) {
                    printLinesWithNesting(nesting, entry.toString());
                    entry.setDisplayed();
                }
                nesting += 1;
            }
        }

        private void printLinesWithNesting(int nesting, String descr) {
            String prefix = nestingPrefix(nesting);
            descr.lines().forEach( line -> System.out.println(prefix + line) );
        }

        private String nestingPrefix(int nesting) {
            return Collections.nCopies(nesting, "    ").stream().collect(Collectors.joining());
        }

    }

}

