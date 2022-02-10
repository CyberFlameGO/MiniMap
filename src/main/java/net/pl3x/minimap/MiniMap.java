package net.pl3x.minimap;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.Monitor;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.GL;
import net.pl3x.minimap.gui.font.Font;
import net.pl3x.minimap.gui.layer.Background;
import net.pl3x.minimap.gui.layer.BottomText;
import net.pl3x.minimap.gui.layer.Directions;
import net.pl3x.minimap.gui.layer.Frame;
import net.pl3x.minimap.gui.layer.Layer;
import net.pl3x.minimap.gui.layer.Map;
import net.pl3x.minimap.gui.layer.Mask;
import net.pl3x.minimap.gui.layer.Players;
import net.pl3x.minimap.manager.FileManager;
import net.pl3x.minimap.manager.TileManager;
import net.pl3x.minimap.scheduler.Scheduler;
import net.pl3x.minimap.scheduler.Task;
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

    public ClientPlayerEntity player;

    public boolean visible = true;
    public float scaleFactor;
    public float size;
    public float deltaZoom;
    public float angle;
    public float centerX;
    public float centerY;
    private int lastWidth;
    private int lastHeight;

    private Task tickTask;
    private long tick;

    private MiniMap() {
    }

    public void initialize() {
        HudRenderCallback.EVENT.register(MiniMap.INSTANCE::render);
    }

    public void start() {
        if (!Config.getConfig().enabled) {
            return; // disabled
        }

        this.scaleFactor = 0F;
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

        TileManager.INSTANCE.stop();
        FileManager.INSTANCE.stop();

        this.layers.clear();
    }

    public boolean canRender() {
        if (!this.visible) {
            return false; // hidden
        }

        this.player = CLIENT.player;
        if (this.player == null) {
            return false; //  no player
        }

        // don't render when debug hud is showing
        return !CLIENT.options.debugEnabled;
    }

    public void render(MatrixStack matrixStack, float delta) {
        if (!canRender()) {
            return;
        }

        // smooth delta zoom
        int zoom = Math.min(Math.max(Config.getConfig().zoom, 0), 7) * 64 + 64;
        if (Math.abs(zoom - this.deltaZoom) > 0.01F) {
            this.deltaZoom += delta / 5F * (zoom - this.deltaZoom);
        } else {
            this.deltaZoom = zoom;
        }

        // angle of player rotation
        this.angle = (this.player.getYaw(delta) - 180F) % 360F;

        // setup opengl stuff
        matrixStack.push();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        // don't allow Mojang disable blending after drawing text
        Font.ALLOW_DISABLE_BLENDING_AFTER_DRAWING_TEXT = false;

        // render layers
        this.layers.forEach(layer -> layer.render(matrixStack));

        // allow Mojang disable blending after drawing text
        Font.ALLOW_DISABLE_BLENDING_AFTER_DRAWING_TEXT = true;

        // clean up opengl stuff
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        matrixStack.pop();
    }

    public void tick() {
        if (this.tick++ >= Config.getConfig().updateInterval) {
            this.layers.forEach(Layer::update);
            this.tick = 0L;
        }
        updateWindow();
    }

    public void updateWindow() {
        int width = CLIENT.getWindow().getScaledWidth();
        int height = CLIENT.getWindow().getScaledHeight();
        float scaleFactor = GL.scale() / 2F; // todo - replace this with something better

        if (this.lastWidth == width && this.lastHeight == height && this.scaleFactor == scaleFactor) {
            return; // nothing changed
        }

        this.lastWidth = width;
        this.lastHeight = height;
        this.scaleFactor = scaleFactor;

        this.size = Config.getConfig().size / this.scaleFactor;
        float scale = 1F;

        Monitor monitor = CLIENT.getWindow().getMonitor();
        if (monitor != null) {
            float windowHeight = CLIENT.getWindow().getHeight();
            float monitorHeight = monitor.getCurrentVideoMode().getHeight();
            scale = MathHelper.clamp(windowHeight / monitorHeight / 0.9F, 0.5F, 1F);
            this.size = size * scale;
        }

        this.centerX = (int) switch (Config.getConfig().anchorX) {
            case LOW -> 0F;
            case MID -> width / 2F;
            case HIGH -> width;
        } + Config.getConfig().anchorOffsetX * scale / this.scaleFactor;

        this.centerY = (int) switch (Config.getConfig().anchorY) {
            case LOW -> 0F;
            case MID -> height / 2F;
            case HIGH -> height;
        } + Config.getConfig().anchorOffsetY * scale / this.scaleFactor;
    }
}
