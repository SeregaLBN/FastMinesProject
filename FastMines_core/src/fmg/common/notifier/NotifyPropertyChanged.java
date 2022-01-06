package fmg.common.notifier;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Stream;

import fmg.common.Logger;
import fmg.common.Pair;
import fmg.common.ui.UiInvoker;
import fmg.core.types.Property;

/** Notifies owner clients that a owner property value has changed */
public final class NotifyPropertyChanged implements AutoCloseable, INotifyPropertyChanged
{
    private final INotifyPropertyChanged owner;
    private final List<PropertyChangeListener> propertyChanges;
    private final boolean deferredNotifications;
    private final Map<String /* propertyName */, Pair<Object /* old value */, Object /* new value */>> deferrNotifications = new HashMap<>();
    private boolean disposed = false;
    private Map<String /* propertyName */, Field> cachedFields = new HashMap<>();

    public NotifyPropertyChanged(INotifyPropertyChanged owner) { this(owner, false); }
    public NotifyPropertyChanged(INotifyPropertyChanged owner, boolean deferredNotifications) {
        this.owner = owner;
        this.propertyChanges = new ArrayList<>();
        this.deferredNotifications = deferredNotifications;
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        propertyChanges.add(listener);

        int count = propertyChanges.size();
        if (count > 4)
            Logger.error("Suspiciously many subscribers! count=" + count);
    }
    @Override
    public void removeListener(PropertyChangeListener listener) {
        if (!propertyChanges.contains(listener))
            throw new IllegalArgumentException("NotifyPropertyChanged.removeListener: Illegal listener=" + listener);
        propertyChanges.remove(listener);
    }


    /** Set the value to the specified property  and throw event to listeners */
    public <T> boolean setProperty(T oldValue, T newValue, String propertyName) {
        if (disposed) {
            if (newValue != null) {
                Logger.error("Illegal call property " + owner.getClass().getCanonicalName() + "."+ propertyName + ": object already disposed!");
                return false;
            }
        }

        try {
            Field fld = findField(propertyName);
            Object oldValueReal = fld.get(owner);
            if (((oldValueReal == null) && (oldValue != null)) ||
                ((oldValueReal != null) && !oldValueReal.equals(oldValue)))
            {
                // illegal usage
                Logger.error("Different old values");
            }
            if ((oldValueReal == null) && (newValue == null))
                return false;
            if ((oldValueReal != null) && oldValueReal.equals(newValue))
                return false;
            fld.set(owner, newValue);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        firePropertyChanged(oldValue, newValue, propertyName);
        return true;
    }

    protected final void firePropertyChanged(int oldValue, int newValue, String propertyName) {
        firePropertyChanged(Integer.valueOf(oldValue), Integer.valueOf(newValue), propertyName);
    }

    public final void firePropertyChanged(double oldValue, double newValue, String propertyName) {
        firePropertyChanged(Double.valueOf(oldValue), Double.valueOf(newValue), propertyName);
    }

    protected final void firePropertyChanged(boolean oldValue, boolean newValue, String propertyName) {
        firePropertyChanged(Boolean.valueOf(oldValue), Boolean.valueOf(newValue), propertyName);
    }

    public final void firePropertyChanged(String propertyName) {
        firePropertyChanged(null, null, propertyName);
    }

    public <T> void firePropertyChanged(T oldValue, T newValue, String propertyName) {
        if (disposed)
            return;

        if (!deferredNotifications) {
            //Logger.info("firePropertyChanged: " + propertyName + ": " + newValue);
            propertyChanges.forEach(item -> item.propertyChange(new PropertyChangeEvent(owner, propertyName, oldValue, newValue)));
        } else {
            boolean shedule;
            {
                Pair<Object /* old value */, Object /* new value */> event = deferrNotifications.get(propertyName);
                boolean isFirstEvent = (event == null); // this is the first event with this name?
                if (isFirstEvent) {
                    event = new Pair<>(oldValue, newValue);
                } else {
                    @SuppressWarnings("unchecked")
                    T realOldValue = (T)event.first; // restore real old value
                    if ((realOldValue != null) && realOldValue.equals(newValue)) {
                        // Nothing to fire. First event OldValue and last event NewValue is same objects.
                        deferrNotifications.remove(propertyName); // HINT_1
                        return;
                    }
                    event = new Pair<>(realOldValue, newValue);
                }
                shedule = isFirstEvent;
                deferrNotifications.put(propertyName, event); // Re-save only the last event (with initial old value)
            }
            if (shedule) {
                /** /
                Logger.info("Defer shedule:\n   " +
                        Stream.of(Thread.currentThread().getStackTrace())
                            .filter(x -> x.getClassName().startsWith("fmg."))
                            .map(x -> x.toString())
                          //.map(x -> x.substring(9))
                            .filter(x -> !x.contains(".lambda$"))
                          //.filter(x -> !x.contains(".firePropertyChanged("))
                            .collect(Collectors.joining("\n   ")));
                /**/
                UiInvoker.DEFERRED.accept(() -> {
                    if (disposed)
                        return;

                    if (!deferrNotifications.containsKey(propertyName))
                        return; // event already deleted (see HINT_1)
                    Pair<Object /* old value */, Object /* new value */> event = deferrNotifications.remove(propertyName);
                    if (event == null)
                        Logger.error("hmmm... invalid usage ;(");
                    else
                        propertyChanges.forEach(item -> item.propertyChange(new PropertyChangeEvent(owner, propertyName, event.first, event.second)));
                });
            }
        }
    }

    private Field findField(String propertyName) {
        Field field = cachedFields.get(propertyName);
        if (field == null) {
            Optional<Field> opt = getDeclaredFields(owner)
                .filter(f -> {
                    Property p = f.getDeclaredAnnotation(Property.class);
                    if (p == null)
                        return false;
                    return p.value().equals(propertyName); })
                .findAny();
            if (!opt.isPresent())
                throw new RuntimeException("Property '" + propertyName + "' not found");
            field = opt.get();
            field.setAccessible(true);
            cachedFields.put(propertyName, field);
        }
        return field;
    }

    private static <T> Stream<Field> getDeclaredFields(T obj) {
        Class<?> clazz = obj.getClass();
        List<Stream<Field>> arr = new ArrayList<>();
        do {
            arr.add(Arrays.stream(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        } while (clazz != null);
        return arr.stream().flatMap(x -> x);
    }


    public boolean isDisposed() { return disposed; }

    @Override
    public void close() {
        disposed = true;
        cachedFields.clear();

        if (!deferrNotifications.isEmpty())
            Logger.debug("Not all deferred notifications handled! Count={0}", deferrNotifications.size());
        deferrNotifications.clear();

        if (!propertyChanges.isEmpty())
            // strong unsubscribe
            throw new IllegalStateException("Illegal usage: Not all listeners were unsubscribed (type " + owner.getClass().getName() + "): count=" + propertyChanges.size());
        propertyChanges.clear();
    }

}
