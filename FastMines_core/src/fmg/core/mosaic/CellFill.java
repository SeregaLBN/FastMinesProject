package fmg.core.mosaic;

import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import fmg.common.Color;
import fmg.common.notifier.INotifyPropertyChanged;
import fmg.common.notifier.NotifyPropertyChanged;
import fmg.core.types.Property;

/** all that apply to the background fill of cells */
public class CellFill implements AutoCloseable, INotifyPropertyChanged {

    public static final String PROPERTY_MODE = "Mode";

    /** режим заливки фона ячеек */
    @Property(PROPERTY_MODE)
    private int mode = 0;

    /** кэшированные цвета фона ячеек
     * <br/> Нет цвета? - создасться с нужной интенсивностью! */
    private final Map<Integer, Color> colors = new HashMap<>();

    protected NotifyPropertyChanged notifier = new NotifyPropertyChanged(this);

    /** режим заливки фона ячеек */
    public int getMode() { return mode; }

    /** режим заливки фона ячеек
     * @param newFillMode
     *  <li> 0 - цвет заливки фона по-умолчанию
     *  <li> not 0 - радуга %)
     */
    public void setMode(int newFillMode) {
        if (notifier.setProperty(mode, newFillMode, PROPERTY_MODE))
            colors.clear();
    }

    /** кэшированные цвета фона ячеек
     * <br/> Нет цвета? - создасться с нужной интенсивностью! */
    public Color getColor(int index) {
        Color color = colors.get(index);
        if (color == null) {
            color = Color.RandomColor().brighter(0.45);
            colors.put(index, color);
        }
        return color;
    }

    @Override
    public void close() {
        notifier.close();
        colors.clear();
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        notifier.addListener(listener);
    }
    @Override
    public void removeListener(PropertyChangeListener listener) {
        notifier.removeListener(listener);
    }

}
