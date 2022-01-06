package fmg.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Very simple logger */
public final class Logger {

    /** may be rewrited */
    public static Consumer<String>   ERROR_WRITER = System.err::println;
    public static Consumer<String> WARNING_WRITER = System.out::println;
    public static Consumer<String>    INFO_WRITER = System.out::println;
    public static Consumer<String>   DEBUG_WRITER = null;
    public static boolean USE_DATE_PREFIX = true;

    private enum ELevel { ERROR, WARNING, INFO, DEBUG }

    private static final String SEPAR_SPACES = "  ";

    private static void write(ELevel level, String format, Object... args) {
        Consumer<String> writer = null;
        switch (level) {
        case ERROR  : writer =   ERROR_WRITER; break;
        case WARNING: writer = WARNING_WRITER; break;
        case INFO   : writer =    INFO_WRITER; break;
        case DEBUG  : writer =   DEBUG_WRITER; break;
        }
        if (writer == null)
            return;

        try {

            StringBuilder sb = new StringBuilder();

            if (USE_DATE_PREFIX)
                sb.append('[')
                  .append(new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()))
                  .append("]  ");

            sb.append(level.name()).append(SEPAR_SPACES);
            if (level == ELevel.INFO)
                sb.append(' ');

            sb.append("Th=")
              .append(Thread.currentThread().getId())
              .append(SEPAR_SPACES);
            if (args.length > 0)
                sb.append(new MessageFormat(format, Locale.US).format(args));
            else
                sb.append(format);

            writer.accept(sb.toString());

        } catch(Exception ex) {
            System.err.println(ex);
            writer.accept(format);
        }
    }

    public static void error(String message, Throwable ex) {
        try (StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw))
        {
            ex.printStackTrace(pw);
            write(ELevel.ERROR, "{0}: {1}", message, sw.toString());
        } catch (Exception ex2) {
            // ignore
        }
    }

    public static void error(String message) {
        write(ELevel.ERROR, message);
    }

    public static void warn(String message) {
        write(ELevel.WARNING, message);
    }

    public static void info(String message) {
        write(ELevel.INFO, message);
    }

    public static void debug(String message) {
        write(ELevel.DEBUG, message);
    }

    public static void error(String format, Object... args) {
        write(ELevel.ERROR, format, args);
    }

    public static void warn(String format, Object... args) {
        write(ELevel.WARNING, format, args);
    }

    public static void info(String format, Object... args) {
        write(ELevel.INFO, format, args);
    }

    public static void debug(String format, Object... args) {
        write(ELevel.DEBUG, format, args);
    }

    public static final class Tracer implements AutoCloseable {
        private final String hint;
        private final Supplier<String> disposeMessage;

        private static final ThreadLocal<Integer> THREAD_CONTEXT = new ThreadLocal<>();
        static {
            THREAD_CONTEXT.set(-1);
        }

        public Tracer() { this(null, null, null); }

        public Tracer(String hint) { this(hint, null, null); }

        public Tracer(String hint, String ctorMessage) { this(hint, ctorMessage, null); }

        public Tracer(String hint, Supplier<String> disposeMessage) { this(hint, null, disposeMessage); }

        public Tracer(String hint, String ctorMessage, Supplier<String> disposeMessage) {
            inc();
            this.hint = hint;
            this.disposeMessage = disposeMessage;
            if (ctorMessage == null)
                Logger.debug("{0}> {1}", prefix(), hint);
            else
                Logger.debug("{0}> {1}: {2}", prefix(), hint, ctorMessage);
        }

        private static void inc() {
            THREAD_CONTEXT.set(THREAD_CONTEXT.get() + 1);
        }
        private static void dec() {
            THREAD_CONTEXT.set(THREAD_CONTEXT.get() - 1);
        }
        private static String prefix() {
            int n = THREAD_CONTEXT.get();
            String tab = "   ";
            //return String.format("%0" + n + "d", 0).replace("0", tab);
            return String.join("", Collections.nCopies(n, tab));
            //return tab.repeat(n); // java 11
        }

        @Override
        public void close() {
            if (disposeMessage == null)
                Logger.debug("{0}< {1}", prefix(), hint);
            else
                Logger.debug("{0}< {1}: {2}", prefix(), hint, disposeMessage.get());
            dec();
        }

        public void put(String format, Object... args) {
            if (args.length > 0)
                format = new MessageFormat(format).format(args);
            Logger.debug("{0}  {1}: {2}", prefix(), hint, format);
        }

    }

}
