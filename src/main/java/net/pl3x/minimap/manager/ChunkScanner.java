package net.pl3x.minimap.manager;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.minecraft.util.Identifier;

public class ChunkScanner {
    public static final ChunkScanner INSTANCE = new ChunkScanner();

    private ChunkScanner() {
    }

    public void initialize() {
        ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            int x = chunk.getPos().x;
            int z = chunk.getPos().z;
            Identifier key = world.getRegistryKey().getValue();

            // todo
        });

        ClientChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
            int x = chunk.getPos().x;
            int z = chunk.getPos().z;
            Identifier key = world.getRegistryKey().getValue();

            // todo
        });
    }
}
