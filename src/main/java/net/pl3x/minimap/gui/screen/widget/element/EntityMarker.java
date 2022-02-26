package net.pl3x.minimap.gui.screen.widget.element;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.GL;
import net.pl3x.minimap.gui.Icon;
import net.pl3x.minimap.gui.screen.widget.AnimatedWidget;
import net.pl3x.minimap.gui.screen.widget.Widget;
import net.pl3x.minimap.gui.texture.Cursor;
import net.pl3x.minimap.gui.texture.Drawable;
import net.pl3x.minimap.hardware.Mouse;
import net.pl3x.minimap.util.Colors;
import net.pl3x.minimap.util.Mathf;
import net.pl3x.minimap.util.Numbers;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EntityMarker extends AnimatedWidget {
    private final AbstractClientPlayerEntity player;

    public EntityMarker(Widget parent, AbstractClientPlayerEntity player) {
        super(parent, 0, 0, 16, 16);
        this.player = player;
    }

    public void render(MatrixStack matrixStack, boolean fullmap, float delta) {
        AbstractClientPlayerEntity player = this.player == null ? MiniMap.INSTANCE.getPlayer() : this.player;
        if (player == null) {
            return;
        }

        float angle = Numbers.normalizeDegrees(player.getYaw(delta));

        //vanillaStyle(matrixStack, angle, delta);
        headStyle(matrixStack, player, !Config.getConfig().northLocked && !fullmap && this.player == null ? 0F : angle, delta);

        // todo - clean this up once on its own layer
        float mouseX = Mouse.INSTANCE.x();
        float mouseY = Mouse.INSTANCE.y();
        float w = width() / 2F;
        float h = height() / 2F;
        if (mouseX >= x() - w && mouseX <= x() + w && mouseY >= y() - h && mouseY <= y() + h) {
            Mouse.INSTANCE.cursor(Cursor.HAND_POINTER);
        }
    }

    private void vanillaStyle(MatrixStack matrixStack, float angle, float delta) {
        if (Config.getConfig().northLocked) {
            // only rotate if map is northlocked
            GL.rotateScene(matrixStack, x(), y(), angle);
        }
        Icon.PLAYER.draw(matrixStack, x() - width() / 2F, y() - height() / 2F, width(), 0xFFFFFFFF);
    }

    private void headStyle(MatrixStack matrixStack, AbstractClientPlayerEntity player, float angle, float delta) {
        float w = width() / 2F;
        float h = height() / 2F;

        // arrow pointing at angle
        matrixStack.push();
        matrixStack.translate(width() * -Mathf.sinRads(-angle), height() * -Mathf.cosRads(-angle), 0D);
        GL.rotateScene(matrixStack, x(), y(), angle);
        Icon.ARROW.draw(matrixStack, x() - w, y() - h, width(), 0xFFFFFFFF);
        matrixStack.pop();

        // skin layers
        Drawable.draw(matrixStack, player.getSkinTexture(), x() - w, y() - h, x() + w, y() + h, 1 / 8F, 1 / 8F, 2 / 8F, 2 / 8F);
        Drawable.draw(matrixStack, player.getSkinTexture(), x() - w, y() - h, x() + w, y() + h, 5 / 8F, 1 / 8F, 6 / 8F, 2 / 8F);

        // helmet layer
        ItemStack stack = player.getInventory().getArmorStack(3);
        Item item = stack.getItem();
        Helmet helmet = Helmet.get(item);
        if (helmet != null) {
            // helmet is slightly bigger than the head
            w *= 1.2F;
            h *= 1.2F;
            if (item instanceof DyeableArmorItem armor) {
                Drawable.tint(matrixStack, helmet.texture, x() - w, y() - h, x() + w, y() + h, 1 / 8F, 1 / 4F, 2 / 8F, 2 / 4F, armor.getColor(stack) | 0xFF << 24);
            } else {
                Drawable.draw(matrixStack, helmet.texture, x() - w, y() - h, x() + w, y() + h, 1 / 8F, 1 / 4F, 2 / 8F, 2 / 4F);
            }
            if (helmet.overlay != null) {
                Drawable.draw(matrixStack, helmet.overlay, x() - w, y() - h, x() + w, y() + h, 1 / 8F, 1 / 4F, 2 / 8F, 2 / 4F);
            }
        }
    }

    public enum Helmet {
        LEATHER(Items.LEATHER_HELMET, true),
        IRON(Items.IRON_HELMET),
        CHAINMAIL(Items.CHAINMAIL_HELMET),
        GOLD(Items.GOLDEN_HELMET),
        DIAMOND(Items.DIAMOND_HELMET),
        NETHERITE(Items.NETHERITE_HELMET),
        TURTLE(Items.TURTLE_HELMET);

        public final ArmorItem item;
        public final Identifier texture;
        public final Identifier overlay;

        Helmet(Item item) {
            this(item, false);
        }

        Helmet(Item item, boolean overlay) {
            this.item = (ArmorItem) item;

            String name = name().toLowerCase(Locale.ROOT);

            this.texture = new Identifier(String.format("textures/models/armor/%s_layer_1.png", name));
            this.overlay = overlay ? new Identifier(String.format("textures/models/armor/%s_layer_1_overlay.png", name)) : null;
        }

        private static final Map<Item, Helmet> BY_ITEM = new HashMap<>();

        static {
            for (Helmet helmet : values()) {
                BY_ITEM.put(helmet.item, helmet);
            }
        }

        public static Helmet get(Item item) {
            return BY_ITEM.get(item);
        }
    }
}
