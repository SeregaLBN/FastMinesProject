package fmg.common.notyfier;

import java.beans.PropertyChangeListener;

/** Notification of property changes */
public interface INotifyPropertyChanged {

   void addListener(PropertyChangeListener l);
   void removeListener(PropertyChangeListener l);

}
