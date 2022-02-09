package net.pl3x.minimap.gui.screen.widget;

import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.gui.GL;
import net.pl3x.minimap.gui.animation.sidebar.FirstOpen;
import net.pl3x.minimap.gui.animation.sidebar.ToggleHover;
import net.pl3x.minimap.gui.screen.AbstractScreen;
import net.pl3x.minimap.gui.screen.widget.category.AboutCategory;
import net.pl3x.minimap.gui.screen.widget.category.Category;
import net.pl3x.minimap.gui.screen.widget.category.ClockCategory;
import net.pl3x.minimap.gui.screen.widget.category.LayersCategory;
import net.pl3x.minimap.gui.screen.widget.category.PositionCategory;
import net.pl3x.minimap.gui.screen.widget.category.RadarCategory;
import net.pl3x.minimap.gui.screen.widget.category.StyleCategory;
import net.pl3x.minimap.gui.screen.widget.category.WaypointsCategory;

import java.util.ArrayList;
import java.util.List;

public class Sidebar extends AnimatedWidget {
    public static final float DEFAULT_WIDTH = 42.0F;
    public static final float HOVER_WIDTH = 180.0F;

    private final AbstractScreen screen;

    private final List<Category> categories = new ArrayList<>();
    private Category openedCategory;

    public final ToggleHover toggleAnimation;

    public Sidebar(AbstractScreen screen) {
        super(null, 0, 0, 0, screen.height);
        this.screen = screen;
        this.toggleAnimation = new ToggleHover(this);

        addAnimation(new FirstOpen(this));
        addAnimation(this.toggleAnimation);
    }

    public AbstractScreen screen() {
        return this.screen;
    }

    public void init() {
        super.init();

        height(this.screen.height);

        if (this.categories.isEmpty()) {
            this.categories.addAll(List.of(
                    new StyleCategory(this, 0, 20, 0.0F, 32),
                    new PositionCategory(this, 0, 70, 0.5F, 32),
                    new RadarCategory(this, 0, 120, 1.0F, 32),
                    new WaypointsCategory(this, 0, 170, 1.5F, 32),
                    new LayersCategory(this, 0, 220, 2.0F, 32),
                    new ClockCategory(this, 0, 270, 2.5F, 32),
                    new AboutCategory(this, 0, 320, 3.0F, 32)
            ));
        }
    }

    public void render(MatrixStack matrixStack, float mouseX, float mouseY, float delta) {
        super.render(matrixStack, mouseX, mouseY, delta);

        GL.drawSolidRect(matrixStack, 0, 0, width(), height(), 0x99000000, 0xBB000000);

        this.categories.forEach(category -> category.render(matrixStack, mouseX, mouseY, delta));
    }

    @Override
    public void onHoverChange() {
        this.toggleAnimation.hover(hovered());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Category category : this.categories) {
            if (category.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
