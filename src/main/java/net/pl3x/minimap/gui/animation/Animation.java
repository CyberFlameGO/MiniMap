package net.pl3x.minimap.gui.animation;

import net.minecraft.util.math.MathHelper;
import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.util.Colors;

import java.util.Iterator;
import java.util.List;

public abstract class Animation {
    protected float deltaSum;
    protected boolean remove;

    public abstract void tick(float delta);

    public static void tick(List<Animation> animations, float delta) {
        Iterator<Animation> iter = animations.iterator();
        while (iter.hasNext()) {
            Animation animation = iter.next();
            animation.tick(delta);
            if (animation.remove) {
                iter.remove();
            }
        }
    }

    protected float animate(float start, float end, float step, Easing.Func func) {
        if (!Config.getConfig().animations.enabled) {
            // do not animate
            return end;
        }
        return MathHelper.lerp(tween(step, func), start, end);
    }

    protected int animateARGB(int start, int end, float step, Easing.Func func) {
        if (!Config.getConfig().animations.enabled) {
            // do not animate
            return end;
        }
        return Colors.lerpARGB(tween(step, func), start, end);
    }

    private float tween(float step, Easing.Func func) {
        if (!Config.getConfig().animations.tweening) {
            // do not tween/ease
            return step;
        }
        return func != null ? func.apply(step) : step;
    }
}
