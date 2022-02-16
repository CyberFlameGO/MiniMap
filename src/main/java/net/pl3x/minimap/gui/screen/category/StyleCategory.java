package net.pl3x.minimap.gui.screen.category;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.config.Lang;
import net.pl3x.minimap.config.option.Option;
import net.pl3x.minimap.gui.Icon;
import net.pl3x.minimap.gui.font.Font;
import net.pl3x.minimap.gui.layer.Layer;
import net.pl3x.minimap.gui.screen.widget.Category;
import net.pl3x.minimap.gui.screen.widget.element.Checkbox;
import net.pl3x.minimap.gui.screen.widget.element.Slider;
import net.pl3x.minimap.hardware.Monitor;

import java.util.List;

public class StyleCategory extends Category {
    public StyleCategory(float y, float delay) {
        super(Lang.CATEGORY_STYLE, Icon.STYLE, y, delay);
    }

    @Override
    public void init() {
        float x = -200F;
        float y = 100F;
        float v = 30F;

        if (children().isEmpty()) {
            children().addAll(List.of(
                    new Checkbox(this, Text.of("Circular"), x, y += v, new Option<>() {
                        @Override
                        public Boolean get() {
                            return Config.getConfig().circular;
                        }

                        @Override
                        public void set(Boolean value) {
                            Config.getConfig().circular = value;
                        }
                    }),
                    new Checkbox(this, Text.of("North Locked"), x, y += v, new Option<>() {
                        @Override
                        public Boolean get() {
                            return Config.getConfig().northLocked;
                        }

                        @Override
                        public void set(Boolean value) {
                            Config.getConfig().northLocked = value;
                        }
                    }),
                    new Checkbox(this, Text.of("Frame"), x, y += v, new Option<>() {
                        @Override
                        public Boolean get() {
                            return Config.getConfig().showFrame;
                        }

                        @Override
                        public void set(Boolean value) {
                            Config.getConfig().showFrame = value;
                        }
                    }),
                    new Checkbox(this, Text.of("Directions"), x, y += v, new Option<>() {
                        @Override
                        public Boolean get() {
                            return Config.getConfig().showDirections;
                        }

                        @Override
                        public void set(Boolean value) {
                            Config.getConfig().showDirections = value;
                        }
                    }),
                    new Slider(this, Text.of("Update Interval"), x, y += v, 0, 20, new Option<>() {
                        @Override
                        public Integer get() {
                            return Config.getConfig().updateInterval;
                        }

                        @Override
                        public void set(Integer value) {
                            Config.getConfig().updateInterval = value;
                        }
                    }),
                    new Slider(this, Text.of("Opacity"), x, y += v, 0, 0xFF, new Option<>() {
                        @Override
                        public Integer get() {
                            return Config.getConfig().opacity;
                        }

                        @Override
                        public void set(Integer value) {
                            Config.getConfig().opacity = value;
                        }
                    }),
                    new Checkbox(this, Text.of("Bottom Text"), x, y += v, new Option<>() {
                        @Override
                        public Boolean get() {
                            return false;//Config.getConfig().circular;
                        }

                        @Override
                        public void set(Boolean value) {
                            //Config.getConfig().circular = value;
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

        Font.GOODDOG.drawCentered(matrixStack, Lang.CATEGORY_STYLE, x + 3F, y + 3F, 0x88000000);
        Font.GOODDOG.drawCentered(matrixStack, Lang.CATEGORY_STYLE, x, y, 0xFFFFFFFF);

        // draw minimap layers for preview

        float mmX = MiniMap.INSTANCE.centerX;
        float mmY = MiniMap.INSTANCE.centerY;

        matrixStack.push();
        matrixStack.scale(2F, 2F, 2F);

        MiniMap.INSTANCE.centerX = (x + 150F) / 2F;
        MiniMap.INSTANCE.centerY = (y + 175F) / 2F;

        for (Layer layer : MiniMap.INSTANCE.layers) {
            layer.render(matrixStack);
        }

        MiniMap.INSTANCE.centerX = mmX;
        MiniMap.INSTANCE.centerY = mmY;

        matrixStack.pop();
    }
}
