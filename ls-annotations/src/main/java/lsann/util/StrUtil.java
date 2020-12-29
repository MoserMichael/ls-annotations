package lsann.util;

import java.util.Collections;
import java.util.stream.Collectors;

public class StrUtil {

    private static final String TERMINAL_MARK_START_OSX = "\033[1;31m";
    private static final String TERMINAL_MARK_EOF_OSX = "\033[0m";

    private static final String TERMINAL_MARK_START_LINUX = "\033[1;31";
    private static final String TERMINAL_MARK_EOF_LINUX = "\033[0m";

    private static final String HTML_MARK_START = "<b>";
    private static final String HTML_MARK_EOF = "</b>";

    static String markStart = HTML_MARK_START;
    static String markEnd = HTML_MARK_EOF;
    static boolean consoleInit = false;

    public static String sprefix(int nesting) {
        return Collections.nCopies(nesting, "    ").stream().collect(Collectors.joining());
    }

    public static String highlight(String str) {
        if (!consoleInit) {
            // partial solution to checking if stdout is a terminal; java doesn't have isatty(3)
            if (System.console() != null) {
                String OS = System.getProperty("os.name").toLowerCase();

                if (OS.indexOf("mac") >= 0) {
                    markStart = TERMINAL_MARK_START_OSX;
                    markEnd = TERMINAL_MARK_EOF_OSX;
                }
                if (OS.indexOf("nix") >= 0) {
                    markStart = TERMINAL_MARK_START_LINUX;
                    markEnd = TERMINAL_MARK_EOF_LINUX;
                }
            }
            consoleInit = true;
        }
        return markStart + str + markEnd;
    }
}
