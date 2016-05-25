package com.fenchtose.tooltip;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Animation for the Tooltip.
 *
 * Types of Animations available: {@link Type}
 *
 */
public class TooltipAnimation {

    public static final int NONE = 0;
    public static final int FADE = 1;
    public static final int REVEAL = 2;
    public static final int SCALE = 3;
    public static final int SCALE_AND_FADE = 4;

    /**
     * Types of Animations available:
     * <br/>
     * <ul>
     *     <li>{@link #NONE} : No Animation</li>
     *     <li>{@link #FADE} : Fade in and Fade Out</li>
     *     <li>{@link #REVEAL} : Circular Reveal. Center point would be Tip. If tip is not present,
     *     center point would be where it's being anchored. This is supported for API 21 and above.</li>
     *     <li>{@link #SCALE} : Scale animation based on position of the tooltip</li>
     *     <li>{@link #SCALE_AND_FADE} : Scale and Fade animation</li>
     * </ul>
     */
    @IntDef({NONE, FADE, REVEAL, SCALE, SCALE_AND_FADE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {}

    @Type
    private int type;
    private static final int DEFAULT_TYPE = FADE;

    private static final int DEFAULT_DURATION = 400; // ms
    private int duration;

    /**
     * Create a new Animation object for {@link Tooltip}
     *
     * @param type {@link Type}
     * @param duration animation duration in milliseconds
     */
    public TooltipAnimation(@Type int type, int duration) {
        this.type = type;
        this.duration = duration;
    }

    /**
     * Create a new Animation object for {@link Tooltip}, with duration {@link #DEFAULT_DURATION}
     * @param type {@link Type}
     */
    public TooltipAnimation(@Type int type) {
        this(type, DEFAULT_DURATION);
    }

    /**
     * Create a new Animation object for {@link Tooltip}
     * <br/>
     * type {@link #DEFAULT_TYPE}
     * <br/>
     * duration {@link #DEFAULT_DURATION}
     */
    public TooltipAnimation() {
        this(DEFAULT_TYPE, DEFAULT_DURATION);
    }

    @Type
    public int getType() {
        return type;
    }

    public int getDuration() {
        return duration;
    }
}
