package com.winomtech.androidmisc.common.utils;

/**
 * size (width and height dimensions).
 */
public class Size {
    /**
     * Sets the dimensions for pictures.
     *
     * @param w the width (pixels)
     * @param h the height (pixels)
     */
    public Size(int w, int h) {
        width = w;
        height = h;
    }

    /**
     * Compares {@code obj} to this size.
     *
     * @param obj the object to compare this size with.
     * @return {@code true} if the width and height of {@code obj} is the
     * same as those of this size. {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Size)) {
            return false;
        }
        Size s = (Size) obj;
        return width == s.width && height == s.height;
    }

    @Override
    public int hashCode() {
        return width * 32713 + height;
    }

    public int width;
    public int height;
};