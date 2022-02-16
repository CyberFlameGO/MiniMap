package net.pl3x.minimap.gui.animation.sidebar;

import net.pl3x.minimap.gui.animation.Animation;
import net.pl3x.minimap.gui.animation.Easing;
import net.pl3x.minimap.gui.screen.widget.Category;

public class CategorySlide extends Animation {
    private final Category category;
    private final float start;
    private final float end;
    private float delay;

    public CategorySlide(Category category, float start, float end, float delay) {
        this.category = category;
        this.start = start;
        this.end = end;
        this.delay = delay;
    }

    @Override
    public void tick(float delta) {
        // wait for delay
        if ((this.delay -= delta) > 0F) {
            return;
        }

        // step each frame
        float step = Math.min((this.deltaSum += delta) / 10F, 1F);
        this.category.baseY(animate(this.start, this.end, step, Easing.Back.out));

        // check if finished
        if (step >= 1F) {
            this.remove = true;
        }
    }
}
