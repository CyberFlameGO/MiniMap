package net.pl3x.minimap.gui.screen.category;

import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.config.Lang;
import net.pl3x.minimap.gui.Icon;
import net.pl3x.minimap.gui.font.Font;
import net.pl3x.minimap.gui.screen.widget.Category;
import net.pl3x.minimap.hardware.Monitor;

public class PositionCategory extends Category {
    public PositionCategory(float y, float delay) {
        super(Lang.CATEGORY_POSITION, Icon.POSITION, y, delay);
    }

    @Override
    public void renderContent(MatrixStack matrixStack, float mouseX, float mouseY, float delta) {
        float x = (Monitor.width() + this.baseX) / 2F;
        float y = this.baseY + 35F;

        Font.GOODDOG.drawCenteredWithShadow(matrixStack, Lang.CATEGORY_POSITION, x, y, 0xFFFFFFFF);
    }
}
