package net.pl3x.minimap.gui.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.gui.GL;
import net.pl3x.minimap.gui.screen.widget.Sidebar;
import net.pl3x.minimap.hardware.Mouse;

public class OverlayScreen extends Screen {
    protected final Screen parent;

    private final boolean debugEnabled;

    public OverlayScreen(Screen parent) {
        super(Text.of(""));
        this.parent = parent;

        // disable debug info overlay temporarily
        this.debugEnabled = MiniMap.CLIENT.options.debugEnabled;
        MiniMap.CLIENT.options.debugEnabled = false;

        // reset sidebar state
        Sidebar.INSTANCE.resetState();
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    @Override
    public void init() {
        MiniMap.INSTANCE.visible = false;
        MiniMap.CLIENT.options.hudHidden = true;

        this.width = (int) (this.width * GL.scale());
        this.height = (int) (this.height * GL.scale());

        Mouse.INSTANCE.useCursor(true);

        Sidebar.INSTANCE.init();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        // let overlays handle rendering
    }

    @Override
    public void tick() {
        Sidebar.INSTANCE.tick();
    }

    @Override
    public void removed() {
        MiniMap.INSTANCE.visible = true;
        MiniMap.CLIENT.options.hudHidden = false;
        Mouse.INSTANCE.useCursor(false);
    }

    @Override
    public void onClose() {
        MiniMap.CLIENT.setScreen(this.parent);

        Sidebar.INSTANCE.close(true);

        // put back debug info overlay
        MiniMap.CLIENT.options.debugEnabled = this.debugEnabled;
    }

    public void openScreen(Screen screen) {
        MiniMap.CLIENT.setScreen(screen);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return Sidebar.INSTANCE.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return Sidebar.INSTANCE.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }
}
