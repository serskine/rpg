package game.util;

import java.io.PrintStream;

public class Logger {
    public static final PrintStream INFO = System.out;
    public static final PrintStream ERROR = System.err;
    public static final PrintStream WARN = System.err;

    public static void info(final String msg) {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        final String prefix = "" + stackTrace[2].getFileName() + ":" + stackTrace[2].getLineNumber() + " - ";
        INFO.println(prefix + msg);
    }

    public static void error(final String msg, final Throwable throwable) {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        final String prefix = "" + stackTrace[2].getFileName() + ":" + stackTrace[2].getLineNumber() + " - ";
        ERROR.println(prefix + msg);
        throwable.printStackTrace(ERROR);
    }

    public static void warn(final String msg) {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        final String prefix = "" + stackTrace[2].getFileName() + ":" + stackTrace[2].getLineNumber() + " - ";
        WARN.println(prefix + msg);
    }

}
