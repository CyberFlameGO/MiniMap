package net.pl3x.minimap.queue;

import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.tile.Image;
import net.pl3x.minimap.tile.Tile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class WriteQueue implements QueueAction {
    private final Tile tile;

    WriteQueue(Tile tile) {
        this.tile = tile;
    }

    @Override
    public void run() {
        write(this.tile.getBase());
        write(this.tile.getBiomes());
        write(this.tile.getHeight());
        write(this.tile.getFluids());
        write(this.tile.getLight());
    }

    private void write(Image image) {
        // create directories if they don't exist
        if (!Files.exists(image.path().getParent())) {
            try {
                Files.createDirectories(image.path().getParent());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        // write file to tmp on disk
        // this helps prevent corrupt pngs
        ImageWriter writer = null;
        Exception error = null;
        Path tmpPath = image.path().resolveSibling(image.path().getFileName() + ".tmp");
        try (ImageOutputStream out = ImageIO.createImageOutputStream(Files.newOutputStream(tmpPath))) {
            BufferedImage buffer = new BufferedImage(MiniMap.TILE_SIZE, MiniMap.TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
            ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(buffer);
            writer = ImageIO.getImageWriters(type, "png").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                // low quality == high compression
                param.setCompressionQuality(0.0f);
            }
            image.getPixels(buffer);
            writer.setOutput(out);
            writer.write(null, new IIOImage(buffer, null, null), param);
        } catch (IOException e) {
            // store error so we can return early after finally
            error = e;
        } finally {
            if (writer != null) {
                writer.dispose();
            }
        }

        // error out if we couldn't save tmp file
        if (error != null) {
            MiniMap.LOG.warn("Could not save tile image: " + image.path());
            error.printStackTrace();
            return;
        }

        // move tmp file into proper place
        try {
            Files.move(tmpPath, image.path(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e1) {
            try {
                Files.move(tmpPath, image.path(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }
}
