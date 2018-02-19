package fmg.common.notyfier;

import java.beans.PropertyChangeListener;

public interface INotifyPropertyChanged {

   void addListener(PropertyChangeListener l);
   void removeListener(PropertyChangeListener l);

}
