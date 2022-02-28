package net.pl3x.minimap;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.font.Font;
import net.pl3x.minimap.gui.layer.Background;
import net.pl3x.minimap.gui.layer.BottomText;
import net.pl3x.minimap.gui.layer.Directions;
import net.pl3x.minimap.gui.layer.Frame;
import net.pl3x.minimap.gui.layer.Layer;
import net.pl3x.minimap.gui.layer.Map;
import net.pl3x.minimap.gui.layer.Mask;
import net.pl3x.minimap.gui.layer.Players;
import net.pl3x.minimap.gui.screen.widget.Sidebar;
import net.pl3x.minimap.gui.texture.Texture;
import net.pl3x.minimap.hardware.Monitor;
import net.pl3x.minimap.manager.ChunkScanner;
import net.pl3x.minimap.manager.FileManager;
import net.pl3x.minimap.manager.TileManager;
import net.pl3x.minimap.scheduler.Scheduler;
import net.pl3x.minimap.scheduler.Task;
import net.pl3x.minimap.util.Mathf;
import net.pl3x.minimap.util.Numbers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class MiniMap {
    public static final String MODID = "minimap";
    public static final int TILE_SIZE = 512;
    public static final MiniMap INSTANCE = new MiniMap();
    public static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    public static final Logger LOG = LogManager.getLogger("MiniMap");

    private final List<Layer> layers = new ArrayList<>();

    private ClientPlayerEntity player;
    private ClientWorld world;

    private Texture background = Texture.SKY_OVERWORLD;

    private boolean visible = true;
    private float size;
    private float deltaZoom;
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

    public float getDeltaZoom() {
        return this.deltaZoom;
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
        HudRenderCallback.EVENT.register(MiniMap.INSTANCE::render);
    }

    public void start() {
        if (!Config.getConfig().enabled) {
            return; // disabled
        }

        this.size = 0F;
        this.deltaZoom = 0F;
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

        this.layers.add(new Mask());
        this.layers.add(new Background());
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
            return true; //  no player
        }

        // don't render when debug hud is showing
        return CLIENT.options.debugEnabled;
    }

    public void render(MatrixStack matrixStack, float delta) {
        if (dontRender()) {
            return;
        }

        // smooth delta zoom
        int zoom = (int) (Mathf.clamp(0, 7, Config.getConfig().zoom) * 64 + 64);
        if (Math.abs(zoom - this.deltaZoom) > 0.01F) {
            this.deltaZoom += delta / 5F * (zoom - this.deltaZoom);
        } else {
            this.deltaZoom = zoom;
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
        this.layers.forEach(layer -> layer.render(matrixStack));

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
}
