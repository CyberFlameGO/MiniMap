package net.pl3x.minimap.gui.animation.sidebar;

import net.pl3x.minimap.gui.animation.Animation;
import net.pl3x.minimap.gui.animation.Easing;
import net.pl3x.minimap.gui.screen.widget.CategoryTab;

public class IconSlideOut extends Animation {
    private final CategoryTab tab;
    private final float start;
    private final float end;

    public IconSlideOut(CategoryTab tab) {
        this.tab = tab;
        this.start = tab.iconX();
        this.end = -tab.iconSize();
    }

    public void tick(float delta) {
        // step each frame
        float step = Math.min((this.deltaSum += delta) / 5.0F, 1.0F);
        this.tab.iconX(animate(this.start, this.end, step, Easing.Back.out));//Config.getConfig().animations.sidebar.iconSlideOut));

        // check if finished
        if (this.tab.iconX() == this.end) {
            this.remove = true;
        }
    }
}
