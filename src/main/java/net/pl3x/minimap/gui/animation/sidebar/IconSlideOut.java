package net.pl3x.minimap.gui.animation.sidebar;

import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.animation.Animation;
import net.pl3x.minimap.gui.screen.widget.element.Tab;

public class IconSlideOut extends Animation {
    private final Tab tab;
    private final float start;
    private final float end;

    public IconSlideOut(Tab tab) {
        this.tab = tab;
        this.start = tab.iconX();
        this.end = -tab.iconSize();
    }

    @Override
    public void tick(float delta) {
        // step each frame
        float step = Math.min((this.deltaSum += delta) / 8F, 1F);
        this.tab.iconX(animate(this.start, this.end, step, Config.getConfig().animations.sidebar.fullyClose));

        // check if finished
        if (this.tab.iconX() == this.end) {
            this.remove = true;
        }
    }
}
