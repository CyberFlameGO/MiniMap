package net.pl3x.minimap.gui.screen.widget.category;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.GL;
import net.pl3x.minimap.gui.Icon;
import net.pl3x.minimap.gui.animation.sidebar.ColorHover;
import net.pl3x.minimap.gui.animation.sidebar.IconSlideIn;
import net.pl3x.minimap.gui.font.Font;
import net.pl3x.minimap.gui.screen.widget.AnimatedWidget;
import net.pl3x.minimap.gui.screen.widget.Sidebar;
import net.pl3x.minimap.gui.texture.Cursor;
import net.pl3x.minimap.hardware.Mouse;

public class Tab extends AnimatedWidget {
    public static final int DEFAULT_COLOR = 0xAAFFFFFF;
    public static final int HOVER_COLOR = 0xFF3399FF;
    public static final int ACTIVE_COLOR = 0xFFFF9933;

    private final Category category;

    private final Icon icon;
    private final float iconSize;
    private float iconX;
    private float iconY;

    private final Text text;
    private float textX;
    private float textY;

    private final ColorHover colorHoverAnimation;
    private int color;

    public Tab(Category category, float x, float y, float delay, Text text, Icon icon, float iconSize) {
        super(category, x, y, 0F, iconSize);

        this.category = category;

        this.icon = icon;
        this.iconSize = iconSize;
        iconX(x - iconSize);
        iconY(y);

        this.text = text;
        textX(x + iconSize + 15F);
        textY(y + 5F);

        this.colorHoverAnimation = new ColorHover(this);
        color(Tab.DEFAULT_COLOR);

        addAnimation(new IconSlideIn(this, this.iconX, x + 5F, delay));
        addAnimation(this.colorHoverAnimation);
    }

    public Category category() {
        return this.category;
    }

    public Icon icon() {
        return this.icon;
    }

    public float iconSize() {
        return this.iconSize;
    }

    public float iconX() {
        return this.iconX;
    }

    public void iconX(float iconX) {
        this.iconX = iconX;
    }

    public float iconY() {
        return this.iconY;
    }

    public void iconY(float iconY) {
        this.iconY = iconY;
    }

    public Text text() {
        return this.text;
    }

    public float textX() {
        return this.textX;
    }

    public void textX(float textX) {
        this.textX = textX;
    }

    public float textY() {
        return this.textY;
    }

    public void textY(float textY) {
        this.textY = textY;
    }

    public int color() {
        return this.color;
    }

    public void color(int color) {
        this.color = color;
    }

    @Override
    public float width() {
        return parent().width();
    }

    @Override
    public void render(MatrixStack matrixStack, float mouseX, float mouseY, float delta) {
        super.render(matrixStack, mouseX, mouseY, delta);

        this.icon.draw(matrixStack, iconX(), iconY(), iconSize(), color());

        if (parent().width() > Sidebar.DEFAULT_WIDTH) {
            if (parent().width() < Sidebar.HOVER_WIDTH) {
                // only trim text if sidebar is between open and
                // closed states since this is sort of expensive
                StringVisitable str = Font.RALEWAY_MEDIUM.trimToWidth(text(), (int) (parent().width() - textX()));
                for (OrderedText orderedText : Font.RALEWAY_MEDIUM.wrapLines(str, Integer.MAX_VALUE)) {
                    Font.RALEWAY_MEDIUM.draw(matrixStack, orderedText, textX(), textY(), color());
                }
            } else {
                Font.RALEWAY_MEDIUM.draw(matrixStack, text(), textX(), textY(), color());
            }
        }

        if (hovered()) {
            Mouse.INSTANCE.cursor(Cursor.HAND);
        }
    }

    @Override
    public void onHoverChange() {
        updateTabColor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hovered()) {
            if (category().sidebar().openedCategory() != category()) {
                category().sidebar().openedCategory(category());
                category().sidebar().open(GL.width());
            } else {
                category().sidebar().close(false);
            }
            category().sidebar().updateTabColors();
            return true;
        }
        return false;
    }

    public void updateTabColor() {
        if (hovered()) {
            this.colorHoverAnimation.set(HOVER_COLOR, Config.getConfig().animations.sidebar.colorHoverOn);
        } else if (category().sidebar().openedCategory() == category()) {
            this.colorHoverAnimation.set(ACTIVE_COLOR, Config.getConfig().animations.sidebar.colorActive);
        } else {
            this.colorHoverAnimation.set(DEFAULT_COLOR, Config.getConfig().animations.sidebar.colorHoverOff);
        }
    }
}
