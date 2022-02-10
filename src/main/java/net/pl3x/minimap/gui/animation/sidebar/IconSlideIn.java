package net.pl3x.minimap.gui.animation.sidebar;

import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.animation.Animation;
import net.pl3x.minimap.gui.screen.widget.category.Tab;

public class IconSlideIn extends Animation {
    private final Tab tab;
    private final float start;
    private final float end;
    private float delay;

    public IconSlideIn(Tab tab, float start, float end, float delay) {
        this.tab = tab;
        this.start = start;
        this.end = end;
        this.delay = delay;
    }

    @Override
    public void tick(float delta) {
        // wait for delay
        if ((this.delay -= delta) > 0F) {
            return;
        }

        // step each frame
        float step = Math.min((this.deltaSum += delta) / 8F, 1F);
        this.tab.iconX(animate(this.start, this.end, step, Config.getConfig().animations.sidebar.firstOpen));

        // check if finished
        if (this.tab.iconX() == this.end) {
            this.remove = true;
        }
    }
}
