package net.pl3x.minimap.gui.layer;

import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.MiniMap;

public abstract class Layer {
    protected final MiniMap mm;

    public Layer() {
        this.mm = MiniMap.INSTANCE;
    }

    public abstract void render(MatrixStack matrixStack, float delta);

    public void update() {
    }

    public void stop() {
    }
}
