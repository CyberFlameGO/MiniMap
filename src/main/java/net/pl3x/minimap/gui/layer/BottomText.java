package net.pl3x.minimap.gui.layer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.font.Font;
import net.pl3x.minimap.util.Biomes;
import net.pl3x.minimap.util.Clock;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class BottomText extends Layer {
    private final List<Text> text = new ArrayList<>();

    @Override
    public void render(MatrixStack matrixStack) {
        if (this.text.isEmpty()) {
            return;
        }

        float x = mm.getCenterX();
        float y = mm.getCenterY() + mm.getSize() / 2F + Font.DEFAULT.height() * 2F;
        int color = 0xFFFFFF | (Config.getConfig().opacity << 24);
        int i = 0;

        // use a blend that supports translucent pixels
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

        matrixStack.push();
        for (Text text : this.text) {
            Font.DEFAULT.drawCenteredWithShadow(matrixStack, text, x, y + Font.DEFAULT.height() * i++, color);
        }
        matrixStack.pop();
    }

    @Override
    public void update() {
        this.text.clear();
        String[] lines = Config.getConfig().bottomText.split("\n");
        for (String line : lines) {
            if (StringUtils.isBlank(line)) {
                continue;
            }
            this.text.add(new TranslatableText(line
                    .replace("{x}", Integer.toString(mm.getPlayer().getBlockX()))
                    .replace("{y}", Integer.toString(mm.getPlayer().getBlockY()))
                    .replace("{z}", Integer.toString(mm.getPlayer().getBlockZ()))
                    .replace("{biome}", Biomes.INSTANCE.getBiomeName(mm.getPlayer()))
                    .replace("{clock}", Clock.INSTANCE.getTime(mm.getWorld()))));
        }
    }
}
