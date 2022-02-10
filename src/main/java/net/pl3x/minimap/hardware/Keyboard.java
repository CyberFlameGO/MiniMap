package net.pl3x.minimap.hardware;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.screen.OverlayScreen;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class Keyboard {
    public static final Keyboard INSTANCE = new Keyboard();

    private final List<GlobalKey> globalKeys = new ArrayList<>();

    public void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> this.globalKeys.forEach(Keyboard.GlobalKey::tick));

        this.globalKeys.clear();
        this.globalKeys.addAll(List.of(
                new GlobalKey("minimap.key.map.open", "minimap.key.category", GLFW.GLFW_KEY_N, () -> {
                    if (MiniMap.CLIENT.currentScreen == null) {
                        MiniMap.CLIENT.setScreen(new OverlayScreen(null));
                    }
                }),
                new GlobalKey("minimap.key.minimap.zoom.out", "minimap.key.category", GLFW.GLFW_KEY_PAGE_UP, () -> {
                    if (Config.getConfig().zoom < 7) {
                        Config.getConfig().zoom++;
                        Config.save();
                    }
                }),
                new GlobalKey("minimap.key.minimap.zoom.in", "minimap.key.category", GLFW.GLFW_KEY_PAGE_DOWN, () -> {
                    if (Config.getConfig().zoom > 0) {
                        Config.getConfig().zoom--;
                        Config.save();
                    }
                })
        ));
    }

    private static class GlobalKey {
        private final Action action;
        private final KeyBinding binding;

        private GlobalKey(String name, String category, int keyCode, Action action) {
            this.action = action;
            this.binding = KeyBindingHelper.registerKeyBinding(new KeyBinding(name, keyCode, category));
        }

        private void tick() {
            while (this.binding.wasPressed()) {
                this.action.execute();
            }
        }

        @FunctionalInterface
        private interface Action {
            void execute();
        }
    }
}
