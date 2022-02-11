package net.pl3x.minimap.gui.texture.meta;

import com.google.gson.JsonObject;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.JsonHelper;

public record CursorMetadata(int hotX, int hotY, int width, int height, int frames, int frametime) {
    public static final CursorMetadataReader READER = new CursorMetadataReader();
    public static final String KEY = "cursor";

    public CursorMetadata() {
        this(0, 0, 32, 32, 0, 0);
    }

    public static class CursorMetadataReader implements ResourceMetadataReader<CursorMetadata> {
        @Override
        public CursorMetadata fromJson(JsonObject json) {
            int hotX = 0, hotY = 0, width = 0, height = 0, frames = 0, frametime = 0;
            JsonObject hotspot = json.getAsJsonObject("hotspot");
            if (hotspot != null) {
                hotX = JsonHelper.getInt(hotspot, "x", hotX);
                hotY = JsonHelper.getInt(hotspot, "y", hotY);
            }
            JsonObject size = json.getAsJsonObject("size");
            if (size != null) {
                width = JsonHelper.getInt(size, "width", width);
                height = JsonHelper.getInt(size, "height", height);
            }
            JsonObject animation = json.getAsJsonObject("animation");
            if (animation != null) {
                frames = JsonHelper.getInt(animation, "frames", frames);
                frametime = JsonHelper.getInt(animation, "frametime", frametime);
            }
            return new CursorMetadata(hotX, hotY, width, height, frames, frametime);
        }

        @Override
        public String getKey() {
            return CursorMetadata.KEY;
        }
    }
}
