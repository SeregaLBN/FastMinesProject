package fmg.common.ui;

public interface ITimer extends AutoCloseable {

   /** miliseconds */
   long getInterval();
   /** miliseconds */
   void setInterval(long delay);

   /** set null - stop timer; otherwise - started */
   void setCallback(Runnable cb);

   @Override
   void close();

}
