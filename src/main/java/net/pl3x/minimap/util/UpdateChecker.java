package net.pl3x.minimap.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.pl3x.minimap.MiniMap;
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
            private long lastChecked = 0;

            @Override
            public void run() {
                long now = System.currentTimeMillis();
                if (this.lastChecked + 900000 > now) {
                    return;
                }
                this.lastChecked = now;
                ThreadManager.INSTANCE.runAsync(() -> checkForUpdates(), ThreadManager.INSTANCE.getHttpIOExecutor());
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
        this.hasUpdate = getCurrentVersion() > 0 && getLatestVersion() - getCurrentVersion() > 0;

        System.out.println("latest: " + getLatestVersion());
        System.out.println("current: " + getCurrentVersion());
        System.out.println("diff: " + (getLatestVersion() - getCurrentVersion()));
        System.out.println("has update: " + hasUpdate());
    }
}
