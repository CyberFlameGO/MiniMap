package net.pl3x.minimap.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.gui.font.Font;
import net.pl3x.minimap.gui.texture.Texture;
import net.pl3x.minimap.manager.ThreadManager;
import net.pl3x.minimap.scheduler.Scheduler;
import net.pl3x.minimap.scheduler.Task;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UpdateChecker {
    public static final UpdateChecker INSTANCE = new UpdateChecker();

    private static final String MODRINTH_URL = "https://api.modrinth.com/v2/project/%s/%s";

    private final String minecraftVersion;
    private final int currentVersion;

    private int latestVersion;
    private boolean hasUpdate;

    private long lastChecked = 0;

    private UpdateChecker() {
        ModContainer modContainer = FabricLoader.getInstance().getModContainer(MiniMap.MODID).orElse(null);
        if (modContainer == null) {
            throw new RuntimeException("Invalid ModID " + MiniMap.MODID);
        }
        String[] version = modContainer.getMetadata().getVersion().getFriendlyString().split("-");
        this.minecraftVersion = version[0];
        int current;
        try {
            current = Integer.parseInt(version[1]);
        } catch (NumberFormatException e) {
            current = -1;
        }
        this.currentVersion = current;

        Scheduler.INSTANCE.addTask(new Task(20, true) {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                if (lastChecked + 900000 > now) { // 15 minutes
                    return;
                }
                ThreadManager.INSTANCE.runAsync(() -> {
                    try {
                        checkForUpdates();
                    } catch (Exception e) {
                        MiniMap.LOG.warn("Error checking for updates");
                        latestVersion = Status.ERROR;
                    }
                }, ThreadManager.INSTANCE.getHttpIOExecutor());
            }
        });
    }

    public String getMinecraftVersion() {
        return this.minecraftVersion;
    }

    public int getCurrentVersion() {
        return this.currentVersion;
    }

    public int getLatestVersion() {
        return this.latestVersion;
    }

    public Boolean hasUpdate() {
        return this.hasUpdate;
    }

    public void checkForUpdates() {
        // reset latest version while we check
        this.latestVersion = Status.CHECKING;
        this.lastChecked = System.currentTimeMillis();

        String response = "{}";
        try {
            URIBuilder builder = new URIBuilder(String.format(MODRINTH_URL, MiniMap.MODID, "version"));
            builder.addParameter("loaders", String.format("[\"%s\"]", "fabric"));
            builder.addParameter("game_versions", String.format("[\"%s\"]", getMinecraftVersion()));
            response = IOUtils.toString(new URL(builder.toString()), StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }

        List<Integer> versions = new ArrayList<>();
        JsonArray json = JsonParser.parseString(response).getAsJsonArray();
        for (int i = 0; i < json.size(); i++) {
            JsonObject obj = json.get(i).getAsJsonObject();
            versions.add(Integer.parseInt(obj.get("version_number").getAsString().split("-")[1]));
        }

        Collections.sort(versions);
        Collections.reverse(versions);

        this.latestVersion = versions.get(0);

        this.hasUpdate = getCurrentVersion() > 0 && getLatestVersion() > 0 && getLatestVersion() - getCurrentVersion() > 0;

        if (hasUpdate()) {
            MiniMap.getClient().getToastManager().add(new UpdateToast());
        }
    }

    public static class Status {
        public static final int CHECKING = -1;
        public static final int ERROR = -2;
    }

    public class UpdateToast implements Toast {
        private boolean soundPlayed;

        @Override
        public Visibility draw(MatrixStack matrixStack, ToastManager manager, long startTime) {

            // draw toast background
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, Toast.TEXTURE);
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            manager.drawTexture(matrixStack, 0, 0, 0, 0, this.getWidth(), this.getHeight());

            // draw text
            Font.DEFAULT.drawCentered(matrixStack, "MiniMap Update Available", 92.5F, 11.5F, 0xFF55ff55);
            Font.DEFAULT.drawCentered(matrixStack, String.format("%s build %d", minecraftVersion, latestVersion), 92.5F, 22.5F, -1);

            // draw icon
            Texture.ICON.draw(matrixStack, 6, 6, 26, 26, 0, 0, 1, 1);

            // play update alert sound
            if (!this.soundPlayed && startTime > 0L) {
                this.soundPlayed = true;
                // todo get custom update alert sound
                MiniMap.getClient().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1F, 1F));
            }

            return startTime >= 5000L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
        }
    }
}
