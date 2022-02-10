package net.pl3x.minimap.gui.animation.sidebar;

import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.GL;
import net.pl3x.minimap.gui.animation.Animation;
import net.pl3x.minimap.gui.animation.Easing;
import net.pl3x.minimap.gui.screen.widget.Sidebar;

public class SidebarAnimation extends Animation {
    private final Sidebar sidebar;

    private float start = Sidebar.DEFAULT_WIDTH;
    private float end = Sidebar.DEFAULT_WIDTH;

    public Easing.Func func;
    public float easeSpeed = 7.5F;

    private Sidebar.State state = Sidebar.State.CLOSED;

    public SidebarAnimation(Sidebar sidebar) {
        this.sidebar = sidebar;
        this.func = Config.getConfig().animations.sidebar.firstOpen;
    }

    @Override
    public void tick(float delta) {
        // update state
        if (this.sidebar.state != this.state) {
            this.state = this.sidebar.state;
            setWidth(switch (this.state) {
                case NOT_HOVERED -> Sidebar.DEFAULT_WIDTH;
                case HOVERED -> Sidebar.HOVER_WIDTH;
                case OPENED -> GL.width();
                default -> 0.0F;
            });
        }

        // check if animation needed
        if (this.sidebar.width() == this.end) {
            if (this.state == Sidebar.State.CLOSED) {
                // reset state after closing animations finished
                this.sidebar.resetState();
            }
            return;
        }

        // step each frame
        float step = Math.min((this.deltaSum += delta) / this.easeSpeed, 1.0F);
        this.sidebar.width(animate(this.start, this.end, step, this.func));
    }

    public void setWidth(float width) {
        setWidth(width, true);
    }

    public void setWidth(float width, boolean resetDelta) {
        this.start = this.sidebar.width();
        this.end = width;
        if (resetDelta) {
            this.deltaSum = 0;
        }
    }
}
