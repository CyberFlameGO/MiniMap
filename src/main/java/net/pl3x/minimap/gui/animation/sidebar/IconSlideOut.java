package net.pl3x.minimap.gui.animation.sidebar;

import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.animation.Animation;
import net.pl3x.minimap.gui.animation.Easing;
import net.pl3x.minimap.gui.screen.widget.category.Category;

public class IconSlideOut extends Animation {
    private final Category category;
    private final float start;
    private final float end;

    public IconSlideOut(Category category) {
        this.category = category;
        this.start = category.iconX;
        this.end = -category.iconSize;
    }

    public void tick(float delta) {
        // step each frame
        float step = Math.min((this.deltaSum += delta) / 5.0F, 1.0F);
        this.category.iconX = animate(this.start, this.end, step, Easing.Back.out);//Config.getConfig().animations.sidebar.iconSlideOut);

        // check if finished
        if (this.category.iconX == this.end) {
            this.remove = true;
        }
    }
}
