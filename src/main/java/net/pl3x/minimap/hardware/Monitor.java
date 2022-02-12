package net.pl3x.minimap.hardware;

import net.pl3x.minimap.MiniMap;

public class Monitor {
    public static long getId() {
        return MiniMap.CLIENT.getWindow().getHandle();
    }

    public static float scale() {
        return (float) MiniMap.CLIENT.getWindow().getScaleFactor();
    }

    public static float width() {
        return MiniMap.CLIENT.getWindow().getScaledWidth();
    }

    public static float height() {
        return MiniMap.CLIENT.getWindow().getScaledWidth();
    }
}
