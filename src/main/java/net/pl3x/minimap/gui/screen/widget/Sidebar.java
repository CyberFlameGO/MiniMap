package net.pl3x.minimap.gui.screen.widget;

import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.GL;
import net.pl3x.minimap.gui.animation.sidebar.IconSlideOut;
import net.pl3x.minimap.gui.animation.sidebar.SidebarAnimation;
import net.pl3x.minimap.gui.screen.OverlayScreen;
import net.pl3x.minimap.gui.screen.category.AboutCategory;
import net.pl3x.minimap.gui.screen.category.ClockCategory;
import net.pl3x.minimap.gui.screen.category.LayersCategory;
import net.pl3x.minimap.gui.screen.category.PositionCategory;
import net.pl3x.minimap.gui.screen.category.RadarCategory;
import net.pl3x.minimap.gui.screen.category.StyleCategory;
import net.pl3x.minimap.gui.screen.category.WaypointsCategory;
import net.pl3x.minimap.gui.texture.Cursor;
import net.pl3x.minimap.hardware.Monitor;
import net.pl3x.minimap.hardware.Mouse;
import net.pl3x.minimap.sound.Sound;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class Sidebar extends AnimatedWidget {
    public static final Sidebar INSTANCE = new Sidebar();

    public static final float DEFAULT_WIDTH = 40F;
    public static final float HOVER_WIDTH = 180F;

    private final List<Category> categories = new ArrayList<>();
    private final SidebarAnimation sidebarAnimation;

    private Category selected;
    private State state;

    private Sidebar() {
        super(null, 0F, 0F, 0F, 0F);

        this.sidebarAnimation = new SidebarAnimation(this);
        addAnimation(this.sidebarAnimation);
    }

    public Category selected() {
        return this.selected;
    }

    public void select(Category category) {
        boolean hadPrevious = false;
        if (this.selected != null) {
            this.selected.close();
            hadPrevious = true;
        }
        this.selected = category;
        if (category != null) {
            this.selected.open(hadPrevious ? 0F : 2.5F);
        }
    }

    public State state() {
        return this.state;
    }

    public void state(State state) {
        this.state = state;
    }

    @Override
    public float height() {
        return Monitor.height();
    }

    @Override
    public void init() {
        if (this.categories.isEmpty()) {
            this.categories.addAll(List.of(
                    new StyleCategory(20F, 0F),
                    new PositionCategory(70F, 1.5F),
                    new RadarCategory(120F, 3F),
                    new WaypointsCategory(170F, 4.5F),
                    new LayersCategory(220F, 6F),
                    new ClockCategory(0270F, 7.5F),
                    new AboutCategory(320F, 9F)
            ));
            children().addAll(this.categories);
        }

        if (MiniMap.CLIENT.currentScreen instanceof OverlayScreen overlayScreen) {
            if (state() == State.OPENED) {
                // fix width when fully open and screen size changes
                this.sidebarAnimation.setWidth(overlayScreen.width(), false);
            } else {
                // set initial state
                this.state(hovered() ? State.HOVERED : State.NOT_HOVERED);
                this.sidebarAnimation.func = Config.getConfig().animations.sidebar.firstOpen;
                this.sidebarAnimation.easeSpeed = 7.5F;
                Sound.WHOOSH.play();
            }
        }

        super.init();
    }

    @Override
    public void render(MatrixStack matrixStack, float mouseX, float mouseY, float delta) {
        if (hovered()) {
            Mouse.INSTANCE.cursor(Cursor.ARROW);
        }

        // draw background
        GL.drawSolidRect(matrixStack, 0F, 0F, width(), height(), 0x99000000, 0xBB000000);

        // draw children on top of background
        super.render(matrixStack, mouseX, mouseY, delta);

        // draw fancy separator line
        if (selected() != null && this.width() > HOVER_WIDTH) {
            GL.drawLine(matrixStack, HOVER_WIDTH, 0F, HOVER_WIDTH, height(), 1F, 0xBB000000);
        }
    }

    @Override
    public void onHoverChange() {
        if (state() != State.OPENED && state() != State.CLOSED) {
            hover(hovered());
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (state() == State.OPENED) {
                close(false);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void updateTabColors() {
        this.categories.forEach(category -> category.tab().updateTabColor());
    }

    public void open() {
        this.state(State.OPENED);
        this.sidebarAnimation.func = Config.getConfig().animations.sidebar.toggleOpen;
        this.sidebarAnimation.easeSpeed = 7.5F;
        Sound.WHOOSH.play();
    }

    public void close(boolean fully) {
        select(null);
        if (fully) {
            this.state(State.CLOSED);
            this.sidebarAnimation.func = Config.getConfig().animations.sidebar.fullyClose;
            this.sidebarAnimation.easeSpeed = 10F;
            this.categories.forEach(category -> {
                // remove any current animations
                category.tab().animations().clear();
                // and add removal animations instead
                category.tab().addAnimation(new IconSlideOut(category.tab()));
            });
            Sound.WHOOSH.play();
        } else {
            this.state(hovered() ? State.HOVERED : State.NOT_HOVERED);
            this.sidebarAnimation.func = Config.getConfig().animations.sidebar.toggleClose;
            this.sidebarAnimation.easeSpeed = 7.5F;
            Sound.WHOOSH.play();
        }
        this.updateTabColors();
    }

    public void hover(boolean hover) {
        if (hover) {
            this.state(State.HOVERED);
            this.sidebarAnimation.func = Config.getConfig().animations.sidebar.toggleHoverOn;
        } else {
            this.state(State.NOT_HOVERED);
            this.sidebarAnimation.func = Config.getConfig().animations.sidebar.toggleHoverOff;
        }
        this.sidebarAnimation.easeSpeed = 10F;
        Sound.WHOOSH.play();
    }

    public void resetState() {
        this.state(State.CLOSED);
        this.width(0.1F);
        this.categories.clear();
        children().clear();
    }

    public boolean closed() {
        return state() == State.CLOSED && width() <= 0F;
    }

    public enum State {
        CLOSED,
        NOT_HOVERED,
        HOVERED,
        OPENED
    }
}
