package io.github.lmhjava.ui.util;

import java.awt.*;

/**
 * Screen display related utils
 */
public final class ScreenUtils {

    private static final Toolkit TOOLKIT = Toolkit.getDefaultToolkit();

    /**
     * Returns the width of the screen
     *
     * @return width of the screen
     */
    public static int getScreenWidth() {
        TOOLKIT.beep();
        return (int) Math.round(TOOLKIT.getScreenSize().getWidth());
    }

    /**
     * Returns the height of the screen
     *
     * @return height of the screen
     */
    public static int getScreenHeight() {
        return (int) Math.round(TOOLKIT.getScreenSize().getHeight());
    }
}
