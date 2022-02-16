package net.pl3x.minimap.gui.screen.widget.element;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.pl3x.minimap.config.option.Option;
import net.pl3x.minimap.gui.font.Font;
import net.pl3x.minimap.gui.screen.widget.AnimatedWidget;
import net.pl3x.minimap.gui.screen.widget.Category;
import net.pl3x.minimap.gui.screen.widget.Widget;
import net.pl3x.minimap.gui.texture.Texture;
import net.pl3x.minimap.hardware.Monitor;
import net.pl3x.minimap.util.Mathf;

public class Slider extends AnimatedWidget {
    private final Text label;
    private final float size;

    private final float min;
    private final float max;
    private final Option<Integer> option;

    public Slider(Widget parent, Text label, float x, float y, float min, float max, Option<Integer> option) {
        super(parent, x, y, 100, 20);
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
    }

    @Override
    public void render(MatrixStack matrixStack, float mouseX, float mouseY, float delta) {
        super.render(matrixStack, mouseX, mouseY, delta);

        float x = Mathf.inverseLerp(this.min, this.max, this.option.get());

        Texture.WIDGETS.draw(matrixStack, x(), y(), x() + this.size / 2, y() + this.size, 0F / 25.6F, 0F / 25.6F, 0.5F / 25.6F, 1F / 25.6F);
        Texture.WIDGETS.draw(matrixStack, x() + width() + this.size / 2, y(), x() + width() + this.size, y() + this.size, 1.5F / 25.6F, 0F / 25.6F, 2F / 25.6F, 1F / 25.6F);
        Texture.WIDGETS.draw(matrixStack, x() + this.size / 2, y(), x() + width() + this.size / 2, y() + this.size, 0.5F / 25.6F, 0F / 25.6F, 0.5F / 25.6F, 1F / 25.6F);

        Texture.WIDGETS.tint(matrixStack, x() + this.width() * x, y(), x() + this.size + this.width() * x, y() + this.size, 2F / 25.6F, 0F / 25.6F, 3F / 25.6F, 1F / 25.6F, 0xFF20A1FF);

        //Font.NOTOSANS.draw(matrixStack, this.label, x() + this.size * 2.5F + 1F, y() + 2F + 1F, 0x88000000);
        Font.NOTOSANS.draw(matrixStack, this.label, x() + this.width() + 30F, y() + 2F, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }
}
