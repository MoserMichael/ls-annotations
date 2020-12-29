package lsann.asmtools;

import java.util.*;

public class SigParse {

    static private class PosParse {
        PosParse(String desc) {
            this.desc = desc;
            this.pos = 0;
        }

        char at(int offset) {
            return this.desc.charAt(this.pos + offset );
        }

        void movePos(int step) {
            this.pos += step;
            if (this.pos > this.desc.length()) {
                throw new IllegalArgumentException("read past input: " + this.desc);
            }
        }

        boolean isEof() {
            return this.pos == this.desc.length();
        }

        String desc;
        int pos;
    }

    public static void parseFuncSigToList(LinkedList<String> paramList, String functionSig) {
        PosParse pos = new PosParse(functionSig);
        parseFuncToListImpl(paramList, pos);
        if (!pos.isEof()) {
            throw new IllegalArgumentException("Error while parscing function signature : " + functionSig + " pos: " + pos.pos);
        }
    }

    public static String parseFuncSig(String functionSig, String functionName) {
        PosParse pos = new PosParse(functionSig);
        StringBuffer parsedVal = new StringBuffer();
        parseFunc(parsedVal, pos, functionName);

        if (!pos.isEof()) {
            throw new IllegalArgumentException("Error while parscing function signature : " + functionSig + " pos: " + pos.pos);
        }
        return new String(parsedVal);
    }

    public static String parseTypeSig(String typeSig) {
        PosParse pos = new PosParse(typeSig);
        StringBuffer parsedVal = new StringBuffer();
        parseType(parsedVal, pos);

        if (!pos.isEof()) {
            throw new IllegalArgumentException("Error while parscing type signature : " + typeSig);
        }

        return new String(parsedVal);
    }

    private static void parseFuncToListImpl(LinkedList<String> paramList, PosParse pos) {
        if (pos.at(0) != '(') {
            throw new IllegalArgumentException("function type signature should start with (");
        }

        pos.movePos(1);

        while (pos.at(0) != ')') {

            StringBuffer desc = new StringBuffer();
            parseType(desc,pos);
            paramList.addLast(new String(desc));
        }
        pos.movePos(1);

        StringBuffer desc = new StringBuffer();
        parseType(desc,pos);
        paramList.addFirst(new String(desc));
    }

    private static void parseFunc(StringBuffer parsedVal, PosParse pos, String functionName) {
        if (pos.at(0) != '(') {
            throw new IllegalArgumentException("function type signature should start with (");
        }

        StringBuffer desc = new StringBuffer("(");
        pos.movePos(1);

        boolean first = true;
        while (pos.at(0) != ')') {
            if (!first) {
                desc.append(", ");
            }
            first = false;

            parseType(desc,pos);
        }
        desc.append(")");
        pos.movePos(1);

        parseType(parsedVal,pos);
        parsedVal.append(" ");
        parsedVal.append(functionName);
        parsedVal.append(desc);
    }

    private static void parseType(StringBuffer parsedVal, PosParse pos) {
        String desc = "";

        if (isBasicType(pos.at(0))) {
            parsedVal.append( basicTypeDesc(pos.at(0)) );
        } else if (isArrayPrefix(pos.at(0))) {
            parseArrayType(parsedVal, pos);
        } else if (isFullyQualifiedPrefix(pos.at(0))) {
            pos.movePos(1);
            parseFullyQualified(parsedVal, pos);
        } else {
            throw new IllegalArgumentException("illegal type prefix at offset: " + pos.pos);
        }
        pos.movePos(1);
    }

    private static void parseArrayType(StringBuffer parsedVal, PosParse pos) {
        String suffix = "";
        while(isArrayPrefix(pos.at(0))) {
            suffix += "[]";
            pos.movePos(1);
        }
        parseType(parsedVal, pos);
        parsedVal.append(suffix);

        pos.movePos(-1);
    }

    private static boolean isArrayPrefix(char ch) {
        return ch == '[';
    }

    private static void parseFullyQualified(StringBuffer parsedVal, PosParse pos) {
        String desc = "";
        char ch;

        while( (ch = pos.at(0)) != ';') {
            if (ch == '/') {
                parsedVal.append(".");
            } else {
                parsedVal.append(ch);
            }
            pos.movePos( 1 );
        }
    }

    private static boolean isFullyQualifiedPrefix(char ch) {
        return ch == 'L';
    }

    private static boolean isBasicType(char ch) {
        switch (ch) {
            case 'Z':
            case 'B':
            case 'C':
            case 'S':
            case 'I':
            case 'J':
            case 'F':
            case 'D':
            case 'V':
                return true;
            default:
                return false;
        }
    }

    private static String basicTypeDesc(char ch) {
        switch(ch) {
            case 'Z':
                return "boolean";
            case 'B':
                return "byte";
            case 'C':
                return "char";
            case 'S':
                return "short";
            case 'I':
                return "int";
            case 'J':
                return "long";
            case 'F':
                return "float";
            case 'D':
                return "double";
            case 'V':
                return "void";
            default:
                throw new IllegalArgumentException("character is not a primitive type signature");
        }
    }
}
