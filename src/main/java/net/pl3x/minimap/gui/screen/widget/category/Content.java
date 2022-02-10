package net.pl3x.minimap.gui.screen.widget.category;

import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.gui.GL;
import net.pl3x.minimap.gui.screen.widget.AnimatedWidget;

public class Content extends AnimatedWidget {
    private final Tab tab;

    public Content(Tab tab, float x, float y) {
        super(tab, x, y, 0F, 0F);

        this.tab = tab;
    }

    public Tab tab() {
        return this.tab;
    }

    @Override
    public float width() {
        return GL.width();
    }

    @Override
    public float height() {
        return GL.height();
    }

    @Override
    public void render(MatrixStack matrixStack, float mouseX, float mouseY, float delta) {
        super.render(matrixStack, mouseX, mouseY, delta);
    }
}
