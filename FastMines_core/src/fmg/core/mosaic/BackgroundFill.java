package fmg.core.mosaic;

import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import fmg.common.Color;
import fmg.common.notifier.INotifyPropertyChanged;
import fmg.common.notifier.NotifyPropertyChanged;

/** all that apply to the background fill of cells */
public class BackgroundFill implements AutoCloseable, INotifyPropertyChanged {

    /** режим заливки фона ячеек */
    private int _mode = 0;

    /** кэшированные цвета фона ячеек
     * <br/> Нет цвета? - создасться с нужной интенсивностью! */
    private final Map<Integer, Color> _colors = new HashMap<Integer, Color>() {
        private static final long serialVersionUID = 1L;
        @Override
        public Color get(Object key) {
            assert key instanceof Integer;
            Color res = super.get(key);
            if (res == null) {
                res = Color.RandomColor().brighter(0.45);
                super.put((Integer)key, res);
            }
            return res;
        }
     };

    public static final String PROPERTY_MODE = "Mode";
    protected NotifyPropertyChanged _notifier = new NotifyPropertyChanged(this);

    /** режим заливки фона ячеек */
    public int getMode() { return _mode; }

    /** режим заливки фона ячеек
     * @param newFillMode
     *  <li> 0 - цвет заливки фона по-умолчанию
     *  <li> not 0 - радуга %)
     */
    public void setMode(int newFillMode) {
        if (_notifier.setProperty(_mode, newFillMode, PROPERTY_MODE))
            _colors.clear();
    }

    /** кэшированные цвета фона ячеек
     * <br/> Нет цвета? - создасться с нужной интенсивностью! */
    public Map<Integer, Color> getColors() {
        return _colors;
    }

    @Override
    public void close() {
        _notifier.close();
        _colors.clear();
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        _notifier.addListener(listener);
    }
    @Override
    public void removeListener(PropertyChangeListener listener) {
        _notifier.removeListener(listener);
    }

}
