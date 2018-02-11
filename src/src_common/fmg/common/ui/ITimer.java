package fmg.common.ui;

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
