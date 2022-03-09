package net.pl3x.minimap.gui.font;

import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.FontType;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.pl3x.minimap.MiniMap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Font {
    private static final Set<Font> REGISTERED_FONTS = new HashSet<>();
    private static final String PROVIDER_JSON = "{\"providers\":[{\"type\":\"ttf\",\"file\":\"minimap:%s.ttf\",\"shift\":[0, 0],\"size\":%f,\"oversample\":%f}]}";

    public static final Font DEFAULT = register("default_font", MiniMap.getClient().textRenderer.fontHeight, 1F);
    public static final Font GOODDOG = register("gooddog", 32F, 4F);
    public static final Font LATO = register("lato", 20F, 4F);
    public static final Font NOTOSANS = register("notosans", 20F, 4F);
    public static final Font RALEWAY = register("raleway", 28F, 4F);
    public static final Font ROBOTO = register("roboto", 20F, 4F);

    public static boolean FIX_MOJANGS_TEXT_RENDERER_CRAP = false;

    private static Font register(String name, float height, float oversample) {
        Font font = new Font(name, height, oversample);
        REGISTERED_FONTS.add(font);
        return font;
    }

    public static void initialize() {
        REGISTERED_FONTS.forEach(font -> {
            if (font == Font.DEFAULT) {
                font.textRenderer = MiniMap.getClient().textRenderer;
            } else {
                font.textRenderer = generateTextRenderer(font);
            }
        });
    }

    private static TextRenderer generateTextRenderer(Font font) {
        JsonObject data = JsonHelper.deserialize(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(), String.format(PROVIDER_JSON, font.name, font.height(), font.oversample), JsonObject.class);
        if (data == null) {
            MiniMap.LOG.error("Could not deserialize internal font!");
            return MiniMap.getClient().textRenderer;
        }
        List<net.minecraft.client.font.Font> list = Lists.newArrayList();
        JsonArray jsonArray = JsonHelper.getArray(data, "providers");
        for (int i = jsonArray.size() - 1; i >= 0; --i) {
            try {
                JsonObject jsonObject = JsonHelper.asObject(jsonArray.get(i), "providers[" + i + "]");
                net.minecraft.client.font.Font mcFont = FontType.byId(JsonHelper.getString(jsonObject, "type")).createLoader(jsonObject).load(MiniMap.getClient().getResourceManager());
                if (mcFont != null) {
                    list.add(mcFont);
                }
                MiniMap.LOG.info("Loaded font " + font.name);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        FontStorage storage = new FontStorage(MiniMap.getClient().getTextureManager(), new Identifier(MiniMap.MODID, "font/" + font.name + "_" + font.height()));
        storage.setFonts(list);
        return new TextRenderer(id -> storage);
    }

    private final String name;
    private final float height;
    private final float oversample;

    private TextRenderer textRenderer;

    public Font(String name, float height, float oversample) {
        this.name = name;
        this.height = height;
        this.oversample = oversample;
    }

    public TextRenderer textRenderer() {
        return this.textRenderer;
    }

    public TextHandler textHandler() {
        return textRenderer().getTextHandler();
    }

    public int width(String text) {
        return textRenderer().getWidth(text);
    }

    public int width(Text text) {
        return textRenderer().getWidth(text);
    }

    public int width(OrderedText text) {
        return textRenderer().getWidth(text);
    }

    public float height() {
        return this.height;
    }

    public int centerX(String text, float x) {
        return (int) (x - width(text) / 2F);
    }

    public int centerX(Text text, float x) {
        return (int) (x - width(text) / 2F);
    }

    public int centerX(OrderedText text, float x) {
        return (int) (x - width(text) / 2F);
    }

    public int centerY(float y) {
        return (int) (y - height() / 2F);
    }

    public String trimToWidth(String text, int maxWidth) {
        return textRenderer().trimToWidth(text, maxWidth);
    }

    public String trimToWidth(String text, int maxWidth, boolean backwards) {
        return textRenderer().trimToWidth(text, maxWidth, backwards);
    }

    public StringVisitable trimToWidth(StringVisitable text, int width) {
        return textRenderer().trimToWidth(text, width);
    }

    public void drawTrimmed(MatrixStack matrixStack, StringVisitable text, int x, int y, int maxWidth, int color) {
        for (Iterator<OrderedText> iter = wrapLines(text, maxWidth).iterator(); iter.hasNext(); y += height()) {
            this.draw(matrixStack, iter.next(), x, y, color);
        }
    }

    public int getWrappedLinesHeight(String text, int maxWidth) {
        return (int) Math.ceil(height() * textHandler().wrapLines(text, maxWidth, Style.EMPTY).size());
    }

    public List<OrderedText> wrapLines(StringVisitable text, int width) {
        return textRenderer().wrapLines(text, width);
    }

    public boolean isRightToLeft() {
        return textRenderer().isRightToLeft();
    }

    // String

    public void draw(MatrixStack matrixStack, String text, float x, float y) {
        draw(matrixStack, text, x, y, 0xFFFFFFFF);
    }

    public void draw(MatrixStack matrixStack, String text, float x, float y, int color) {
        textRenderer().draw(matrixStack, text, x, y, color);
    }

    public void drawWithShadow(MatrixStack matrixStack, String text, float x, float y) {
        drawWithShadow(matrixStack, text, x, y, 0xFFFFFFFF);
    }

    public void drawWithShadow(MatrixStack matrixStack, String text, float x, float y, int color) {
        textRenderer().drawWithShadow(matrixStack, text, x, y, color);
    }

    public void drawCentered(MatrixStack matrixStack, String text, float x, float y) {
        drawCentered(matrixStack, text, x, y, 0xFFFFFFFF);
    }

    public void drawCentered(MatrixStack matrixStack, String text, float x, float y, int color) {
        draw(matrixStack, text, centerX(text, x), centerY(y), color);
    }

    public void drawCenteredWithShadow(MatrixStack matrixStack, String text, float x, float y) {
        drawCenteredWithShadow(matrixStack, text, x, y, 0xFFFFFFFF);
    }

    public void drawCenteredWithShadow(MatrixStack matrixStack, String text, float x, float y, int color) {
        drawWithShadow(matrixStack, text, centerX(text, x), centerY(y), color);
    }

    // Text

    public void draw(MatrixStack matrixStack, Text text, float x, float y) {
        draw(matrixStack, text, x, y, 0xFFFFFFFF);
    }

    public void draw(MatrixStack matrixStack, Text text, float x, float y, int color) {
        textRenderer().draw(matrixStack, text, x, y, color);
    }

    public void drawWithShadow(MatrixStack matrixStack, Text text, float x, float y) {
        drawWithShadow(matrixStack, text, x, y, 0xFFFFFFFF);
    }

    public void drawWithShadow(MatrixStack matrixStack, Text text, float x, float y, int color) {
        textRenderer().drawWithShadow(matrixStack, text, x, y, color);
    }

    public void drawCentered(MatrixStack matrixStack, Text text, float x, float y) {
        drawCentered(matrixStack, text, x, y, 0xFFFFFFFF);
    }

    public void drawCentered(MatrixStack matrixStack, Text text, float x, float y, int color) {
        draw(matrixStack, text, centerX(text, x), centerY(y), color);
    }

    public void drawCenteredWithShadow(MatrixStack matrixStack, Text text, float x, float y) {
        drawCenteredWithShadow(matrixStack, text, x, y, 0xFFFFFFFF);
    }

    public void drawCenteredWithShadow(MatrixStack matrixStack, Text text, float x, float y, int color) {
        drawWithShadow(matrixStack, text, centerX(text, x), centerY(y), color);
    }

    // OrderedText

    public void draw(MatrixStack matrixStack, OrderedText text, float x, float y) {
        draw(matrixStack, text, x, y, 0xFFFFFFFF);
    }

    public void draw(MatrixStack matrixStack, OrderedText text, float x, float y, int color) {
        textRenderer().draw(matrixStack, text, x, y, color);
    }

    public void drawWithShadow(MatrixStack matrixStack, OrderedText text, float x, float y) {
        drawWithShadow(matrixStack, text, x, y, 0xFFFFFFFF);
    }

    public void drawWithShadow(MatrixStack matrixStack, OrderedText text, float x, float y, int color) {
        textRenderer().drawWithShadow(matrixStack, text, x, y, color);
    }

    public void drawCentered(MatrixStack matrixStack, OrderedText text, float x, float y) {
        drawCentered(matrixStack, text, x, y, 0xFFFFFFFF);
    }

    public void drawCentered(MatrixStack matrixStack, OrderedText text, float x, float y, int color) {
        draw(matrixStack, text, centerX(text, x), centerY(y), color);
    }

    public void drawCenteredWithShadow(MatrixStack matrixStack, OrderedText text, float x, float y) {
        drawCenteredWithShadow(matrixStack, text, x, y, 0xFFFFFFFF);
    }

    public void drawCenteredWithShadow(MatrixStack matrixStack, OrderedText text, float x, float y, int color) {
        drawWithShadow(matrixStack, text, centerX(text, x), centerY(y), color);
    }
}
