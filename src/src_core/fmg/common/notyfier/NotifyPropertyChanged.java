package fmg.common.notyfier;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Stream;

import fmg.common.Pair;
import fmg.common.ui.Factory;

/** Notifies owner clients that a owner property value has changed */
public final class NotifyPropertyChanged implements AutoCloseable//, INotifyPropertyChanged
{
    private final INotifyPropertyChanged _owner;
    private final PropertyChangeSupport _propertyChanges;
    private final boolean _deferredNotifications;
    private final Map<String /* propertyName */, Pair<Object /* old value */, Object /* new value */>> _deferrNotifications = new HashMap<>();
    private boolean _disposed = false;
    private int _holded;

    public NotifyPropertyChanged(INotifyPropertyChanged owner) { this(owner, false); }
    public NotifyPropertyChanged(INotifyPropertyChanged owner, boolean deferredNotifications) {
        _owner = owner;
        _propertyChanges = new PropertyChangeSupport(_owner);
        _deferredNotifications = deferredNotifications;
    }

    public void addListener   (PropertyChangeListener listener) { _propertyChanges.   addPropertyChangeListener(listener); }
    public void removeListener(PropertyChangeListener listener) { _propertyChanges.removePropertyChangeListener(listener); }

    /** set notifer to pause */
    public AutoCloseable hold() {
        ++_holded; // lock
        return () -> --_holded; // unlock
    }
    public boolean isHolded() {
        return _holded != 0;
    }


    /** Set the value to the specified property  and throw event to listeners */
    public <T> boolean setProperty(T oldValue, T newValue, String propertyName) {
        if (isHolded())
            return false;
        if (_disposed) {
            if (newValue != null) {
                System.err.println("Illegal call property " + _owner.getClass().getCanonicalName() + "."+ propertyName + ": object already disposed!");
                return false;
            }
        }

        try {
            Field fld = findField(propertyName);
            fld.setAccessible(true);
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

        onPropertyChanged(oldValue, newValue, propertyName);
        return true;
    }

    protected final void onPropertyChanged(int oldValue, int newValue, String propertyName) {
        onPropertyChanged(Integer.valueOf(oldValue), Integer.valueOf(newValue), propertyName);
    }

    public final void onPropertyChanged(double oldValue, double newValue, String propertyName) {
        onPropertyChanged(Double.valueOf(oldValue), Double.valueOf(newValue), propertyName);
    }

    protected final void onPropertyChanged(boolean oldValue, boolean newValue, String propertyName) {
        onPropertyChanged(Boolean.valueOf(oldValue), Boolean.valueOf(newValue), propertyName);
    }

    public final void onPropertyChanged(String propertyName) {
        onPropertyChanged(null, null, propertyName);
    }

    public <T> void onPropertyChanged(T oldValue, T newValue, String propertyName) {
        if (_disposed || isHolded())
            return;

        if (!_deferredNotifications) {
            //System.out.println("onPropertyChanged: " + propertyName + ": " + newValue);
            _propertyChanges.firePropertyChange(propertyName, oldValue, newValue);
        } else {
            boolean shedule;
            {
                Pair<Object /* old value */, Object /* new value */> event = _deferrNotifications.get(propertyName);
                shedule = (event == null);
                event = new Pair<>((event == null) ? oldValue : event.first, newValue);
                _deferrNotifications.put(propertyName, event); // Re-save only the last event (with initial old value)
            }
            if (shedule) {
                /** /
                LoggerSimple.put("Deferr shedule:\n   " +
                        Stream.of(Thread.currentThread().getStackTrace())
                            .filter(x -> x.getClassName().startsWith("fmg."))
                            .map(x -> x.toString())
                          //.map(x -> x.substring(9))
                            .filter(x -> !x.contains(".lambda$"))
                          //.filter(x -> !x.contains(".onPropertyChanged("))
                            .collect(Collectors.joining("\n   ")));
                /**/
                Factory.DEFERR_INVOKER.accept(() -> {
                    if (_disposed || isHolded())
                        return;

                    Pair<Object /* old value */, Object /* new value */> event = _deferrNotifications.remove(propertyName);
                    if (event == null)
                        System.err.println("hmmm... invalid usage ;(");
                    else
                        _propertyChanges.firePropertyChange(propertyName, event.first, event.second);
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

    @Override
    public void close() {
        _disposed = true;
        _cachedFields.clear();
        _deferrNotifications.clear();
    }

}
