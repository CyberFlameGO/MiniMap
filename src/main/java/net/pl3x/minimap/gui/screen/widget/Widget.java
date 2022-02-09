package net.pl3x.minimap.gui.screen.widget;

import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.MiniMap;

public abstract class Widget {
    private final Widget parent;

    private float x;
    private float y;

    private float width;
    private float height;

    private boolean hovered;
    private boolean wasHovered;

    public Widget(Widget parent, float x, float y, float width, float height) {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Widget parent() {
        return this.parent;
    }

    public void init() {
    }

    public float x() {
        return this.x;
    }

    public void x(float x) {
        this.x = x;
    }

    public float y() {
        return this.y;
    }

    public void y(float y) {
        this.y = y;
    }

    public float width() {
        return this.width;
    }

    public void width(float width) {
        this.width = width;
    }

    public float height() {
        return this.height;
    }

    public void height(float height) {
        this.height = height;
    }

    public boolean hovered() {
        return this.hovered;
    }

    public void render(MatrixStack matrixStack, float mouseX, float mouseY, float delta) {
        updateMouseState(mouseX, mouseY);
    }

    public void updateMouseState(float mouseX, float mouseY) {
        // check if mouse is hovering this widget
        this.hovered = mouseX >= x() && mouseX <= x() + width() && mouseY >= y() && mouseY <= y() + height();
        if (this.wasHovered != this.hovered && MiniMap.CLIENT.isWindowFocused()) {
            onHoverChange();
            this.wasHovered = this.hovered;
        }

        // check if mouse clicked this widget
    }

    public void onHoverChange() {
    }

    protected boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }
}
