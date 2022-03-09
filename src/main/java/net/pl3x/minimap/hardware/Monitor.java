package net.pl3x.minimap.hardware;

import net.pl3x.minimap.MiniMap;

public class Monitor {
    public static long getId() {
        return MiniMap.getClient().getWindow().getHandle();
    }

    public static float scale() {
        return (float) MiniMap.getClient().getWindow().getScaleFactor();
    }

    public static float width() {
        return MiniMap.getClient().getWindow().getWidth();
    }

    public static float height() {
        return MiniMap.getClient().getWindow().getHeight();
    }
}
