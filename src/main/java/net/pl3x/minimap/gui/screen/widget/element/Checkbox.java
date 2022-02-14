package net.pl3x.minimap.gui.screen.widget.element;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.pl3x.minimap.gui.animation.Animation;
import net.pl3x.minimap.gui.animation.Easing;
import net.pl3x.minimap.gui.font.Font;
import net.pl3x.minimap.gui.screen.widget.AnimatedWidget;
import net.pl3x.minimap.gui.screen.widget.Category;
import net.pl3x.minimap.gui.texture.Texture;
import net.pl3x.minimap.util.Colors;

public class Checkbox extends AnimatedWidget {
    public static final int COLOR_OFF = 0xFFC0CCDA;
    public static final int COLOR_ON = 0xFF20A0FF;
    //public static final int COLOR_DISABLED_ON = 0xFFB0D7F5;
    //public static final int COLOR_DISABLED_OFF = 0xFFD3DCE6;

    private final Text label;
    private final float size;

    private boolean value;

    private float toggleX;
    private int color = 0xFFC0CCDA;

    public Checkbox(Category parent, Text label, float x, float y) {
        super(parent, x, y, 20 * 2, 20);
        this.label = label;
        this.size = 20;
    }

    @Override
    public Category parent() {
        return (Category) super.parent();
    }

    public float x() {
        return super.x() + parent().baseX();
    }

    public float y() {
        return super.y() + parent().baseY();
    }

    @Override
    public void init() {
        animations().clear();
        addAnimation(new ToggleValue(this));
    }

    @Override
    public void render(MatrixStack matrixStack, float mouseX, float mouseY, float delta) {
        super.render(matrixStack, mouseX, mouseY, delta);

        RenderSystem.setShaderColor(Colors.red(this.color) / 255F, Colors.green(this.color) / 255F, Colors.blue(this.color) / 255F, Colors.alpha(this.color) / 255F);
        Texture.ICONS.draw(matrixStack, x(), y(), x() + this.size * 2, y() + this.size, 0F / 16F, 14F / 16F, 2F / 16F, 15F / 16F);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        Texture.ICONS.draw(matrixStack, x() + this.toggleX, y(), x() + this.size + this.toggleX, y() + this.size, 2F / 16F, 14F / 16F, 3F / 16F, 15F / 16F);

        Font.NOTOSANS.draw(matrixStack, this.label, x() + this.size * 2.5F, y() + 3F, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hovered()) {
            this.value = !this.value;
            animations().clear();
            addAnimation(new ToggleValue(this));
            return true;
        }
        return false;
    }

    private static class ToggleValue extends Animation {
        private final Checkbox checkbox;

        private final int startColor;
        private final int endColor;

        private final float startX;
        private final float endX;

        private ToggleValue(Checkbox checkbox) {
            this.checkbox = checkbox;

            this.startColor = checkbox.color;
            this.endColor = checkbox.value ? COLOR_ON : COLOR_OFF;

            this.startX = checkbox.toggleX;
            this.endX = checkbox.value ? checkbox.size : 0F;
        }

        @Override
        public void tick(float delta) {
            // step each frame
            float step = Math.min((this.deltaSum += delta) / 10F, 1F);
            this.checkbox.color = animateARGB(this.startColor, this.endColor, step, Easing.Bounce.out);
            this.checkbox.toggleX = animate(this.startX, this.endX, step, Easing.Bounce.out);

            // check if finished
            if (step >= 1F) {
                this.remove = true;
            }
        }
    }
}
