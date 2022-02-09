package net.pl3x.minimap.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.gui.animation.Easing;
import net.pl3x.minimap.manager.FileManager;
import net.pl3x.minimap.util.Anchor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

@SuppressWarnings("CanBeFinal")
public class Config {
    public boolean enabled = true;
    public boolean northLocked = false;
    public boolean circular = true;
    public boolean showFrame = true;
    public boolean showDirections = true;

    public String bottomText = "{biome}\n{clock}\n{x}, {z}\n{y}";

    public boolean clockRealTime = false;
    public String clockFormat = "hh:mm a";

    public Anchor anchorX = Anchor.HIGH;
    public Anchor anchorY = Anchor.LOW;
    public int anchorOffsetX = -75;
    public int anchorOffsetY = 75;

    public int size = 128;
    public int zoom = 3;

    public int updateInterval = 5;

    public Animations animations = new Animations();

    private static final Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .serializeNulls()
            .setLenient()
            .setPrettyPrinting()
            .enableComplexMapKeySerialization()
            .registerTypeAdapter(Easing.Func.class, new Easing.Adapter())
            .create();

    private static Config config;

    public static Config getConfig() {
        if (config == null) {
            reload();
        }
        return config;
    }

    public static void reload() {
        try (Reader reader = new BufferedReader(new FileReader(FileManager.INSTANCE.configFile.toFile()))) {
            config = gson.fromJson(reader, Config.class);
            MiniMap.LOG.info("Loaded existing config");
        } catch (IOException e) {
            config = new Config();
            MiniMap.LOG.info("Loaded new config");
        }
        save();
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(FileManager.INSTANCE.configFile.toFile())) {
            gson.toJson(config, writer);
            writer.flush();
            MiniMap.LOG.info("Saved config");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
