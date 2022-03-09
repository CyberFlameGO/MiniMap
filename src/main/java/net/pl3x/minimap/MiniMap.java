package net.pl3x.minimap;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.GL;
import net.pl3x.minimap.gui.font.Font;
import net.pl3x.minimap.gui.screen.widget.Radar;
import net.pl3x.minimap.gui.screen.widget.Sidebar;
import net.pl3x.minimap.gui.texture.Texture;
import net.pl3x.minimap.hardware.Keyboard;
import net.pl3x.minimap.hardware.Monitor;
import net.pl3x.minimap.manager.ChunkScanner;
import net.pl3x.minimap.manager.FileManager;
import net.pl3x.minimap.manager.ResourceManager;
import net.pl3x.minimap.manager.TileManager;
import net.pl3x.minimap.scheduler.Scheduler;
import net.pl3x.minimap.scheduler.Task;
import net.pl3x.minimap.tile.Tile;
import net.pl3x.minimap.util.Biomes;
import net.pl3x.minimap.util.Clock;
import net.pl3x.minimap.util.Mathf;
import net.pl3x.minimap.util.Numbers;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class MiniMap implements ClientModInitializer {
    public static final String MODID = "minimap";
    public static final Logger LOG = LogManager.getLogger("MiniMap");

    public static MiniMap INSTANCE;

    public static MinecraftClient getClient() {
        return MinecraftClient.getInstance();
    }

    public MiniMap() {
        INSTANCE = this;
    }

    private ClientWorld world;

    private Texture background;
    private final List<Text> bottomText = new ArrayList<>();

    private Radar radar;

    private boolean visible = true;
    private float size;

    private float angle;
    private float centerX;
    private float centerY;
    private float deltaZoom;

    private float lastWidth;
    private float lastHeight;

    private Task tickTask;
    private long tick;

    @Override
    public void onInitializeClient() {
        if (Config.getConfig() == null) {
            new IllegalStateException("Could not load minimap configuration").printStackTrace();
            return;
        }

        Scheduler.INSTANCE.initialize();
        ResourceManager.INSTANCE.initialize();
        Keyboard.INSTANCE.initialize();

        HudRenderCallback.EVENT.register((matrixStack, delta) -> render(matrixStack, getClient().getLastFrameDuration()));

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> start());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> stop());
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

        this.radar = new Radar();

        this.tickTask = Scheduler.INSTANCE.addTask(0, true, this::tick);
    }

    public void stop() {
        if (this.radar != null) {
            this.radar = null;
        }

        if (this.tickTask != null) {
            this.tickTask.cancel();
            this.tickTask = null;
        }

        ChunkScanner.INSTANCE.stop();
        TileManager.INSTANCE.stop();

        Sidebar.INSTANCE.close(true);
    }

    public ClientPlayerEntity getPlayer() {
        return getClient().player;
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

    public boolean dontRender() {
        if (!isVisible()) {
            return true; // hidden
        }

        if (getPlayer() == null) {
            return true; // no player
        }

        // don't render when debug hud is showing
        return getClient().options.debugEnabled;
    }

    public void render(MatrixStack matrixStack, float delta) {
        if (dontRender()) {
            return;
        }

        // angle of player rotation
        this.angle = Numbers.normalizeDegrees(getPlayer().getYaw(delta));

        // calculate delta zoom
        this.deltaZoom = calcZoom(Config.getConfig().zoom, this.deltaZoom, delta);

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

        drawLayers(matrixStack, delta);

        // allow Mojang disable blending after drawing text
        Font.FIX_MOJANGS_TEXT_RENDERER_CRAP = false;

        // clean up opengl stuff
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        matrixStack.pop();
    }

    public void drawLayers(MatrixStack matrixStack, float delta) {
        // draw map
        drawMap(
            matrixStack,
            0F, 0F,
            getSize(),
            getSize(),
            getCenterX(),
            getCenterY(),
            0F,
            0F,
            Config.getConfig().circular,
            this.deltaZoom,
            Config.getConfig().northLocked ? 0 : getAngle(),
            delta
        );

        // use a blend that supports translucent pixels
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // draw radar markers
        if (this.radar != null) {
            this.radar.render(matrixStack, delta);
        }

        // draw frame
        if (Config.getConfig().showFrame) {
            (Config.getConfig().circular ? Texture.FRAME_CIRCLE : Texture.FRAME_SQUARE)
                .draw(matrixStack, getCenterX() - getSize() / 2F, getCenterY() - getSize() / 2F, getSize(), getSize());
        }

        // draw directions
        if (Config.getConfig().showDirections) {
            float angle = Config.getConfig().northLocked ? 0 : getAngle();
            float distance = getSize() / 2F + Font.DEFAULT.height() / 2F;
            if (!Config.getConfig().circular && !Config.getConfig().northLocked && angle != 0F) {
                distance /= Mathf.cosRads(45F - Math.abs(45F + (-Math.abs(angle) % 90F)));
            }

            matrixStack.push();
            matrixStack.translate(distance * Mathf.sinRads(angle + 180F), distance * Mathf.cosRads(angle + 180F), 0D);
            Font.DEFAULT.drawCenteredWithShadow(matrixStack, "N", getCenterX(), getCenterY(), 0xFFFFFF | (Config.getConfig().opacity << 24));
            matrixStack.pop();
            matrixStack.push();
            matrixStack.translate(distance * Mathf.sinRads(angle + 90F), distance * Mathf.cosRads(angle + 90F), 0D);
            Font.DEFAULT.drawCenteredWithShadow(matrixStack, "E", getCenterX(), getCenterY(), 0xFFFFFF | (Config.getConfig().opacity << 24));
            matrixStack.pop();
            matrixStack.push();
            matrixStack.translate(distance * Mathf.sinRads(angle), distance * Mathf.cosRads(angle), 0D);
            Font.DEFAULT.drawCenteredWithShadow(matrixStack, "S", getCenterX(), getCenterY(), 0xFFFFFF | (Config.getConfig().opacity << 24));
            matrixStack.pop();
            matrixStack.push();
            matrixStack.translate(distance * Mathf.sinRads(angle - 90F), distance * Mathf.cosRads(angle - 90F), 0D);
            Font.DEFAULT.drawCenteredWithShadow(matrixStack, "W", getCenterX(), getCenterY(), 0xFFFFFF | (Config.getConfig().opacity << 24));
            matrixStack.pop();
        }

        // draw bottom text
        if (!this.bottomText.isEmpty()) {
            float y = getCenterY() + getSize() / 2F + Font.DEFAULT.height() * 2F;
            int color = 0xFFFFFF | (Config.getConfig().opacity << 24);
            int i = 0;

            matrixStack.push();
            for (Text text : this.bottomText) {
                Font.DEFAULT.drawCenteredWithShadow(matrixStack, text, getCenterX(), y + Font.DEFAULT.height() * i++, color);
            }
            matrixStack.pop();
        }
    }

    public void tick() {
        if (dontRender()) {
            return;
        }
        if (this.tick++ >= Config.getConfig().updateInterval) {
            updateBackground();
            updateBottomText();
            this.tick = 0L;
        }
        updateWindow();

        if (this.radar != null) {
            this.radar.update();
        }
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

        net.minecraft.client.util.Monitor monitor = getClient().getWindow().getMonitor();
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

    public void updateBackground() {
        RegistryKey<World> key = getPlayer().world.getRegistryKey();
        if (key == World.OVERWORLD) {
            setBackground(Texture.SKY_OVERWORLD);
        } else if (key == World.NETHER) {
            setBackground(Texture.SKY_THE_NETHER);
        } else if (key == World.END) {
            setBackground(Texture.SKY_THE_END);
        } else {
            setBackground(Texture.SKY_OVERWORLD);
        }
    }

    public void updateBottomText() {
        this.bottomText.clear();
        String[] lines = Config.getConfig().bottomText.split("\n");
        for (String line : lines) {
            if (StringUtils.isBlank(line)) {
                continue;
            }
            this.bottomText.add(new TranslatableText(line
                .replace("{x}", Integer.toString(getPlayer().getBlockX()))
                .replace("{y}", Integer.toString(getPlayer().getBlockY()))
                .replace("{z}", Integer.toString(getPlayer().getBlockZ()))
                .replace("{biome}", Biomes.INSTANCE.getBiomeName(getPlayer()))
                .replace("{clock}", Clock.INSTANCE.getTime(getWorld()))));
        }
    }
}
