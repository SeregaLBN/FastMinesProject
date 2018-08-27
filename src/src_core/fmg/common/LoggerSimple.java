package fmg.common;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class LoggerSimple {

   /** may be override */
   public static Consumer<String> DEFAULT_WRITER = message -> System.out.println(message);

   public static void Put(String format, Object... args) {
      try {
         String prefix = '[' + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()) + "]  Th=" + Thread.currentThread().getId() + "  ";
         if (args.length > 0) {
            DEFAULT_WRITER.accept(prefix + new MessageFormat(format).format(args));
         } else {
            DEFAULT_WRITER.accept(prefix + format);
         }
      } catch(Throwable ex) {
         System.err.println(ex);
         if (DEFAULT_WRITER != null)
            DEFAULT_WRITER.accept(format);
      }
   }

   public static final class Tracer implements AutoCloseable {
      private final String _hint;
      private final Supplier<String>_disposeMessage;

      public Tracer() { this(null, null, null); }

      public Tracer(String hint) { this(hint, null, null); }

      public Tracer(String hint, String ctorMessage) { this(hint, ctorMessage, null); }

      public Tracer(String hint, Supplier<String> disposeMessage) { this(hint, null, disposeMessage); }

      public Tracer(String hint, String ctorMessage, Supplier<String> disposeMessage) {
         _hint = hint;
         _disposeMessage = disposeMessage;
         if (ctorMessage == null)
            LoggerSimple.Put("> {0}", hint);
         else
            LoggerSimple.Put("> {0}: {1}", hint, ctorMessage);
      }

      @Override
      public void close() {
         if (_disposeMessage == null)
            LoggerSimple.Put("< {0}", _hint);
         else
            LoggerSimple.Put("< {0}: {1}", _hint, _disposeMessage.get());
      }

      public void Put(String format, Object... args) {
         if (args.length > 0)
            format = new MessageFormat(format).format(args);
         LoggerSimple.Put("  {0}: {1}", _hint, format);
      }

   }

}
