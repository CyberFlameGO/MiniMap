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

        float x = mm.getCenterX();
        float y = mm.getCenterY() + mm.getSize() / 2F + Font.DEFAULT.height() * 2F;
        int color = 0xFFFFFF | (Config.getConfig().opacity << 24);

        matrixStack.push();
        for (int i = 0; i < this.text.length; i++) {
            Font.DEFAULT.drawCenteredWithShadow(matrixStack, this.text[i], x, y + Font.DEFAULT.height() * i, color);
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
                    .replace("{x}", Integer.toString(mm.getPlayer().getBlockX()))
                    .replace("{y}", Integer.toString(mm.getPlayer().getBlockY()))
                    .replace("{z}", Integer.toString(mm.getPlayer().getBlockZ()))
                    .replace("{biome}", Biomes.INSTANCE.getBiomeName(mm.getPlayer()))
                    .replace("{clock}", Clock.INSTANCE.getTime(mm.getWorld()));
        }
    }
}
