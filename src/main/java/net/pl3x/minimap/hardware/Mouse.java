package net.pl3x.minimap.hardware;

import net.minecraft.client.util.Window;
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

    private final Window window;
    private boolean windowFocused;
    private boolean windowHovered;

    private Mouse() {
        this.window = MiniMap.CLIENT.getWindow();
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

    public boolean useCursor() {
        return this.cursorEnabled;
    }

    public void useCursor(boolean useCursor) {
        this.cursorEnabled = useCursor;
        visibility(useCursor);
    }

    public void update() {
        GLFW.glfwGetCursorPos(MiniMap.CLIENT.getWindow().getHandle(), this.rawX, this.rawY);

        this.mouseX = (float) rawX[0];
        this.mouseY = (float) rawY[0];

        if (this.windowFocused != MiniMap.CLIENT.isWindowFocused()) {
            this.windowFocused = MiniMap.CLIENT.isWindowFocused();
            if (this.cursorEnabled) {
                visibility(this.windowFocused);
            }
        }

        this.windowHovered = x() >= 0 && x() < this.window.getWidth() && y() >= 0 && y() < this.window.getHeight();
    }

    public void render(MatrixStack matrixStack, float delta) {
        if (this.cursorEnabled && this.cursorRender && this.windowHovered) {
            cursor().animate(matrixStack, delta);
        }
    }

    public void visibility(boolean cursorRender) {
        if (!cursorRender) {
            cursor(null);
        }
        this.cursorRender = cursorRender;
        GLFW.glfwSetInputMode(this.window.getHandle(), GLFW.GLFW_CURSOR, cursorRender ? GLFW.GLFW_CURSOR_HIDDEN : GLFW.GLFW_CURSOR_NORMAL);
    }
}
