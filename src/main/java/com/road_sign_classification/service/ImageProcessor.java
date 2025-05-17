package com.road_sign_classification.service;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import java.io.InputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.nio.FloatBuffer;

public class ImageProcessor {

    // Method to process the image without any NDArray manipulation
    public NDList processImage(InputStream imageStream, NDManager manager) throws Exception {
        // Load image using standard Java (BufferedImage)
        BufferedImage image = ImageIO.read(imageStream);

        // Resize the image to 224x224 (manual resizing)
        BufferedImage resizedImage = new BufferedImage(224, 224, BufferedImage.TYPE_INT_RGB);
        resizedImage.getGraphics().drawImage(image, 0, 0, 224, 224, null);

        // Convert resized image to a float array (normalized 0.0 - 1.0)
        float[] imageData = new float[3 * 224 * 224];
        int index = 0;
        for (int y = 0; y < 224; y++) {
            for (int x = 0; x < 224; x++) {
                int pixel = resizedImage.getRGB(x, y);
                imageData[index++] = ((pixel >> 16) & 0xFF) / 255.0f; // Red channel
                imageData[index++] = ((pixel >> 8) & 0xFF) / 255.0f;  // Green channel
                imageData[index++] = (pixel & 0xFF) / 255.0f;         // Blue channel
            }
        }

        // Create a FloatBuffer from the image data
        FloatBuffer buffer = FloatBuffer.wrap(imageData);

        // Create an NDArray directly with the correct shape (1, 3, 224, 224)
        NDArray array = manager.create(buffer, new Shape(1, 3, 224, 224), DataType.FLOAT32);

        return new NDList(array);
    }
}
