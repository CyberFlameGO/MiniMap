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
import net.pl3x.minimap.util.UpdateChecker;

public class AboutCategory extends Category {
    public AboutCategory(float y, float delay) {
        super(Lang.CATEGORY_ABOUT, Icon.ABOUT, y, delay);
    }

    @Override
    public void renderContent(MatrixStack matrixStack, float mouseX, float mouseY, float delta) {
        float x = (Monitor.width() + baseX()) / 2F;
        float y = baseY() + 80F;

        Texture.ICON.tint(matrixStack, x - 32 + 4F, y + 4F, 64, 64, 0x88000000);
        Texture.ICON.draw(matrixStack, x - 32, y, 64, 64);

        Font.GOODDOG.drawCentered(matrixStack, Lang.TITLE, x + 3F, y + 85F + 3F, 0x88000000);
        Font.GOODDOG.drawCentered(matrixStack, Lang.TITLE, x, y + 85F, 0xFFFFFFFF);

        Font.NOTOSANS.drawCentered(matrixStack, "Copyright (c) 2020-2022", x + 1F, y + 140 + 1F, 0x88000000);
        Font.NOTOSANS.drawCentered(matrixStack, "Copyright (c) 2020-2022", x, y + 140, 0xFFFFFFFF);

        Font.NOTOSANS.drawCentered(matrixStack, "William Blake Galbreath", x + 1F, y + 160 + 1F, 0x88000000);
        Font.NOTOSANS.drawCentered(matrixStack, "William Blake Galbreath", x, y + 160, 0xFFFFFFFF);

        String mc = UpdateChecker.INSTANCE.getMinecraftVersion();
        int current = UpdateChecker.INSTANCE.getCurrentVersion();
        int latest = UpdateChecker.INSTANCE.getLatestVersion();

        // todo cache this component somewhere
        Text currentVersion = new TranslatableText("Current Version: " + mc + (current < 0 ? " SNAPSHOT" : " build " + current));

        Font.NOTOSANS.drawCentered(matrixStack, currentVersion, x + 1F, y + 230 + 1F, 0x88000000);
        Font.NOTOSANS.drawCentered(matrixStack, currentVersion, x, y + 230, 0xFFFFFFFF);

        // todo cache this component somewhere
        Text text;
        if (latest > 0) {
            text = new TranslatableText("Latest Version: " + mc + " build " + latest);
        } else if (latest == UpdateChecker.Status.CHECKING) {
            text = new TranslatableText("Checking for updates...").formatted(Formatting.LIGHT_PURPLE);
        } else if (latest == UpdateChecker.Status.ERROR) {
            text = new TranslatableText("Error checking for updates!").formatted(Formatting.RED);
        } else if (latest == current) {
            text = new TranslatableText("Up to date!").formatted(Formatting.GREEN);
        } else {
            text = new TranslatableText("Unknown").formatted(Formatting.GOLD);
        }

        Font.NOTOSANS.drawCentered(matrixStack, text.copy().formatted(Formatting.RESET), x + 1F, y + 260 + 1F, 0x88000000);
        Font.NOTOSANS.drawCentered(matrixStack, text, x, y + 260, 0xFFFFFFFF);

        if (UpdateChecker.INSTANCE.hasUpdate()) {
            Font.ROBOTO.drawCentered(matrixStack, "Updates Available!", x + 1F, y + 280 + 1F, 0x88880000);
            Font.ROBOTO.drawCentered(matrixStack, "Updates Available!", x, y + 280, 0xFFFF5555);
        }
    }
}
