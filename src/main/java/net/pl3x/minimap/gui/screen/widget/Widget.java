package net.pl3x.minimap.gui.screen.widget;

import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.MiniMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class Widget {
    private final Widget parent;

    private float x;
    private float y;

    private float width;
    private float height;

    private boolean hovered;
    private boolean wasHovered;
    private Widget focused;
    private boolean dragging;

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
        this.hovered = isMouseOver(mouseX, mouseY);
        if (this.wasHovered != this.hovered && MiniMap.getClient().isWindowFocused()) {
            onHoverChange();
            this.wasHovered = this.hovered;
        }

        children().forEach(widget -> widget.render(matrixStack, mouseX, mouseY, delta));
    }

    public void tick() {
    }

    public void onHoverChange() {
    }

    public boolean isMouseOver(float mouseX, float mouseY) {
        return mouseX >= x() && mouseX <= x() + width() && mouseY >= y() && mouseY <= y() + height();
    }

    public Optional<Widget> hoveredElement(float mouseX, float mouseY) {
        for (Widget widget : children()) {
            if (!widget.isMouseOver(mouseX, mouseY)) continue;
            return Optional.of(widget);
        }
        return Optional.empty();
    }

    public boolean mouseClicked(float mouseX, float mouseY, int button) {
        for (Widget widget : children()) {
            if (widget.mouseClicked(mouseX, mouseY, button)) {
                this.focused = widget;
                if (button == 0) {
                    this.dragging = true;
                }
                return true;
            }
        }
        return false;
    }

    public boolean mouseReleased(float mouseX, float mouseY, int button) {
        this.dragging = false;
        return this.hoveredElement(mouseX, mouseY).filter(widget -> widget.mouseReleased(mouseX, mouseY, button)).isPresent();
    }

    public boolean mouseDragged(float mouseX, float mouseY, int button, float deltaX, float deltaY) {
        if (button == 0 && this.dragging && this.focused != null) {
            return this.focused.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        return false;
    }

    public boolean mouseScrolled(float mouseX, float mouseY, float amount) {
        return this.hoveredElement(mouseX, mouseY).filter((widget) -> widget.mouseScrolled(mouseX, mouseY, amount)).isPresent();
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
