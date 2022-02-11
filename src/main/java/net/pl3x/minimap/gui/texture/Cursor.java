package net.pl3x.minimap.gui.texture;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.gui.texture.meta.AnimationMetadata;
import net.pl3x.minimap.gui.texture.meta.CursorMetadata;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Cursor extends Drawable {
    private static final Set<Cursor> REGISTERED_CURSORS = new HashSet<>();

    public static final Cursor ARROW = register(new Cursor("arrow", Texture.CURSOR_ARROW));
    public static final Cursor HAND = register(new Cursor("hand", Texture.CURSOR_HAND));

    private static Cursor register(Cursor cursor) {
        REGISTERED_CURSORS.add(cursor);
        return cursor;
    }

    public static void initialize() {
        REGISTERED_CURSORS.forEach(cursor -> {
            try {
                Resource resource = cursor.resource();
                if (resource.hasMetadata()) {
                    cursor.meta = cursor.load(resource);
                } else {
                    cursor.meta = new CursorMetadata();
                }
                cursor.texture.meta = new AnimationMetadata(
                        cursor.meta.width(),
                        cursor.meta.height(),
                        cursor.meta.frames(),
                        cursor.meta.frametime()
                );
                MiniMap.LOG.info("Loaded mouse cursor " + cursor.identifier);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private final Texture texture;
    private CursorMetadata meta;

    public Cursor(String name, Texture texture) {
        super(new Identifier(MiniMap.MODID, "textures/cursor/" + name + ".png"));
        this.texture = texture;
    }

    protected CursorMetadata load(Resource resource) {
        return resource.getMetadata(CursorMetadata.READER);
    }

    protected Resource resource() throws IOException {
        return MiniMap.CLIENT.getResourceManager().getResource(this.identifier);
    }

    public void draw(MatrixStack matrixStack, float x, float y, float delta) {
        this.texture.animate(matrixStack, x - this.meta.hotX(), y - this.meta.hotY(), delta);
    }
}
