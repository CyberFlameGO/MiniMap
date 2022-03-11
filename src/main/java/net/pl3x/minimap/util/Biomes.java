package net.pl3x.minimap.util;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.pl3x.minimap.config.Advanced;

public class Biomes {
    public static final Biomes INSTANCE = new Biomes();

    private Biomes() {
    }

    public Biome getBiome(ClientPlayerEntity player) {
        return getBiome(player.clientWorld, player.getBlockPos());
    }

    public Biome getBiome(ClientWorld world, BlockPos pos) {
        RegistryEntry<Biome> entry = world.getBiome(pos);
        return entry == null ? null : entry.value();
    }

    public String getBiomeName(ClientPlayerEntity player) {
        return getBiomeName(player.clientWorld, getBiome(player));
    }

    public String getBiomeName(ClientWorld world, Biome biome) {
        Identifier identifier = getId(world, biome);
        return I18n.translate(identifier == null ? "unknown" : "biome.minecraft." + identifier.getPath());
    }

    public Identifier getId(ClientWorld world, Biome biome) {
        return registry(world).getId(biome);
    }

    public RegistryKey<Biome> getKey(ClientWorld world, Biome biome) {
        return Biomes.INSTANCE.registry(world).getKey(biome).orElse(null);
    }

    public int getColor(ClientWorld world, Biome biome) {
        RegistryKey<Biome> key = Biomes.INSTANCE.getKey(world, biome);
        return key == null ? 0 : Advanced.getConfig().biomeColors.get(key);
    }

    public Registry<Biome> registry(ClientWorld world) {
        return world.getRegistryManager().get(Registry.BIOME_KEY);
    }
}
