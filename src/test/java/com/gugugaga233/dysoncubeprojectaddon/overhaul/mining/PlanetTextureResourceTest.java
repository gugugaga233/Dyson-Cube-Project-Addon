package com.gugugaga233.dysoncubeprojectaddon.overhaul.mining;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class PlanetTextureResourceTest {
    @Test
    void everyPlanetHasAValidTexture() throws IOException {
        ClassLoader classLoader = PlanetTextureResourceTest.class.getClassLoader();
        for (PlanetType type : PlanetType.values()) {
            String path = "assets/" + type.texture().getNamespace() + "/" + type.texture().getPath();
            try (InputStream input = classLoader.getResourceAsStream(path)) {
                assertNotNull(input, () -> "Missing planet texture: " + path);
                BufferedImage image = ImageIO.read(input);
                assertNotNull(image, () -> "Invalid PNG planet texture: " + path);
                assertEquals(128, image.getWidth(), () -> "Unexpected texture width: " + path);
                assertEquals(128, image.getHeight(), () -> "Unexpected texture height: " + path);
                boolean visible = false;
                for (int y = 0; y < image.getHeight() && !visible; y++) {
                    for (int x = 0; x < image.getWidth(); x++) {
                        if ((image.getRGB(x, y) >>> 24) != 0) {
                            visible = true;
                            break;
                        }
                    }
                }
                assertTrue(visible, () -> "Fully transparent planet texture: " + path);
            }
        }
    }
}

