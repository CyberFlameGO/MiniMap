package net.pl3x.minimap.gui.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.MiniMap;
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
    private int centerX;
    private int centerZ;

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

        // tick toasts over fullmap
        if (!Sidebar.INSTANCE.closed()) {
            MiniMap.CLIENT.getProfiler().swap("toasts");
            MiniMap.CLIENT.options.hudHidden = false;
            MiniMap.CLIENT.getToastManager().draw(new MatrixStack());
            MiniMap.CLIENT.options.hudHidden = true;
            MiniMap.CLIENT.getProfiler().pop();
        }

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

        if (MiniMap.INSTANCE.getBackground() != null) {
            RenderSystem.setShaderColor(1F, 1F, 1F, 0.95F);
            MiniMap.INSTANCE.getBackground().draw(matrixStack, 0F, 0F, Monitor.width(), Monitor.height(), 0F, 0F, Monitor.width() / MiniMap.TILE_SIZE, Monitor.height() / MiniMap.TILE_SIZE);
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        }

        int halfWidth = Math.round(Monitor.width() / 2F);
        int halfHeight = Math.round(Monitor.height() / 2F);

        int limitX = MiniMap.TILE_SIZE * (int) Math.ceil((float) halfWidth / MiniMap.TILE_SIZE);
        int limitZ = MiniMap.TILE_SIZE * (int) Math.ceil((float) halfHeight / MiniMap.TILE_SIZE);

        for (int screenX = -limitX; screenX <= limitX; screenX += MiniMap.TILE_SIZE) {
            for (int screenZ = -limitZ; screenZ <= limitZ; screenZ += MiniMap.TILE_SIZE) {
                int blockX = screenX - (this.centerX & (MiniMap.TILE_SIZE - 1));
                int blockZ = screenZ - (this.centerZ & (MiniMap.TILE_SIZE - 1));

                Tile tile = TileManager.INSTANCE.getTile(MiniMap.INSTANCE.getWorld(), Numbers.blockToRegion(blockX + this.centerX), Numbers.blockToRegion(blockZ + this.centerZ), true);
                if (tile != null && tile.isReady()) {
                    tile.draw(matrixStack, blockX + halfWidth, blockZ + halfHeight);
                }
            }
        }

        // todo - move this to a new layer
        matrixStack.push();
        this.playerMarker.x(halfWidth);
        this.playerMarker.y(halfHeight);
        this.playerMarker.width(16);
        this.playerMarker.height(16);
        this.playerMarker.render(matrixStack, true, delta);
        matrixStack.pop();
    }

    @Override
    public void tick() {
        Sidebar.INSTANCE.tick();
    }

    public void open() {
        this.state = State.OPENED;
        Sidebar.INSTANCE.resetState();
        this.centerX = MiniMap.INSTANCE.getPlayer().getBlockX();
        this.centerZ = MiniMap.INSTANCE.getPlayer().getBlockZ();
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
