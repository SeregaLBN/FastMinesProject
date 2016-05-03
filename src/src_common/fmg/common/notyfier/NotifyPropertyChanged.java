package fmg.common.notyfier;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public abstract class NotifyPropertyChanged // implements INotifyPropertyChanged
{
   private PropertyChangeSupport propertyChanges = new PropertyChangeSupport(this);
   public void addListener(PropertyChangeListener l) { propertyChanges.addPropertyChangeListener(l); }
   public void removePropertyChangeListener(PropertyChangeListener l) { propertyChanges.removePropertyChangeListener(l); }

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

      onPropertyChanged(oldValue, newValue, propertyName);
      return true;
   }

   protected void onPropertyChanged(int oldValue, int newValue, String propertyName) {
      onPropertyChanged(Integer.valueOf(oldValue), Integer.valueOf(newValue), propertyName);
   }

   protected void onPropertyChanged(boolean oldValue, boolean newValue, String propertyName) {
      onPropertyChanged(Boolean.valueOf(oldValue), Boolean.valueOf(newValue), propertyName);
   }

   protected void onPropertyChanged(String propertyName) {
      onPropertyChanged(null, null, propertyName);
   }

   protected void onPropertyChanged(Object oldValue, Object newValue, String propertyName) {
    //System.out.println("onPropertyChanged: " + propertyName + ": " + newValue);
      propertyChanges.firePropertyChange(propertyName, oldValue, newValue);
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

}
