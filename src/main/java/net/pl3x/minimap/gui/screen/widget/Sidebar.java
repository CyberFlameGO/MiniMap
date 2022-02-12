package net.pl3x.minimap.gui.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.GL;
import net.pl3x.minimap.gui.animation.sidebar.IconSlideOut;
import net.pl3x.minimap.gui.animation.sidebar.SidebarAnimation;
import net.pl3x.minimap.gui.font.Font;
import net.pl3x.minimap.gui.screen.OverlayScreen;
import net.pl3x.minimap.gui.screen.widget.category.AboutCategory;
import net.pl3x.minimap.gui.screen.widget.category.Category;
import net.pl3x.minimap.gui.screen.widget.category.ClockCategory;
import net.pl3x.minimap.gui.screen.widget.category.LayersCategory;
import net.pl3x.minimap.gui.screen.widget.category.PositionCategory;
import net.pl3x.minimap.gui.screen.widget.category.RadarCategory;
import net.pl3x.minimap.gui.screen.widget.category.StyleCategory;
import net.pl3x.minimap.gui.screen.widget.category.WaypointsCategory;
import net.pl3x.minimap.gui.texture.Cursor;
import net.pl3x.minimap.hardware.Monitor;
import net.pl3x.minimap.hardware.Mouse;
import net.pl3x.minimap.sound.Sound;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class Sidebar extends AnimatedWidget {
    public static final Sidebar INSTANCE = new Sidebar();

    public static final float DEFAULT_WIDTH = 20F;
    public static final float HOVER_WIDTH = 90F;

    private final List<Category> categories = new ArrayList<>();
    private final SidebarAnimation sidebarAnimation;

    private Category openedCategory;
    private State state;

    public Sidebar() {
        super(null, 0F, 0F, 0F, 0F);

        this.sidebarAnimation = new SidebarAnimation(this);
        addAnimation(this.sidebarAnimation);

        HudRenderCallback.EVENT.register(this::render);
    }

    public Category openedCategory() {
        return this.openedCategory;
    }

    public void openedCategory(Category category) {
        this.openedCategory = category;
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
                    new StyleCategory(this, 0F, 5F, 0F, 16F),
                    new PositionCategory(this, 0F, 30F, 1.5F, 16F),
                    new RadarCategory(this, 0F, 55F, 3F, 16F),
                    new WaypointsCategory(this, 0F, 80F, 4.5F, 16F),
                    new LayersCategory(this, 0F, 105F, 6F, 16F),
                    new ClockCategory(this, 0F, 130F, 7.5F, 16F),
                    new AboutCategory(this, 0F, 155F, 9F, 16F)
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

    private void render(MatrixStack matrixStack, float delta) {
        // quick check to see if we should be rendering anything at all
        if (state() == State.CLOSED && width() <= 0F) {
            // nope. lets save some cpu
            return;
        }

        boolean useMouse = MiniMap.CLIENT.currentScreen instanceof OverlayScreen;

        // setup opengl stuff
        matrixStack.push();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        // don't allow Mojang disable blending after drawing text
        Font.ALLOW_DISABLE_BLENDING_AFTER_DRAWING_TEXT = false;

        // update our own mouse positions before rendering anything
        if (useMouse) {
            Mouse.INSTANCE.update();
            Mouse.INSTANCE.cursor(Cursor.ARROW);
        }

        // render everything
        this.render(matrixStack, Mouse.INSTANCE.x(), Mouse.INSTANCE.y(), delta);

        // render our mouse after everything is rendered
        if (useMouse) {
            Mouse.INSTANCE.render(matrixStack, delta);
        }

        // allow Mojang disable blending after drawing text
        Font.ALLOW_DISABLE_BLENDING_AFTER_DRAWING_TEXT = true;

        // clean up opengl stuff
        RenderSystem.disableBlend();
        matrixStack.pop();
    }

    @Override
    public void render(MatrixStack matrixStack, float mouseX, float mouseY, float delta) {
        // draw background
        GL.drawSolidRect(matrixStack, 0F, 0F, width(), height(), 0x99000000, 0xBB000000);

        // draw children on top of background
        super.render(matrixStack, mouseX, mouseY, delta);

        // draw fancy separator line
        if (this.openedCategory != null && this.width() > HOVER_WIDTH) {
            GL.drawSolidRect(matrixStack, HOVER_WIDTH, 0F, HOVER_WIDTH + 1F, height(), 0xBB000000);
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
        this.sidebarAnimation.easeSpeed = 20F;
    }

    public void close(boolean fully) {
        this.openedCategory = null;
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
            this.sidebarAnimation.easeSpeed = 20F;
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
        this.width(0F);
        this.categories.clear();
        children().clear();
    }

    public enum State {
        CLOSED,
        NOT_HOVERED,
        HOVERED,
        OPENED
    }
}
