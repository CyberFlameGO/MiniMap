package net.pl3x.minimap.gui.screen.widget.element;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.pl3x.minimap.config.option.Option;
import net.pl3x.minimap.gui.animation.Animation;
import net.pl3x.minimap.gui.animation.Easing;
import net.pl3x.minimap.gui.font.Font;
import net.pl3x.minimap.gui.screen.widget.AnimatedWidget;
import net.pl3x.minimap.gui.screen.widget.Category;
import net.pl3x.minimap.gui.screen.widget.Widget;
import net.pl3x.minimap.gui.texture.Cursor;
import net.pl3x.minimap.gui.texture.Texture;
import net.pl3x.minimap.hardware.Monitor;
import net.pl3x.minimap.hardware.Mouse;

public class Checkbox extends AnimatedWidget {
    public static final int COLOR_OFF = 0xFFAA2031;
    public static final int COLOR_ON = 0xFF31AA20;

    private final Text label;
    private final float size;
    private final Option<Boolean> option;

    private float toggleX;
    private int color = COLOR_OFF;

    public Checkbox(Widget parent, Text label, float x, float y, Option<Boolean> option) {
        super(parent, x, y, 20 * 2, 20);
        this.label = label;
        this.size = 20;
        this.option = option;
    }

    @Override
    public Category parent() {
        return (Category) super.parent();
    }

    public float x() {
        return (Monitor.width() + parent().baseX()) / 2F + super.x();
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

        Texture.WIDGETS.tint(matrixStack, x(), y(), x() + this.size * 2, y() + this.size, 0F / 25.6F, 0F / 25.6F, 2F / 25.6F, 1F / 25.6F, this.color);
        Texture.WIDGETS.draw(matrixStack, x() + this.toggleX, y(), x() + this.size + this.toggleX, y() + this.size, 2F / 25.6F, 0F / 25.6F, 3F / 25.6F, 1F / 25.6F);

        Font.NOTOSANS.draw(matrixStack, this.label, x() + this.size * 2.5F + 1F, y() + 2F + 1F, 0x88000000);
        Font.NOTOSANS.draw(matrixStack, this.label, x() + this.size * 2.5F, y() + 2F, 0xFFFFFFFF);

        if (hovered()) {
            Mouse.INSTANCE.cursor(Cursor.HAND_POINTER);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hovered()) {
            this.option.set(!this.option.get());
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
            this.endColor = checkbox.option.get() ? COLOR_ON : COLOR_OFF;

            this.startX = checkbox.toggleX;
            this.endX = checkbox.option.get() ? checkbox.size : 0F;
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
