package fmg.common.notifier;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Stream;

import fmg.common.Logger;
import fmg.common.Pair;
import fmg.common.ui.UiInvoker;

/** Notifies owner clients that a owner property value has changed */
public final class NotifyPropertyChanged implements AutoCloseable, INotifyPropertyChanged
{
    private final INotifyPropertyChanged _owner;
    private final List<PropertyChangeListener> _propertyChanges;
    private final boolean _deferredNotifications;
    private final Map<String /* propertyName */, Pair<Object /* old value */, Object /* new value */>> _deferrNotifications = new HashMap<>();
    private boolean _disposed = false;

    public NotifyPropertyChanged(INotifyPropertyChanged owner) { this(owner, false); }
    public NotifyPropertyChanged(INotifyPropertyChanged owner, boolean deferredNotifications) {
        _owner = owner;
        _propertyChanges = new ArrayList<>();
        _deferredNotifications = deferredNotifications;
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        _propertyChanges.add(listener);

        int count = _propertyChanges.size();
        if (count > 4)
            System.err.println("Suspiciously many subscribers! count=" + count);
    }
    @Override
    public void removeListener(PropertyChangeListener listener) {
        if (!_propertyChanges.contains(listener))
            throw new IllegalArgumentException("NotifyPropertyChanged.removeListener: Illegal listener=" + listener);
        _propertyChanges.remove(listener);
    }


    /** Set the value to the specified property  and throw event to listeners */
    public <T> boolean setProperty(T oldValue, T newValue, String propertyName) {
        if (_disposed) {
            if (newValue != null) {
                System.err.println("Illegal call property " + _owner.getClass().getCanonicalName() + "."+ propertyName + ": object already disposed!");
                return false;
            }
        }

        try {
            Field fld = findField(propertyName);
            Object oldValueReal = fld.get(_owner);
            if (((oldValueReal == null) && (oldValue != null)) ||
                ((oldValueReal != null) && !oldValueReal.equals(oldValue)))
            {
                // illegal usage
                System.err.println("Different old values");
            }
            if ((oldValueReal == null) && (newValue == null))
                return false;
            if ((oldValueReal != null) && oldValueReal.equals(newValue))
                return false;
            fld.set(_owner, newValue);
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
        if (_disposed)
            return;

        if (!_deferredNotifications) {
            //System.out.println("firePropertyChanged: " + propertyName + ": " + newValue);
            _propertyChanges.forEach(item -> item.propertyChange(new PropertyChangeEvent(_owner, propertyName, oldValue, newValue)));
        } else {
            boolean shedule;
            {
                Pair<Object /* old value */, Object /* new value */> event = _deferrNotifications.get(propertyName);
                boolean isFirstEvent = (event == null); // this is the first event with this name?
                if (isFirstEvent) {
                    event = new Pair<>(oldValue, newValue);
                } else {
                    @SuppressWarnings("unchecked")
                    T realOldValue = (T)event.first; // restore real old value
                    if ((realOldValue != null) && realOldValue.equals(newValue)) {
                        // Nothing to fire. First event OldValue and last event NewValue is same objects.
                        _deferrNotifications.remove(propertyName); // HINT_1
                        return;
                    }
                    event = new Pair<>(realOldValue, newValue);
                }
                shedule = isFirstEvent;
                _deferrNotifications.put(propertyName, event); // Re-save only the last event (with initial old value)
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
                    if (_disposed)
                        return;

                    if (!_deferrNotifications.containsKey(propertyName))
                        return; // event already deleted (see HINT_1)
                    Pair<Object /* old value */, Object /* new value */> event = _deferrNotifications.remove(propertyName);
                    if (event == null)
                        System.err.println("hmmm... invalid usage ;(");
                    else
                        _propertyChanges.forEach(item -> item.propertyChange(new PropertyChangeEvent(_owner, propertyName, event.first, event.second)));
                });
            }
        }
    }

    private Map<String /* propertyName */, Field> _cachedFields = new HashMap<>();
    private Field findField(String propertyName) {
        Field field = _cachedFields.get(propertyName);
        if (field == null) {
            Optional<Field> opt = getPlainFields(_owner)
                .filter(fld -> fld.getName().equalsIgnoreCase(propertyName) ||
                        fld.getName().equalsIgnoreCase("_" + propertyName))
                .findAny();
            if (!opt.isPresent())
                throw new RuntimeException("Property '" + propertyName + "' not found");
            field = opt.get();
            field.setAccessible(true);
            _cachedFields.put(propertyName, field);
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

    private static <T> Stream<Field> getPlainFields(T obj) {
        return getDeclaredFields(obj)
                .filter(fld -> {
                    int m = fld.getModifiers();
                    return !Modifier.isTransient(m) &&
                           !Modifier.isStatic(m); });
    }

    public boolean isDisposed() { return _disposed; }

    @Override
    public void close() {
        _disposed = true;
        _cachedFields.clear();

        if (!_deferrNotifications.isEmpty())
            Logger.info("Not all deferred notifications handled! Count={0}", _deferrNotifications.size());
        _deferrNotifications.clear();

        if (!_propertyChanges.isEmpty())
            // strong unsubscribe
            throw new IllegalStateException("Illegal usage: Not all listeners were unsubscribed (type " + _owner.getClass().getName() + "): count=" + _propertyChanges.size());
        _propertyChanges.clear();
    }

}
