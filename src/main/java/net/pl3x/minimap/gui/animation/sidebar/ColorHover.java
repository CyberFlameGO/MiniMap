package net.pl3x.minimap.gui.animation.sidebar;

import net.pl3x.minimap.gui.animation.Animation;
import net.pl3x.minimap.gui.animation.Easing;
import net.pl3x.minimap.gui.screen.widget.element.Tab;

public class ColorHover extends Animation {
    private final Tab tab;

    private int start = Tab.DEFAULT_COLOR;
    private int end = Tab.DEFAULT_COLOR;

    public Easing.Func easingFunc;

    public ColorHover(Tab tab) {
        this.tab = tab;
    }

    @Override
    public void tick(float delta) {
        // check if animation needed
        if (this.tab.color() == this.end) {
            return;
        }

        // step each frame
        float step = Math.min((this.deltaSum += delta) / 7.5F, 1F);
        this.tab.color(animateARGB(this.start, this.end, step, this.easingFunc));
    }

    public void set(int color, Easing.Func easingFunc) {
        this.easingFunc = easingFunc;
        this.start = this.tab.color();
        this.end = color;
        this.deltaSum = 0F;
    }
}
