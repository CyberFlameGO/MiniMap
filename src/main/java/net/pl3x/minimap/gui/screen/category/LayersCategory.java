package net.pl3x.minimap.gui.screen.category;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.config.Lang;
import net.pl3x.minimap.config.option.Option;
import net.pl3x.minimap.gui.Icon;
import net.pl3x.minimap.gui.font.Font;
import net.pl3x.minimap.gui.screen.widget.Category;
import net.pl3x.minimap.gui.screen.widget.element.Checkbox;
import net.pl3x.minimap.hardware.Monitor;

import java.util.List;

public class LayersCategory extends Category {
    public LayersCategory(float y, float delay) {
        super(Lang.CATEGORY_LAYERS, Icon.LAYERS, y, delay);
    }

    @Override
    public void init() {
        float x = -200F;
        float y = 100F;

        if (children().isEmpty()) {
            children().addAll(List.of(
                    new Checkbox(this, Text.of("Base Layer"), x, y += 30F, new Option<>() {
                        @Override
                        public Boolean get() {
                            return Config.getConfig().layers.base;
                        }

                        @Override
                        public void set(Boolean value) {
                            Config.getConfig().layers.base = value;
                            Config.save();
                        }
                    }),
                    new Checkbox(this, Text.of("Biomes Layer"), x, y += 30F, new Option<>() {
                        @Override
                        public Boolean get() {
                            return Config.getConfig().layers.biomes;
                        }

                        @Override
                        public void set(Boolean value) {
                            Config.getConfig().layers.biomes = value;
                            Config.save();
                        }
                    }),
                    new Checkbox(this, Text.of("Heightmap Layer"), x, y += 30F, new Option<>() {
                        @Override
                        public Boolean get() {
                            return Config.getConfig().layers.heightmap;
                        }

                        @Override
                        public void set(Boolean value) {
                            Config.getConfig().layers.heightmap = value;
                            Config.save();
                        }
                    }),
                    new Checkbox(this, Text.of("Fluids Layer"), x, y += 30F, new Option<>() {
                        @Override
                        public Boolean get() {
                            return Config.getConfig().layers.fluids;
                        }

                        @Override
                        public void set(Boolean value) {
                            Config.getConfig().layers.fluids = value;
                            Config.save();
                        }
                    }),
                    new Checkbox(this, Text.of("Lightmap Layer"), x, y += 30F, new Option<>() {
                        @Override
                        public Boolean get() {
                            return Config.getConfig().layers.lightmap;
                        }

                        @Override
                        public void set(Boolean value) {
                            Config.getConfig().layers.lightmap = value;
                            Config.save();
                        }
                    })
            ));
        }

        super.init();
    }

    @Override
    public void renderContent(MatrixStack matrixStack, float mouseX, float mouseY, float delta) {
        float x = (Monitor.width() + baseX()) / 2F;
        float y = baseY() + 35F;

        Font.GOODDOG.drawCentered(matrixStack, Lang.CATEGORY_LAYERS, x + 3F, y + 3F, 0x88000000);
        Font.GOODDOG.drawCentered(matrixStack, Lang.CATEGORY_LAYERS, x, y, 0xFFFFFFFF);
    }
}
