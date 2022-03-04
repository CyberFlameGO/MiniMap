package net.pl3x.minimap.gui.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.network.ClientPlayerEntity;
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

        float scale = 1F;

        float width = Monitor.width();//MiniMap.INSTANCE.getSize();
        float height = Monitor.height();//MiniMap.INSTANCE.getSize();
        float halfWidth = width / 2F;
        float halfHeight = height / 2F;

        float centerX = Math.round(Monitor.width() / 2F);//MiniMap.INSTANCE.getCenterX();
        float centerZ = Math.round(Monitor.height() / 2F);//MiniMap.INSTANCE.getCenterZ();

        ClientPlayerEntity player = MiniMap.INSTANCE.getPlayer();
        float playerPosX = (float) player.getX();
        float playerPosZ = (float) player.getZ();
        int playerBlockX = player.getBlockX();
        int playerBlockZ = player.getBlockZ();

        // blend mode to only draw alpha levels
        RenderSystem.blendFuncSeparate(GL11.GL_ZERO, GL11.GL_ONE, GL11.GL_SRC_COLOR, GL11.GL_ZERO);
        // draw low alpha on all pixels (will hide anything we draw)
        GL.drawSolidRect(matrixStack, 0, 0, Monitor.width(), Monitor.height(), 0x01 << 24);

        // translate everything to position
        matrixStack.push();
        matrixStack.translate(centerX, centerZ, 0F);

        // draw high alpha square/circle where we want to draw the map (the part that will show)
        GL.drawSolidRect(matrixStack, -halfWidth, -halfHeight, halfWidth, halfHeight, 0xFF << 24);
        //GL.drawSolidCirc(matrixStack, 0, 0, halfWidth, 0xFF << 24);

        // blend mode to only writes where high alpha values exist
        RenderSystem.blendFunc(GL11.GL_DST_ALPHA, GL11.GL_ONE_MINUS_DST_ALPHA);

        // draw background/sky
        if (MiniMap.INSTANCE.getBackground() != null) {
            MiniMap.INSTANCE.getBackground().draw(matrixStack, -halfWidth, -halfHeight, halfWidth, halfHeight, 0F, 0F, width / Tile.SIZE, height / Tile.SIZE);
        }

        // translate tiles to player position
        matrixStack.push();
        matrixStack.translate(-playerPosX * scale, -playerPosZ * scale, 0F);
        //GL.rotateScene(matrixStack, playerPosX * scale, playerPosZ * scale, -MiniMap.INSTANCE.getAngle());
        matrixStack.scale(scale, scale, 1F);

        // draw tiles
        int regionX, regionZ;
        for (float screenX = -halfWidth; Numbers.regionToBlock((regionX = Numbers.blockToRegion((int) (screenX + playerBlockX)))) < halfWidth + playerPosX; screenX += Tile.SIZE) {
            for (float screenZ = -halfHeight; Numbers.regionToBlock((regionZ = Numbers.blockToRegion((int) (screenZ + playerBlockZ)))) < halfHeight + playerPosZ; screenZ += Tile.SIZE) {
                Tile tile = TileManager.INSTANCE.getTile(MiniMap.INSTANCE.getWorld(), regionX, regionZ, true);
                if (tile != null && tile.isReady()) {
                    tile.draw(matrixStack, Numbers.regionToBlock(regionX), Numbers.regionToBlock(regionZ));
                }
            }
        }

        // finished translating tiles to offset
        matrixStack.pop();

        // blend mode for full translucent pixel support
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // todo - move this to a new layer
        matrixStack.push();
        this.playerMarker.x(-this.offsetX);
        this.playerMarker.y(-this.offsetY);
        this.playerMarker.width(16);
        this.playerMarker.height(16);
        this.playerMarker.render(matrixStack, true, delta);
        matrixStack.pop();

        // finished translating everything to center of monitor
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
