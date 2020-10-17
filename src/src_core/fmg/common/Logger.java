package fmg.common;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Very simple logger */
public final class Logger {

    /** may be override */
    public static Consumer<String> DEFAULT_WRITER = null; // System.out::println;
    public static boolean USE_DATE_PREFIX = true;

    public static void info(String format, Object... args) {
        if (DEFAULT_WRITER == null)
            return;

        try {
            String prefix = USE_DATE_PREFIX
                                ? '[' + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()) + "]  Th=" + Thread.currentThread().getId() + "  "
                                :                                                                    "Th=" + Thread.currentThread().getId() + "  ";
            if (args.length > 0) {
                DEFAULT_WRITER.accept(prefix + new MessageFormat(format, Locale.US).format(args));
            } else {
                DEFAULT_WRITER.accept(prefix + format);
            }
        } catch(Throwable ex) {
            System.err.println(ex);
            DEFAULT_WRITER.accept(format);
        }
    }

    public static final class Tracer implements AutoCloseable {
        private final String _hint;
        private final Supplier<String> _disposeMessage;

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
            _hint = hint;
            _disposeMessage = disposeMessage;
            if (ctorMessage == null)
                Logger.info("{0}> {1}", prefix(), hint);
            else
                Logger.info("{0}> {1}: {2}", prefix(), hint, ctorMessage);
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
            if (_disposeMessage == null)
                Logger.info("{0}< {1}", prefix(), _hint);
            else
                Logger.info("{0}< {1}: {2}", prefix(), _hint, _disposeMessage.get());
            dec();
        }

        public void put(String format, Object... args) {
            if (args.length > 0)
                format = new MessageFormat(format).format(args);
            Logger.info("{0}  {1}: {2}", prefix(), _hint, format);
        }

    }

}
