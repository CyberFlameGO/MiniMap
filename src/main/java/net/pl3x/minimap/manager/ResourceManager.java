package net.pl3x.minimap.manager;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.font.Font;
import net.pl3x.minimap.gui.texture.Cursor;
import net.pl3x.minimap.gui.texture.Texture;
import net.pl3x.minimap.sound.Sound;
import net.pl3x.minimap.util.UpdateChecker;

public class ResourceManager {
    public static final ResourceManager INSTANCE = new ResourceManager();

    private ResourceManager() {
    }

    public void initialize() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
            new SimpleSynchronousResourceReloadListener() {
                @Override
                public Identifier getFabricId() {
                    return new Identifier(MiniMap.MODID, "assets");
                }

                @Override
                public void reload(net.minecraft.resource.ResourceManager manager) {
                    Config.reload();
                    Texture.initialize();
                    Cursor.initialize();
                    Font.initialize();
                    Sound.initialize();
                    MiniMap.LOG.info("Version " + UpdateChecker.INSTANCE.getCurrentVersion() + " loaded.");
                }
            }
        );
    }
}
