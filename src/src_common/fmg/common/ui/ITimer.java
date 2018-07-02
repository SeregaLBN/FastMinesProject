package fmg.common.ui;

/** Timer interface. Pulsates in the UI thread. */
public interface ITimer extends AutoCloseable {

   /** milliseconds */
   long getInterval();
   /** milliseconds */
   void setInterval(long delay);

   /** set null - stop timer; otherwise - started */
   void setCallback(Runnable cb);

   @Override
   void close();

}
