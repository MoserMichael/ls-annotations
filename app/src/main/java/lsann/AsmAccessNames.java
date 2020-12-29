package lsann;

import org.objectweb.asm.*;

public class AsmAccessNames {

    public static final int SCOPE_CLASS = 0x1;
    public static final int SCOPE_FIELD = 0x2;
    public static final int SCOPE_METHOD = 0x4;
    public static final int SCOPE_PARAMETER = 0x8;
    public static final int SCOPE_MODULE = 0x8;


    static private class Entry {
        int mask;
        int scope;
        String name;
        Entry(int mask, String name, int scope) {
            this.mask = mask;
            this.scope = scope;
            this.name = name;
        }
    }

    static private Entry arr[] = {
            new Entry(Opcodes.ACC_PUBLIC, "public", SCOPE_CLASS|SCOPE_FIELD|SCOPE_METHOD),
            new Entry(Opcodes.ACC_PRIVATE,"private", SCOPE_CLASS|SCOPE_FIELD|SCOPE_METHOD),
            new Entry(Opcodes.ACC_PROTECTED, "protected", SCOPE_CLASS|SCOPE_FIELD|SCOPE_METHOD),
            new Entry(Opcodes.ACC_STATIC, "static", SCOPE_FIELD|SCOPE_METHOD),
            new Entry(Opcodes.ACC_FINAL, "final", SCOPE_CLASS|SCOPE_FIELD|SCOPE_METHOD|SCOPE_PARAMETER),
            new Entry(Opcodes.ACC_SUPER, "super", SCOPE_CLASS),
            new Entry(Opcodes.ACC_SYNCHRONIZED, "synchronized", SCOPE_METHOD),
            new Entry(Opcodes.ACC_OPEN, "open", SCOPE_MODULE),
            new Entry(Opcodes.ACC_TRANSITIVE, "transitive", SCOPE_MODULE),
            new Entry(Opcodes.ACC_VOLATILE, "volatile", SCOPE_FIELD),
            new Entry(Opcodes.ACC_BRIDGE, "bridge?", SCOPE_METHOD),
            new Entry(Opcodes.ACC_STATIC_PHASE, "static", SCOPE_MODULE),
            new Entry(Opcodes.ACC_VARARGS, "varargs?", SCOPE_METHOD),
            new Entry(Opcodes.ACC_TRANSIENT, "transient", SCOPE_FIELD),
            new Entry(Opcodes.ACC_NATIVE, "native", SCOPE_METHOD),
            new Entry(Opcodes.ACC_INTERFACE, "interface", SCOPE_CLASS),
            new Entry(Opcodes.ACC_ABSTRACT, "private", SCOPE_CLASS | SCOPE_METHOD),
            new Entry(Opcodes.ACC_STRICT, "private", SCOPE_METHOD),
            new Entry(Opcodes.ACC_SYNTHETIC, "synthetic?", SCOPE_CLASS|SCOPE_FIELD|SCOPE_METHOD|SCOPE_PARAMETER|SCOPE_MODULE),
            new Entry(Opcodes.ACC_ANNOTATION, "annotation", SCOPE_CLASS),
            new Entry(Opcodes.ACC_ENUM, "enum", SCOPE_FIELD),
            new Entry(Opcodes.ACC_MANDATED, "mandated",SCOPE_CLASS|SCOPE_FIELD|SCOPE_METHOD),
            new Entry(Opcodes.ACC_MODULE, "module", SCOPE_CLASS),
            new Entry(Opcodes.ACC_RECORD, "record", SCOPE_CLASS),
            new Entry(Opcodes.ACC_DEPRECATED, "deprecated", SCOPE_CLASS|SCOPE_FIELD|SCOPE_METHOD),
    };

    static String get(int mask, int scope) {
        String ret = "";
        for(int i = 0; i < arr.length; ++i) {
            if ((arr[i].mask & mask) != 0 && (arr[i].scope & scope) != 0) {
                if (ret != "") {
                    ret += " ";
                }
                ret += arr[i].name;
            }
        }
        return ret;
    }

}
