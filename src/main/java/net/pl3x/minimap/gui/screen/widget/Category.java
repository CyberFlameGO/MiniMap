package net.pl3x.minimap.gui.screen.widget;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.pl3x.minimap.gui.Icon;
import net.pl3x.minimap.gui.animation.sidebar.CategorySlide;
import net.pl3x.minimap.gui.screen.widget.element.Tab;
import net.pl3x.minimap.hardware.Monitor;

public abstract class Category extends AnimatedWidget {
    private final Tab tab;

    private float baseX;
    private float baseY;

    public Category(Text text, Icon icon, float y, float delay) {
        super(null, 0F, y, 0F, 0F);

        this.tab = new Tab(this, y, delay, text, icon, 32F);

        baseX(Sidebar.HOVER_WIDTH);
        baseY(0F);
    }

    public Tab tab() {
        return this.tab;
    }

    public float baseX() {
        return this.baseX;
    }

    public void baseX(float baseX) {
        this.baseX = baseX;
    }

    public float baseY() {
        return this.baseY;
    }

    public void baseY(float baseY) {
        this.baseY = baseY;
    }

    public boolean selected() {
        return Sidebar.INSTANCE.selected() == this;
    }

    @Override
    public float width() {
        return Monitor.width();
    }

    @Override
    public float height() {
        return Monitor.height();
    }

    @Override
    public boolean hovered() {
        // only considered hovered if this category is selected
        return selected() && super.hovered();
    }

    @Override
    public void render(MatrixStack matrixStack, float mouseX, float mouseY, float delta) {
        // always draw tabs
        this.tab.render(matrixStack, mouseX, mouseY, delta);

        // only draw children and content if this category is selected or is animating
        if (selected() || !animations().isEmpty()) {
            super.render(matrixStack, mouseX, mouseY, delta);
            renderContent(matrixStack, mouseX, mouseY, delta);
        }
    }

    public abstract void renderContent(MatrixStack matrixStack, float mouseX, float mouseY, float delta);

    @Override
    public boolean mouseClicked(float mouseX, float mouseY, int button) {
        // only listen to mouse clicks if this category is selected
        return this.tab.mouseClicked(mouseX, mouseY, button) || (selected() && super.mouseClicked(mouseX, mouseY, button));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // only listen to key presses if this category is selected
        return selected() && super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void open(float delay) {
        this.baseY(-Monitor.height());
        animations().clear();
        addAnimation(new CategorySlide(this, baseY(), 0F, delay));
    }

    public void close() {
        animations().clear();
        addAnimation(new CategorySlide(this, baseY(), Monitor.height(), 0F));
    }
}
