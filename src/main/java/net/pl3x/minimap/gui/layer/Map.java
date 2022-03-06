package net.pl3x.minimap.gui.layer;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.texture.Texture;

public class Map extends Layer {
    private float deltaZoom;

    @Override
    public void render(MatrixStack matrixStack, float delta) {
        this.deltaZoom = MiniMap.INSTANCE.calcZoom(Config.getConfig().zoom, this.deltaZoom, delta);

        mm.drawMap(
            matrixStack,
            0F, 0F,
            mm.getSize(),
            mm.getSize(),
            mm.getCenterX(),
            mm.getCenterY(),
            0F,
            0F,
            Config.getConfig().circular,
            this.deltaZoom,
            Config.getConfig().northLocked ? 0 : mm.getAngle(),
            delta
        );
    }

    @Override
    public void update() {
        RegistryKey<World> key = mm.getPlayer().world.getRegistryKey();
        if (key == World.OVERWORLD) {
            mm.setBackground(Texture.SKY_OVERWORLD);
        } else if (key == World.NETHER) {
            mm.setBackground(Texture.SKY_THE_NETHER);
        } else if (key == World.END) {
            mm.setBackground(Texture.SKY_THE_END);
        } else {
            mm.setBackground(Texture.SKY_OVERWORLD);
        }
    }
}
