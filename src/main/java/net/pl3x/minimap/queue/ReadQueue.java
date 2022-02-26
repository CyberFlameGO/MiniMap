package net.pl3x.minimap.queue;

import net.pl3x.minimap.tile.Image;
import net.pl3x.minimap.tile.Tile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;

public class ReadQueue implements QueueAction {
    private final Tile tile;

    public ReadQueue(Tile tile) {
        this.tile = tile;
    }

    @Override
    public void run() {
        read(this.tile.getBase());
        read(this.tile.getBiomes());
        read(this.tile.getHeight());
        read(this.tile.getFluids());
        read(this.tile.getLight());
        this.tile.upload();
        this.tile.setReady(true);
    }

    private void read(Image image) {
        if (!Files.exists(image.path())) {
            return;
        }

        ImageReader reader = null;
        try (ImageInputStream in = ImageIO.createImageInputStream(Files.newInputStream(image.path()))) {
            reader = ImageIO.getImageReadersByFormatName("png").next();
            reader.setInput(in, true, false);
            BufferedImage buffer = reader.read(0);
            image.setPixels(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.dispose();
            }
        }
    }
}
