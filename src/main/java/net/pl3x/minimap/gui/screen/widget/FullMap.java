package net.pl3x.minimap.gui.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.gui.font.Font;
import net.pl3x.minimap.gui.screen.OverlayScreen;
import net.pl3x.minimap.gui.texture.Cursor;
import net.pl3x.minimap.hardware.Monitor;
import net.pl3x.minimap.hardware.Mouse;
import org.lwjgl.opengl.GL11;

public class FullMap extends AnimatedWidget {
    public static final FullMap INSTANCE = new FullMap();

    private State state;

    private boolean dragging;
    private float offsetX;
    private float offsetZ;
    private float panX;
    private float panY;

    private int zoom;
    private float deltaZoom;
    private float zoomCenterX;
    private float zoomCenterZ;

    private FullMap() {
        super(null, 0F, 0F, 0F, 0F);

        HudRenderCallback.EVENT.register((matrixStack, delta) ->
            render(matrixStack, MiniMap.getClient().getLastFrameDuration())
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

        // get mouse ready
        boolean useMouse = Mouse.INSTANCE.cursorEnabled() && MiniMap.getClient().currentScreen instanceof OverlayScreen;
        float mouseX, mouseY;

        // update mouse position before rendering anything
        if (useMouse) {
            // poll hardware for mouse position
            Mouse.INSTANCE.update();
            mouseX = Mouse.INSTANCE.x();
            mouseY = Mouse.INSTANCE.y();

            // set default cursor
            Mouse.INSTANCE.cursor(this.dragging ? Cursor.HAND_GRAB : Cursor.HAND_OPEN);
        } else {
            mouseX = (float) MiniMap.getClient().mouse.getX();
            mouseY = (float) MiniMap.getClient().mouse.getY();
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

        // tick toasts over fullmap
        if (!Sidebar.INSTANCE.closed()) {
            MiniMap.getClient().getProfiler().swap("toasts");
            boolean hudHidden = MiniMap.getClient().options.hudHidden;
            MiniMap.getClient().options.hudHidden = false;
            MiniMap.getClient().getToastManager().draw(new MatrixStack());
            MiniMap.getClient().options.hudHidden = hudHidden;
            MiniMap.getClient().getProfiler().pop();
        }

        // render our mouse after everything is rendered
        if (useMouse) {
            Mouse.INSTANCE.render(matrixStack, delta);
        }

        // allow Mojang disable blending after drawing text
        Font.FIX_MOJANGS_TEXT_RENDERER_CRAP = false;

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

        this.deltaZoom = MiniMap.INSTANCE.calcZoom(this.zoom, this.deltaZoom, delta);

        MiniMap.INSTANCE.drawMap(
            matrixStack,
            this.zoomCenterX, this.zoomCenterZ,
            Monitor.width(), Monitor.height(),
            Math.round(Monitor.width() / 2F), Math.round(Monitor.height() / 2F),
            this.offsetX, this.offsetZ,
            false,
            this.deltaZoom,
            0F,
            delta
        );
    }

    @Override
    public void tick() {
        Sidebar.INSTANCE.tick();
    }

    public void open() {
        this.zoom = 4;
        this.deltaZoom = 10F;
        this.zoomCenterX = 0F;
        this.zoomCenterZ = 0F;
        this.offsetX = 0F;
        this.offsetZ = 0F;
        this.panX = 0F;
        this.panY = 0F;
        this.state = State.OPENED;
        Sidebar.INSTANCE.resetState();
    }

    public void close() {
        this.state = State.CLOSED;
        Sidebar.INSTANCE.close(true);
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY, int button) {
        if (!Sidebar.INSTANCE.hovered()) {
            this.dragging = true;
            this.panX = mouseX;
            this.panY = mouseY;
        }
        return Sidebar.INSTANCE.mouseClicked(mouseX, mouseY, button)
            || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(float mouseX, float mouseY, int button) {
        this.dragging = false;
        this.panX = 0;
        this.panY = 0;
        return Sidebar.INSTANCE.mouseReleased(mouseX, mouseY, button)
            || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(float mouseX, float mouseY, int button, float deltaX, float deltaY) {
        if (!Sidebar.INSTANCE.hovered() && this.dragging) {
            this.offsetX -= (mouseX - this.panX) / MiniMap.INSTANCE.calcZoom(this.zoom, this.deltaZoom, 1F);
            this.offsetZ -= (mouseY - this.panY) / MiniMap.INSTANCE.calcZoom(this.zoom, this.deltaZoom, 1F);
            this.panX = mouseX;
            this.panY = mouseY;
        }
        return Sidebar.INSTANCE.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
            || super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(float mouseX, float mouseY, float amount) {
        if (!Sidebar.INSTANCE.hovered() && !this.dragging) {
            int centerX = Math.round(Monitor.width() / 2F);
            int centerZ = Math.round(Monitor.height() / 2F);
            this.zoomCenterX = mouseX - centerX;
            this.zoomCenterZ = mouseY - centerZ;
            if (amount > 0) {
                if (this.zoom < 10) {
                    this.zoom++;
                }
            } else {
                if (this.zoom > 0) {
                    this.zoom--;
                }
            }
        }
        return Sidebar.INSTANCE.mouseScrolled(mouseX, mouseY, amount)
            || super.mouseScrolled(mouseX, mouseY, amount);
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
