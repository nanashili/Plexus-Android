package com.plexus.imageeditor.model;

import com.plexus.imageeditor.Bounds;
import com.plexus.imageeditor.Renderer;

import java.util.UUID;

/**
 * A special {@link Renderer} that controls another {@link EditorElement}.
 * <p>
 * It has a reference to the {@link EditorElement#getId()} and a {@link ControlPoint} which it is in control of.
 * <p>
 * The presence of this interface on the selected element is used to launch a ThumbDragEditSession.
 */
public interface ThumbRenderer extends Renderer {

    ControlPoint getControlPoint();

    UUID getElementToControl();

    enum ControlPoint {

        CENTER_LEFT(Bounds.LEFT, Bounds.CENTRE_Y),
        CENTER_RIGHT(Bounds.RIGHT, Bounds.CENTRE_Y),

        TOP_CENTER(Bounds.CENTRE_X, Bounds.TOP),
        BOTTOM_CENTER(Bounds.CENTRE_X, Bounds.BOTTOM),

        TOP_LEFT(Bounds.LEFT, Bounds.TOP),
        TOP_RIGHT(Bounds.RIGHT, Bounds.TOP),
        BOTTOM_LEFT(Bounds.LEFT, Bounds.BOTTOM),
        BOTTOM_RIGHT(Bounds.RIGHT, Bounds.BOTTOM);

        private final float x;
        private final float y;

        ControlPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public ControlPoint opposite() {
            switch (this) {
                case CENTER_LEFT:
                    return CENTER_RIGHT;
                case CENTER_RIGHT:
                    return CENTER_LEFT;
                case TOP_CENTER:
                    return BOTTOM_CENTER;
                case BOTTOM_CENTER:
                    return TOP_CENTER;
                case TOP_LEFT:
                    return BOTTOM_RIGHT;
                case TOP_RIGHT:
                    return BOTTOM_LEFT;
                case BOTTOM_LEFT:
                    return TOP_RIGHT;
                case BOTTOM_RIGHT:
                    return TOP_LEFT;
                default:
                    throw new RuntimeException();
            }
        }

        public boolean isHorizontalCenter() {
            return this == ControlPoint.CENTER_LEFT || this == ControlPoint.CENTER_RIGHT;
        }

        public boolean isVerticalCenter() {
            return this == ControlPoint.TOP_CENTER || this == ControlPoint.BOTTOM_CENTER;
        }

        public boolean isCenter() {
            return isHorizontalCenter() || isVerticalCenter();
        }
    }
}
