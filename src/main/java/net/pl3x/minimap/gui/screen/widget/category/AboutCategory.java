package net.pl3x.minimap.gui.screen.widget.category;

import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.config.Lang;
import net.pl3x.minimap.gui.Icon;
import net.pl3x.minimap.gui.font.Font;
import net.pl3x.minimap.gui.screen.widget.Sidebar;

public class AboutCategory extends Category {
    private float baseX;
    private float baseY;

    public AboutCategory(Sidebar sidebar, float x, float y, float delay, float size) {
        super(sidebar, x, y, delay, Lang.CATEGORY_ABOUT, Icon.ABOUT, size);

        this.baseX = Sidebar.HOVER_WIDTH;
        this.baseY = 0F;
    }

    @Override
    public void render(MatrixStack matrixStack, float mouseX, float mouseY, float delta) {
        super.render(matrixStack, mouseX, mouseY, delta);

        float x = (client().getWindow().getWidth() + this.baseX) / 2F;
        float y = this.baseY + 35F;

        Font.RALEWAY_SEMIBOLD.drawCenteredWithShadow(matrixStack, Lang.CATEGORY_ABOUT, x, y, 0xFFFFFFFF);

        Font.RALEWAY_MEDIUM.drawCenteredWithShadow(matrixStack, Lang.TITLE, x, y + 50, 0xFFFFFFFF);
    }
}
