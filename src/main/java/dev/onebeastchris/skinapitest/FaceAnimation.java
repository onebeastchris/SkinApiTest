package dev.onebeastchris.skinapitest;

/**
 * An animated face: a second geometry, named under {@code animated_face} in the skin resource patch, textured by
 * an animation image whose frames are stacked top to bottom.
 * <p>
 * Unverified on a client: that the frames are stacked vertically, that the face geometry's texture size is
 * the size of one frame, and that a face bone named like a bone of the default geometry follows it.
 *
 * @param geometryName the face geometry's identifier
 * @param geometryEntry the face geometry, as an entry of {@code minecraft:geometry}
 * @param frameWidth the width of one frame
 * @param frameHeight the height of one frame
 * @param frames the number of frames
 * @param image every frame as RGBA, row by row, frames stacked top to bottom
 */
record FaceAnimation(String geometryName, String geometryEntry, int frameWidth, int frameHeight, int frames, byte[] image) {

    FaceAnimation {
        if (image.length != frameWidth * frameHeight * frames * 4) {
            throw new IllegalArgumentException("Image has %d bytes, expected %d frames of %d x %d"
                .formatted(image.length, frames, frameWidth, frameHeight));
        }
    }
}
