package net.pl3x.minimap.gui.screen.widget.category;

import net.pl3x.minimap.config.Lang;
import net.pl3x.minimap.gui.Icon;
import net.pl3x.minimap.gui.screen.widget.Sidebar;

public class WaypointsCategory extends Category {
    public WaypointsCategory(Sidebar sidebar, float x, float y, float delay, float size) {
        super(sidebar, x, y, delay, Lang.CATEGORY_WAYPOINTS, Icon.WAYPOINTS, size);
    }
}
