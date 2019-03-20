package fmg.common.notifier;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import fmg.common.LoggerSimple;
import fmg.common.Pair;
import fmg.common.ui.Factory;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * Simple UnitTest wrapper for testing {@link INotifyPropertyChanged} objects
 *  @param <T> the tested object
 */
public class PropertyChangeExecutor<T extends INotifyPropertyChanged> {

    private final T data;
    private final Map<String /* property name */, Pair<Integer /* count of modifies */, Object /* last modified value */>> modifiedProperties = new HashMap<>();

    public PropertyChangeExecutor(T data) {
        this.data = data;
    }

    /**
     * unit test executor
     * @param notificationsTimeoutMs timeout call validator if you do not receive a notification
     * @param maxWaitTimeoutMs maximum timeout to wait for all notifications
     * @param modificator data modifier (executable in UI thread)
     * @param validator data validator (executable in current thread)
     */
    public void run(
        long notificationsTimeoutMs/* = 100*/,
        long maxWaitTimeoutMs/* = 1000*/,
        Runnable modificator,
        Consumer<Map<String, Pair<Integer, Object>>> validator)
    {
        Subject<PropertyChangeEvent> subject = PublishSubject.create();
        PropertyChangeListener onDataPropertyChanged = ev -> {
            String name = ev.getPropertyName();
            LoggerSimple.put("PropertyChangeExecutor::onDataPropertyChanged: ev.name=" + name);
            modifiedProperties.put(name, new Pair<>(1 + (modifiedProperties.containsKey(name) ? modifiedProperties.get(name).first : 0), ev.getNewValue()));
            subject.onNext(ev);
        };
        data.addListener(onDataPropertyChanged);

        Disposable dis = null;
        try {
            Signal signal = new Signal();
            dis = subject.timeout(notificationsTimeoutMs, TimeUnit.MILLISECONDS)
            .subscribe(ev -> {
                LoggerSimple.put("onNext: ev={0}", ev);
            }, ex -> {
              //LoggerSimple.put("onError: " + ex);
                LoggerSimple.put("timeout after " + notificationsTimeoutMs + "ms.");
                signal.set();
            });
            Factory.DEFERR_INVOKER.accept(modificator);
            if (!signal.await(maxWaitTimeoutMs))
                throw new RuntimeException("Wait timeout " + maxWaitTimeoutMs + "ms.");

            LoggerSimple.put("  checking... modifiedProperties=[{0}]", modifiedProperties.entrySet().stream().map(kv -> kv.getKey()+":"+kv.getValue().first).collect(Collectors.joining("; ")));
            validator.accept(modifiedProperties);
        } finally {
            data.removeListener(onDataPropertyChanged);
            if (dis != null)
                dis.dispose();
        }
    }

}
