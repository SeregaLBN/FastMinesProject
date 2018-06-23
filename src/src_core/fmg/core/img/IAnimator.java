package fmg.core.img;

import java.util.function.Consumer;

/** Perform tasks for calculating / changing the subscribers animation. Executed in the current UI thread */
public interface IAnimator {

   void subscribe(Object subscriber, Consumer<Long /** time in ms from start subscribe */> subscriberCallbackMethod);
   void pause(Object subscriber);
   void unsubscribe(Object subscriber);

}
