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
import net.pl3x.minimap.util.Mathf;

public class Slider extends AnimatedWidget {
    private final Text label;
    private final float size;

    private final float min;
    private final float max;
    private final Option<Integer> option;

    private boolean dragging;

    private float valX;

    public Slider(Widget parent, Text label, float x, float y, float min, float max, Option<Integer> option) {
        super(parent, x, y, 150, 20);
        this.label = label;
        this.size = 20;
        this.min = min;
        this.max = max;
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
        addAnimation(new SetValue(this));
    }

    @Override
    public void render(MatrixStack matrixStack, float mouseX, float mouseY, float delta) {
        super.render(matrixStack, mouseX, mouseY, delta);

        Font.NOTOSANS.draw(matrixStack, this.label.getString() + " " + this.option.get(), x() + 1F, y() + 1F, 0x88000000);
        Font.NOTOSANS.draw(matrixStack, this.label.getString() + " " + this.option.get(), x(), y(), 0xFFFFFFFF);

        Texture.WIDGETS.tint(matrixStack, x(), y() + 20, x() + this.size / 2, y() + this.size + 20, 0F / 25.6F, 1F / 25.6F, 0.5F / 25.6F, 2F / 25.6F, 0xFF79848c);
        Texture.WIDGETS.tint(matrixStack, x() + width() + this.size / 2, y() + 20, x() + width() + this.size, y() + this.size + 20, 1.5F / 25.6F, 1F / 25.6F, 2F / 25.6F, 2F / 25.6F, 0xFF79848c);
        Texture.WIDGETS.tint(matrixStack, x() + this.size / 2, y() + 20, x() + width() + this.size / 2, y() + this.size + 20, 0.5F / 25.6F, 1F / 25.6F, 0.5F / 25.6F, 2F / 25.6F, 0xFF79848c);

        Texture.WIDGETS.draw(matrixStack, x() + this.width() * this.valX, y() + 20, x() + this.size + this.width() * this.valX, y() + this.size + 20, 2F / 25.6F, 0F / 25.6F, 3F / 25.6F, 1F / 25.6F);

        if (hovered()) {
            Mouse.INSTANCE.cursor(Cursor.HAND_POINTER);
        }
    }

    @Override
    public boolean isMouseOver(float mouseX, float mouseY) {
        return mouseX >= x() && mouseX <= x() + width() + this.size && mouseY >= y() + 20 && mouseY <= y() + height() + 20;
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY, int button) {
        if (hovered()) {
            setValueFromMouse(mouseX);
            this.dragging = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(float mouseX, float mouseY, int button) {
        this.dragging = false;
        return hovered();
    }

    @Override
    public boolean mouseDragged(float mouseX, float mouseY, int button, float deltaX, float deltaY) {
        if (button == 0 && this.dragging) {
            setValueFromMouse(mouseX);
            return true;
        }
        return false;
    }

    public void setValueFromMouse(float mouseX) {
        float pos = (mouseX - x() - this.size / 2F) / width();
        float percent = Mathf.lerp(this.min, this.max, pos);
        int val = Math.round(Mathf.clamp(this.min, this.max, percent));
        this.option.set(val);
        animations().clear();
        addAnimation(new SetValue(this));
    }

    private static class SetValue extends Animation {
        private final Slider slider;

        private final float start;
        private final float end;

        private SetValue(Slider slider) {
            this.slider = slider;

            this.start = slider.valX;
            this.end = Mathf.inverseLerp(this.slider.min, this.slider.max, slider.option.get());
        }

        @Override
        public void tick(float delta) {
            // step each frame
            float step = Math.min((this.deltaSum += delta) / 2.5F, 1F);
            this.slider.valX = animate(this.start, this.end, step, Easing.Cubic.out);

            // check if finished
            if (step >= 1F) {
                this.remove = true;
            }
        }
    }
}
