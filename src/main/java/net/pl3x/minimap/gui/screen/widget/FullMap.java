package net.pl3x.minimap.gui.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.gui.font.Font;
import net.pl3x.minimap.gui.screen.OverlayScreen;
import net.pl3x.minimap.gui.texture.Cursor;
import net.pl3x.minimap.gui.texture.Texture;
import net.pl3x.minimap.hardware.Monitor;
import net.pl3x.minimap.hardware.Mouse;
import org.lwjgl.opengl.GL11;

public class FullMap extends AnimatedWidget {
    public static final FullMap INSTANCE = new FullMap();

    private State state;

    private FullMap() {
        super(null, 0F, 0F, 0F, 0F);

        HudRenderCallback.EVENT.register((matrixStack, delta) ->
                render(matrixStack, MiniMap.CLIENT.getLastFrameDuration())
        );
    }

    @Override
    public float width() {
        return Monitor.width();
    }

    @Override
    public float height() {
        return Monitor.height();
    }

    @Override
    public void init() {
        Sidebar.INSTANCE.init();
    }

    private void render(MatrixStack matrixStack, float delta) {
        // quick check to see if we should be rendering
        if (this.closed() && Sidebar.INSTANCE.closed()) {
            // nope. lets save some cpu
            return;
        }
        //System.out.println("hmm " + this.state + " " + Sidebar.INSTANCE.state());

        // get mouse ready
        boolean useMouse = Mouse.INSTANCE.cursorEnabled() && MiniMap.CLIENT.currentScreen instanceof OverlayScreen;
        float mouseX, mouseY;

        // update mouse position before rendering anything
        if (useMouse) {
            // poll hardware for mouse position
            Mouse.INSTANCE.update();
            mouseX = Mouse.INSTANCE.x();
            mouseY = Mouse.INSTANCE.y();

            // set default cursor
            Mouse.INSTANCE.cursor(Cursor.HAND_OPEN);
        } else {
            mouseX = (float) MiniMap.CLIENT.mouse.getX();
            mouseY = (float) MiniMap.CLIENT.mouse.getY();
        }

        // setup opengl stuff
        matrixStack.push();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        // fixed scaling
        float scale = 1F / Monitor.scale();
        if (scale != 1F) {
            matrixStack.scale(scale, scale, scale);
        }

        // don't allow Mojang disable blending after drawing text
        Font.FIX_MOJANGS_TEXT_RENDERER_CRAP = true;

        // render everything
        FullMap.INSTANCE.render(matrixStack, mouseX, mouseY, delta);
        Sidebar.INSTANCE.render(matrixStack, mouseX, mouseY, delta);

        // allow Mojang disable blending after drawing text
        Font.FIX_MOJANGS_TEXT_RENDERER_CRAP = false;

        // render our mouse after everything is rendered
        if (useMouse) {
            // render mouse above everything else
            matrixStack.translate(0D, 0D, 10D);
            Mouse.INSTANCE.render(matrixStack, delta);
        }

        // clean up opengl stuff
        RenderSystem.disableBlend();
        matrixStack.pop();
    }

    @Override
    public void render(MatrixStack matrixStack, float mouseX, float mouseY, float delta) {
        if (closed()) {
            return;
        }

        super.render(matrixStack, mouseX, mouseY, delta);

        for (int x = 0; x < Monitor.width() + 512; x += 512) {
            for (int y = 0; y < Monitor.height() + 512; y += 512) {
                Texture.MINIMAP.draw(matrixStack, x, y, 512, 512);
            }
        }
    }

    @Override
    public void tick() {
        Sidebar.INSTANCE.tick();
    }

    public void open() {
        this.state = State.OPENED;
        Sidebar.INSTANCE.resetState();
    }

    public void close() {
        this.state = State.CLOSED;
        Sidebar.INSTANCE.close(true);
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY, int button) {
        return Sidebar.INSTANCE.mouseClicked(mouseX, mouseY, button)
                || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(float mouseX, float mouseY, int button) {
        return Sidebar.INSTANCE.mouseReleased(mouseX, mouseY, button)
                || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(float mouseX, float mouseY, int button, float deltaX, float deltaY) {
        return Sidebar.INSTANCE.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
                || super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return Sidebar.INSTANCE.keyPressed(keyCode, scanCode, modifiers)
                || super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean closed() {
        return this.state == State.CLOSED;
    }

    public enum State {
        CLOSED,
        OPENED
    }
}
