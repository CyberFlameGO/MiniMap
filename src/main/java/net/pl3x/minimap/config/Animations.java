package net.pl3x.minimap.config;

import net.pl3x.minimap.gui.animation.Easing;

@SuppressWarnings("CanBeFinal")
public class Animations {
    public boolean enabled = true;
    public boolean tweening = true;

    public Sidebar sidebar = new Sidebar();

    public static class Sidebar {
        public Easing.Func firstOpen = Easing.Back.out;

        public Easing.Func iconSlideIn = Easing.Back.out;

        public Easing.Func colorHoverOn = Easing.Cubic.out;
        public Easing.Func colorHoverOff = Easing.Cubic.out;

        public Easing.Func toggleHoverOn = Easing.Back.out;
        public Easing.Func toggleHoverOff = Easing.Back.in;

        public Easing.Func toggleOpen = Easing.Bounce.out;
        public Easing.Func toggleClose = Easing.Bounce.out;
    }
}
