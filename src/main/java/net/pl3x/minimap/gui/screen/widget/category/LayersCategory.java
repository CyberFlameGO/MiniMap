package net.pl3x.minimap.gui.screen.widget.category;

import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.config.Lang;
import net.pl3x.minimap.gui.Icon;
import net.pl3x.minimap.gui.font.Font;
import net.pl3x.minimap.gui.screen.widget.Sidebar;
import net.pl3x.minimap.hardware.Monitor;

public class LayersCategory extends Category {
    public LayersCategory(Sidebar sidebar, float x, float y, float delay, float size) {
        super(sidebar, x, y, delay, Lang.CATEGORY_LAYERS, Icon.LAYERS, size);
    }

    @Override
    public void renderContent(MatrixStack matrixStack, float mouseX, float mouseY, float delta) {
        float x = (Monitor.width() + this.baseX) / 2F;
        float y = this.baseY + 35F;

        Font.RALEWAY_SEMIBOLD.drawCenteredWithShadow(matrixStack, Lang.CATEGORY_LAYERS, x, y, 0xFFFFFFFF);
    }
}
