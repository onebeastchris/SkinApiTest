package dev.onebeastchris.skinapitest;

import org.geysermc.geyser.api.skin.Skin;
import org.geysermc.geyser.api.skin.SkinData;
import org.geysermc.geyser.api.skin.SkinGeometry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Builds a skin out of solid-coloured cubes.
 * <p>
 * The 128x128 texture is split into 64x32 slots, one per colour. Every cube uses box UV at the corner of its
 * colour's slot, so a cube must fit its box UV layout into one slot: {@code 2 * (width + depth) <= 64} and
 * {@code depth + height <= 32}. Bigger shapes have to be split into several cubes.
 * <p>
 * Bones named like the player's bones ({@code body}, {@code head}, {@code rightArm}, {@code leftArm},
 * {@code rightLeg}, {@code leftLeg}) are moved by the player's animations.
 */
final class CubeModel {
    private static final int TEXTURE_SIZE = 128;
    private static final int SLOT_WIDTH = 64;
    private static final int SLOT_HEIGHT = 32;
    private static final int SLOTS_PER_ROW = TEXTURE_SIZE / SLOT_WIDTH;
    private static final int MAX_COLORS = SLOTS_PER_ROW * (TEXTURE_SIZE / SLOT_HEIGHT);

    private final String id;
    private final List<Integer> palette = new ArrayList<>();
    private final List<String> bones = new ArrayList<>();
    private String bone;
    private final List<String> cubes = new ArrayList<>();

    CubeModel(String id) {
        this.id = id;
    }

    /**
     * Starts a new bone; the following cubes belong to it.
     */
    CubeModel bone(String name, String parent, double pivotX, double pivotY, double pivotZ) {
        finishBone();
        String parentJson = parent == null ? "" : "\"parent\": \"" + parent + "\", ";
        bone = "{\"name\": \"%s\", %s\"pivot\": [%s, %s, %s], \"cubes\": [".formatted(name, parentJson, num(pivotX), num(pivotY), num(pivotZ));
        return this;
    }

    CubeModel cube(int rgb, double x, double y, double z, double sizeX, double sizeY, double sizeZ) {
        return cube(rgb, x, y, z, sizeX, sizeY, sizeZ, "");
    }

    /**
     * A cube turned around the vertical axis through (pivotX, pivotZ).
     */
    CubeModel turnedCube(int rgb, double x, double y, double z, double sizeX, double sizeY, double sizeZ, double yaw, double pivotX, double pivotZ) {
        return cube(rgb, x, y, z, sizeX, sizeY, sizeZ, ", \"pivot\": [%s, %s, %s], \"rotation\": [0, %s, 0]"
            .formatted(num(pivotX), num(y), num(pivotZ), num(yaw)));
    }

    private CubeModel cube(int rgb, double x, double y, double z, double sizeX, double sizeY, double sizeZ, String rotation) {
        if (bone == null) {
            throw new IllegalStateException("Add a bone before adding cubes");
        }
        double width = Math.ceil(sizeX);
        double height = Math.ceil(sizeY);
        double depth = Math.ceil(sizeZ);
        if (2 * (width + depth) > SLOT_WIDTH || depth + height > SLOT_HEIGHT) {
            throw new IllegalArgumentException("Cube %s x %s x %s does not fit a %d x %d colour slot".formatted(sizeX, sizeY, sizeZ, SLOT_WIDTH, SLOT_HEIGHT));
        }

        int slot = palette.indexOf(rgb);
        if (slot == -1) {
            if (palette.size() == MAX_COLORS) {
                throw new IllegalStateException("A model can use at most " + MAX_COLORS + " colours");
            }
            slot = palette.size();
            palette.add(rgb);
        }
        int u = (slot % SLOTS_PER_ROW) * SLOT_WIDTH;
        int v = (slot / SLOTS_PER_ROW) * SLOT_HEIGHT;

        cubes.add("{\"origin\": [%s, %s, %s], \"size\": [%s, %s, %s], \"uv\": [%d, %d]%s}"
            .formatted(num(x), num(y), num(z), num(sizeX), num(sizeY), num(sizeZ), u, v, rotation));
        return this;
    }

    private void finishBone() {
        if (bone != null) {
            bones.add(bone + String.join(", ", cubes) + "]}");
            cubes.clear();
        }
    }

    SkinData build() {
        String resourcePatch = "{\"geometry\":{\"default\":\"" + geometryName() + "\"}}";
        return new SkinData(new Skin("skinapitest:" + id, texture()), JavaSkinFetcher.NO_CAPE, new SkinGeometry(resourcePatch, geometryFile(geometryEntry())));
    }

    String id() {
        return id;
    }

    String geometryName() {
        return "geometry.skinapitest." + id;
    }

    /**
     * @return this model as an entry of {@code minecraft:geometry}
     */
    String geometryEntry() {
        finishBone();
        bone = null;
        return """
            {
              "description": {
                "identifier": "%s",
                "texture_width": %d,
                "texture_height": %d,
                "visible_bounds_width": 3,
                "visible_bounds_height": 3,
                "visible_bounds_offset": [0, 1.5, 0]
              },
              "bones": [
                %s
              ]
            }""".formatted(geometryName(), TEXTURE_SIZE, TEXTURE_SIZE, String.join(",\n    ", bones));
    }

    static String geometryFile(String... entries) {
        return """
            {
              "format_version": "1.12.0",
              "minecraft:geometry": [
            %s
              ]
            }""".formatted(String.join(",\n", entries));
    }

    /**
     * RGBA, row by row. Unused slots stay transparent.
     */
    byte[] texture() {
        byte[] data = new byte[TEXTURE_SIZE * TEXTURE_SIZE * 4];
        for (int y = 0; y < TEXTURE_SIZE; y++) {
            for (int x = 0; x < TEXTURE_SIZE; x++) {
                int slot = (y / SLOT_HEIGHT) * SLOTS_PER_ROW + x / SLOT_WIDTH;
                if (slot >= palette.size()) {
                    continue;
                }
                int rgb = palette.get(slot);
                int i = (y * TEXTURE_SIZE + x) * 4;
                data[i] = (byte) (rgb >> 16);
                data[i + 1] = (byte) (rgb >> 8);
                data[i + 2] = (byte) rgb;
                data[i + 3] = (byte) 0xFF;
            }
        }
        return data;
    }

    private static String num(double value) {
        return String.format(Locale.ROOT, "%.3f", value);
    }
}
