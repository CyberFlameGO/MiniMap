package net.pl3x.minimap.gui.screen.widget.category;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.pl3x.minimap.gui.Icon;
import net.pl3x.minimap.gui.screen.widget.AnimatedWidget;
import net.pl3x.minimap.gui.screen.widget.Sidebar;

public abstract class Category extends AnimatedWidget {
    private final Sidebar sidebar;
    private final Tab tab;

    protected float baseX;
    protected float baseY;

    public Category(Sidebar sidebar, float x, float y, float delay, Text text, Icon icon, float iconSize) {
        super(sidebar, x, y, sidebar.width(), iconSize);

        this.sidebar = sidebar;
        this.tab = new Tab(this, x, y, delay, text, icon, iconSize);

        this.baseX = Sidebar.HOVER_WIDTH;
        this.baseY = 0F;

        children().add(this.tab);
    }

    public Sidebar sidebar() {
        return this.sidebar;
    }

    public Tab tab() {
        return this.tab;
    }

    @Override
    public float width() {
        return Math.min(parent().width(), Sidebar.HOVER_WIDTH);
    }

    @Override
    public void render(MatrixStack matrixStack, float mouseX, float mouseY, float delta) {
        super.render(matrixStack, mouseX, mouseY, delta);

        if (sidebar().openedCategory() == this) {
            renderContent(matrixStack, mouseX, mouseY, delta);
        }
    }

    public abstract void renderContent(MatrixStack matrixStack, float mouseX, float mouseY, float delta);
}
