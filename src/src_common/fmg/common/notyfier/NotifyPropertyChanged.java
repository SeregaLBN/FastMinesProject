package fmg.common.notyfier;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Stream;

/** Notifies clients that a property value has changed */
public class NotifyPropertyChanged implements AutoCloseable, INotifyPropertyChanged {

   private PropertyChangeSupport propertyChanges = new PropertyChangeSupport(this);
   private boolean _disposed = false;
   private Object _owner;

   public NotifyPropertyChanged() { _owner = this; }
   public NotifyPropertyChanged(Object owner) { _owner = owner; }

   @Override
   public void addListener(PropertyChangeListener listener) { propertyChanges.addPropertyChangeListener(listener); }
   @Override
   public void removeListener(PropertyChangeListener listener) { propertyChanges.removePropertyChangeListener(listener); }

   @Deprecated // used reflection :(
   private <T> boolean setProperty(T newValue, String propertyName) {
      Object oldValue;
      try {
         Field fld = findField(propertyName);
         fld.setAccessible(true);
         oldValue = fld.get(_owner);
         if ((oldValue == null) && (newValue == null))
            return false;
         if ((oldValue != null) && oldValue.equals(newValue))
            return false;
         fld.set(_owner, newValue);
      } catch (Exception ex) {
         throw new RuntimeException(ex);
      }

      onPropertyChanged(oldValue, newValue, propertyName);
      return true;
   }

   /** Mark that the parameter is not used. */
   @Documented
   @Retention(RetentionPolicy.RUNTIME)
   @Target(value={ElementType.PARAMETER})
   @interface Unused { }

   public <T> boolean setProperty(@Unused T oldValue, T newValue, String propertyName) {
      return setProperty(newValue, propertyName);
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

   public void onPropertyChanged(Object oldValue, Object newValue, String propertyName) {
      if (_disposed)
         return;
    //System.out.println("onPropertyChanged: " + propertyName + ": " + newValue);
      propertyChanges.firePropertyChange(propertyName, oldValue, newValue);
   }

   public <TProperty> void onPropertyChangedRethrow(TProperty source, PropertyChangeEvent childEvent, String propertyName) {
      onPropertyChanged(null, source, propertyName);
      onPropertyChanged(childEvent.getOldValue(), childEvent.getNewValue(), propertyName + "." + childEvent.getPropertyName());
   }

   private Map<String /* propertyName */, Field> _cachedFields = new HashMap<>();
   private Field findField(String propertyName) {
      Field field = _cachedFields.get(propertyName);
      if (field == null) {
         field = getPlainFields(_owner)
            .filter(fld -> fld.getName().equalsIgnoreCase(propertyName) ||
                           fld.getName().equalsIgnoreCase("_" + propertyName))
            .findAny()
            .orElseThrow(()-> new RuntimeException("Property '" + propertyName + "' not found"));
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
   }

}
