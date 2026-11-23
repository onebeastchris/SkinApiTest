package dev.onebeastchris.skinapitest;

import org.geysermc.geyser.api.skin.Skin;
import org.geysermc.geyser.api.skin.SkinData;
import org.geysermc.geyser.api.skin.SkinGeometry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Silly replacement player models: a solid-colour texture on a geometry built from stacked cubes.
 * Every cube uses box UV at [0, 0] and is at most 16 pixels wide, so it always samples inside the
 * fully coloured 64x64 texture.
 */
enum Shape {
    CONE(0xFFD800) {
        @Override
        List<String> cubes() {
            // 24 one-pixel layers, radius shrinking from 8 to 0
            List<String> cubes = new ArrayList<>();
            for (int y = 0; y < 24; y++) {
                octagonLayer(cubes, y, 1, 8 * (1 - (y + 0.5) / 24));
            }
            return cubes;
        }
    },
    PYRAMID(0xFFFFFF) {
        @Override
        List<String> cubes() {
            // 8 two-pixel steps, 16 wide at the bottom
            List<String> cubes = new ArrayList<>();
            for (int step = 0; step < 8; step++) {
                double size = 16 - step * 2;
                cubes.add(cube(-size / 2, step * 2, -size / 2, size, 2, size, null));
            }
            return cubes;
        }
    },
    SPHERE(0x2ECC40) {
        @Override
        List<String> cubes() {
            // radius 8, centred 8 pixels above the feet, in one-pixel layers
            List<String> cubes = new ArrayList<>();
            for (int y = 0; y < 16; y++) {
                double dy = y + 0.5 - 8;
                octagonLayer(cubes, y, 1, Math.sqrt(64 - dy * dy));
            }
            return cubes;
        }
    };

    private static final int TEXTURE_SIZE = 64;

    private final int rgb;

    Shape(int rgb) {
        this.rgb = rgb;
    }

    abstract List<String> cubes();

    String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    SkinData skinData() {
        String geometryName = "geometry.skinapitest." + id();
        String geometry = """
            {
              "format_version": "1.12.0",
              "minecraft:geometry": [{
                "description": {
                  "identifier": "%s",
                  "texture_width": %d,
                  "texture_height": %d,
                  "visible_bounds_width": 2,
                  "visible_bounds_height": 3,
                  "visible_bounds_offset": [0, 1.5, 0]
                },
                "bones": [{
                  "name": "root",
                  "pivot": [0, 0, 0],
                  "cubes": [
                    %s
                  ]
                }]
              }]
            }""".formatted(geometryName, TEXTURE_SIZE, TEXTURE_SIZE, String.join(",\n        ", cubes()));

        String resourcePatch = "{\"geometry\":{\"default\":\"" + geometryName + "\"}}";
        return new SkinData(
            new Skin("skinapitest:" + id(), solidTexture()),
            JavaSkinFetcher.NO_CAPE,
            new SkinGeometry(resourcePatch, geometry)
        );
    }

    private byte[] solidTexture() {
        byte[] data = new byte[TEXTURE_SIZE * TEXTURE_SIZE * 4];
        for (int i = 0; i < data.length; i += 4) {
            data[i] = (byte) (rgb >> 16);
            data[i + 1] = (byte) (rgb >> 8);
            data[i + 2] = (byte) rgb;
            data[i + 3] = (byte) 0xFF;
        }
        return data;
    }

    /**
     * Approximates a round slice with an octagon: two squares, the second turned 45 degrees.
     */
    private static void octagonLayer(List<String> cubes, double y, double height, double radius) {
        double size = 2 * radius;
        cubes.add(cube(-radius, y, -radius, size, height, size, null));
        cubes.add(cube(-radius, y, -radius, size, height, size, new double[] {0, y, 0}));
    }

    private static String cube(double x, double y, double z, double sx, double sy, double sz, double[] rotationPivot) {
        String rotation = rotationPivot == null ? "" : ", \"pivot\": [%s, %s, %s], \"rotation\": [0, 45, 0]"
            .formatted(num(rotationPivot[0]), num(rotationPivot[1]), num(rotationPivot[2]));
        return "{\"origin\": [%s, %s, %s], \"size\": [%s, %s, %s], \"uv\": [0, 0]%s}"
            .formatted(num(x), num(y), num(z), num(sx), num(sy), num(sz), rotation);
    }

    private static String num(double value) {
        return String.format(Locale.ROOT, "%.3f", value);
    }
}
