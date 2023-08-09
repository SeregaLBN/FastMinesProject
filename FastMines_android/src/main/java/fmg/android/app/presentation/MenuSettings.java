package fmg.android.app.presentation;

import java.util.function.Consumer;

/** Main menu model */
public class MenuSettings implements AutoCloseable {

    public static final boolean DEFAULT_SPLIT_PANE_OPEN = true;

    public static final String PROPERTY_SPLIT_PANE_OPEN = "SplitPaneOpen";

    private boolean splitPaneOpen = DEFAULT_SPLIT_PANE_OPEN;

    private Consumer<String> changedCallback;

    public boolean isSplitPaneOpen() {
        return splitPaneOpen;
    }

    public void setSplitPaneOpen(boolean splitPaneOpen) {
        if (this.splitPaneOpen == splitPaneOpen)
            return;
        this.splitPaneOpen = splitPaneOpen;
        if (changedCallback != null)
            changedCallback.accept(PROPERTY_SPLIT_PANE_OPEN);
    }

    @Override
    public void close() {
        changedCallback = null;
    }

    public void setListener(Consumer<String> callback) {
        if ((callback != null) && (changedCallback != null))
            throw new IllegalArgumentException("Can only set the controller once");
        changedCallback = callback;
    }

}
