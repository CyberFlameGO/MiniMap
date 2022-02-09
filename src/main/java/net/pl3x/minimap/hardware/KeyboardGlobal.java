package net.pl3x.minimap.hardware;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.screen.FullMapScreen;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class KeyboardGlobal {
    public static final KeyboardGlobal INSTANCE = new KeyboardGlobal();

    private final List<GlobalKey> globalKeys = new ArrayList<>();

    public void initialize() {
        this.globalKeys.clear();
        this.globalKeys.addAll(List.of(
                new GlobalKey("minimap.key.map.open", "minimap.key.category", GLFW.GLFW_KEY_M, () -> {
                    if (MiniMap.CLIENT.currentScreen == null) {
                        MiniMap.CLIENT.setScreen(new FullMapScreen(null));
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

        ClientTickEvents.END_CLIENT_TICK.register(client -> this.globalKeys.forEach(KeyboardGlobal.GlobalKey::tick));
    }

    public static class GlobalKey extends Key {
        private final KeyBinding binding;

        public GlobalKey(String name, String category, int keyCode, Action action) {
            super(action);
            this.binding = KeyBindingHelper.registerKeyBinding(new KeyBinding(name, keyCode, category));
        }

        public void tick() {
            while (this.binding.wasPressed()) {
                this.action.execute();
            }
        }
    }
}
