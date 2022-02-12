package net.pl3x.minimap.gui.screen.widget;

import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.MiniMap;

import java.util.ArrayList;
import java.util.List;

public abstract class Widget {
    private final Widget parent;

    private float x;
    private float y;

    private float width;
    private float height;

    private boolean hovered;
    private boolean wasHovered;

    private final List<Widget> children = new ArrayList<>();

    public Widget(Widget parent, float x, float y, float width, float height) {
        this.parent = parent;
        x(x);
        y(y);
        width(width);
        height(height);
    }

    public Widget parent() {
        return this.parent;
    }

    public void init() {
        children().forEach(Widget::init);
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

    public List<Widget> children() {
        return this.children;
    }

    public void render(MatrixStack matrixStack, float mouseX, float mouseY, float delta) {
        // check if mouse is hovering this widget
        this.hovered = mouseX >= x() && mouseX <= x() + width() && mouseY >= y() && mouseY <= y() + height();
        if (this.wasHovered != this.hovered && MiniMap.CLIENT.isWindowFocused()) {
            onHoverChange();
            this.wasHovered = this.hovered;
        }

        children().forEach(widget -> widget.render(matrixStack, mouseX, mouseY, delta));
    }

    public void tick() {
    }

    public void onHoverChange() {
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Widget widget : children()) {
            if (widget.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (Widget widget : children()) {
            if (widget.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return false;
    }
}
