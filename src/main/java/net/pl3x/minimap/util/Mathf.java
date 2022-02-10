package net.pl3x.minimap.util;

public class Mathf {
    public static final float PI = (float) Math.PI;

    public static float cosRads(float degree) {
        return (float) java.lang.Math.cos(java.lang.Math.toRadians(degree));
    }

    public static float sinRads(float degree) {
        return (float) java.lang.Math.sin(java.lang.Math.toRadians(degree));
    }

    public static float cos(float value) {
        return (float) java.lang.Math.cos(value);
    }

    public static float pow(float value, float power) {
        return (float) java.lang.Math.pow(value, power);
    }

    public static float sin(float value) {
        return (float) java.lang.Math.sin(value);
    }

    public static float sqrt(float value) {
        return (float) java.lang.Math.sqrt(value);
    }
}
