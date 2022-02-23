package net.pl3x.minimap.manager;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.mixin.MinecraftServerAccess;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FileManager {
    public static final FileManager INSTANCE = new FileManager();

    private final Map<Identifier, Path> worldDirs = new HashMap<>();

    public final Path configDir;
    public final Path configFile;
    public final Path dataDir;

    public Path tilesDir;

    private FileManager() {
        this.configDir = FabricLoader.getInstance().getConfigDir();
        this.configFile = resolve(configDir, MiniMap.MODID + ".json");
        this.dataDir = resolve(configDir, MiniMap.MODID);
    }

    public void start() {
        this.worldDirs.clear();
        this.tilesDir = null;

        if (MiniMap.CLIENT.isInSingleplayer()) {
            MinecraftServerAccess server = (MinecraftServerAccess) MiniMap.CLIENT.getServer();
            if (server == null) {
                throw new RuntimeException("Cannot obtain single player world name");
            }
            this.tilesDir = resolve(this.dataDir, "singleplayer/" + server.getSession().getDirectoryName());
        } else {
            ServerInfo server = MiniMap.CLIENT.getCurrentServerEntry();
            if (server == null) {
                throw new RuntimeException("Cannot obtain multiplayer server ip address");
            }
            this.tilesDir = resolve(this.dataDir, "multiplayer/" + server.address);
        }
    }

    public Path getWorldDir(World world) {
        return getWorldDir(world.getRegistryKey().getValue());
    }

    public Path getWorldDir(Identifier identifier) {
        Path path = this.worldDirs.get(identifier);
        if (path == null) {
            path = resolve(this.tilesDir, identifier.toString());
            this.worldDirs.put(identifier, path);
        }
        return path;
    }

    private Path resolve(Path path, String value) {
        String sanitized = value
                .replace(":", "-")
                .toLowerCase(Locale.ROOT);
        return path.resolve(sanitized);
    }
}
