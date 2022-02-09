package net.pl3x.minimap;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.hardware.KeyboardGlobal;
import net.pl3x.minimap.manager.ChunkScanner;
import net.pl3x.minimap.manager.ResourceManager;
import net.pl3x.minimap.scheduler.Scheduler;

public class MiniMapClientInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if (Config.getConfig() == null) {
            new IllegalStateException("Could not load minimap configuration").printStackTrace();
            return;
        }

        Scheduler.INSTANCE.initialize();
        KeyboardGlobal.INSTANCE.initialize();
        ResourceManager.INSTANCE.initialize();
        ChunkScanner.INSTANCE.initialize();
        MiniMap.INSTANCE.initialize();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> MiniMap.INSTANCE.start());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> MiniMap.INSTANCE.stop());
    }
}
