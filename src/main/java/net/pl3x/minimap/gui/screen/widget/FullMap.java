package net.pl3x.minimap.gui.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.gui.GL;
import net.pl3x.minimap.gui.font.Font;
import net.pl3x.minimap.gui.screen.OverlayScreen;
import net.pl3x.minimap.gui.screen.widget.element.EntityMarker;
import net.pl3x.minimap.gui.texture.Cursor;
import net.pl3x.minimap.hardware.Monitor;
import net.pl3x.minimap.hardware.Mouse;
import net.pl3x.minimap.manager.TileManager;
import net.pl3x.minimap.tile.Tile;
import net.pl3x.minimap.util.Numbers;
import org.lwjgl.opengl.GL11;

public class FullMap extends AnimatedWidget {
    public static final FullMap INSTANCE = new FullMap();

    // todo - move this to a new layer
    private final EntityMarker playerMarker;

    private State state;

    private boolean dragging;
    private float offsetX;
    private float offsetY;
    private float panX;
    private float panY;

    private FullMap() {
        super(null, 0F, 0F, 0F, 0F);

        HudRenderCallback.EVENT.register((matrixStack, delta) ->
            render(matrixStack, MiniMap.CLIENT.getLastFrameDuration())
        );

        // todo - move this to a new layer
        this.playerMarker = new EntityMarker(this, null);
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
        boolean useMouse = Mouse.INSTANCE.cursorEnabled() && MiniMap.CLIENT.currentScreen instanceof OverlayScreen;
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

        // tick toasts over fullmap
        if (!Sidebar.INSTANCE.closed()) {
            MiniMap.CLIENT.getProfiler().swap("toasts");
            boolean hudHidden = MiniMap.CLIENT.options.hudHidden;
            MiniMap.CLIENT.options.hudHidden = false;
            MiniMap.CLIENT.getToastManager().draw(new MatrixStack());
            MiniMap.CLIENT.options.hudHidden = hudHidden;
            MiniMap.CLIENT.getProfiler().pop();
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

        if (MiniMap.INSTANCE.getBackground() != null) {
            MiniMap.INSTANCE.getBackground().draw(matrixStack, 0F, 0F, Monitor.width(), Monitor.height(), 0F, 0F, Monitor.width() / MiniMap.TILE_SIZE, Monitor.height() / MiniMap.TILE_SIZE);
        }

        float halfWidth = Monitor.width() / 2F;
        float halfHeight = Monitor.height() / 2F;

        float limitX = MiniMap.TILE_SIZE * (int) Math.ceil(halfWidth / MiniMap.TILE_SIZE);
        float limitZ = MiniMap.TILE_SIZE * (int) Math.ceil(halfHeight / MiniMap.TILE_SIZE);

        float centerX = (float) MiniMap.INSTANCE.getPlayer().getX() + this.offsetX;
        float centerZ = (float) MiniMap.INSTANCE.getPlayer().getZ() + this.offsetY;

        float tiledX = centerX - Numbers.regionToBlock(Numbers.blockToRegion(Math.round(centerX)));
        float tiledZ = centerZ - Numbers.regionToBlock(Numbers.blockToRegion(Math.round(centerZ)));

        for (float screenX = -limitX; screenX <= limitX; screenX += MiniMap.TILE_SIZE) {
            for (float screenZ = -limitZ; screenZ <= limitZ; screenZ += MiniMap.TILE_SIZE) {
                float x = screenX - tiledX;
                float z = screenZ - tiledZ;

                Tile tile = TileManager.INSTANCE.getTile(
                    MiniMap.INSTANCE.getWorld(),
                    Numbers.blockToRegion(Math.round(x + centerX)),
                    Numbers.blockToRegion(Math.round(z + centerZ)),
                    true
                );

                x = (float) Math.floor(x + halfWidth);
                z = (float) Math.floor(z + halfHeight);

                if (tile != null && tile.isReady()) {
                    tile.draw(matrixStack, x, z);
                }

                GL.drawSolidRect(matrixStack, x, 0, x + 1, Monitor.height(), 0xFFFF0000);
                GL.drawSolidRect(matrixStack, 0, z, Monitor.width(), z + 1, 0xFFFF0000);
                Font.DEFAULT.drawWithShadow(matrixStack, (int) Math.ceil(x - halfWidth + centerX) + "," + (int) Math.ceil(z - halfHeight + centerZ), x + 3, z + 3);
            }
        }

        // todo - move this to a new layer
        matrixStack.push();
        this.playerMarker.x(halfWidth - this.offsetX);
        this.playerMarker.y(halfHeight - this.offsetY);
        this.playerMarker.width(16);
        this.playerMarker.height(16);
        this.playerMarker.render(matrixStack, true, delta);
        Font.DEFAULT.drawWithShadow(matrixStack, Math.round(centerX - this.offsetX) + "," + Math.round(centerZ - this.offsetY), halfWidth - this.offsetX + 8, halfHeight - this.offsetY + 8);
        matrixStack.pop();
    }

    @Override
    public void tick() {
        Sidebar.INSTANCE.tick();
    }

    public void open() {
        this.offsetX = 0;
        this.offsetY = 0;
        this.panX = 0;
        this.panY = 0;
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
            this.offsetX -= mouseX - this.panX;
            this.offsetY -= mouseY - this.panY;
            this.panX = mouseX;
            this.panY = mouseY;
        }
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
