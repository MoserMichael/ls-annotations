package lsann;

import lsann.asmtools.AsmAccessNames;
import lsann.asmtools.SigParse;
import lsann.util.*;
import org.objectweb.asm.Opcodes;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class AstDefinition {

    public static abstract class RepBase {
        RepBase parentDecl;
        int access;
        String type;
        List<AnnotationRep> annotations;

        RepBase(int access, String type) {
            this.access = access;
            this.type = type;
        }

        void addAnnotation(AnnotationRep rep) {
            if (annotations == null) {
                annotations = new LinkedList<AnnotationRep>();
            }
            annotations.add(rep);
        }

        boolean mustDisplay() {
            return annotations != null;
        }

        RepBase getTopDecl() {
            if (this.parentDecl == null) {
                return this;
            }
            assert(this.parentDecl != this);
            return this.parentDecl.getTopDecl();
        }

        public String show(int nesting, String highlight) {
            if (this.annotations != null) {
                String prefix = "\n" + StrUtil.sprefix(nesting);
                return this.annotations.stream().map(s -> {
                    String ret = s.show(nesting + 1);
                    if (highlight != null && highlight.equals(s.type)) {
                        return StrUtil.highlight(ret);
                    } else {
                        return ret;
                    }
                }).collect(Collectors.joining(prefix, prefix, "\n"));
            }
            return "";
        }

        protected boolean showDecl(String highlight) {
            if (highlight == null) {
                return true;
            }
            if (annotations == null) {
                return false;
            }
            return annotations.stream().anyMatch(e -> e.type.equals(highlight));
        }

    }

    public static class ClassRep extends RepBase {
        String superName;
        List<String> interfaces;
        LinkedList<MethodRep> methodList = new LinkedList<MethodRep>();
        LinkedList<FieldRep> fieldList = new LinkedList<FieldRep>();

        void add(RepBase entry) {
            if (entry.getClass() == MethodRep.class) {
                methodList.add((MethodRep) entry);
            }
            if (entry.getClass() == FieldRep.class) {
                fieldList.add((FieldRep) entry);
            }
        }

        public ClassRep(int access, String type, String superName, List<String> interfaces) {
            super(access, type);

            if (definesAnnotation()) {
                this.access = access & (~(Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT));
            }

            this.interfaces = interfaces;

            if (superName != "java/lang/Object") {
                this.superName = superName; //superName.replace('/','.');
            }
        }

        @Override
        public String show(int nesting, String highlight) {

            String prefix = StrUtil.sprefix(nesting);

            String displayName = super.show(nesting, highlight) +
                    AsmAccessNames.get(this.access, AsmAccessNames.SCOPE_CLASS) + " " +
                    type.replace('/', '.');
            if (superName != null) {
                displayName += "\n  extends " + superName;//.replace( '/', '.' );
            }
            if (interfaces != null && !interfaces.isEmpty()) {
                displayName += "\n  implements " +
                        interfaces.stream()
                                .map(s -> s.replace('/', '.'))
                                .collect(Collectors.joining(",", "", ""));
            }

            displayName += "{\n";

            displayName += fieldList.stream()
                    .map(e -> {
                        if (e.showDecl(highlight)) {
                            return e.show(nesting + 1, highlight);
                        }
                        return "";
                    })
                    .filter(e -> !e.equals(""))
                    .collect(Collectors.joining("\n" + prefix, "", ""));

            displayName += methodList.stream()
                    .map(e -> {
                        if (e.showDecl(highlight)) {
                            return e.show(nesting + 1, highlight);
                        }
                        return "";
                    })
                    .filter(e -> !e.equals(""))
                    .collect(Collectors.joining("\n" + prefix, "", ""));

            displayName += "}\n";

            return displayName;
        }

        @Override
        boolean mustDisplay() {
            return super.mustDisplay() ||
                    !methodList.isEmpty() || !fieldList.isEmpty() || definesAnnotation();
        }

        boolean definesAnnotation() {
            return (this.access & Opcodes.ACC_ANNOTATION) == Opcodes.ACC_ANNOTATION;
        }
    }

    public static class MethodRep extends RepBase {
        String name;
        List<MethodParamRep> paramRep = new LinkedList<MethodParamRep>();

        MethodRep(int access, String name, String type) {
            super(access, type);
            this.name = name;
        }

        @Override
        public String show(int nesting, String highlight) {
            String desc =  super.show(nesting, highlight) + StrUtil.sprefix(nesting);

            String specifier =  AsmAccessNames.get(this.access,AsmAccessNames.SCOPE_METHOD);
            if (!specifier.equals("")) {
                desc += specifier + " ";
            }

            if (!this.name.equals("<init>")) {
                desc += type + " ";
            }

            desc += this.name;
            desc += "(" + makeParamDescription(nesting+1, highlight) + ");\n";

            return desc;
        }

        private String makeParamDescription(int nesting, String highligh) {
            String delim = "\n" + StrUtil.sprefix(nesting);
            return this.paramRep.stream().
                    map(s -> s.show(nesting+1, highligh)).
                    collect(Collectors.joining(",", "",""));
        }

        protected boolean showDecl(String highlight) {
            if (highlight == null) {
                return true;
            }
            if (super.showDecl(highlight)) {
                return true;
            }
            return paramRep.stream().anyMatch( e -> {
                if (e.annotations != null) {
                    return e.annotations.stream().anyMatch(f -> f.type.equals(highlight));
                }
                return false;
            });
        }

    }

    public static class FieldRep extends RepBase {
        String name;
        Object value;

        FieldRep(int access, String name, String type, Object value) {
            super(access, type);
            this.name = name;
            this.value = value;
        }

        @Override
        public String show(int nesting, String highlight) {
            String fieldDesc = super.show(nesting,highlight) + StrUtil.sprefix(nesting) +
                    AsmAccessNames.get(access,AsmAccessNames.SCOPE_FIELD) + " " +
                    type  + " " + name;

            if (value != null) {
                fieldDesc += " = ";
                if (value.getClass() == java.lang.String.class) {
                    fieldDesc += '"' + value.toString() + '"';
                } else {
                    fieldDesc += value.toString();
                }
            }
            fieldDesc += ";";

            return fieldDesc;
        }
    };




    public static class MethodParamRep extends RepBase {
        String name;

        MethodParamRep(String type, RepBase parentDecl) {
            super(0, type);
            this.type = type;
            this.parentDecl = parentDecl;
        }

        @Override
        public String show(int nesting, String highlight) {
            String desc = super.show(nesting, highlight);

            if (desc != "") {
                desc += StrUtil.sprefix(nesting);
            }

            String spec = AsmAccessNames.get(access,AsmAccessNames.SCOPE_PARAMETER);
            if (!spec.equals("")) {
                desc += spec + " ";
            }

            desc += type;
            if (name != null) {
                desc += " " + name;
            }
            return desc;
        }
    }//MethodParamRep

    public static abstract class AnnotationBaseRep {
        abstract public String show(int nesting);

    }

    public static class AnnotationEnumValRep extends AnnotationBaseRep {
        AnnotationEnumValRep(String name, String type, String value) {
            this.name = name;
            this.type = type;
            this.value = value;
        }
        String name;
        String type;
        String value;

        @Override
        public String show(int nesting) {
            return name + '=' + type + '.' + value;
        }
    }


    public static class AnnotationValueRep extends AnnotationBaseRep {
        AnnotationValueRep(String name, Class type, Object value) {
            this.name = name;
            this.type = type;
            this.value = value;
        }
        String name;
        Class type;
        Object value;

        @Override
        public String show(int nesting) {
            String desc = "";

            if (name != null) {
                desc += name;
                desc += '=';
            }

            if (type == String.class) {
                desc += "\"" + value.toString() + '"';
            }
            else if (type == org.objectweb.asm.Type.class) {
                desc += SigParse.parseTypeSig(value.toString()) + ".class";
            } else {
                desc += value.toString();
            }
            return desc;
        }
    }

    public static class AnnotationCompoundRep extends AnnotationBaseRep {
        List<AnnotationBaseRep> nestedAnnotations;

        void add(AnnotationBaseRep rep) {
            if (nestedAnnotations == null) {
                nestedAnnotations = new LinkedList<AnnotationBaseRep>();
            }
            nestedAnnotations.add(rep);
        }

        @Override
        public String show(int nesting) {
            if (nestedAnnotations == null || nestedAnnotations.isEmpty()) {
                return "";
            }

            //return nestedAnnotations.stream().map(e -> e.show(nesting+1)).
            //        collect(Collectors.joining(",","",""));

            String prefix = "\n" + StrUtil.sprefix(nesting);
            return nestedAnnotations.stream().map(e -> prefix + e.show(nesting+1)).
                    collect(Collectors.joining(",","",""));
        }
    }//AnnotationCompoundRep

    public static class AnnotationArrayRep extends AnnotationCompoundRep {
        AnnotationArrayRep(String name) {
            this.name = name;
        }

        String name;

        @Override
        public String show(int nesting) {
            return name + "={" + super.show(nesting) + "}";
        }
    }//AnnotationArrayRep

    public static class AnnotationRep extends AnnotationCompoundRep {
        String type;
        boolean visible; // visible at runtime?

        AnnotationRep(String typeDesc, boolean visible)  {
            this.type = typeDesc;
            this.visible = visible;
        }


        @Override
        public String show(int nesting) {
            String desc = '@' + type;
            String rest = super.show(nesting);
            if (!rest.equals("")) {
                desc += "(" + rest + ")";
            }
            return desc;
        }
    }//AnnotationRep

    public static class AnnotationNestedRep extends AnnotationRep {
        String name;

        AnnotationNestedRep(String name, String typeDesc, boolean visible)  {
            super(typeDesc, visible);
            this.name = name;
        }
        @Override
        public String show(int nesting) {
            String desc="";
            if (name != null) {
                desc += name + '=';
            }
            return desc + super.show(nesting+1);
        }
    }//AnnotationNestedRep

}
