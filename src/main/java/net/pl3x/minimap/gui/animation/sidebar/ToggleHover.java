package net.pl3x.minimap.gui.animation.sidebar;

import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.animation.Animation;
import net.pl3x.minimap.gui.animation.Easing;
import net.pl3x.minimap.gui.screen.widget.Sidebar;
import net.pl3x.minimap.sound.Sound;

public class ToggleHover extends Animation {
    private final Sidebar sidebar;

    private float start = Sidebar.DEFAULT_WIDTH;
    private float end = Sidebar.DEFAULT_WIDTH;

    private boolean hovered;
    private boolean opened;

    private boolean recheck;

    public Easing.Func func;
    private float easeSpeed = 7.5F;

    public ToggleHover(Sidebar sidebar) {
        this.sidebar = sidebar;
    }

    public void tick(float delta) {
        // don't toggle until open animation gives us an easing function
        if (this.func == null) {
            return;
        }

        // check if animation needed
        if (this.sidebar.width() == this.end) {
            if (this.recheck) {
                this.recheck = false;
                hover(this.hovered);
            }
            return;
        }

        // step each frame
        float step = Math.min((this.deltaSum += delta) / this.easeSpeed, 1.0F);
        this.sidebar.width(animate(this.start, this.end, step, this.func));
    }

    public void hover(boolean hovered) {
        this.hovered = hovered;
        if (this.sidebar.width() > Sidebar.HOVER_WIDTH) {
            this.recheck = !this.opened; // don't recheck if opened
            return;
        }
        this.func = hovered ? Config.getConfig().animations.sidebar.toggleHoverOn : Config.getConfig().animations.sidebar.toggleHoverOff;
        this.start = this.sidebar.width();
        this.end = hovered ? Sidebar.HOVER_WIDTH : Sidebar.DEFAULT_WIDTH;
        this.easeSpeed = 7.5F;
        this.deltaSum = 0.0F;
        Sound.WHOOSH.play();
    }

    public void toggleOpen() {
        this.opened = !this.opened;
        this.func = this.opened ? Config.getConfig().animations.sidebar.toggleOpen : Config.getConfig().animations.sidebar.toggleClose;
        this.start = this.sidebar.width();
        this.end = this.opened ? this.sidebar.screen().width() : this.hovered ? Sidebar.HOVER_WIDTH : Sidebar.DEFAULT_WIDTH;
        this.easeSpeed = 20.0F;
        this.deltaSum = 0.0F;
    }
}
