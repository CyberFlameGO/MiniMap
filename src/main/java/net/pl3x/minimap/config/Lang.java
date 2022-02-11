package net.pl3x.minimap.config;

import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class Lang {
    public static final Text TITLE = new TranslatableText("minimap.title");

    public static final Text OPTIONS_TITLE = new TranslatableText("minimap.options.title");

    public static final Text CATEGORY_STYLE = new TranslatableText("minimap.sidebar.category.style");
    public static final Text CATEGORY_POSITION = new TranslatableText("minimap.sidebar.category.position");
    public static final Text CATEGORY_RADAR = new TranslatableText("minimap.sidebar.category.radar");
    public static final Text CATEGORY_WAYPOINTS = new TranslatableText("minimap.sidebar.category.waypoints");
    public static final Text CATEGORY_LAYERS = new TranslatableText("minimap.sidebar.category.layers");
    public static final Text CATEGORY_CLOCK = new TranslatableText("minimap.sidebar.category.clock");
    public static final Text CATEGORY_ABOUT = new TranslatableText("minimap.sidebar.category.about");

    public static final Text MINIMAP_ENABLED = new TranslatableText("minimap.options.minimap.enabled");
    public static final Text MINIMAP_ENABLED_TOOLTIP = new TranslatableText("minimap.options.minimap.enabled.tooltip");
    public static final Text NORTH_LOCKED = new TranslatableText("minimap.options.minimap.north-locked");
    public static final Text NORTH_LOCKED_TOOLTIP = new TranslatableText("minimap.options.minimap.north-locked.tooltip");
    public static final Text FRAME = new TranslatableText("minimap.options.minimap.frame");
    public static final Text FRAME_TOOLTIP = new TranslatableText("minimap.options.minimap.frame.tooltip");
    public static final Text CIRCULAR = new TranslatableText("minimap.options.minimap.circular");
    public static final Text CIRCULAR_TOOLTIP = new TranslatableText("minimap.options.minimap.circular.tooltip");
    public static final Text DIRECTIONS = new TranslatableText("minimap.options.minimap.directions");
    public static final Text DIRECTIONS_TOOLTIP = new TranslatableText("minimap.options.minimap.directions.tooltip");
    public static final Text COORDINATES = new TranslatableText("minimap.options.minimap.bottom-text");
    public static final Text COORDINATES_TOOLTIP = new TranslatableText("minimap.options.minimap.bottom-text.tooltip");
    public static final Text CLOCK_TYPE = new TranslatableText("minimap.options.minimap.clock-type");
    public static final Text CLOCK_TYPE_TOOLTIP = new TranslatableText("minimap.options.minimap.clock-type.tooltip");
    public static final Text CLOCK_TYPE_REAL_TIME = new TranslatableText("minimap.options.minimap.clock-type.real-time");
    public static final Text CLOCK_TYPE_WORLD_TIME = new TranslatableText("minimap.options.minimap.clock-type.world-time");
    public static final Text CLOCK_FORMAT = new TranslatableText("minimap.options.minimap.clock-format");
    public static final Text CLOCK_FORMAT_TOOLTIP = new TranslatableText("minimap.options.minimap.clock-format.tooltip");
    public static final Text UPDATE_INTERVAL = new TranslatableText("minimap.options.minimap.update-interval");
    public static final Text UPDATE_INTERVAL_TOOLTIP = new TranslatableText("minimap.options.minimap.update-interval.tooltip");
    public static final Text POSITION = new TranslatableText("minimap.options.minimap.position");
    public static final Text POSITION_TOOLTIP = new TranslatableText("minimap.options.minimap.position.tooltip");
    public static final Text POSITION_ERROR = new TranslatableText("minimap.options.minimap.position.error").formatted(Formatting.RED);

    public static final Text YES = ScreenTexts.YES.copy().formatted(Formatting.GREEN);
    public static final Text NO = ScreenTexts.NO.copy().formatted(Formatting.RED);
}
