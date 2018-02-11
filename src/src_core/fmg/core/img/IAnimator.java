package fmg.core.img;

import java.util.function.Consumer;

public interface IAnimator {

   void subscribe(Object subscriber, Consumer<Long /** time from start subscribe */> subscriberCallbackMethod);
   void unsubscribe(Object subscriber);

}
