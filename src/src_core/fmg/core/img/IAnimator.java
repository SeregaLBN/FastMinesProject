package fmg.core.img;

import java.util.function.Consumer;

public interface IAnimator {

   void subscribe(Object subscriber, Consumer<Long /** time from start subscribe */> subscriberCallbackMethod);
   void pause(Object subscriber);
   void unsubscribe(Object subscriber);

}
