package net.pl3x.minimap.util;

import net.minecraft.util.math.MathHelper;

public class Mathf {
    public static final float PI = (float) Math.PI;
    public static final float SQRT_OF_2 = Mathf.sqrt(2F);
    public static final float DEG_TO_RAD = PI / 180F;

    public static float cosRads(float degree) {
        return MathHelper.cos(degree * DEG_TO_RAD);
    }

    public static float sinRads(float degree) {
        return MathHelper.sin(degree * DEG_TO_RAD);
    }

    public static float cos(float value) {
        return MathHelper.cos(value);
    }

    public static float pow(float value, float power) {
        return (float) Math.pow(value, power);
    }

    public static float sin(float value) {
        return MathHelper.sin(value);
    }

    public static float sqrt(float value) {
        return (float) Math.sqrt(value);
    }

    public static float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }

    public static float inverseLerp(float a, float b, float t) {
        return (t - a) / (b - a);
    }

    public static float clamp(float min, float max, float value) {
        return MathHelper.clamp(value, min, max);
    }
}
