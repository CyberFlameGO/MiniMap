package net.pl3x.minimap.gui.texture;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.gui.texture.meta.CursorMetadata;
import net.pl3x.minimap.hardware.Mouse;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Cursor extends Drawable {
    private static final Set<Cursor> REGISTERED_CURSORS = new HashSet<>();

    public static final Cursor ARROW = register(new Cursor("arrow"));
    public static final Cursor HAND = register(new Cursor("hand"));

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
                }
                MiniMap.LOG.info("Loaded mouse cursor " + cursor.identifier);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    protected CursorMetadata meta;

    protected float x;
    protected float y;

    private float time;
    private int frame;

    public Cursor(String name) {
        super(new Identifier(MiniMap.MODID, "textures/cursor/" + name + ".png"));
    }

    protected CursorMetadata load(Resource resource) {
        return resource.getMetadata(CursorMetadata.READER);
    }

    protected Resource resource() throws IOException {
        return MiniMap.CLIENT.getResourceManager().getResource(this.identifier);
    }

    public void animate(MatrixStack matrixStack, float delta) {
        this.x = Mouse.INSTANCE.x() - this.meta.hotX();
        this.y = Mouse.INSTANCE.y() - this.meta.hotY();

        float u, v;

        if (this.meta.frames() > 0) {
            if ((this.time += delta) >= this.meta.frametime()) {
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

        draw(matrixStack, this.x, this.y, this.x + this.meta.width(), this.y + this.meta.height(), 0F, u, 1F, v);
    }
}
