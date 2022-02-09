package net.pl3x.minimap.util;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class Biomes {
    public static final Biomes INSTANCE = new Biomes();

    private Biomes() {
    }

    public Biome getBiome(ClientPlayerEntity player) {
        return getBiome(player.world, player.getBlockPos());
    }

    public Biome getBiome(World world, BlockPos pos) {
        return world.getBiome(pos);
    }

    public String getBiomeName(ClientPlayerEntity player) {
        return getBiomeName(player.world, getBiome(player));
    }

    public String getBiomeName(World world, Biome biome) {
        Identifier identifier = registry(world).getId(biome);
        return I18n.translate(identifier == null ? "unknown" : "biome.minecraft." + identifier.getPath());
    }

    private Registry<Biome> registry(World world) {
        return world.getRegistryManager().get(Registry.BIOME_KEY);
    }
}
