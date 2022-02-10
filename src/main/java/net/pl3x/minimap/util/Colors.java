package net.pl3x.minimap.util;

import net.minecraft.util.math.MathHelper;

import java.awt.Color;

public class Colors {
    public static int shade(int color, int shade) {
        float ratio = switch (shade) {
            case 0 -> 180F / 0xFF;
            case 1 -> 220F / 0xFF;
            case 2 -> 255F / 0xFF;
            default -> throw new IllegalStateException("Unexpected shade: " + shade);
        };
        return shade(color, ratio);
    }

    public static int shade(int color, float ratio) {
        return argb(
                (int) (alpha(color) * ratio),
                (int) (red(color) * ratio),
                (int) (green(color) * ratio),
                (int) (blue(color) * ratio)
        );
    }

    public static int lerpARGB(float delta, int color0, int color1) {
        if (color0 == color1) return color0;
        if (delta >= 1F) return color1;
        if (delta <= 0F) return color0;
        return argb(
                (int) MathHelper.lerp(delta, alpha(color0), alpha(color1)),
                (int) MathHelper.lerp(delta, red(color0), red(color1)),
                (int) MathHelper.lerp(delta, green(color0), green(color1)),
                (int) MathHelper.lerp(delta, blue(color0), blue(color1))
        );
    }

    public static int lerpHSB(float delta, int color0, int color1) {
        float[] hsb0 = Color.RGBtoHSB(red(color0), green(color0), blue(color0), null);
        float[] hsb1 = Color.RGBtoHSB(red(color1), green(color1), blue(color1), null);
        return setAlpha(
                (int) MathHelper.lerp(delta, alpha(color0), alpha(color1)),
                Color.HSBtoRGB(
                        lerpShortestAngle(delta, hsb0[0], hsb1[0]),
                        MathHelper.lerp(delta, hsb0[1], hsb1[1]),
                        MathHelper.lerp(delta, hsb0[2], hsb1[2])
                )
        );
    }

    public static float lerpShortestAngle(float delta, float start, float end) {
        float distCW = (end >= start ? end - start : 1F - (start - end));
        float distCCW = (start >= end ? start - end : 1F - (end - start));
        float direction = (distCW <= distCCW ? distCW : -1F * distCCW);
        return (start + (direction * delta));
    }

    public static int alpha(int argb) {
        return argb >> 24 & 0xFF;
    }

    public static int red(int argb) {
        return argb >> 16 & 0xFF;
    }

    public static int green(int argb) {
        return argb >> 8 & 0xFF;
    }

    public static int blue(int argb) {
        return argb & 0xFF;
    }

    public static int argb(int alpha, int red, int green, int blue) {
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    public static int mix(int color0, int color1) {
        return argb(alpha(color0) * alpha(color1) / 0xFF, red(color0) * red(color1) / 0xFF, green(color0) * green(color1) / 0xFF, blue(color0) * blue(color1) / 0xFF);
    }

    public static int setAlpha(int alpha, int color) {
        return alpha << 24 | color & 0x00FFFFFF;
    }

    public static int fromHex(String color) {
        return (int) Long.parseLong(color.replace("#", ""), 16);
    }
}
