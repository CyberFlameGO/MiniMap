package net.pl3x.minimap.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.pl3x.minimap.config.Advanced;

import java.awt.Color;

public class Colors {
    public static int shade(int color, float ratio) {
        return argb(alpha(color), (int) (red(color) * ratio), (int) (green(color) * ratio), (int) (blue(color) * ratio));
    }

    public static int lerpARGB(int color0, int color1, float delta) {
        if (color0 == color1) return color0;
        if (delta >= 1F) return color1;
        if (delta <= 0F) return color0;
        return argb(
            (int) Mathf.lerp(alpha(color0), alpha(color1), delta),
            (int) Mathf.lerp(red(color0), red(color1), delta),
            (int) Mathf.lerp(green(color0), green(color1), delta),
            (int) Mathf.lerp(blue(color0), blue(color1), delta)
        );
    }

    public static int inverseLerpARGB(int color0, int color1, float delta) {
        if (color0 == color1) return color0;
        if (delta >= 1F) return color1;
        if (delta <= 0F) return color0;
        return argb(
            (int) Mathf.inverseLerp(alpha(color0), alpha(color1), delta),
            (int) Mathf.inverseLerp(red(color0), red(color1), delta),
            (int) Mathf.inverseLerp(green(color0), green(color1), delta),
            (int) Mathf.inverseLerp(blue(color0), blue(color1), delta)
        );
    }

    public static int lerpHSB(int color0, int color1, float delta) {
        float[] hsb0 = Color.RGBtoHSB(red(color0), green(color0), blue(color0), null);
        float[] hsb1 = Color.RGBtoHSB(red(color1), green(color1), blue(color1), null);
        return setAlpha(
            (int) Mathf.lerp(alpha(color0), alpha(color1), delta),
            Color.HSBtoRGB(
                lerpShortestAngle(hsb0[0], hsb1[0], delta),
                Mathf.lerp(hsb0[1], hsb1[1], delta),
                Mathf.lerp(hsb0[2], hsb1[2], delta)
            )
        );
    }

    public static int inverseLerpHSB(int color0, int color1, float delta) {
        float[] hsb0 = Color.RGBtoHSB(red(color0), green(color0), blue(color0), null);
        float[] hsb1 = Color.RGBtoHSB(red(color1), green(color1), blue(color1), null);
        return setAlpha(
            (int) Mathf.inverseLerp(alpha(color0), alpha(color1), delta),
            Color.HSBtoRGB(
                lerpShortestAngle(hsb0[0], hsb1[0], delta),
                Mathf.inverseLerp(hsb0[1], hsb1[1], delta),
                Mathf.inverseLerp(hsb0[2], hsb1[2], delta)
            )
        );
    }

    public static float lerpShortestAngle(float start, float end, float delta) {
        float distCW = (end >= start ? end - start : 1F - (start - end));
        float distCCW = (start >= end ? start - end : 1F - (end - start));
        float direction = (distCW <= distCCW ? distCW : -1F * distCCW);
        return (start + (direction * delta));
    }

    public static int rgb2bgr(int color) {
        // Minecraft flips red and blue for some reason. let's flip them back
        return (alpha(color) << 24) | (blue(color) << 16) | (green(color) << 8) | red(color);
    }

    public static int alpha(int argb) {
        return argb >> 24 & 0xFF;
    }

    public static int red(int argb) {
        return argb >> 16 & 0xFF;
    }

    public static int green(int argb) {
        return argb >> 8 & 0xFF;
    }

    public static int blue(int argb) {
        return argb & 0xFF;
    }

    public static int argb(int alpha, int red, int green, int blue) {
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    public static int mix(int color0, int color1) {
        int a = alpha(color0);
        if (a == 0) {
            return 0;
        }
        float ratio = alpha(color1) / 255F;
        float iRatio = 1F - ratio;
        int r = (int) ((red(color0) * iRatio) + (red(color1) * ratio));
        int g = (int) ((green(color0) * iRatio) + (green(color1) * ratio));
        int b = (int) ((blue(color0) * iRatio) + (blue(color1) * ratio));
        return (a << 24 | r << 16 | g << 8 | b);
    }

    public static int setAlpha(int alpha, int color) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    public static int fromHex(String color) {
        return (int) Long.parseLong(color.replace("#", ""), 16);
    }

    public static int getBlockColor(ClientWorld world, BlockState state, BlockPos pos) {
        Block block = state.getBlock();
        int color = Advanced.getConfig().blockColors.getOrDefault(block, -1);

        if (color != 0) {
            if (block == Blocks.MELON_STEM || block == Blocks.PUMPKIN_STEM) {
                int age = state.get(StemBlock.AGE);
                color = argb(0, age * 32, 0xFF - age * 8, age * 4);
            } else if (block == Blocks.WHEAT) {
                color = Colors.lerpARGB(MapColor.DARK_GREEN.color, color, (state.get(CropBlock.AGE) + 1) / 8F);
            } else if (block == Blocks.REDSTONE_WIRE) {
                color = RedstoneWireBlock.getWireColor(state.get(RedstoneWireBlock.POWER));
            } else if (block == Blocks.GRASS_BLOCK || block == Blocks.GRASS || block == Blocks.TALL_GRASS || block == Blocks.FERN || block == Blocks.LARGE_FERN || block == Blocks.POTTED_FERN || block == Blocks.SUGAR_CANE) {
                // hate how slow this is, but it's accurate and blends
                color = BiomeColors.getGrassColor(world, pos);
            } else if (block == Blocks.VINE || block == Blocks.OAK_LEAVES || block == Blocks.JUNGLE_LEAVES || block == Blocks.ACACIA_LEAVES || block == Blocks.DARK_OAK_LEAVES) {
                // hate how slow this is, but it's accurate and blends
                color = shade(BiomeColors.getFoliageColor(world, pos), 200 / 255F);
            }
        }

        if (color < 0) {
            return state.getMapColor(world, pos).color;
        }

        return color;
    }
}
