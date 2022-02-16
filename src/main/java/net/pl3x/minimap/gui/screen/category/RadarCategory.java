package net.pl3x.minimap.gui.screen.category;

import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.config.Lang;
import net.pl3x.minimap.gui.Icon;
import net.pl3x.minimap.gui.font.Font;
import net.pl3x.minimap.gui.screen.widget.Category;
import net.pl3x.minimap.hardware.Monitor;

public class RadarCategory extends Category {
    public RadarCategory(float y, float delay) {
        super(Lang.CATEGORY_RADAR, Icon.RADAR, y, delay);
    }

    @Override
    public void renderContent(MatrixStack matrixStack, float mouseX, float mouseY, float delta) {
        float x = (Monitor.width() + baseX()) / 2F;
        float y = baseY() + 35F;

        Font.GOODDOG.drawCentered(matrixStack, Lang.CATEGORY_RADAR, x + 3F, y + 3F, 0x88000000);
        Font.GOODDOG.drawCentered(matrixStack, Lang.CATEGORY_RADAR, x, y, 0xFFFFFFFF);
    }
}
