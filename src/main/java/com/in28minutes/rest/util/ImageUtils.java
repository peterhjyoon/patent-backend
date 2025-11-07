package com.in28minutes.rest.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;

public class ImageUtils {

    public static String resizeAndConvertToBase64PNG(InputStream inputStream, int maxWidth, int maxHeight) throws Exception {
        BufferedImage originalImage = ImageIO.read(inputStream);

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        double scale = Math.min((double) maxWidth / width, (double) maxHeight / height);
        int newWidth = (int) (width * scale);
        int newHeight = (int) (height * scale);

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "png", outputStream);

        // ✅ Return raw base64 string, no data URI prefix
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }
}