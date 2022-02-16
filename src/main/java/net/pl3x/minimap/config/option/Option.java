package net.pl3x.minimap.config.option;

public abstract class Option<T> {
    public abstract T get();

    public abstract void set(T value);
}
