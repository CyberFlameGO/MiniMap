package net.pl3x.minimap.gui.screen.widget;

import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.gui.animation.Animation;

import java.util.ArrayList;
import java.util.List;

public abstract class AnimatedWidget extends Widget {
    private final List<Animation> animations = new ArrayList<>();

    public AnimatedWidget(Widget parent, float x, float y, float width, float height) {
        super(parent, x, y, width, height);
    }

    @Override
    public void render(MatrixStack matrixStack, float mouseX, float mouseY, float delta) {
        super.render(matrixStack, mouseX, mouseY, delta);
        tickAnimations(delta);
    }

    public List<Animation> animations() {
        return this.animations;
    }

    public void addAnimation(Animation animation) {
        this.animations.add(animation);
    }

    public void tickAnimations(float delta) {
        Animation.tick(this.animations, delta);
    }
}
