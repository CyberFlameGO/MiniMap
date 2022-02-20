package net.pl3x.minimap.util;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

import java.util.HashMap;
import java.util.Map;

public class Biomes {
    public static final Biomes INSTANCE = new Biomes();

    public static boolean ALLOW_NULL_BIOMES = false;

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
        Identifier identifier = getKey(world, biome);
        return I18n.translate(identifier == null ? "unknown" : "biome.minecraft." + identifier.getPath());
    }

    public Identifier getKey(World world, BlockPos pos) {
        return getKey(world, world.getBiome(pos));
    }

    public Identifier getKey(World world, Biome biome) {
        return registry(world).getId(biome);
    }

    public Registry<Biome> registry(World world) {
        return world.getRegistryManager().get(Registry.BIOME_KEY);
    }

    public static int color(World world, Biome biome) {
        return Color.get(world, biome);
    }

    public enum Color {
        THE_VOID(BiomeKeys.THE_VOID, 0x00000000),
        PLAINS(BiomeKeys.PLAINS, 0xFF8DB360),
        SUNFLOWER_PLAINS(BiomeKeys.SUNFLOWER_PLAINS, 0xFFB5DB88),
        SNOWY_PLAINS(BiomeKeys.SNOWY_PLAINS, 0xFFFFFFFF),
        ICE_SPIKES(BiomeKeys.ICE_SPIKES, 0xFFB4DCDC),
        DESERT(BiomeKeys.DESERT, 0xFFFA9418),
        SWAMP(BiomeKeys.SWAMP, 0xFF07F9B2),
        FOREST(BiomeKeys.FOREST, 0xFF056621),
        FLOWER_FOREST(BiomeKeys.FLOWER_FOREST, 0xFF2D8E49),
        BIRCH_FOREST(BiomeKeys.BIRCH_FOREST, 0xFF307444),
        DARK_FOREST(BiomeKeys.DARK_FOREST, 0xFF40511A),
        OLD_GROWTH_BIRCH_FOREST(BiomeKeys.OLD_GROWTH_BIRCH_FOREST, 0xFF307444),
        OLD_GROWTH_PINE_TAIGA(BiomeKeys.OLD_GROWTH_PINE_TAIGA, 0xFF596651),
        OLD_GROWTH_SPRUCE_TAIGA(BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA, 0xFF818E79),
        TAIGA(BiomeKeys.TAIGA, 0xFF0B6659),
        SNOWY_TAIGA(BiomeKeys.SNOWY_TAIGA, 0xFF31554A),
        SAVANNA(BiomeKeys.SAVANNA, 0xFFBDB25F),
        SAVANNA_PLATEAU(BiomeKeys.SAVANNA_PLATEAU, 0xFFA79D64),
        WINDSWEPT_HILLS(BiomeKeys.WINDSWEPT_HILLS, 0xFF597D72),
        WINDSWEPT_GRAVELLY_HILLS(BiomeKeys.WINDSWEPT_GRAVELLY_HILLS, 0xFF789878),
        WINDSWEPT_FOREST(BiomeKeys.WINDSWEPT_FOREST, 0xFF589C6C),
        WINDSWEPT_SAVANNA(BiomeKeys.WINDSWEPT_SAVANNA, 0xFFE5DA87),
        JUNGLE(BiomeKeys.JUNGLE, 0xFF537B09),
        SPARSE_JUNGLE(BiomeKeys.SPARSE_JUNGLE, 0xFF628B17),
        BAMBOO_JUNGLE(BiomeKeys.BAMBOO_JUNGLE, 0xFF768E14),
        BADLANDS(BiomeKeys.BADLANDS, 0xFFD94515),
        ERODED_BADLANDS(BiomeKeys.ERODED_BADLANDS, 0xFFFF6D3D),
        WOODED_BADLANDS(BiomeKeys.WOODED_BADLANDS, 0xFFB09765),
        MEADOW(BiomeKeys.MEADOW, 0xFF2C4205),
        GROVE(BiomeKeys.GROVE, 0xFF888888),
        SNOWY_SLOPES(BiomeKeys.SNOWY_SLOPES, 0xFFA0A0A0),
        FROZEN_PEAKS(BiomeKeys.FROZEN_PEAKS, 0xFFA0A0A0),
        JAGGED_PEAKS(BiomeKeys.JAGGED_PEAKS, 0xFFA0A0A0),
        STONY_PEAKS(BiomeKeys.STONY_PEAKS, 0xFF888888),
        RIVER(BiomeKeys.RIVER, 0xFF0000FF),
        FROZEN_RIVER(BiomeKeys.FROZEN_RIVER, 0xFFA0A0FF),
        BEACH(BiomeKeys.BEACH, 0xFFFADE55),
        SNOWY_BEACH(BiomeKeys.SNOWY_BEACH, 0xFFFAF0C0),
        STONY_SHORE(BiomeKeys.STONY_SHORE, 0xFFA2A284),
        WARM_OCEAN(BiomeKeys.WARM_OCEAN, 0xFF0000AC),
        LUKEWARM_OCEAN(BiomeKeys.LUKEWARM_OCEAN, 0xFF000090),
        DEEP_LUKEWARM_OCEAN(BiomeKeys.DEEP_LUKEWARM_OCEAN, 0xFF000040),
        OCEAN(BiomeKeys.OCEAN, 0xFF000070),
        DEEP_OCEAN(BiomeKeys.DEEP_OCEAN, 0xFF000030),
        COLD_OCEAN(BiomeKeys.COLD_OCEAN, 0xFF202070),
        DEEP_COLD_OCEAN(BiomeKeys.DEEP_COLD_OCEAN, 0xFF202038),
        FROZEN_OCEAN(BiomeKeys.FROZEN_OCEAN, 0xFF7070D6),
        DEEP_FROZEN_OCEAN(BiomeKeys.DEEP_FROZEN_OCEAN, 0xFF404090),
        MUSHROOM_FIELDS(BiomeKeys.MUSHROOM_FIELDS, 0xFFFF00FF),
        DRIPSTONE_CAVES(BiomeKeys.DRIPSTONE_CAVES, 0xFF888888),
        LUSH_CAVES(BiomeKeys.LUSH_CAVES, 0xFF7BA331),
        NETHER_WASTES(BiomeKeys.NETHER_WASTES, 0xFFBF3B3B),
        WARPED_FOREST(BiomeKeys.WARPED_FOREST, 0xFF49907B),
        CRIMSON_FOREST(BiomeKeys.CRIMSON_FOREST, 0xFFDD0808),
        SOUL_SAND_VALLEY(BiomeKeys.SOUL_SAND_VALLEY, 0xFF5E3830),
        BASALT_DELTAS(BiomeKeys.BASALT_DELTAS, 0xFF403636),
        THE_END(BiomeKeys.THE_END, 0xFF8080FF),
        END_HIGHLANDS(BiomeKeys.END_HIGHLANDS, 0xFF8080FF),
        END_MIDLANDS(BiomeKeys.END_MIDLANDS, 0xFF8080FF),
        SMALL_END_ISLANDS(BiomeKeys.SMALL_END_ISLANDS, 0xFF8080FF),
        END_BARRENS(BiomeKeys.END_BARRENS, 0xFF8080FF);

        private final RegistryKey<Biome> biome;
        private final int color;

        Color() {
            this(BiomeKeys.PLAINS, 0);
        }

        Color(RegistryKey<Biome> biome, int color) {
            this.biome = biome;
            this.color = color;
        }

        public static int get(World world, Biome biome) {
            RegistryKey<Biome> key = Biomes.INSTANCE.registry(world).getKey(biome).orElse(null);
            return key == null ? 0 : get(key);
        }

        public static int get(RegistryKey<Biome> biome) {
            return COLORS.get(biome);
        }

        private static final Map<RegistryKey<Biome>, Integer> COLORS = new HashMap<>();

        static {
            for (Color color : values()) {
                COLORS.put(color.biome, color.color);
            }
        }
    }
}
