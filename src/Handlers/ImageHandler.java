package Handlers;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageHandler {
    public static int height;
    public static int width;
    public String imageFile;

    public int[][][] readImageRGB(String path) {
        // RGB
        BufferedImage img;
        try {
            img = ImageIO.read(new File(path));
            height = img.getHeight();
            width = img.getWidth();
            int[][][] pixels = new int[height][width][4];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int pixel = img.getRGB(j, i);
                    int red = (pixel & 0x00ff0000) >> 16;
                    int green = (pixel & 0x0000ff00) >> 8;
                    int alpha = (pixel & 0xff000000) >> 24;
                    int blue = pixel & 0x000000ff;
                    pixels[i][j][0] = red;
                    pixels[i][j][1] = green;
                    pixels[i][j][2] = blue;
                    pixels[i][j][3] = alpha;
                }
            }
            return pixels;
        }
        catch (IOException e) {
            return null;
        }
    }
    public void writeImageRGB(int[][][] pixels, int width, int height, String outPath) {
        // RGB
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int red = pixels[i][j][0];
                int green = pixels[i][j][1];
                int blue = pixels[i][j][2];
                int alpha = pixels[i][j][3];
                int rgb = (alpha << 24) | (red << 16) | (green << 8) | blue ;
                image.setRGB(j, i, rgb);
            }
        }
        File ImageFile = new File(outPath);
        try {
            ImageIO.write(image, "jpg", ImageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static int[][] readImageGray(String path) {
        // GRAY
        BufferedImage img;
        try {
            img = ImageIO.read(new File(path));
            height = img.getHeight();
            width = img.getWidth();
            int[][] pixels = new int[height][width];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int pixel = img.getRGB(j, i);
                    int red = (pixel & 0x00ff0000) >> 16;
                    pixels[i][j] = red;
                }
            }
            return pixels;
        }
        catch (IOException e) {
            return null;
        }
    }
    public void writeImageGray(int[][] pixels, int width, int height, String outPath) {
        // GRAY
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int rgb = (pixels[i][j] << 16) | (pixels[i][j] << 8) | pixels[i][j];
                image.setRGB(j, i, rgb);
            }
        }
        File ImageFile = new File(outPath);
        try {
            ImageIO.write(image, "jpg", ImageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
