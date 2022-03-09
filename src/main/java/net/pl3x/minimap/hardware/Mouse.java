package net.pl3x.minimap.hardware;

import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.gui.texture.Cursor;
import org.lwjgl.glfw.GLFW;

public class Mouse {
    public static final Mouse INSTANCE = new Mouse();

    private final double[] rawX = new double[1];
    private final double[] rawY = new double[1];

    private float mouseX;
    private float mouseY;

    private Cursor cursor;
    private boolean cursorEnabled;
    private boolean cursorRender;

    private boolean windowFocused;
    private boolean windowHovered;

    private Mouse() {
    }

    public float x() {
        return this.mouseX;
    }

    public float y() {
        return this.mouseY;
    }

    public Cursor cursor() {
        return this.cursor;
    }

    public void cursor(Cursor cursor) {
        this.cursor = cursor;
    }

    public boolean cursorEnabled() {
        return this.cursorEnabled;
    }

    public void cursorEnabled(boolean enabled) {
        this.cursorEnabled = enabled;
        visibility(enabled);
    }

    public void update() {
        GLFW.glfwGetCursorPos(Monitor.getId(), this.rawX, this.rawY);

        this.mouseX = (float) rawX[0];
        this.mouseY = (float) rawY[0];

        if (this.windowFocused != MiniMap.getClient().isWindowFocused()) {
            this.windowFocused = MiniMap.getClient().isWindowFocused();
            if (this.cursorEnabled) {
                visibility(this.windowFocused);
            }
        }

        this.windowHovered = x() >= 0F && x() < Monitor.width() && y() >= 0F && y() < Monitor.height();
    }

    public void render(MatrixStack matrixStack, float delta) {
        if (this.cursorEnabled && this.cursorRender && this.windowHovered) {
            matrixStack.push();
            // draw mouse above everything else
            matrixStack.translate(0, 0, 1900);
            cursor().draw(matrixStack, Mouse.INSTANCE.x(), Mouse.INSTANCE.y(), delta);
            matrixStack.pop();
        }
    }

    public void visibility(boolean cursorRender) {
        if (!cursorRender) {
            cursor(null);
        }
        this.cursorRender = cursorRender;
        GLFW.glfwSetInputMode(Monitor.getId(), GLFW.GLFW_CURSOR, cursorRender ? GLFW.GLFW_CURSOR_HIDDEN : GLFW.GLFW_CURSOR_NORMAL);
    }
}
