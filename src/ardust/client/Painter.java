package ardust.client;

import ardust.shared.Constants;
import ardust.shared.Loader;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Hashtable;

public class Painter {

    private static final double TEXTURE_EPSILON = 0.0001; // way smaller than a pixel, saves us from some rounding artifacts
    private double screenWidth;
    private double screenHeight;
    private double textureWidth;
    private double textureHeight;

    int textureId;

    private int scale = 0;


    void setScreenDimensions(int width, int height) {
        screenWidth = width;
        screenHeight = height;
    }

    void setScale(int scale) {
        this.scale = scale;
    }

    IntBuffer createIntBuffer(int size) {
        ByteBuffer temp = ByteBuffer.allocateDirect(4 * size);
        temp.order(ByteOrder.nativeOrder());

        return temp.asIntBuffer();
    }

    void init() {
        IntBuffer tmp = createIntBuffer(1);
        GL11.glGenTextures(tmp);
        textureId = tmp.get(0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

        BufferedImage image;
        try {
            image = ImageIO.read(Loader.getRequiredResourceAsStream("resources/placeholder.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        textureWidth = image.getWidth();
        textureHeight = image.getHeight();

        ByteBuffer textureBuffer = convertImageData(image);

        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, textureBuffer);
    }

    private ByteBuffer convertImageData(BufferedImage bufferedImage) {
        ByteBuffer imageBuffer;
        WritableRaster raster;
        BufferedImage texImage;

        int texWidth = 2;
        int texHeight = 2;

        while (texWidth < bufferedImage.getWidth()) {
            texWidth *= 2;
        }
        while (texHeight < bufferedImage.getHeight()) {
            texHeight *= 2;
        }

        raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, texWidth, texHeight, 4, null);
        texImage = new BufferedImage(new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                new int[]{8, 8, 8, 8},
                true,
                false,
                ComponentColorModel.OPAQUE,
                DataBuffer.TYPE_BYTE), raster, false, new Hashtable());

        Graphics2D g = (Graphics2D) texImage.getGraphics();
        g.setColor(new Color(0f, 0f, 0f, 0f));
        g.fillRect(0, 0, texWidth, texHeight);
        g.translate(0, texHeight);
        AffineTransform t = AffineTransform.getScaleInstance(1, -1);
        g.drawImage(bufferedImage, t, null);

        byte[] data = ((DataBufferByte) texImage.getRaster().getDataBuffer()).getData();

        imageBuffer = ByteBuffer.allocateDirect(data.length);
        imageBuffer.order(ByteOrder.nativeOrder());
        imageBuffer.put(data, 0, data.length);
        imageBuffer.flip();

        return imageBuffer;
    }

    void start() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    void draw(int x, int y, int textureX, int textureY, int width, int height, boolean flip) {

        double screenleftx = x * scale;
        double screenrightx = (x + width) * scale;
        double screentopy = y * scale;
        double screenbottomy = (y + height) * scale;

        if ((screenbottomy < 0) || (screenrightx < 0) || (screenleftx > screenWidth) || (screentopy > screenHeight))
            return;

        double textureleftx = textureX / textureWidth - TEXTURE_EPSILON;
        double texturerightx = (textureX + width) / textureWidth + TEXTURE_EPSILON;
        double texturetopy = 1.0 - (textureY / textureHeight - TEXTURE_EPSILON);
        double texturebottomy = 1.0 - ((textureY + height) / textureHeight + TEXTURE_EPSILON);

        // is this order correct ?

        if (!flip) {
            GL11.glTexCoord2f((float) textureleftx, (float) texturetopy);
            GL11.glVertex2f((float) screenleftx, (float) screentopy);

            GL11.glTexCoord2f((float) texturerightx, (float) texturetopy);
            GL11.glVertex2f((float) screenrightx, (float) screentopy);

            GL11.glTexCoord2f((float) texturerightx, (float) texturebottomy);
            GL11.glVertex2f((float) screenrightx, (float) screenbottomy);

            GL11.glTexCoord2f((float) textureleftx, (float) texturebottomy);
            GL11.glVertex2f((float) screenleftx, (float) screenbottomy);
        } else {
            GL11.glTexCoord2f((float) textureleftx, (float) texturetopy);
            GL11.glVertex2f((float) screenrightx, (float) screentopy);


            GL11.glTexCoord2f((float) texturerightx, (float) texturetopy);
            GL11.glVertex2f((float) screenleftx, (float) screentopy);

            GL11.glTexCoord2f((float) texturerightx, (float) texturebottomy);
            GL11.glVertex2f((float) screenleftx, (float) screenbottomy);

            GL11.glTexCoord2f((float) textureleftx, (float) texturebottomy);
            GL11.glVertex2f((float) screenrightx, (float) screenbottomy);
        }


    }

    public void flush() {
        GL11.glEnd();
    }

    public void getSourceRectFromTileSheetIndex(int index, Rectangle result) {
        if (textureWidth <= 0)
            result.setBounds(0, 0, 0, 0);
        else
            result.setBounds((index * Constants.TILE_BASE_WIDTH) % (int) (textureWidth),
                    ((index * Constants.TILE_BASE_WIDTH) / (int) (textureWidth)) * Constants.TILE_DRAW_HEIGHT,
                    Constants.TILE_BASE_WIDTH,
                    Constants.TILE_DRAW_HEIGHT);
    }

    public int getDrawableWidth() {
        return (int) (screenWidth / Constants.PIXEL_SCALE);
    }

    public int getDrawableHeight() {
        return (int) (screenHeight / Constants.PIXEL_SCALE);
    }

    static ByteBuffer buffer = ByteBuffer.allocateDirect(4);

    public static Color getColorAt(int x, int y) {
        GL11.glReadPixels(x, y, 1, 1, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        return new Color(((int) buffer.get(0)) & 0xff, ((int) buffer.get(1)) & 0xff, ((int) buffer.get(2)) & 0xff, ((int) buffer.get(3)) & 0xff);
    }
}
