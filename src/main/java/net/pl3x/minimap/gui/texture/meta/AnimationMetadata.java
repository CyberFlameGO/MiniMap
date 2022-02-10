package net.pl3x.minimap.gui.texture.meta;

import com.google.gson.JsonObject;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.JsonHelper;

public record AnimationMetadata(int width, int height, int frames, int frametime) {
    public static final AnimationMetadataReader READER = new AnimationMetadataReader();
    public static final String KEY = "animation";

    public float width(float width) {
        return this.width > 0F ? this.width : width;
    }

    public float height(float height) {
        return this.height > 0F ? this.height : height;
    }

    public static class AnimationMetadataReader implements ResourceMetadataReader<AnimationMetadata> {
        @Override
        public AnimationMetadata fromJson(JsonObject json) {
            int width = 0, height = 0, frames = 0, frametime = 0;
            JsonObject size = json.getAsJsonObject("size");
            if (size != null) {
                width = JsonHelper.getInt(size, "width", width);
                height = JsonHelper.getInt(size, "height", height);
            }
            frames = JsonHelper.getInt(json, "frames", frames);
            frametime = JsonHelper.getInt(json, "frametime", frametime);
            return new AnimationMetadata(width, height, frames, frametime);
        }

        @Override
        public String getKey() {
            return AnimationMetadata.KEY;
        }
    }
}
