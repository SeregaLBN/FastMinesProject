package fmg.jfx.img;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javafx.animation.AnimationTimer;

import fmg.core.img.IAnimator;

public class Animator implements IAnimator, AutoCloseable {

    private static class SubscribeInfo {
        public boolean active = true;    // enabled?
        public long    startTime = new Date().getTime(); // start time of subscribe
        public Consumer<Long /* time from the beginning of the subscription */> callback;
    }
    /** one timer to all subscribers */
    private final AnimationTimer timer;
    private final Map<Object /* subscriber */, SubscribeInfo> subscribers;

    private static Animator singleton;
    public static Animator getSingleton() { // not synchronized. since should work only in the thread of the UI.
        if (singleton == null)
            singleton = new Animator();
        return singleton;
    }

    private Animator() {
        subscribers = new HashMap<>();
        timer = new AnimationTimer() {

            @Override
            public void handle(long now) {
                long currentTime = new Date().getTime();
                subscribers.forEach((k, v) -> {
                    if (v.active)
                        v.callback.accept(currentTime - v.startTime);
                });
            }

        };
        timer.start();
    }

    @Override
    public void subscribe(Object subscriber, Consumer<Long /* time from start subscribe */> subscriberCallback) {
        SubscribeInfo info = subscribers.get(subscriber);
        if (info == null) {
            info = new SubscribeInfo();
            info.callback = subscriberCallback;
            subscribers.put(subscriber, info);
        } else {
            if (info.active)
                return; // alredy run

            info.active = true;
            info.startTime = new Date().getTime() - info.startTime; // apply of pause delta time
        }
    }

    @Override
    public void pause(Object subscriber) {
        SubscribeInfo info = subscribers.get(subscriber);
        if (info == null)
            return;

        if (!info.active)
            return; // alredy paused

        info.active = false;
        info.startTime = new Date().getTime() - info.startTime; // set of pause delta time
    }

    @Override
    public void unsubscribe(Object subscriber) {
        subscribers.remove(subscriber);
    }

    @Override
    public void close() {
        timer.stop();
        subscribers.clear();
    }

}
