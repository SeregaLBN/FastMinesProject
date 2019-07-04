package fmg.swing.utils;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Toolkit;

public final class ScreenResolutionHelper {
    private ScreenResolutionHelper() {}

    /** get current display size */
    public static Dimension getScreenSize() {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }

    public static Insets getScreenPadding(GraphicsConfiguration gc) {
        return Toolkit.getDefaultToolkit().getScreenInsets(gc);
    }

    public static Dimension getDesktopSize(GraphicsConfiguration gc) {
        Dimension screenSize = getScreenSize();
        Insets screenPadding = getScreenPadding(gc);
        return new Dimension(
            screenSize.width - (screenPadding.left + screenPadding.right),
            screenSize.height - (screenPadding.top + screenPadding.bottom));
    }

}
