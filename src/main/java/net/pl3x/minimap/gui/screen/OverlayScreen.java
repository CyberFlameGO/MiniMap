package net.pl3x.minimap.gui.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.gui.screen.widget.FullMap;
import net.pl3x.minimap.hardware.Monitor;
import net.pl3x.minimap.hardware.Mouse;

public class OverlayScreen extends Screen {
    protected final Screen parent;

    private boolean alreadyInitialized;
    private boolean debugEnabled;

    public OverlayScreen(Screen parent) {
        super(Text.of(""));
        this.parent = parent;

        // don't do anything here. ModMenu creates a new instance
        // if this class when the list of mods is opened, and it
        // really screws with the states of fullmap and sidebar.
        // moved logic down into init() with !alreadyInitialized
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    @Override
    public void init() {
        if (!alreadyInitialized) {
            this.alreadyInitialized = true;

            // disable debug info overlay temporarily
            this.debugEnabled = MiniMap.CLIENT.options.debugEnabled;
            MiniMap.CLIENT.options.debugEnabled = false;

            // open fullmap and reset all states
            FullMap.INSTANCE.open();
        }

        MiniMap.INSTANCE.setVisible(false);
        MiniMap.CLIENT.options.hudHidden = true;

        this.width = (int) (width() * Monitor.scale());
        this.height = (int) (height() * Monitor.scale());

        Mouse.INSTANCE.cursorEnabled(true);

        FullMap.INSTANCE.init();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        // let overlays handle rendering
    }

    @Override
    public void tick() {
        FullMap.INSTANCE.tick();
    }

    @Override
    public void removed() {
        MiniMap.INSTANCE.setVisible(true);
        MiniMap.CLIENT.options.hudHidden = false;
        Mouse.INSTANCE.cursorEnabled(false);
    }

    @Override
    public void onClose() {
        MiniMap.CLIENT.setScreen(this.parent);

        FullMap.INSTANCE.close();

        // put back debug info overlay
        MiniMap.CLIENT.options.debugEnabled = this.debugEnabled;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return FullMap.INSTANCE.mouseClicked((float) mouseX * Monitor.scale(), (float) mouseY * Monitor.scale(), button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return FullMap.INSTANCE.mouseReleased((float) mouseX * Monitor.scale(), (float) mouseY * Monitor.scale(), button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return FullMap.INSTANCE.mouseDragged((float) mouseX * Monitor.scale(), (float) mouseY * Monitor.scale(), button, (float) deltaX, (float) deltaY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return FullMap.INSTANCE.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }
}
