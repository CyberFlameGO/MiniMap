package net.pl3x.minimap.gui.layer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.gui.screen.widget.element.EntityMarker;
import org.lwjgl.opengl.GL11;

public class Players extends Layer {
    private final EntityMarker entityMarker;

    public Players() {
        this.entityMarker = new EntityMarker(null, null);
    }

    @Override
    public void render(MatrixStack matrixStack, float delta) {
        // use a blend that supports translucent pixels
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // show self
        matrixStack.push();
        this.entityMarker.x(mm.getCenterX());
        this.entityMarker.y(mm.getCenterY());
        this.entityMarker.width(16F);
        this.entityMarker.height(16F);
        this.entityMarker.render(matrixStack, false, 1F);
        matrixStack.pop();
    }
}
