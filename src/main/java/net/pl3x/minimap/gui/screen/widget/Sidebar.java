package net.pl3x.minimap.gui.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.GL;
import net.pl3x.minimap.gui.animation.Easing;
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
import net.pl3x.minimap.hardware.Mouse;
import net.pl3x.minimap.sound.Sound;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class Sidebar extends AnimatedWidget {
    public static final Sidebar INSTANCE = new Sidebar();

    public static final float DEFAULT_WIDTH = 42.0F;
    public static final float HOVER_WIDTH = 180.0F;

    private final List<Category> categories = new ArrayList<>();
    private final SidebarAnimation sidebarAnimation;

    public Category openedCategory;
    public State state;

    public Sidebar() {
        super(null, 0, 0, 0, 0);

        this.sidebarAnimation = new SidebarAnimation(this);
        addAnimation(this.sidebarAnimation);

        HudRenderCallback.EVENT.register(this::render);
    }

    @Override
    public float height() {
        return GL.height();
    }

    @Override
    public void init() {
        if (this.categories.isEmpty()) {
            this.categories.addAll(List.of(
                    new StyleCategory(this, 0, 20, 0.0F, 32),
                    new PositionCategory(this, 0, 70, 1.5F, 32),
                    new RadarCategory(this, 0, 120, 3.0F, 32),
                    new WaypointsCategory(this, 0, 170, 4.5F, 32),
                    new LayersCategory(this, 0, 220, 6.0F, 32),
                    new ClockCategory(this, 0, 270, 7.5F, 32),
                    new AboutCategory(this, 0, 320, 9.0F, 32)
            ));
        }

        if (client().currentScreen instanceof OverlayScreen overlayScreen) {
            if (this.state == State.OPENED) {
                this.sidebarAnimation.setWidth(overlayScreen.width(), false);
            } else {
                this.state = State.NOT_HOVERED;
                this.sidebarAnimation.setWidth(Sidebar.DEFAULT_WIDTH);
                this.sidebarAnimation.func = Easing.Back.out;//Config.getConfig().animations.sidebar.toggleClose;
                this.sidebarAnimation.easeSpeed = 7.5F;
            }
        }
    }

    private void render(MatrixStack matrixStack, float delta) {
        // quick check to see if we should be rendering anything at all
        if (this.state == State.CLOSED && width() <= 0.0F) {
            // nope. lets save some cpu
            return;
        }

        boolean useMouse = client().currentScreen instanceof OverlayScreen;

        // fix scaling
        float screenScaled = 1.0F / GL.scale();

        // setup opengl stuff
        matrixStack.push();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        matrixStack.scale(screenScaled, screenScaled, screenScaled);

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
        super.render(matrixStack, mouseX, mouseY, delta);

        GL.drawSolidRect(matrixStack, 0, 0, width(), height(), 0x99000000, 0xBB000000);

        if (this.openedCategory != null && this.width() > HOVER_WIDTH) {
            GL.drawSolidRect(matrixStack, HOVER_WIDTH, 0, HOVER_WIDTH + 1, height(), 0xBB000000);
        }

        this.categories.forEach(category -> category.render(matrixStack, mouseX, mouseY, delta));
    }

    @Override
    public void onHoverChange() {
        if (this.state != State.OPENED && this.state != State.CLOSED) {
            hover(hovered());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Category category : this.categories) {
            if (category.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (this.state == State.OPENED) {
                close(false);
                return true;
            }
        }
        return false;
    }

    public void updateTabColors() {
        this.categories.forEach(Category::updateTabColor);
    }

    public void close(boolean fully) {
        this.openedCategory = null;
        if (fully) {
            this.state = State.CLOSED;
            this.sidebarAnimation.setWidth(0.0F);
            this.sidebarAnimation.func = Easing.Back.out;//Config.getConfig().animations.sidebar.firstOpen;
            this.categories.forEach(category -> category.addAnimation(new IconSlideOut(category)));
            this.sidebarAnimation.easeSpeed = 7.5F;
        } else {
            if (hovered()) {
                this.state = State.HOVERED;
                this.sidebarAnimation.setWidth(Sidebar.HOVER_WIDTH);
            } else {
                this.state = State.NOT_HOVERED;
                this.sidebarAnimation.setWidth(Sidebar.DEFAULT_WIDTH);
            }
            this.sidebarAnimation.func = Easing.Bounce.out;//Config.getConfig().animations.sidebar.toggleClose;
            this.sidebarAnimation.easeSpeed = 20F;
        }
        this.updateTabColors();
    }

    public void hover(boolean hover) {
        if (hover) {
            this.state = State.HOVERED;
            this.sidebarAnimation.setWidth(Sidebar.HOVER_WIDTH);
            this.sidebarAnimation.func = Easing.Elastic.out;//Config.getConfig().animations.sidebar.toggleHoverOn;
        } else {
            this.state = State.NOT_HOVERED;
            this.sidebarAnimation.setWidth(Sidebar.DEFAULT_WIDTH);
            this.sidebarAnimation.func = Easing.Quintic.out;//Config.getConfig().animations.sidebar.toggleHoverOff;
        }
        this.sidebarAnimation.easeSpeed = 7.5F;
        Sound.WHOOSH.play();
    }

    public void open(float width) {
        this.state = State.OPENED;
        this.sidebarAnimation.setWidth(width);
        this.sidebarAnimation.func = Config.getConfig().animations.sidebar.toggleOpen;
        this.sidebarAnimation.easeSpeed = 20F;
    }

    public void resetState() {
        this.state = State.CLOSED;
        this.width(0.0F);
        this.categories.clear();
    }

    public enum State {
        CLOSED,
        NOT_HOVERED,
        HOVERED,
        OPENED
    }
}
