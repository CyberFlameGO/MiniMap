package net.pl3x.minimap.gui.screen.widget.category;

import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.config.Lang;
import net.pl3x.minimap.gui.Icon;
import net.pl3x.minimap.gui.screen.widget.Sidebar;

public class AboutCategory extends Category {
    public AboutCategory(Sidebar sidebar, float x, float y, float delay, float size) {
        super(sidebar, x, y, delay, Lang.CATEGORY_ABOUT, Icon.ABOUT, size);
    }

    public void renderContent(MatrixStack matrixStack) {
    }
}
