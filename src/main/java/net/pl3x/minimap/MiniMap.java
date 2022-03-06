package net.pl3x.minimap;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.GL;
import net.pl3x.minimap.gui.font.Font;
import net.pl3x.minimap.gui.layer.BottomText;
import net.pl3x.minimap.gui.layer.Directions;
import net.pl3x.minimap.gui.layer.Frame;
import net.pl3x.minimap.gui.layer.Layer;
import net.pl3x.minimap.gui.layer.Map;
import net.pl3x.minimap.gui.layer.Players;
import net.pl3x.minimap.gui.screen.widget.Sidebar;
import net.pl3x.minimap.gui.texture.Texture;
import net.pl3x.minimap.hardware.Monitor;
import net.pl3x.minimap.manager.ChunkScanner;
import net.pl3x.minimap.manager.FileManager;
import net.pl3x.minimap.manager.TileManager;
import net.pl3x.minimap.scheduler.Scheduler;
import net.pl3x.minimap.scheduler.Task;
import net.pl3x.minimap.tile.Tile;
import net.pl3x.minimap.util.Mathf;
import net.pl3x.minimap.util.Numbers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class MiniMap {
    public static final String MODID = "minimap";
    public static final MiniMap INSTANCE = new MiniMap();
    public static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    public static final Logger LOG = LogManager.getLogger("MiniMap");

    private final List<Layer> layers = new ArrayList<>();

    private ClientPlayerEntity player;
    private ClientWorld world;

    private Texture background = Texture.SKY_OVERWORLD;

    private boolean visible = true;
    private float size;

    private float angle;
    private float centerX;
    private float centerY;

    private float lastWidth;
    private float lastHeight;

    private Task tickTask;
    private long tick;

    public MiniMap() {
    }

    public List<Layer> getLayers() {
        return this.layers;
    }

    public ClientPlayerEntity getPlayer() {
        return this.player;
    }

    public ClientWorld getWorld() {
        return this.world;
    }

    public void setWorld(ClientWorld world) {
        this.world = world;
    }

    public Texture getBackground() {
        return this.background;
    }

    public void setBackground(Texture texture) {
        this.background = texture;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public float getSize() {
        return this.size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public float getAngle() {
        return this.angle;
    }

    public float getCenterX() {
        return this.centerX;
    }

    public void setCenterX(float x) {
        this.centerX = x;
    }

    public float getCenterY() {
        return this.centerY;
    }

    public void setCenterY(float y) {
        this.centerY = y;
    }

    public void initialize() {
        HudRenderCallback.EVENT.register((matrixStack, delta) ->
            render(matrixStack, CLIENT.getLastFrameDuration())
        );
    }

    public void start() {
        if (!Config.getConfig().enabled) {
            return; // disabled
        }

        this.size = 0F;
        this.angle = 0F;
        this.centerX = 0F;
        this.centerY = 0F;
        this.lastWidth = 0;
        this.lastHeight = 0;
        this.tick = 0L;

        updateWindow();

        FileManager.INSTANCE.start();
        TileManager.INSTANCE.start();
        ChunkScanner.INSTANCE.start();

        this.layers.add(new Map());
        this.layers.add(new Frame());
        this.layers.add(new Players());
        this.layers.add(new Directions());
        this.layers.add(new BottomText());

        this.tickTask = Scheduler.INSTANCE.addTask(0, true, this::tick);
    }

    public void stop() {
        if (this.tickTask != null) {
            this.tickTask.cancel();
            this.tickTask = null;
        }

        this.layers.forEach(Layer::stop);

        ChunkScanner.INSTANCE.stop();
        TileManager.INSTANCE.stop();

        Sidebar.INSTANCE.close(true);

        this.layers.clear();
    }

    public boolean dontRender() {
        if (!isVisible()) {
            return true; // hidden
        }

        this.player = CLIENT.player;
        if (this.player == null) {
            return true; // no player
        }

        // don't render when debug hud is showing
        return CLIENT.options.debugEnabled;
    }

    public void render(MatrixStack matrixStack, float delta) {
        if (dontRender()) {
            return;
        }

        // angle of player rotation
        this.angle = Numbers.normalizeDegrees(this.player.getYaw(delta));

        // setup opengl stuff
        matrixStack.push();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        // fixed scaling
        float scale = 1F / Monitor.scale();
        if (scale != 1F) {
            matrixStack.scale(scale, scale, scale);
        }

        // don't allow Mojang disable blending after drawing text
        Font.FIX_MOJANGS_TEXT_RENDERER_CRAP = true;

        // render layers
        this.layers.forEach(layer -> layer.render(matrixStack, delta));

        // allow Mojang disable blending after drawing text
        Font.FIX_MOJANGS_TEXT_RENDERER_CRAP = false;

        // clean up opengl stuff
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        matrixStack.pop();
    }

    public void tick() {
        if (dontRender()) {
            return;
        }
        if (this.tick++ >= Config.getConfig().updateInterval) {
            this.layers.forEach(Layer::update);
            this.tick = 0L;
        }
        updateWindow();
    }

    public void updateWindow() {
        if (this.lastWidth == Monitor.width() && this.lastHeight == Monitor.height()) {
            return; // nothing changed
        }

        this.lastWidth = Monitor.width();
        this.lastHeight = Monitor.height();

        updateCenter(updateSize());
    }

    private float updateSize() {
        this.size = Config.getConfig().size;
        float scale = Monitor.scale();

        net.minecraft.client.util.Monitor monitor = CLIENT.getWindow().getMonitor();
        if (monitor != null) {
            float monitorHeight = monitor.getCurrentVideoMode().getHeight();
            scale *= Mathf.clamp(0.5F, 1F, Monitor.height() / monitorHeight / 0.9F);
            this.size *= scale;
        }

        return scale;
    }

    private void updateCenter(float scale) {
        this.centerX = (int) switch (Config.getConfig().anchorX) {
            case LOW -> 0F;
            case MID -> Monitor.width() / 2F;
            case HIGH -> Monitor.width();
        } + Config.getConfig().anchorOffsetX * scale;

        this.centerY = (int) switch (Config.getConfig().anchorY) {
            case LOW -> 0F;
            case MID -> Monitor.height() / 2F;
            case HIGH -> Monitor.height();
        } + Config.getConfig().anchorOffsetY * scale;
    }

    public void drawMap(MatrixStack matrixStack, float mouseX, float mouseY, float width, float height, float centerX, float centerZ, float offsetX, float offsetZ, boolean circular, float zoom, float angle, float delta) {
        float halfWidth = width / 2F;
        float halfHeight = height / 2F;
        float playerX = (float) getPlayer().getX();
        float playerZ = (float) getPlayer().getZ();

        // blend mode to only draw alpha levels
        RenderSystem.blendFuncSeparate(GL11.GL_ZERO, GL11.GL_ONE, GL11.GL_SRC_COLOR, GL11.GL_ZERO);

        // draw low alpha on all pixels (will hide anything we draw)
        GL.drawSolidRect(matrixStack, 0, 0, Monitor.width(), Monitor.height(), 0x01 << 24);

        // translate everything to position
        matrixStack.push();
        matrixStack.translate(centerX, centerZ, 0F);

        // draw high alpha square/circle where we want to draw the map (the part that will show)
        if (circular) {
            GL.drawSolidCirc(matrixStack, 0, 0, halfWidth, 0xFF << 24);
        } else {
            GL.drawSolidRect(matrixStack, -halfWidth, -halfHeight, +halfWidth, +halfHeight, 0xFF << 24);
        }

        // blend mode to only writes where high alpha values exist
        RenderSystem.blendFunc(GL11.GL_DST_ALPHA, GL11.GL_ONE_MINUS_DST_ALPHA);

        // draw background/sky
        if (getBackground() != null) {
            getBackground().draw(matrixStack, -halfWidth, -halfHeight, +halfWidth, +halfHeight, 0F, 0F, width / Tile.SIZE, height / Tile.SIZE);
        }

        // scale map to emulate zoom
        matrixStack.scale(zoom, zoom, 1F);

        // translate tiles to player position and drag offset
        matrixStack.translate(-playerX - offsetX, -playerZ - offsetZ, 0F);

        // rotate map to player angle
        if (angle != 0F) {
            GL.rotateScene(matrixStack, playerX, playerZ, -angle);
        }

        // draw tiles
        int regionX, regionZ;
        for (float screenX = -halfWidth + offsetX; Numbers.regionToBlock((regionX = Numbers.blockToRegion((int) (screenX + getPlayer().getBlockX())))) < halfWidth + playerX + offsetX; screenX += Tile.SIZE) {
            for (float screenZ = -halfHeight + offsetZ; Numbers.regionToBlock((regionZ = Numbers.blockToRegion((int) (screenZ + getPlayer().getBlockZ())))) < halfHeight + playerZ + offsetZ; screenZ += Tile.SIZE) {
                Tile tile = TileManager.INSTANCE.getTile(getWorld(), regionX, regionZ, true);
                if (tile != null && tile.isReady()) {
                    tile.draw(matrixStack, Numbers.regionToBlock(regionX), Numbers.regionToBlock(regionZ));
                }
            }
        }

        // put blend mode back to full translucent pixel support
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

        matrixStack.pop();
    }

    public float calcZoom(int zoom, float deltaZoom, float delta) {
        // todo use Ease functions instead of this mess
        float realZoom = (float) (Math.pow(2, MathHelper.clamp(zoom, 0, 10) / 4F));
        if (Math.abs(realZoom - deltaZoom) > 0.01F) {
            deltaZoom += delta / 2.5F * (realZoom - deltaZoom);
        } else {
            deltaZoom = realZoom;
        }
        return deltaZoom;
    }
}
