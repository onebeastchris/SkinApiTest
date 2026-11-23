package dev.onebeastchris.skinapitest;

import java.util.Locale;

/**
 * Crude cube versions of brainrot meme characters. The model's front faces -z.
 */
enum Brainrot {
    /**
     * Tung Tung Tung Sahur: a wooden log with a face, stick arms and legs, and a bat.
     */
    SAHUR {
        @Override
        CubeModel model() {
            int wood = 0xC08A55;
            int darkWood = 0x8B5A2B;
            int limbs = 0xA9743F;
            int bat = 0xE0BC85;
            int white = 0xFFFFFF;
            int black = 0x111111;

            return new CubeModel(id())
                .bone("body", null, 0, 24, 0)
                // the log, made rounder with a turned copy, plus its end grain on top
                .cube(wood, -5, 10, -5, 10, 22, 10)
                .turnedCube(wood, -4.5, 10, -4.5, 9, 22, 9, 45, 0, 0)
                .cube(darkWood, -4, 32, -4, 8, 0.5, 8)
                // big eyes, eyebrows and a wide grin on the front
                .cube(white, -4, 24, -5.5, 3, 3.5, 0.5)
                .cube(white, 1, 24, -5.5, 3, 3.5, 0.5)
                .cube(black, -3, 24.5, -5.8, 1.2, 1.8, 0.3)
                .cube(black, 1.8, 24.5, -5.8, 1.2, 1.8, 0.3)
                .cube(darkWood, -4.5, 28.2, -5.6, 3.5, 0.8, 0.6)
                .cube(darkWood, 1, 28.2, -5.6, 3.5, 0.8, 0.6)
                .cube(black, -3, 19, -5.5, 6, 1.5, 0.5)
                .bone("rightArm", "body", -6, 23, 0)
                .cube(limbs, -7, 13, -1, 2, 10, 2)
                // the bat, held forward in the right hand
                .cube(bat, -6.75, 13.25, -6, 1.5, 1.5, 5)
                .cube(bat, -7.25, 12.75, -16, 2.5, 2.5, 10)
                .bone("leftArm", "body", 6, 23, 0)
                .cube(limbs, 5, 13, -1, 2, 10, 2)
                .bone("rightLeg", null, -2, 10, 0)
                .cube(limbs, -3, 0, -1, 2, 10, 2)
                .bone("leftLeg", null, 2, 10, 0)
                .cube(limbs, 1, 0, -1, 2, 10, 2);
        }

        /**
         * Googly eyes on a flat panel just in front of the static eyes: look left, centre, right, centre, blink, centre.
         */
        @Override
        FaceAnimation faceAnimation() {
            int frameWidth = 16;
            int frameHeight = 8;
            int[] pupilColumns = {0, 1, 2, 1, -1, 1}; // -1 blinks
            byte[] image = new byte[frameWidth * frameHeight * pupilColumns.length * 4];

            for (int frame = 0; frame < pupilColumns.length; frame++) {
                int pupil = pupilColumns[frame];
                // The panel is 8 x 4 pixels. Its front face uses u 0-7 and its back face u 8-15, so paint both.
                for (int half = 0; half < 2; half++) {
                    for (int eye = 0; eye < 2; eye++) {
                        for (int column = 0; column < 3; column++) {
                            for (int row = 0; row < 4; row++) {
                                int rgb;
                                if (pupil == -1) {
                                    rgb = row == 2 ? 0x8B5A2B : 0xC08A55; // closed eyelid
                                } else {
                                    rgb = column == pupil && (row == 1 || row == 2) ? 0x111111 : 0xFFFFFF;
                                }
                                int x = half * 8 + eye * 5 + column;
                                int y = frame * frameHeight + row;
                                paint(image, frameWidth, x, y, rgb);
                            }
                        }
                    }
                }
            }

            String geometryName = "geometry.skinapitest.sahur.face";
            String geometryEntry = """
                {
                  "description": {"identifier": "%s", "texture_width": %d, "texture_height": %d},
                  "bones": [{"name": "body", "pivot": [0, 24, 0], "cubes": [
                    {"origin": [-4, 24, -5.9], "size": [8, 4, 0], "uv": [0, 0]}
                  ]}]
                }""".formatted(geometryName, frameWidth, frameHeight);
            return new FaceAnimation(geometryName, geometryEntry, frameWidth, frameHeight, pupilColumns.length, image);
        }
    },
    /**
     * Tralalero Tralala: a shark standing on legs in sneakers.
     */
    TRALALERO {
        @Override
        CubeModel model() {
            int shark = 0x5B7FA6;
            int belly = 0xEDEDED;
            int shoe = 0x2E6FE0;
            int white = 0xFFFFFF;
            int black = 0x111111;
            int mouth = 0x7A1A1A;

            return new CubeModel(id())
                .bone("body", null, 0, 24, 0)
                // the shark lies along z, snout to the front
                .cube(shark, -4, 10, -10, 8, 8, 20)
                .cube(belly, -3.5, 9.5, -9, 7, 0.5, 17)
                .cube(shark, -3, 11, -13, 6, 6, 3)
                .cube(shark, -0.5, 18, -2, 1, 6, 4)
                .cube(shark, -0.5, 9, 10, 1, 12, 3)
                // eyes on the sides, a mouth with teeth on the front
                .cube(black, -4.3, 15, -9, 0.3, 1.5, 1.5)
                .cube(black, 4, 15, -9, 0.3, 1.5, 1.5)
                .cube(mouth, -2.5, 12, -13.3, 5, 1.5, 0.3)
                .cube(white, -2.5, 13, -13.5, 5, 0.5, 0.2)
                // pectoral fins
                .bone("rightArm", "body", -4, 13, -4)
                .cube(shark, -7, 12, -6, 3, 1, 4)
                .bone("leftArm", "body", 4, 13, -4)
                .cube(shark, 4, 12, -6, 3, 1, 4)
                // legs in sneakers
                .bone("rightLeg", null, -2, 10, 0)
                .cube(shark, -3, 2.5, -1, 2, 7.5, 2)
                .cube(shoe, -3.5, 0.5, -3, 3, 2, 4.5)
                .cube(white, -3.5, 0, -3, 3, 0.5, 4.5)
                .bone("leftLeg", null, 2, 10, 0)
                .cube(shark, 1, 2.5, -1, 2, 7.5, 2)
                .cube(shoe, 0.5, 0.5, -3, 3, 2, 4.5)
                .cube(white, 0.5, 0, -3, 3, 0.5, 4.5);
        }
    };

    abstract CubeModel model();

    /**
     * @return the animated face, or null if the face is static
     */
    FaceAnimation faceAnimation() {
        return null;
    }

    String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    private static void paint(byte[] image, int width, int x, int y, int rgb) {
        int i = (y * width + x) * 4;
        image[i] = (byte) (rgb >> 16);
        image[i + 1] = (byte) (rgb >> 8);
        image[i + 2] = (byte) rgb;
        image[i + 3] = (byte) 0xFF;
    }

    static Brainrot byId(String id) {
        for (Brainrot brainrot : values()) {
            if (brainrot.id().equalsIgnoreCase(id)) {
                return brainrot;
            }
        }
        return null;
    }
}
