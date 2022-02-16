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
    public static final Texture WIDGETS = register(new Texture("gui/widgets"));

    public static final Texture CURSOR_ARROW = register(new Texture("cursor/arrow"));
    public static final Texture CURSOR_HAND_GRAB = register(new Texture("cursor/hand_grab"));
    public static final Texture CURSOR_HAND_OPEN = register(new Texture("cursor/hand_open"));
    public static final Texture CURSOR_HAND_POINTER = register(new Texture("cursor/hand_pointer"));

    private static Texture register(Texture texture) {
        REGISTERED_TEXTURES.add(texture);
        return texture;
    }

    public static void initialize() {
        REGISTERED_TEXTURES.forEach(texture -> {
            try {
                Resource resource = texture.resource();
                if (resource.hasMetadata()) {
                    System.out.println(texture.identifier + " has meta");
                    texture.meta = texture.load(resource);
                } else {
                    texture.meta = new AnimationMetadata();
                }
                MiniMap.LOG.info("Loaded texture " + texture.identifier);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    protected AnimationMetadata meta;

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

    public void animate(MatrixStack matrixStack, float x, float y, float delta) {
        float u, v;
        if (this.meta.frames() > 0) {
            this.time += delta;
            if (this.time >= this.meta.frametime()) {
                this.time = 0F;
                if (++this.frame >= this.meta.frames()) {
                    this.frame = 0;
                }
            }
            float h = 1F / this.meta.frames();
            u = h * this.frame;
            v = u + h;
        } else {
            u = 0F;
            v = 1F;
        }

        draw(matrixStack, x, y, x + this.meta.width(128F), y + this.meta.height(128F), 0F, u, 1F, v);
    }
}
