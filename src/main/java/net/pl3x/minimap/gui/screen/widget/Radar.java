package net.pl3x.minimap.gui.screen.widget;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Box;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.screen.widget.element.EntityMarker;

import java.util.List;

public class Radar {
    private final EntityMarker selfMarker;

    private Box apothem = new Box(0, 0, 0, 0, 0, 0);

    public Radar() {
        this.selfMarker = new EntityMarker(null, null);
    }

    public void render(MatrixStack matrixStack, float delta) {
        if (Config.getConfig().radar.showSelf) {
            // show self
            matrixStack.push();
            this.selfMarker.x(MiniMap.INSTANCE.getCenterX());
            this.selfMarker.y(MiniMap.INSTANCE.getCenterY());
            this.selfMarker.width(16F);
            this.selfMarker.height(16F);
            this.selfMarker.render(matrixStack, false, 1F);
            matrixStack.pop();
        }

        if (Config.getConfig().radar.showPlayers) {
            // show other players
        }

        if (Config.getConfig().radar.showMobs) {
            List<LivingEntity> entities = MiniMap.INSTANCE.getWorld().getEntitiesByClass(LivingEntity.class, this.apothem, EntityPredicates.VALID_LIVING_ENTITY);
            for (LivingEntity entity : entities) {
                System.out.println(entity.getType() + " " + entity.getBlockPos());
            }
        }
    }

    public void update() {
        int x = MiniMap.INSTANCE.getPlayer().getBlockX();
        int z = MiniMap.INSTANCE.getPlayer().getBlockZ();
        float s = MiniMap.INSTANCE.getSize() / 2F;
        int minY = MiniMap.INSTANCE.getWorld().getBottomY();
        this.apothem = new Box(x - s, minY, z - s, x + s, minY, z + s);
    }
}
