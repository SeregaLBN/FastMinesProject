package fmg.android.app.presentation;

import java.beans.PropertyChangeListener;

import fmg.common.notifier.INotifyPropertyChanged;
import fmg.common.notifier.NotifyPropertyChanged;

/** Main menu save data */
public class MenuSettings implements INotifyPropertyChanged, AutoCloseable {

    public static final boolean DEFAULT_SPLIT_PANE_OPEN = true;

    private boolean splitPaneOpen = DEFAULT_SPLIT_PANE_OPEN;

    protected final NotifyPropertyChanged notifier = new NotifyPropertyChanged(this, true);

    public static final String PROPERTY_SPLIT_PANE_OPEN = "SplitPaneOpen";

    public void copyFrom(MenuSettings from) {
        setSplitPaneOpen(from.isSplitPaneOpen());
    }

    public boolean isSplitPaneOpen() {
        return splitPaneOpen;
    }

    public void setSplitPaneOpen(boolean splitPaneOpen) {
        notifier.setProperty(this.splitPaneOpen, splitPaneOpen, PROPERTY_SPLIT_PANE_OPEN);
    }

    @Override
    public void close() {
        notifier.close();
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
