package net.pl3x.minimap.gui.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.gui.screen.widget.Sidebar;
import net.pl3x.minimap.gui.texture.Cursor;
import net.pl3x.minimap.hardware.Mouse;
import org.lwjgl.glfw.GLFW;

public class FullMapScreen extends AbstractScreen {
    private final Sidebar sidebar;
    private final boolean debugEnabled;

    public FullMapScreen(Screen parent) {
        super(parent);
        this.sidebar = new Sidebar(this);

        // disable debug info overlay temporarily
        this.debugEnabled = MiniMap.CLIENT.options.debugEnabled;
        MiniMap.CLIENT.options.debugEnabled = false;

        // setup key listeners
        KEYBOARD.listen(GLFW.GLFW_KEY_PAGE_UP, () -> System.out.println("TODO zoom in full map"));
        KEYBOARD.listen(GLFW.GLFW_KEY_PAGE_DOWN, () -> System.out.println("TODO zoom out full map"));
    }

    @Override
    public void init() {
        super.init();
        this.sidebar.init();
    }

    @Override
    public void render(MatrixStack matrixStack, float mouseX, float mouseY, float delta) {
        this.sidebar.render(matrixStack, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        super.onClose();

        // put back debug info overlay
        MiniMap.CLIENT.options.debugEnabled = this.debugEnabled;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.sidebar.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }
}
