package net.pl3x.minimap.gui.animation.sidebar;

import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.animation.Animation;
import net.pl3x.minimap.gui.screen.widget.Sidebar;

public class FirstOpen extends Animation {
    private final Sidebar sidebar;

    public FirstOpen(Sidebar sidebar) {
        this.sidebar = sidebar;
    }

    public void tick(float delta) {
        // step each frame
        float step = Math.min((this.deltaSum += delta) / 5.0F, 1.0F);
        this.sidebar.width(animate(0, Sidebar.DEFAULT_WIDTH, step, Config.getConfig().animations.sidebar.firstOpen));

        // check if finished
        if (this.sidebar.width() == Sidebar.DEFAULT_WIDTH) {
            this.remove = true;

            // give toggle animation an easing function so it can run
            this.sidebar.toggleAnimation.func = Config.getConfig().animations.sidebar.toggleHoverOn;
        }
    }
}
