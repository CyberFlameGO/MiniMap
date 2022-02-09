package net.pl3x.minimap.gui.animation.sidebar;

import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.animation.Animation;
import net.pl3x.minimap.gui.screen.widget.category.Category;

public class IconSlideIn extends Animation {
    private final Category category;
    private final float start;
    private final float end;
    private float delay;

    public IconSlideIn(Category category, float start, float end, float delay) {
        this.category = category;
        this.start = start;
        this.end = end;
        this.delay = delay;
    }

    public void tick(float delta) {
        // wait for delay
        if ((this.delay -= delta) > 0.0F) {
            return;
        }

        // step each frame
        float step = Math.min((this.deltaSum += delta) / 6.0F, 1.0F);
        this.category.iconX = animate(this.start, this.end, step, Config.getConfig().animations.sidebar.iconSlideIn);

        // check if finished
        if (this.category.iconX == this.end) {
            this.remove = true;
        }
    }
}
