package net.pl3x.minimap.gui;

import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.gui.texture.Texture;

public enum Icon {
    AXOLOTL(0, 0),
    BAT(1, 0),
    BEE(2, 0),
    BLAZE(3, 0),
    CAT(4, 0),
    CAVE_SPIDER(5, 0),
    CHICKEN(6, 0),
    COD(7, 0),
    COW(8, 0),
    CREEPER(9, 0),
    DOLPHIN(10, 0),
    DONKEY(11, 0),
    DROWNED(12, 0),
    ELDER_GUARDIAN(13, 0),
    ENDER_DRAGON(14, 0),
    ENDERMAN(15, 0),
    ENDERMITE(0, 1),
    EVOKER(1, 1),
    FOX(2, 1),
    GHAST(3, 1),
    GIANT(4, 1),
    GLOW_SQUID(5, 1),
    GOAT(6, 1),
    GUARDIAN(7, 1),
    HOGLIN(8, 1),
    HORSE(9, 1),
    HUSK(10, 1),
    ILLUSIONER(11, 1),
    IRON_GOLEM(12, 1),
    LLAMA(13, 1),
    MAGMA_CUBE(14, 1),
    MOOSHROOM(15, 1),
    MULE(0, 2),
    OCELOT(1, 2),
    PANDA(2, 2),
    PARROT(3, 2),
    PHANTOM(4, 2),
    PIG(5, 2),
    PIGLIN_BRUTE(6, 2),
    PIGLIN(7, 2),
    PILLAGER(8, 2),
    POLAR_BEAR(9, 2),
    PUFFERFISH(10, 2),
    RABBIT(11, 2),
    RAVAGER(12, 2),
    SALMON(13, 2),
    SHEEP(14, 2),
    SHULKER(15, 2),
    SILVERFISH(0, 3),
    SKELETON(1, 3),
    SKELETON_HORSE(2, 3),
    SLIME(3, 3),
    SNOW_GOLEM(4, 3),
    SPIDER(5, 3),
    SQUID(6, 3),
    STRAY(7, 3),
    STRIDER(8, 3),
    TRADER_LLAMA(9, 3),
    TROPICAL_FISH(10, 3),
    TURTLE(11, 3),
    VEX(12, 3),
    VILLAGER(13, 3),
    VINDICATOR(14, 3),
    WANDERING_TRADER(15, 3),
    WITCH(0, 4),
    WITHER(1, 4),
    WITHER_SKELETON(2, 4),
    WOLF(3, 4),
    ZOGLIN(4, 4),
    ZOMBIE(5, 4),
    ZOMBIE_HORSE(6, 4),
    ZOMBIE_VILLAGER(7, 4),
    ZOMBIFIED_PIGLIN(8, 4),

    RADAR(0, 15),
    ABOUT(1, 15),
    STYLE(2, 15),
    CLOCK(3, 15),
    POSITION(4, 15),
    WAYPOINTS(5, 15),
    LAYERS(6, 15),
    MAP(7, 15),
    ARROW(8, 15),
    PLAYER(9, 15);

    private final int u, v;

    Icon(int u, int v) {
        this.u = u;
        this.v = v;
    }

    public int u() {
        return this.u;
    }

    public int v() {
        return this.v;
    }

    public float u0() {
        return u() / 16F;
    }

    public float v0() {
        return v() / 16F;
    }

    public float u1() {
        return (u() + 1) / 16F;
    }

    public float v1() {
        return (v() + 1) / 16F;
    }

    public void draw(MatrixStack matrixStack, float x, float y, float size) {
        Texture.ICONS.draw(matrixStack, x, y, x + size, y + size, u0(), v0(), u1(), v1());
    }

    public void tint(MatrixStack matrixStack, float x, float y, float size, int tint) {
        Texture.ICONS.tint(matrixStack, x, y, x + size, y + size, u0(), v0(), u1(), v1(), tint);
    }
}
