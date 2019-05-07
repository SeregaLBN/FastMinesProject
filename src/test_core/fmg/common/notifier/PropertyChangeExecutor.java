package fmg.common.notifier;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import fmg.common.LoggerSimple;
import fmg.common.Pair;
import fmg.common.ui.UiInvoker;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * Simple UnitTest wrapper for testing {@link INotifyPropertyChanged} objects
 * @param <T> the tested object
 */
public class PropertyChangeExecutor<T extends INotifyPropertyChanged & AutoCloseable> {

    private final boolean needClose;
    private final Supplier<T> dataCreator;
    private final Map<String /* property name */, Pair<Integer /* count of modifies */, Object /* last modified value */>> modifiedProperties = new HashMap<>();

    /** @param dataCreator called from UI thread
     *  @param needClose need call AutoCloseable.close() for dataCreator result */
    public PropertyChangeExecutor(Supplier<T> dataCreator, boolean needClose) {
        if (dataCreator == null)
            throw new IllegalArgumentException();
        this.needClose = needClose;
        this.dataCreator = dataCreator;
    }
    /** @param dataCreator called from UI thread */
    public PropertyChangeExecutor(Supplier<T> dataCreator) {
        this(dataCreator, true);
    }

    /** Unit test executor
     * @param notificationsTimeoutMs timeout call validator if you do not receive a notification
     * @param maxWaitTimeoutMs maximum timeout to wait for all notifications
     * @param modificator data modifier (executable in UI thread)
     * @param validator data validator (executable in called thread)
     */
    public void run(
        long notificationsTimeoutMs/* = 100*/,
        long maxWaitTimeoutMs/* = 1000*/,
        Consumer<T> modificator,
        BiConsumer<T, Map<String, Pair<Integer, Object>>> validator)
    {
        Subject<PropertyChangeEvent> subject = PublishSubject.create();
        PropertyChangeListener onDataPropertyChanged = ev -> {
            String name = ev.getPropertyName();
            LoggerSimple.put("PropertyChangeExecutor.onDataPropertyChanged: ev.name=" + name);
            modifiedProperties.put(name, new Pair<>(1 + (modifiedProperties.containsKey(name) ? modifiedProperties.get(name).first : 0), ev.getNewValue()));
            subject.onNext(ev);
        };

        Object[] data = { null };
        Disposable dis = null;
        Throwable[] ex1 = { null };
        try {
            Signal signal = new Signal();
            dis = subject
                .timeout(notificationsTimeoutMs, TimeUnit.MILLISECONDS)
                .subscribe(ev -> {
                    LoggerSimple.put("onNext: ev={0}", ev);
                }, ex -> {
                  //LoggerSimple.put("onError: " + ex);
                    LoggerSimple.put("timeout after " + notificationsTimeoutMs + "ms.");
                    signal.set();
                });
            UiInvoker.DEFERRED.accept(() -> {
                if (ex1[0] != null)
                    return;
                try {
                    T d = dataCreator.get(); // 1. Construct in UI thread!
                    d.addListener(onDataPropertyChanged);
                    data[0] = d;
                    modificator.accept(d);
                } catch(Throwable ex) {
                    ex1[0] = ex;
                }
            });
            if (!signal.await(maxWaitTimeoutMs)) {
                ex1[0] = new RuntimeException("Wait timeout " + maxWaitTimeoutMs + "ms.");
            } else {
                if (ex1[0] == null) {
                    LoggerSimple.put("  checking... modifiedProperties=[{0}]", modifiedProperties.entrySet().stream().map(kv -> kv.getKey()+":"+kv.getValue().first).collect(Collectors.joining("; ")));
                    @SuppressWarnings("unchecked")
                    T d = (T)data[0];
                    try {
                        validator.accept(d, modifiedProperties);
                    } catch(Throwable ex) {
                        ex1[0] = ex;
                    }
                }
            }
        } finally {
            if (dis != null) {
                dis.dispose();
                dis = null;
            }

            @SuppressWarnings("unchecked")
            T d = (T)data[0];
            if (d != null) {
                Signal signal = new Signal();
                UiInvoker.DEFERRED.accept(() -> {
                    try {
                        d.removeListener(onDataPropertyChanged);
                        if (needClose)
                            d.close(); // 2. Destruct in UI thread!
                        signal.set();
                    } catch(Throwable ex) {
                        if (ex1[0] == null)
                            ex1[0] = ex;
                        else
                            System.err.println(ex);
                    }
                });
                if (!signal.await(maxWaitTimeoutMs)) {
                    String errMsg = "Wait free timeout " + maxWaitTimeoutMs + "ms.";
                    if (ex1[0] == null) {
                        ex1[0] = new RuntimeException(errMsg);
                    } else {
                        System.err.println(errMsg);
                    }
                }
            }
        }

        if (ex1[0] != null) {
            Throwable ex = ex1[0];
            System.err.println(ex);
            if (ex instanceof Error)
                throw (Error)ex;
            if (ex instanceof RuntimeException)
                throw (RuntimeException)ex;
            throw new RuntimeException(ex);
        }
    }

}
