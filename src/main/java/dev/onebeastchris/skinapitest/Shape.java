package dev.onebeastchris.skinapitest;

import org.geysermc.geyser.api.skin.SkinData;

import java.util.Locale;

/**
 * Silly replacement player models: one solid colour on a geometry built from stacked cubes.
 */
enum Shape {
    CONE(0xFFD800) {
        @Override
        void addCubes(CubeModel model) {
            // 24 one-pixel layers, radius shrinking from 8 to 0
            for (int y = 0; y < 24; y++) {
                octagonLayer(model, y, 8 * (1 - (y + 0.5) / 24));
            }
        }
    },
    PYRAMID(0xFFFFFF) {
        @Override
        void addCubes(CubeModel model) {
            // 8 two-pixel steps, 16 wide at the bottom
            for (int step = 0; step < 8; step++) {
                double size = 16 - step * 2;
                model.cube(rgb, -size / 2, step * 2, -size / 2, size, 2, size);
            }
        }
    },
    SPHERE(0x2ECC40) {
        @Override
        void addCubes(CubeModel model) {
            // radius 8, centred 8 pixels above the feet, in one-pixel layers
            for (int y = 0; y < 16; y++) {
                double dy = y + 0.5 - 8;
                octagonLayer(model, y, Math.sqrt(64 - dy * dy));
            }
        }
    };

    final int rgb;

    Shape(int rgb) {
        this.rgb = rgb;
    }

    abstract void addCubes(CubeModel model);

    String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    SkinData skinData() {
        CubeModel model = new CubeModel(id()).bone("root", null, 0, 0, 0);
        addCubes(model);
        return model.build();
    }

    /**
     * Approximates a round slice with an octagon: two squares, the second turned 45 degrees.
     */
    void octagonLayer(CubeModel model, double y, double radius) {
        double size = 2 * radius;
        model.cube(rgb, -radius, y, -radius, size, 1, size);
        model.turnedCube(rgb, -radius, y, -radius, size, 1, size, 45, 0, 0);
    }
}
