package net.pl3x.minimap.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.manager.FileManager;
import net.pl3x.minimap.queue.DiskIOQueue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class Advanced {
    public Map<Block, Integer> blockColors = Map.ofEntries(
        Map.entry(Blocks.LAVA, 0xEA5C0F),

        Map.entry(Blocks.ORANGE_TULIP, 0xBD6A22),
        Map.entry(Blocks.PINK_TULIP, 0xEBC5FD),
        Map.entry(Blocks.RED_TULIP, 0x9B221A),
        Map.entry(Blocks.WHITE_TULIP, 0xD6E8E8),

        Map.entry(Blocks.WHEAT, 0xDCBB65),
        Map.entry(Blocks.ATTACHED_MELON_STEM, 0xE0C71C),
        Map.entry(Blocks.ATTACHED_PUMPKIN_STEM, 0xE0C71C),

        Map.entry(Blocks.POTTED_ALLIUM, 0xB878ED),
        Map.entry(Blocks.POTTED_AZURE_BLUET, 0xF7F7F7),
        Map.entry(Blocks.POTTED_BLUE_ORCHID, 0x2ABFFD),
        Map.entry(Blocks.POTTED_CORNFLOWER, 0x466AEB),
        Map.entry(Blocks.POTTED_DANDELION, 0xFFEC4F),
        Map.entry(Blocks.POTTED_LILY_OF_THE_VALLEY, 0xFFFFFF),
        Map.entry(Blocks.POTTED_ORANGE_TULIP, 0xBD6A22),
        Map.entry(Blocks.POTTED_OXEYE_DAISY, 0xD6E8E8),
        Map.entry(Blocks.POTTED_PINK_TULIP, 0xEBC5FD),
        Map.entry(Blocks.POTTED_POPPY, 0xED302C),
        Map.entry(Blocks.POTTED_RED_TULIP, 0x9B221A),
        Map.entry(Blocks.POTTED_WHITE_TULIP, 0xD6E8E8),
        Map.entry(Blocks.POTTED_WITHER_ROSE, 0x211A16),

        Map.entry(Blocks.POTTED_OAK_SAPLING, 0x0),
        Map.entry(Blocks.POTTED_SPRUCE_SAPLING, 0x0),
        Map.entry(Blocks.POTTED_BIRCH_SAPLING, 0x0),
        Map.entry(Blocks.POTTED_JUNGLE_SAPLING, 0x0),
        Map.entry(Blocks.POTTED_ACACIA_SAPLING, 0x0),
        Map.entry(Blocks.POTTED_DARK_OAK_SAPLING, 0x0),
        Map.entry(Blocks.POTTED_FERN, 0x0),
        Map.entry(Blocks.POTTED_RED_MUSHROOM, 0x0),
        Map.entry(Blocks.POTTED_BROWN_MUSHROOM, 0x0),
        Map.entry(Blocks.POTTED_DEAD_BUSH, 0x0),
        Map.entry(Blocks.POTTED_CACTUS, 0x0),
        Map.entry(Blocks.POTTED_BAMBOO, 0x0),
        Map.entry(Blocks.POTTED_CRIMSON_FUNGUS, 0x0),
        Map.entry(Blocks.POTTED_WARPED_FUNGUS, 0x0),
        Map.entry(Blocks.POTTED_CRIMSON_ROOTS, 0x0),
        Map.entry(Blocks.POTTED_WARPED_ROOTS, 0x0),
        Map.entry(Blocks.POTTED_AZALEA_BUSH, 0x0),
        Map.entry(Blocks.POTTED_FLOWERING_AZALEA_BUSH, 0x0),

        Map.entry(Blocks.POWERED_RAIL, 0x0),
        Map.entry(Blocks.DETECTOR_RAIL, 0x0),
        Map.entry(Blocks.RAIL, 0x0),
        Map.entry(Blocks.ACTIVATOR_RAIL, 0x0),

        Map.entry(Blocks.TORCH, 0x0),
        Map.entry(Blocks.WALL_TORCH, 0x0),
        Map.entry(Blocks.LADDER, 0x0),
        Map.entry(Blocks.LEVER, 0x0),
        Map.entry(Blocks.REDSTONE_TORCH, 0x0),
        Map.entry(Blocks.REDSTONE_WALL_TORCH, 0x0),
        Map.entry(Blocks.STONE_BUTTON, 0x0),
        Map.entry(Blocks.SOUL_TORCH, 0x0),
        Map.entry(Blocks.SOUL_WALL_TORCH, 0x0),
        Map.entry(Blocks.REPEATER, 0x0),
        Map.entry(Blocks.TRIPWIRE_HOOK, 0x0),
        Map.entry(Blocks.TRIPWIRE, 0x0),
        Map.entry(Blocks.COMPARATOR, 0x0),

        Map.entry(Blocks.OAK_BUTTON, 0x0),
        Map.entry(Blocks.SPRUCE_BUTTON, 0x0),
        Map.entry(Blocks.BIRCH_BUTTON, 0x0),
        Map.entry(Blocks.JUNGLE_BUTTON, 0x0),
        Map.entry(Blocks.ACACIA_BUTTON, 0x0),
        Map.entry(Blocks.DARK_OAK_BUTTON, 0x0),
        Map.entry(Blocks.CRIMSON_BUTTON, 0x0),
        Map.entry(Blocks.WARPED_BUTTON, 0x0),
        Map.entry(Blocks.POLISHED_BLACKSTONE_BUTTON, 0x0),
        Map.entry(Blocks.SKELETON_SKULL, 0x0),
        Map.entry(Blocks.SKELETON_WALL_SKULL, 0x0),
        Map.entry(Blocks.WITHER_SKELETON_SKULL, 0x0),
        Map.entry(Blocks.WITHER_SKELETON_WALL_SKULL, 0x0),
        Map.entry(Blocks.ZOMBIE_HEAD, 0x0),
        Map.entry(Blocks.ZOMBIE_WALL_HEAD, 0x0),
        Map.entry(Blocks.PLAYER_HEAD, 0x0),
        Map.entry(Blocks.PLAYER_WALL_HEAD, 0x0),
        Map.entry(Blocks.CREEPER_HEAD, 0x0),
        Map.entry(Blocks.CREEPER_WALL_HEAD, 0x0),
        Map.entry(Blocks.DRAGON_HEAD, 0x0),
        Map.entry(Blocks.DRAGON_WALL_HEAD, 0x0),
        Map.entry(Blocks.END_ROD, 0x0),
        Map.entry(Blocks.SCAFFOLDING, 0x0),
        Map.entry(Blocks.CANDLE, 0x0),
        Map.entry(Blocks.WHITE_CANDLE, 0x0),
        Map.entry(Blocks.ORANGE_CANDLE, 0x0),
        Map.entry(Blocks.MAGENTA_CANDLE, 0x0),
        Map.entry(Blocks.LIGHT_BLUE_CANDLE, 0x0),
        Map.entry(Blocks.YELLOW_CANDLE, 0x0),
        Map.entry(Blocks.LIME_CANDLE, 0x0),
        Map.entry(Blocks.PINK_CANDLE, 0x0),
        Map.entry(Blocks.GRAY_CANDLE, 0x0),
        Map.entry(Blocks.LIGHT_GRAY_CANDLE, 0x0),
        Map.entry(Blocks.CYAN_CANDLE, 0x0),
        Map.entry(Blocks.PURPLE_CANDLE, 0x0),
        Map.entry(Blocks.BLUE_CANDLE, 0x0),
        Map.entry(Blocks.BROWN_CANDLE, 0x0),
        Map.entry(Blocks.GREEN_CANDLE, 0x0),
        Map.entry(Blocks.RED_CANDLE, 0x0),
        Map.entry(Blocks.BLACK_CANDLE, 0x0),

        Map.entry(Blocks.FLOWER_POT, 0x0),

        Map.entry(Blocks.ALLIUM, 0xB878ED),
        Map.entry(Blocks.AZURE_BLUET, 0xF7F7F7),
        Map.entry(Blocks.BLUE_ORCHID, 0x2ABFFD),
        Map.entry(Blocks.CORNFLOWER, 0x466AEB),
        Map.entry(Blocks.DANDELION, 0xFFEC4F),
        Map.entry(Blocks.LILY_OF_THE_VALLEY, 0xFFFFFF),
        Map.entry(Blocks.OXEYE_DAISY, 0xD6E8E8),
        Map.entry(Blocks.POPPY, 0xED302C),
        Map.entry(Blocks.WITHER_ROSE, 0x211A16),

        Map.entry(Blocks.LILAC, 0xB66BB2),
        Map.entry(Blocks.PEONY, 0xEBC5FD),
        Map.entry(Blocks.ROSE_BUSH, 0x9B221A),
        Map.entry(Blocks.SUNFLOWER, 0xFFEC4F),

        Map.entry(Blocks.LILY_PAD, 0x208030),

        Map.entry(Blocks.GRASS, 0x0),
        Map.entry(Blocks.TALL_GRASS, 0x0),

        Map.entry(Blocks.GLASS, 0xFFFFFF),
        Map.entry(Blocks.MYCELIUM, 0x6F6265),
        Map.entry(Blocks.TERRACOTTA, 0x9E6246),

        Map.entry(Blocks.BIRCH_LEAVES, 0x668644), // 25% darker than normal
        Map.entry(Blocks.SPRUCE_LEAVES, 0x4e7a4e) // 25% darker than normal
    );

    public Map<RegistryKey<Biome>, Integer> biomeColors = Map.ofEntries(
        Map.entry(BiomeKeys.THE_VOID, 0x00000000),
        Map.entry(BiomeKeys.PLAINS, 0xFF8DB360),
        Map.entry(BiomeKeys.SUNFLOWER_PLAINS, 0xFFB5DB88),
        Map.entry(BiomeKeys.SNOWY_PLAINS, 0xFFFFFFFF),
        Map.entry(BiomeKeys.ICE_SPIKES, 0xFFB4DCDC),
        Map.entry(BiomeKeys.DESERT, 0xFFFA9418),
        Map.entry(BiomeKeys.SWAMP, 0xFF07F9B2),
        Map.entry(BiomeKeys.FOREST, 0xFF056621),
        Map.entry(BiomeKeys.FLOWER_FOREST, 0xFF2D8E49),
        Map.entry(BiomeKeys.BIRCH_FOREST, 0xFF307444),
        Map.entry(BiomeKeys.DARK_FOREST, 0xFF40511A),
        Map.entry(BiomeKeys.OLD_GROWTH_BIRCH_FOREST, 0xFF307444),
        Map.entry(BiomeKeys.OLD_GROWTH_PINE_TAIGA, 0xFF596651),
        Map.entry(BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA, 0xFF818E79),
        Map.entry(BiomeKeys.TAIGA, 0xFF0B6659),
        Map.entry(BiomeKeys.SNOWY_TAIGA, 0xFF31554A),
        Map.entry(BiomeKeys.SAVANNA, 0xFFBDB25F),
        Map.entry(BiomeKeys.SAVANNA_PLATEAU, 0xFFA79D64),
        Map.entry(BiomeKeys.WINDSWEPT_HILLS, 0xFF597D72),
        Map.entry(BiomeKeys.WINDSWEPT_GRAVELLY_HILLS, 0xFF789878),
        Map.entry(BiomeKeys.WINDSWEPT_FOREST, 0xFF589C6C),
        Map.entry(BiomeKeys.WINDSWEPT_SAVANNA, 0xFFE5DA87),
        Map.entry(BiomeKeys.JUNGLE, 0xFF537B09),
        Map.entry(BiomeKeys.SPARSE_JUNGLE, 0xFF628B17),
        Map.entry(BiomeKeys.BAMBOO_JUNGLE, 0xFF768E14),
        Map.entry(BiomeKeys.BADLANDS, 0xFFD94515),
        Map.entry(BiomeKeys.ERODED_BADLANDS, 0xFFFF6D3D),
        Map.entry(BiomeKeys.WOODED_BADLANDS, 0xFFB09765),
        Map.entry(BiomeKeys.MEADOW, 0xFF2C4205),
        Map.entry(BiomeKeys.GROVE, 0xFF888888),
        Map.entry(BiomeKeys.SNOWY_SLOPES, 0xFFA0A0A0),
        Map.entry(BiomeKeys.FROZEN_PEAKS, 0xFFA0A0A0),
        Map.entry(BiomeKeys.JAGGED_PEAKS, 0xFFA0A0A0),
        Map.entry(BiomeKeys.STONY_PEAKS, 0xFF888888),
        Map.entry(BiomeKeys.RIVER, 0xFF0000FF),
        Map.entry(BiomeKeys.FROZEN_RIVER, 0xFFA0A0FF),
        Map.entry(BiomeKeys.BEACH, 0xFFFADE55),
        Map.entry(BiomeKeys.SNOWY_BEACH, 0xFFFAF0C0),
        Map.entry(BiomeKeys.STONY_SHORE, 0xFFA2A284),
        Map.entry(BiomeKeys.WARM_OCEAN, 0xFF0000AC),
        Map.entry(BiomeKeys.LUKEWARM_OCEAN, 0xFF000090),
        Map.entry(BiomeKeys.DEEP_LUKEWARM_OCEAN, 0xFF000040),
        Map.entry(BiomeKeys.OCEAN, 0xFF000070),
        Map.entry(BiomeKeys.DEEP_OCEAN, 0xFF000030),
        Map.entry(BiomeKeys.COLD_OCEAN, 0xFF202070),
        Map.entry(BiomeKeys.DEEP_COLD_OCEAN, 0xFF202038),
        Map.entry(BiomeKeys.FROZEN_OCEAN, 0xFF7070D6),
        Map.entry(BiomeKeys.DEEP_FROZEN_OCEAN, 0xFF404090),
        Map.entry(BiomeKeys.MUSHROOM_FIELDS, 0xFFFF00FF),
        Map.entry(BiomeKeys.DRIPSTONE_CAVES, 0xFF888888),
        Map.entry(BiomeKeys.LUSH_CAVES, 0xFF7BA331),
        Map.entry(BiomeKeys.NETHER_WASTES, 0xFFBF3B3B),
        Map.entry(BiomeKeys.WARPED_FOREST, 0xFF49907B),
        Map.entry(BiomeKeys.CRIMSON_FOREST, 0xFFDD0808),
        Map.entry(BiomeKeys.SOUL_SAND_VALLEY, 0xFF5E3830),
        Map.entry(BiomeKeys.BASALT_DELTAS, 0xFF403636),
        Map.entry(BiomeKeys.THE_END, 0xFF8080FF),
        Map.entry(BiomeKeys.END_HIGHLANDS, 0xFF8080FF),
        Map.entry(BiomeKeys.END_MIDLANDS, 0xFF8080FF),
        Map.entry(BiomeKeys.SMALL_END_ISLANDS, 0xFF8080FF),
        Map.entry(BiomeKeys.END_BARRENS, 0xFF8080FF)
    );

    private static final Gson gson = new GsonBuilder()
        .disableHtmlEscaping()
        .serializeNulls()
        .setLenient()
        .setPrettyPrinting()
        .enableComplexMapKeySerialization()
        .registerTypeAdapter(BiomeColorAdapter.TYPE, new BiomeColorAdapter())
        .registerTypeAdapter(BlockColorAdapter.TYPE, new BlockColorAdapter())
        .create();

    private static Advanced config;

    public static Advanced getConfig() {
        if (config == null) {
            reload();
        }
        return config;
    }

    public static void reload() {
        try (Reader reader = new BufferedReader(new FileReader(FileManager.INSTANCE.advancedFile.toFile()))) {
            config = gson.fromJson(reader, Advanced.class);
            MiniMap.LOG.info("Loaded existing advanced config");
        } catch (IOException e) {
            config = new Advanced();
            MiniMap.LOG.info("Loaded new advanced config");
        }
        save();
    }

    public static void save() {
        DiskIOQueue.INSTANCE.write(() -> {
            try (FileWriter writer = new FileWriter(FileManager.INSTANCE.advancedFile.toFile())) {
                gson.toJson(config, writer);
                writer.flush();
                MiniMap.LOG.info("Saved advanced config");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static class BlockColorAdapter implements JsonSerializer<Map<Block, Integer>>, JsonDeserializer<Map<Block, Integer>> {
        public static final Type TYPE = (new TypeToken<Map<Block, Integer>>() {
        }).getType();

        @Override
        public JsonElement serialize(Map<Block, Integer> map, Type type, JsonSerializationContext context) {
            JsonArray arr = new JsonArray();
            for (Map.Entry<Block, Integer> entry : map.entrySet()) {
                JsonObject obj = new JsonObject();
                obj.add(Registry.BLOCK.getId(entry.getKey()).toString(), new JsonPrimitive(entry.getValue()));
                arr.add(obj);
            }
            return arr;
        }

        @Override
        public Map<Block, Integer> deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            Map<Block, Integer> resultMap = new HashMap<>();
            for (JsonElement element : json.getAsJsonArray()) {
                for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                    Block block = Registry.BLOCK.get(new Identifier(entry.getKey()));
                    if (block == Blocks.AIR) {
                        MiniMap.LOG.warn("ignoring color for unknown block in advanced.yml: " + entry.getKey());
                        continue;
                    }
                    resultMap.put(block, entry.getValue().getAsInt());
                }
            }
            return resultMap;
        }
    }

    public static class BiomeColorAdapter implements JsonSerializer<Map<RegistryKey<Biome>, Integer>>, JsonDeserializer<Map<RegistryKey<Biome>, Integer>> {
        public static final Type TYPE = (new TypeToken<Map<RegistryKey<Biome>, Integer>>() {
        }).getType();

        @Override
        public JsonElement serialize(Map<RegistryKey<Biome>, Integer> map, Type type, JsonSerializationContext context) {
            JsonArray arr = new JsonArray();
            for (Map.Entry<RegistryKey<Biome>, Integer> entry : map.entrySet()) {
                JsonObject obj = new JsonObject();
                obj.add(entry.getKey().getValue().toString(), new JsonPrimitive(entry.getValue()));
                arr.add(obj);
            }
            return arr;
        }

        @Override
        public Map<RegistryKey<Biome>, Integer> deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            Map<RegistryKey<Biome>, Integer> resultMap = new HashMap<>();
            Registry<Biome> registry = MiniMap.INSTANCE.getWorld().getRegistryManager().get(Registry.BIOME_KEY);
            for (JsonElement element : json.getAsJsonArray()) {
                for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                    Biome biome = registry.get(new Identifier(entry.getKey()));
                    RegistryKey<Biome> biomeKey = registry.getKey(biome).orElse(null);
                    if (biomeKey == null) {
                        MiniMap.LOG.warn("ignoring color for unknown biome in advanced.yml: " + entry.getKey());
                        continue;
                    }
                    resultMap.put(biomeKey, entry.getValue().getAsInt());
                }
            }
            return resultMap;
        }
    }
}
