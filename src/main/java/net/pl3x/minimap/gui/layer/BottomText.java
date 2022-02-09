package net.pl3x.minimap.gui.layer;

import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.font.Font;
import net.pl3x.minimap.util.Biomes;
import net.pl3x.minimap.util.Clock;
import org.apache.commons.lang3.StringUtils;

public class BottomText extends Layer {
    private String[] text = new String[0];

    @Override
    public void render(MatrixStack matrixStack) {
        if (StringUtils.isBlank(Config.getConfig().bottomText)) {
            return;
        }

        float scale = 0.5F / mm.scaleFactor;

        float x = mm.centerX / scale;
        float y = (mm.centerY + mm.size / 2.0F + Font.DEFAULT.height() / mm.scaleFactor) / scale;

        matrixStack.push();
        matrixStack.scale(scale, scale, scale);
        for (int i = 0; i < this.text.length; i++) {
            Font.DEFAULT.drawCentered(matrixStack, this.text[i], x, y + Font.DEFAULT.height() * i);
        }
        matrixStack.pop();
    }

    @Override
    public void update() {
        this.text = Config.getConfig().bottomText.split("\n");
        for (int i = 0; i < this.text.length; i++) {
            if (StringUtils.isBlank(this.text[i])) {
                continue;
            }
            this.text[i] = this.text[i]
                    .replace("{x}", Integer.toString(mm.player.getBlockX()))
                    .replace("{y}", Integer.toString(mm.player.getBlockY()))
                    .replace("{z}", Integer.toString(mm.player.getBlockZ()))
                    .replace("{biome}", Biomes.INSTANCE.getBiomeName(mm.player))
                    .replace("{clock}", Clock.INSTANCE.getTime(mm.player.world));
        }
    }
}
