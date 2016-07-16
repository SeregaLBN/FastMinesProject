package fmg.common.notyfier;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/** Notifies clients that a property value has changed */
public abstract class NotifyPropertyChanged implements AutoCloseable // implements INotifyPropertyChanged
{
   private PropertyChangeSupport propertyChanges = new PropertyChangeSupport(this);
   private boolean _disposed = false;

   public void addListener(PropertyChangeListener l) { propertyChanges.addPropertyChangeListener(l); }
   public void removeListener(PropertyChangeListener l) { propertyChanges.removePropertyChangeListener(l); }

   @Deprecated // used reflection :(
   protected <T> boolean setProperty(T newValue, String propertyName) {
      Object oldValue;
      try {
         Field fld = findField(propertyName);
         fld.setAccessible(true);
         oldValue = fld.get(this);
         if ((oldValue == null) && (newValue == null))
            return false;
         if ((oldValue != null) && oldValue.equals(newValue))
            return false;
         fld.set(this, newValue);
      } catch (Exception ex) {
         throw new RuntimeException(ex);
      }

      onSelfPropertyChanged(oldValue, newValue, propertyName);
      return true;
   }

   protected final void onSelfPropertyChanged(int oldValue, int newValue, String propertyName) {
      onSelfPropertyChanged(Integer.valueOf(oldValue), Integer.valueOf(newValue), propertyName);
   }

   protected final void onSelfPropertyChanged(boolean oldValue, boolean newValue, String propertyName) {
      onSelfPropertyChanged(Boolean.valueOf(oldValue), Boolean.valueOf(newValue), propertyName);
   }

   protected final void onSelfPropertyChanged(String propertyName) {
      onSelfPropertyChanged(null, null, propertyName);
   }

   protected void onSelfPropertyChanged(Object oldValue, Object newValue, String propertyName) {
      if (_disposed)
         return;
    //System.out.println("onSelfPropertyChanged: " + propertyName + ": " + newValue);
      propertyChanges.firePropertyChange(propertyName, oldValue, newValue);
   }

   protected <TProperty> void onSelfPropertyChangedRethrow(TProperty source, PropertyChangeEvent ev, String propertyName) {
      onSelfPropertyChanged(null, source, propertyName);
      onSelfPropertyChanged(ev.getOldValue(), ev.getNewValue(), propertyName + "." + ev.getPropertyName());
   }

   private Field findField(String propertyName) {
      return getPlainFields(this)
         .filter(fld -> fld.getName().equalsIgnoreCase(propertyName) ||
                        fld.getName().equalsIgnoreCase("_" + propertyName))
         .findAny()
         .orElseThrow(()-> new RuntimeException("Property '" + propertyName + "' not found"));
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
   }

}
