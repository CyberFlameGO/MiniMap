package net.pl3x.minimap.hardware;

import net.minecraft.client.util.InputUtil;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.scheduler.Scheduler;

import java.util.HashMap;
import java.util.Map;

public class KeyboardScreen implements Runnable {
    private final Map<Integer, Key> keys = new HashMap<>();

    public KeyboardScreen() {
        Scheduler.INSTANCE.addTask(0, true, this);
    }

    public void listen(int code, Key.Action action) {
        this.keys.put(code, new Key(action));
    }

    public boolean isListening(int code) {
        return this.keys.containsKey(code);
    }

    public boolean isPressed(int code) {
        return InputUtil.isKeyPressed(MiniMap.CLIENT.getWindow().getHandle(), code);
    }

    @Override
    public void run() {
        this.keys.forEach((code, key) -> {
            if (isPressed(code)) {
                key.press();
            } else if (key.pressed()) {
                key.release();
            }
        });
    }

    public static class Key {
        private final Action action;

        private int down = -1;

        public Key(Action action) {
            this.action = action;
        }

        public void press() {
            this.down++;
            if (this.down == 0 || this.down > 5) {
                this.action.execute();
            }
        }

        public void release() {
            this.down = -1;
        }

        public boolean pressed() {
            return this.down > -1;
        }

        @FunctionalInterface
        public interface Action {
            void execute();
        }
    }
}
