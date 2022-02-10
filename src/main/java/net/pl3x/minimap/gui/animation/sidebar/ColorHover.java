package net.pl3x.minimap.gui.animation.sidebar;

import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.animation.Animation;
import net.pl3x.minimap.gui.animation.Easing;
import net.pl3x.minimap.gui.screen.widget.category.Category;

public class ColorHover extends Animation {
    private final Category category;

    private int start = Category.DEFAULT_COLOR;
    private int end = Category.DEFAULT_COLOR;

    public Easing.Func easingFunc;

    public ColorHover(Category category) {
        this.category = category;
    }

    public void tick(float delta) {
        // check if animation needed
        if (this.category.color == this.end) {
            return;
        }

        // step each frame
        float step = Math.min((this.deltaSum += delta) / 5F, 1.0F);
        this.category.color = animateARGB(this.start, this.end, step, this.easingFunc);
    }

    public void set(int color, Easing.Func easingFunc) {
        this.easingFunc = easingFunc;
        this.start = this.category.color;
        this.end = color;
        this.deltaSum = 0.0F;
    }

    public void hover(boolean hovered) {
        this.easingFunc = hovered ? Config.getConfig().animations.sidebar.colorHoverOn : Config.getConfig().animations.sidebar.colorHoverOff;
        this.start = this.category.color;
        this.end = hovered ? Category.HOVER_COLOR : Category.DEFAULT_COLOR;
        this.deltaSum = 0.0F;
    }
}
