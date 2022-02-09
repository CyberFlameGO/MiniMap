package net.pl3x.minimap.gui.texture;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.gui.texture.meta.AnimationMetadata;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Texture extends Drawable {
    private static final Set<Texture> REGISTERED_TEXTURES = new HashSet<>();

    public static final Texture ICON = register(new Texture("icon"));
    public static final Texture ICONS = register(new Texture("gui/icons"));
    public static final Texture FRAME_CIRCLE = register(new Texture("gui/map/frame_circle"));
    public static final Texture FRAME_SQUARE = register(new Texture("gui/map/frame_square"));
    public static final Texture MASK_CIRCLE = register(new Texture("gui/map/mask_circle"));
    public static final Texture MASK_SQUARE = register(new Texture("gui/map/mask_square"));
    public static final Texture MINIMAP = register(new Texture("gui/minimap"));
    public static final Texture PLAYER = register(new Texture("gui/player"));
    public static final Texture SKY_OVERWORLD = register(new Texture("gui/sky/overworld"));
    public static final Texture SKY_THE_NETHER = register(new Texture("gui/sky/the_nether"));
    public static final Texture SKY_THE_END = register(new Texture("gui/sky/the_end"));

    private static Texture register(Texture texture) {
        REGISTERED_TEXTURES.add(texture);
        return texture;
    }

    public static void initialize() {
        REGISTERED_TEXTURES.forEach(texture -> {
            try {
                Resource resource = texture.resource();
                if (resource.hasMetadata()) {
                    texture.meta = texture.load(resource);
                }
                MiniMap.LOG.info("Loaded texture " + texture.identifier);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private AnimationMetadata meta;
    private float time;
    private int frame;

    public Texture(String name) {
        super(new Identifier(MiniMap.MODID, "textures/" + name + ".png"));
    }

    protected AnimationMetadata load(Resource resource) {
        return resource.getMetadata(AnimationMetadata.READER);
    }

    protected Resource resource() throws IOException {
        return MiniMap.CLIENT.getResourceManager().getResource(this.identifier);
    }

    public void animate(MatrixStack matrixStack, float x, float y, float width, float height, float delta) {
        float u, v;
        if (this.meta.frames() > 0) {
            if ((this.time += delta) >= this.meta.frametime()) {
                this.time = 0;
                if (++this.frame >= this.meta.frames()) {
                    this.frame = 0;
                }
            }
            float h = 1.0F / this.meta.frames();
            u = h * this.frame;
            v = u + h;
        } else {
            u = 0.0F;
            v = 1.0F;
        }

        draw(matrixStack, x, y, x + this.meta.width(width), y + this.meta.height(height), 0.0F, u, 1.0F, v);
    }
}
