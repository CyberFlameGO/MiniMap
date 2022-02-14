package net.pl3x.minimap.gui.screen.category;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.pl3x.minimap.config.Lang;
import net.pl3x.minimap.gui.Icon;
import net.pl3x.minimap.gui.font.Font;
import net.pl3x.minimap.gui.screen.widget.Category;
import net.pl3x.minimap.gui.texture.Texture;
import net.pl3x.minimap.hardware.Monitor;

public class AboutCategory extends Category {
    public AboutCategory(float y, float delay) {
        super(Lang.CATEGORY_ABOUT, Icon.ABOUT, y, delay);
    }

    @Override
    public void renderContent(MatrixStack matrixStack, float mouseX, float mouseY, float delta) {
        float x = (Monitor.width() + baseX()) / 2F;
        float y = baseY() + 80F;

        Texture.ICON.draw(matrixStack, x - 32, y, 64, 64);
        Font.GOODDOG.drawCenteredWithShadow(matrixStack, Lang.TITLE, x, y + 85, 0xFFFFFFFF);

        Font.NOTOSANS.drawCenteredWithShadow(matrixStack, "Copyright (c) 2020-2022", x, y + 140, 0xFFFFFFFF);
        Font.NOTOSANS.drawCenteredWithShadow(matrixStack, "William Blake Galbreath", x, y + 160, 0xFFFFFFFF);

        Font.NOTOSANS.drawCenteredWithShadow(matrixStack, "Version: 0.0.1", x, y + 230, 0xFFFFFFFF);

        //Text text = new TranslatableText("Checking for updates...");
        Text text = new TranslatableText("Up to date").formatted(Formatting.GREEN);
        //Text text = new TranslatableText("Update Available (0.0.2)").formatted(Formatting.RED);
        Font.LATO.drawCenteredWithShadow(matrixStack, text, x, y + 260, 0xFFFFFFFF);
    }
}
