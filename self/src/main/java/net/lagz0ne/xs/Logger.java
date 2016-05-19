package net.lagz0ne.xs;

public class Logger {

    public static void debug(String msg) {
//        debug(msg, null);
    }

    public static void debug(String msg, Object ... replacements) {
        System.out.println("DEBUG: " + translate(msg, replacements));
    }

    public static void info(String msg) {
        info(msg, null);
    }

    public static void info(String msg, Object replacements) {
        System.out.print("INFO: " + translate(msg, replacements));
    }

    public static void error(String msg) {
        error(msg, null);
    }

    public static void error(String msg, Object replacements) {
        System.out.print("ERROR: " + translate(msg, replacements));
    }

    private static String translate(String msg, Object ... replacements) {
        if (replacements == null) return msg;

        for (Object replacement : replacements) {
            msg = msg.replaceFirst("\\{\\}", replacement.toString());
        }
        return msg;
    }
}
