package net.pl3x.minimap.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.config.Lang;
import net.pl3x.minimap.gui.GL;
import net.pl3x.minimap.gui.font.Font;
import net.pl3x.minimap.gui.texture.Cursor;
import net.pl3x.minimap.hardware.KeyboardScreen;
import net.pl3x.minimap.hardware.Mouse;
import org.lwjgl.opengl.GL11;

public abstract class AbstractScreen extends Screen {
    protected static final KeyboardScreen KEYBOARD = new KeyboardScreen();

    protected final Screen parent;

    public AbstractScreen(Screen parent) {
        super(Lang.TITLE);
        this.parent = parent;
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
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        // update our own mouse positions
        Mouse.INSTANCE.update();
        Mouse.INSTANCE.cursor(Cursor.ARROW);

        // fix scaling
        float screenScaled = 1.0F / GL.scale();

        // setup opengl stuff
        matrixStack.push();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        matrixStack.scale(screenScaled, screenScaled, screenScaled);

        // don't allow Mojang disable blending after drawing text
        Font.ALLOW_DISABLE_BLENDING_AFTER_DRAWING_TEXT = false;

        // render everything
        this.render(matrixStack, Mouse.INSTANCE.x(), Mouse.INSTANCE.y(), delta);

        // render our own mouse
        Mouse.INSTANCE.render(matrixStack, delta);

        // allow Mojang disable blending after drawing text
        Font.ALLOW_DISABLE_BLENDING_AFTER_DRAWING_TEXT = true;

        // clean up opengl stuff
        RenderSystem.disableBlend();
        matrixStack.pop();
    }

    public void render(MatrixStack matrixStack, float mouseX, float mouseY, float delta) {
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
    }

    public void openScreen(Screen screen) {
        MiniMap.CLIENT.setScreen(screen);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return KEYBOARD.isListening(keyCode) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return super.charTyped(chr, modifiers);
    }
}
