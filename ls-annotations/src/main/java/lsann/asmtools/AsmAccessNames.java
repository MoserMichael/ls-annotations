package lsann.asmtools;

import org.objectweb.asm.*;

public class AsmAccessNames {

    public static final int SCOPE_CLASS = 1;
    public static final int SCOPE_FIELD = 2;
    public static final int SCOPE_METHOD = 4;
    public static final int SCOPE_PARAMETER = 8;
    public static final int SCOPE_MODULE = 16;


    static private class Entry {
        int mask;
        String name;
        int scope;
        boolean classname;

        Entry(int mask, String name, int scope) {
            this.mask = mask;
            this.name = name;
            this.scope = scope;
        }

        Entry(int mask, String name, int scope, boolean classname) {
            this.mask = mask;
            this.name = name;
            this.scope = scope;
            this.classname = classname;
        }

    }

    static private Entry arr[] = {
            new Entry(Opcodes.ACC_PUBLIC, "public", SCOPE_CLASS|SCOPE_FIELD|SCOPE_METHOD),
            new Entry(Opcodes.ACC_PRIVATE,"private", SCOPE_CLASS|SCOPE_FIELD|SCOPE_METHOD),
            new Entry(Opcodes.ACC_PROTECTED, "protected", SCOPE_CLASS|SCOPE_FIELD|SCOPE_METHOD),

            new Entry(Opcodes.ACC_STATIC, "static", SCOPE_FIELD|SCOPE_METHOD),
            new Entry(Opcodes.ACC_FINAL, "final", SCOPE_CLASS|SCOPE_FIELD|SCOPE_METHOD|SCOPE_PARAMETER),
            new Entry(Opcodes.ACC_ABSTRACT, "abstract", SCOPE_CLASS | SCOPE_METHOD),

            // not really useful https://stackoverflow.com/questions/8949933/what-is-the-purpose-of-the-acc-super-access-flag-on-java-class-files
            //new Entry(Opcodes.ACC_SUPER, "super", SCOPE_CLASS),

            // internal thing.
            //new Entry(Opcodes.ACC_SYNTHETIC, "synthetic?", SCOPE_CLASS|SCOPE_FIELD|SCOPE_METHOD|SCOPE_PARAMETER|SCOPE_MODULE),

            new Entry(Opcodes.ACC_SYNCHRONIZED, "synchronized", SCOPE_METHOD),

            new Entry(Opcodes.ACC_OPEN, "open", SCOPE_MODULE),
            new Entry(Opcodes.ACC_TRANSITIVE, "transitive", SCOPE_MODULE),
            new Entry(Opcodes.ACC_BRIDGE, "bridge?", SCOPE_METHOD),
            new Entry(Opcodes.ACC_STATIC_PHASE, "static", SCOPE_MODULE),

            new Entry(Opcodes.ACC_VOLATILE, "volatile", SCOPE_FIELD),
            new Entry(Opcodes.ACC_VARARGS, "...", SCOPE_METHOD), // after type of single method arg.

            new Entry(Opcodes.ACC_TRANSIENT, "transient", SCOPE_FIELD),
            new Entry(Opcodes.ACC_NATIVE, "native", SCOPE_METHOD),
            new Entry(Opcodes.ACC_STRICT, "strict", SCOPE_METHOD),

            new Entry(Opcodes.ACC_MANDATED, "mandated",SCOPE_CLASS|SCOPE_FIELD|SCOPE_METHOD),
            new Entry(Opcodes.ACC_DEPRECATED, "deprecated", SCOPE_CLASS|SCOPE_FIELD|SCOPE_METHOD),

            new Entry(Opcodes.ACC_ENUM, "enum", SCOPE_CLASS | SCOPE_FIELD, true),
            new Entry(Opcodes.ACC_MODULE, "module", SCOPE_CLASS, true),
            new Entry(Opcodes.ACC_ANNOTATION, "@interface", SCOPE_CLASS, true),
            new Entry(Opcodes.ACC_INTERFACE, "interface", SCOPE_CLASS, true),
            new Entry(Opcodes.ACC_RECORD, "record", SCOPE_CLASS, true),

    };

    public static String get(int mask, int scope) {

        StringBuffer ret = new StringBuffer();
        boolean classname = false;

        for(int i = 0; i < arr.length; ++i) {
            if ((arr[i].scope & scope) != 0){
                if ((arr[i].mask & mask) == arr[i].mask) {
                    classname = classname | arr[i].classname;
                    if (ret.length() != 0) {
                        ret.append(" ");
                    }
                    ret.append(arr[i].name);
                }
            }
        }

        if ((scope & SCOPE_CLASS) != 0  && !classname) {
            if (ret.length() != 0) {
                ret.append(" ");
            }
            ret.append("class");
        }
        return new String(ret);
    }

}
