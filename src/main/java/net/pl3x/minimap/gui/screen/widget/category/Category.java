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

public abstract class Category extends AnimatedWidget {
    public static final int DEFAULT_COLOR = 0xAAFFFFFF;
    public static final int HOVER_COLOR = 0xFF3399FF;

    public final Sidebar sidebar;

    public final Icon icon;
    public final float iconSize;
    public float iconX;
    public float iconY;

    public final Text text;
    public float textX;
    public float textY;

    public int color = Category.DEFAULT_COLOR;

    private final ColorHover colorHoverAnimation;

    public Category(Sidebar sidebar, float x, float y, float delay, Text text, Icon icon, float iconSize) {
        super(sidebar, x, y, sidebar.width(), iconSize);

        this.sidebar = sidebar;

        this.icon = icon;
        this.iconSize = iconSize;
        this.iconX = x - iconSize;
        this.iconY = y;

        this.text = text;
        this.textX = x + iconSize + 15;
        this.textY = y + 5;

        this.colorHoverAnimation = new ColorHover(this);

        addAnimation(new IconSlideIn(this, this.iconX, x + 5, delay));
        addAnimation(this.colorHoverAnimation);
    }

    @Override
    public void init() {
    }

    @Override
    public float width() {
        return parent().width();
    }

    @Override
    public void render(MatrixStack matrixStack, float mouseX, float mouseY, float delta) {
        super.render(matrixStack, mouseX, mouseY, delta);
        renderIcon(matrixStack);
        renderLabel(matrixStack);
        renderContent(matrixStack);
        if (hovered()) {
            // todo on hover icon/label only
            Mouse.INSTANCE.cursor(Cursor.HAND);
        }
    }

    public void renderIcon(MatrixStack matrixStack) {
        this.icon.draw(matrixStack, this.iconX, this.iconY, this.iconSize, this.color);
    }

    public void renderLabel(MatrixStack matrixStack) {
        if (this.parent().width() > Sidebar.DEFAULT_WIDTH) {
            if (this.parent().width() < Sidebar.HOVER_WIDTH) {
                // only trim text if sidebar is between open and
                // closed states since this is sort of expensive
                StringVisitable str = Font.RALEWAY_MEDIUM.trimToWidth(this.text, (int) (this.parent().width() - this.textX));
                for (OrderedText orderedText : Font.RALEWAY_MEDIUM.wrapLines(str, Integer.MAX_VALUE)) {
                    Font.RALEWAY_MEDIUM.draw(matrixStack, orderedText, this.textX, this.textY, this.color);
                }
            } else {
                Font.RALEWAY_MEDIUM.draw(matrixStack, this.text, this.textX, this.textY, this.color);
            }
        }
    }

    public void renderContent(MatrixStack matrixStack) {
        //
    }

    @Override
    public void onHoverChange() {
        //this.colorHoverAnimation.hover(hovered());
        updateTabColor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hovered()) {
            if (this.sidebar.openedCategory != this) {
                this.sidebar.openedCategory = this;
                this.sidebar.open(GL.width());
            } else {
                this.sidebar.close(false);
            }
            this.sidebar.updateTabColors();
            return true;
        }
        return false;
    }

    public void updateTabColor() {
        if (hovered() || this.sidebar.openedCategory == this) {
            this.colorHoverAnimation.set(HOVER_COLOR, Config.getConfig().animations.sidebar.colorHoverOn);
        } else {
            this.colorHoverAnimation.set(DEFAULT_COLOR, Config.getConfig().animations.sidebar.colorHoverOff);
        }
    }
}
