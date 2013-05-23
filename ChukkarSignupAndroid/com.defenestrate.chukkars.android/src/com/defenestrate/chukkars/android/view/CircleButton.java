package com.defenestrate.chukkars.android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.Button;

import com.defenestrate.chukkars.android.R;


/**
 * Circular button with text on top, bottom, and across center.
 *<p>
 * To specify custom view attributes in an XML layout, add this APK's manifest
 * package namespace as an attribute in the top-level layout element:
 *<p>
 * For example,
 *<pre>
 * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
 *</pre>
 */
public class CircleButton extends Button {
    protected static String TAG = CircleButton.class.getSimpleName();

    /** Text */
    private String mTextUpper = null;
    private String mTextLower = null;
    private String mTextLowerLeft = null;
    private String mTextLowerRight = null;
    private String mTextCenter = null;

    /** Draw path */
    private Path mPathTextUpper = null;
    private Path mPathTextLower = null;
    private Path mPathTextLowerLeft = null;
    private Path mPathTextLowerRight = null;
    private Path mPathTextCenter = null;

    /** Paint attributes */
    private Paint mPaintTextUpper = null;
    private Paint mPaintTextLower = null;
    private Paint mPaintTextLowerLeft = null;
    private Paint mPaintTextLowerRight = null;
    private Paint mPaintTextCenter = null;

    /** Width and height */
    private int mViewWidth = 200;
    private int mViewHeight = 200;

    /** Text start angle; positive is clockwise, negative is counterclockwise */
    private float mStartAngleUpper = 180f;
    private float mStartAngleLower = 180f;
    private float mStartAngleLowerLeft = 180f;
    private float mStartAngleLowerRight = 80f;

    /** Text sweep angle; positive is clockwise, negative is counterclockwise */
    private float mSweepAngleUpper = 180f;
    private float mSweepAngleLower = -180f;
    private float mSweepAngleLowerLeft = -100f;
    private float mSweepAngleLowerRight = -80f;

    /** Text size */
    private float mTextSizeUpper = 20f;
    private float mTextSizeLower = 25f;
    private float mTextSizeLowerLeft = 25f;
    private float mTextSizeLowerRight = 25f;
    private float mTextSizeCenter = 30f;

    /** Text color */
    private int mTextColorUpper = Color.LTGRAY;
    private int mTextColorLower = Color.WHITE;
    private int mTextColorLowerLeft = Color.LTGRAY;
    private int mTextColorLowerRight = Color.WHITE;
    private int mTextColorCenter = Color.DKGRAY;

    /** Text bold */
    private boolean mIsTextBoldUpper = false;
    private boolean mIsTextBoldLower = false;
    private boolean mIsTextBoldLowerLeft = false;
    private boolean mIsTextBoldLowerRight = true;
    private boolean mIsTextBoldCenter = false;

    /** Positive offsets towards center; negative offsets away from center */
    private float mTextOffsetFromDefaultUpper = 6.0f;
    private float mTextOffsetUpper = 0f;
    private float mTextOffsetFromDefaultLower = 8.0f;
    private float mTextOffsetLower = 0f;
    private float mTextOffsetFromDefaultLowerLeft = 8.0f;
    private float mTextOffsetLowerLeft = 0f;
    private float mTextOffsetFromDefaultLowerRight = 8.0f;
    private float mTextOffsetLowerRight = 0f;

    /** Positive offsets downwards; negative offsets upwards */
    private float mTextOffsetFromDefaultCenter = 0f;
    private float mTextOffsetCenter = 0f;

    /** Text alignment */
    private TextAlignmentEnum mTextAlignUpper = TextAlignmentEnum.CENTER;
    private TextAlignmentEnum mTextAlignLower = TextAlignmentEnum.CENTER;
    private TextAlignmentEnum mTextAlignLowerLeft = TextAlignmentEnum.RIGHT;
    private TextAlignmentEnum mTextAlignLowerRight = TextAlignmentEnum.LEFT;
    private TextAlignmentEnum mTextAlignCenter = TextAlignmentEnum.CENTER;

    /**
     * Text alignment enum.
     */
    private enum TextAlignmentEnum {
        CENTER(Paint.Align.CENTER),
        LEFT(Paint.Align.LEFT),
        RIGHT(Paint.Align.RIGHT);

        /** Paint text alignment value */
        private Paint.Align mAlign = null;

        /**
         * Constructor maps local enum to Paint.Align enum
         *
         * @param Corresponding paint text alignment enum
         */
        private TextAlignmentEnum(final Paint.Align align) {
            mAlign = align;
        }

        /**
         * Convert from Paint.Align value.
         *
         * @return Paint text alignment enum
         */
        public static TextAlignmentEnum fromPaintAlign(final Paint.Align paintAlign) {
            TextAlignmentEnum align = null;
            switch (paintAlign) {
            case RIGHT:
                align = RIGHT;
                break;
            case LEFT:
                align = LEFT;
                break;
            case CENTER:
            default:
                align = CENTER;
                break;
            }
            return align;
        }

        /**
         * Retrieve corresponding paint text alignment enum.
         *
         * @return Corresponding paint text alignment enum
         */
        public Paint.Align getPaintAlign() {
            return mAlign;
        }
    }

    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context View context for access to current theme, resources, etc.
     */
    public CircleButton(final Context context) {
        super(context);
        calculateView();
    }

    /**
     * Constructor that is called when inflating a view from XML.
     *
     * @param context View context for access to current theme, resources, etc.
     * @param attrs Attributes of the XML tag that is inflating the view
     */
    public CircleButton(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        initFromXml(context, attrs, 0);
    }

    /**
     * Perform inflation from XML and apply a class-specific base style.
     *
     * @param context View context for access to current theme, resources, etc.
     * @param attrs Attributes of the XML tag that is inflating the view
     * @param defStyle Default style
     */
    public CircleButton(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        initFromXml(context, attrs, defStyle);
    }

    /**
     * Perform inflation from XML and apply a class-specific base style.
     *
     * @param context View context for access to current theme, resources, etc.
     * @param attrs Attributes of the XML tag that is inflating the view
     * @param defStyle Default style
     */
    private void initFromXml(final Context context, final AttributeSet attrs, final int defStyle) {
        // Custom view attributes
        final TypedArray taCustom = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.CircleButton, defStyle, defStyle);
        try {
            mTextUpper = taCustom.getString(R.styleable.CircleButton_textTop);
            mTextLower = taCustom.getString(R.styleable.CircleButton_textLower);
            mTextLowerLeft = taCustom.getString(R.styleable.CircleButton_textBottomLeft);
            mTextLowerRight = taCustom.getString(R.styleable.CircleButton_textBottomRight);
            mTextCenter = taCustom.getString(R.styleable.CircleButton_textMiddle);

            mViewWidth = taCustom.getDimensionPixelSize(R.styleable.CircleButton_width, mViewWidth);
            mViewHeight = taCustom.getDimensionPixelSize(R.styleable.CircleButton_height, mViewHeight);

            mStartAngleUpper = taCustom.getFloat(R.styleable.CircleButton_textStartAngleTop, mStartAngleUpper);
            mStartAngleLower = taCustom.getFloat(R.styleable.CircleButton_textStartAngleLower, mStartAngleLower);
            mStartAngleLowerLeft = taCustom.getFloat(R.styleable.CircleButton_textStartAngleBottomLeft, mStartAngleLowerLeft);
            mStartAngleLowerRight = taCustom.getFloat(R.styleable.CircleButton_textStartAngleBottomRight, mStartAngleLowerRight);

            mSweepAngleUpper = taCustom.getFloat(R.styleable.CircleButton_textSweepAngleTop, mSweepAngleUpper);
            mSweepAngleLower = taCustom.getFloat(R.styleable.CircleButton_textSweepAngleLower, mSweepAngleLower);
            mSweepAngleLowerLeft = taCustom.getFloat(R.styleable.CircleButton_textSweepAngleBottomLeft, mSweepAngleLowerLeft);
            mSweepAngleLowerRight = taCustom.getFloat(R.styleable.CircleButton_textSweepAngleBottomRight, mSweepAngleLowerRight);

            mTextSizeUpper = taCustom.getDimension(R.styleable.CircleButton_textSizeTop, mTextSizeUpper);
            mTextSizeLower = taCustom.getDimension(R.styleable.CircleButton_textSizeLower, mTextSizeLower);
            mTextSizeLowerLeft = taCustom.getDimension(R.styleable.CircleButton_textSizeBottomLeft, mTextSizeLowerLeft);
            mTextSizeLowerRight = taCustom.getDimension(R.styleable.CircleButton_textSizeBottomRight, mTextSizeLowerRight);
            mTextSizeCenter = taCustom.getDimension(R.styleable.CircleButton_textSizeMiddle, mTextSizeCenter);

            mTextColorUpper = taCustom.getColor(R.styleable.CircleButton_textColorTop, mTextColorUpper);
            mTextColorLower = taCustom.getColor(R.styleable.CircleButton_textColorLower, mTextColorLower);
            mTextColorLowerLeft = taCustom.getColor(R.styleable.CircleButton_textColorBottomLeft, mTextColorLowerLeft);
            mTextColorLowerRight = taCustom.getColor(R.styleable.CircleButton_textColorBottomRight, mTextColorLowerRight);
            mTextColorCenter = taCustom.getColor(R.styleable.CircleButton_textColorMiddle, mTextColorCenter);

            mIsTextBoldUpper = taCustom.getBoolean(R.styleable.CircleButton_isTextBoldTop, mIsTextBoldUpper);
            mIsTextBoldLower = taCustom.getBoolean(R.styleable.CircleButton_isTextBoldLower, mIsTextBoldLower);
            mIsTextBoldLowerLeft = taCustom.getBoolean(R.styleable.CircleButton_isTextBoldBottomLeft, mIsTextBoldLowerLeft);
            mIsTextBoldLowerRight = taCustom.getBoolean(R.styleable.CircleButton_isTextBoldBottomRight, mIsTextBoldLowerRight);
            mIsTextBoldCenter = taCustom.getBoolean(R.styleable.CircleButton_isTextBoldMiddle, mIsTextBoldCenter);

            mTextOffsetFromDefaultUpper = taCustom.getDimension(R.styleable.CircleButton_textOffsetTop, mTextOffsetFromDefaultUpper);
            mTextOffsetFromDefaultLower = taCustom.getDimension(R.styleable.CircleButton_textOffsetLower, mTextOffsetFromDefaultLower);
            mTextOffsetFromDefaultLowerLeft = taCustom.getDimension(R.styleable.CircleButton_textOffsetBottomLeft, mTextOffsetFromDefaultLowerLeft);
            mTextOffsetFromDefaultLowerRight = taCustom.getDimension(R.styleable.CircleButton_textOffsetBottomRight, mTextOffsetFromDefaultLowerRight);
            mTextOffsetFromDefaultCenter = taCustom.getDimension(R.styleable.CircleButton_textOffsetMiddle, mTextOffsetFromDefaultCenter);

            mTextAlignUpper = TextAlignmentEnum.values()[taCustom.getInteger(R.styleable.CircleButton_textAlignTop, mTextAlignUpper.ordinal())];
            mTextAlignLower = TextAlignmentEnum.values()[taCustom.getInteger(R.styleable.CircleButton_textAlignLower, mTextAlignLower.ordinal())];
            mTextAlignLowerLeft = TextAlignmentEnum.values()[taCustom.getInteger(R.styleable.CircleButton_textAlignBottomLeft, mTextAlignLowerLeft.ordinal())];
            mTextAlignLowerRight = TextAlignmentEnum.values()[taCustom.getInteger(R.styleable.CircleButton_textAlignBottomRight, mTextAlignLowerRight.ordinal())];
            mTextAlignCenter = TextAlignmentEnum.values()[taCustom.getInteger(R.styleable.CircleButton_textAlignMiddle, mTextAlignCenter.ordinal())];
        } finally {
            taCustom.recycle();
        }
        calculateView();
    }

    /**
     * Check text to ensure non-null values.
     * Replace null text to empty string.
     */
    private void checkText() {
        if (mTextUpper == null) {
            mTextUpper = "";
        }
        if (mTextLower == null) {
            mTextLower = "";
        }
        if (mTextLowerLeft == null) {
            mTextLowerLeft = "";
        }
        if (mTextLowerRight == null) {
            mTextLowerRight = "";
        }
        if (mTextCenter == null) {
            mTextCenter = "";
        }
    }

    /**
     * Create paint objects
     */
    private void createPaintObjects() {
        // Upper arc paint attributes
        mPaintTextUpper = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintTextUpper.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintTextUpper.setColor(mTextColorUpper);
        mPaintTextUpper.setTextSize(mTextSizeUpper);
        mPaintTextUpper.setFakeBoldText(mIsTextBoldUpper);
        mPaintTextUpper.setTextAlign(mTextAlignUpper.getPaintAlign());

        // Lower arc paint attributes
        mPaintTextLower = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintTextLower.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintTextLower.setColor(mTextColorLower);
        mPaintTextLower.setTextSize(mTextSizeLower);
        mPaintTextLower.setFakeBoldText(mIsTextBoldLower);
        mPaintTextLower.setTextAlign(mTextAlignLower.getPaintAlign());
        // Lower left arc paint attributes
        mPaintTextLowerLeft = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintTextLowerLeft.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintTextLowerLeft.setColor(mTextColorLowerLeft);
        mPaintTextLowerLeft.setTextSize(mTextSizeLowerLeft);
        mPaintTextLowerLeft.setFakeBoldText(mIsTextBoldLowerLeft);
        mPaintTextLowerLeft.setTextAlign(mTextAlignLowerLeft.getPaintAlign());
        // Lower right arc paint attributes
        mPaintTextLowerRight = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintTextLowerRight.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintTextLowerRight.setColor(mTextColorLowerRight);
        mPaintTextLowerRight.setTextSize(mTextSizeLowerRight);
        mPaintTextLowerRight.setFakeBoldText(mIsTextBoldLowerRight);
        mPaintTextLowerRight.setTextAlign(mTextAlignLowerRight.getPaintAlign());

        // Horizontal center line paint attributes
        mPaintTextCenter = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintTextCenter.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintTextCenter.setColor(mTextColorCenter);
        mPaintTextCenter.setTextSize(mTextSizeCenter);
        mPaintTextCenter.setFakeBoldText(mIsTextBoldCenter);
        mPaintTextCenter.setTextAlign(mTextAlignCenter.getPaintAlign());
    }

    /**
     * Create path objects.
     */
    private void createPathObjects(final RectF rectCircle) {
        // Path of upper text arc
        mPathTextUpper = new Path();
        mPathTextUpper.addArc(rectCircle, mStartAngleUpper, mSweepAngleUpper);

        // Path of lower text arc
        mPathTextLower = new Path();
        mPathTextLower.addArc(rectCircle, mStartAngleLower, mSweepAngleLower);
		// Path of lower left text arc
        mPathTextLowerLeft = new Path();
        mPathTextLowerLeft.addArc(rectCircle, mStartAngleLowerLeft, mSweepAngleLowerLeft);
        // Path of lower right text arc
        mPathTextLowerRight = new Path();
        mPathTextLowerRight.addArc(rectCircle, mStartAngleLowerRight, mSweepAngleLowerRight);

        // Path of horizontal text across center
        mPathTextCenter = new Path();
        mPathTextCenter.lineTo(mViewWidth, 0);
    }

    /**
     * Calculate view.
     */
    private void calculateView() {
        if (isInEditMode()) {
            return;
        }

        // View's rectangle
        final RectF rectCircle = new RectF(0, 0, mViewWidth, mViewHeight);
        // Ensure non-null text
        checkText();
        // Set up paint objects
        createPaintObjects();
        // Set up path objects
        createPathObjects(rectCircle);

        // Pre-allocate rectangles for text height measurements
        final Rect rectTextUpper = new Rect();
        final Rect rectTextCenter = new Rect();

        // Measure text heights
        mPaintTextUpper.getTextBounds(mTextUpper, 0, mTextUpper.length(), rectTextUpper);
        mPaintTextCenter.getTextBounds(mTextCenter, 0, mTextCenter.length(), rectTextCenter);

        // Add upper text height as offset to keep the text inside the circle.
        mTextOffsetUpper = mTextOffsetFromDefaultUpper + rectTextUpper.height();
        // Add center text origin offset
        mTextOffsetCenter = mTextOffsetFromDefaultCenter + ((float)mViewHeight / 2) + ((float)rectTextCenter.height() / 2);
        // Note that the lower text is drawn inside the circle by default.
        // Lower half text offsets are assigned in a similar manner as above
        // for code consistency.
        mTextOffsetLower = mTextOffsetFromDefaultLower;
        mTextOffsetLowerLeft = mTextOffsetFromDefaultLowerLeft;
        mTextOffsetLowerRight = mTextOffsetFromDefaultLowerRight;
    }

    /**
     * Recalculates view attributes, appearance, size, and shape.
     * Should be called after one or more view properties have changed.
     */
    public void recalculateView() {
        calculateView();
        invalidate();
        requestLayout();
    }

    /**
     * Retrieve text in middle.
     *
     * @return Text in middle
     */
    public String getTextMiddle() {
        return mTextCenter;
    }
    /**
     * Modify text in middle.
     *<p>
     * Corresponds to XML attribute string textMiddle
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textMiddle="Center text"
     *</pre>
     *
     * @param text Middle text
     */
    public void setTextMiddle(final String text) {
        mTextCenter = text;
        recalculateView();
    }
    /**
     * Modify text in middle.
     *<p>
     * Corresponds to XML attribute string textMiddle
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textMiddle="Center text"
     *</pre>
     *
     * @param resId Middle text resource ID
     */
    public void setTextMiddle(final int resId) {
        mTextCenter = getResources().getString(resId);
        recalculateView();
    }

    /**
     * Retrieve text across top.
     *
     * @return Text across top
     */
    public String getTextTop() {
        return mTextUpper;
    }
    /**
     * Modify text across top.
     *<p>
     * Corresponds to XML attribute string textTop
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textTop="Upper text"
     *</pre>
     *
     * @param text Top text
     */
    public void setTextTop(final String text) {
        mTextUpper = text;
        recalculateView();
    }

    /**
     * Modify text across top.
     *<p>
     * Corresponds to XML attribute string textTop
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textTop="Upper text"
     *</pre>
     *
     * @param resId Top text resource ID
     */
    public void setTextTop(final int resId) {
        mTextUpper = getResources().getString(resId);
        recalculateView();
    }

    /**
     * Retrieve text in bottom left.
     *
     * @return Text in bottom left.
     */
    public String getTextBottomLeft() {
        return mTextLowerLeft;
    }
    /**
     * Modify text in bottom left.
     *<p>
     * Corresponds to XML attribute string textBottomLeft
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textBottomLeft="Lower left text"
     *</pre>
     *
     * @param text Bottom left text
     */
    public void setTextBottomLeft(final String text) {
        mTextLowerLeft = text;
        recalculateView();
    }
    /**
     * Modify text in bottom left.
     *<p>
     * Corresponds to XML attribute string textBottomLeft
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textBottomLeft="Lower left text"
     *</pre>
     *
     * @param resId Bottom left text resource ID
     */
    public void setTextBottomLeft(final int resId) {
        mTextLowerLeft = getResources().getString(resId);
        recalculateView();
    }

    /**
     * Retrieve text in bottom right.
     *
     * @return Text in bottom right
     */
    public String getTextBottomRight() {
        return mTextLowerRight;
    }
    /**
     * Modify text in bottom right.
     *<p>
     * Corresponds to XML attribute string textBottomRight
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textBottomRight="Lower right text"
     *</pre>
     *
     * @param text Bottom right text
     */
    public void setTextBottomRight(final String text) {
        mTextLowerRight = text;
        recalculateView();
    }
    /**
     * Modify text in bottom right.
     *<p>
     * Corresponds to XML attribute string textBottomRight
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textBottomRight="Lower right text"
     *</pre>
     *
     * @param resId Bottom right text resource ID
     */
    public void setTextBottomRight(final int resId) {
        mTextLowerRight = getResources().getString(resId);
        recalculateView();
    }

    /**
     * Retrieve width.
     *
     * @return Width (pixels)
     */
    public int getViewWidth() {
        return mViewWidth;
    }
    /**
     * Modify width.
     *<p>
     * Corresponds to XML attribute dimension width
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:width="100dp"
     *</pre>
     *
     * @param width View width (pixels)
     */
    public void setViewWidth(final int width) {
        mViewWidth = width;
        recalculateView();
    }
    /**
     * Modify width.
     *<p>
     * Corresponds to XML attribute dimension width
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:width="100dp"
     *</pre>
     *
     * @param width View width (pixels)
     */
    @Override
    public void setWidth(final int width) {
        super.setWidth(width);
        mViewWidth = width;
        recalculateView();
    }

    /**
     * Retrieve height.
     *
     * @return Height (pixels)
     */
    public int getViewHeight() {
        return mViewHeight;
    }
    /**
     * Modify height.
     *<p>
     * Corresponds to XML attribute dimension height
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:height="100dp"
     *</pre>
     *
     * @param height View height (pixels)
     */
    public void setViewHeight(final int height) {
        mViewHeight = height;
        recalculateView();
    }
    /**
     * Modify height.
     *<p>
     * Corresponds to XML attribute dimension height
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:height="100dp"
     *</pre>
     *
     * @param height View height (pixels)
     */
    @Override
    public void setHeight(final int height) {
        super.setHeight(height);
        mViewHeight = height;
        recalculateView();
    }

    /**
     * Retrieve start angle for top text.
     *
     * @return Start angle for top text (degrees clockwise from 3 o'clock)
     */
    public float getTextStartAngleTop() {
        return mStartAngleUpper;
    }
    /**
     * Modify start angle for top text.
     * Positive values are clockwise; negative values are counter-clockwise.
     *<p>
     * Corresponds to XML attribute float textStartAngleTop
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textStartAngleTop="180.0"
     *</pre>
     *
     * @param angle Top text start angle (degrees clockwise from 3 o'clock)
     */
    public void setTextStartAngleTop(final float angle) {
        mStartAngleUpper = angle;
        recalculateView();
    }

    /**
     * Retrieve start angle for bottom left text.
     *
     * @return Start angle for bottom left text (degrees clockwise from 3 o'clock)
     */
    public float getTextStartAngleBottomLeft() {
        return mStartAngleLowerLeft;
    }

    /**
     * Modify start angle for bottom left text.
     * Positive values are clockwise; negative values are counter-clockwise.
     *<p>
     * Corresponds to XML attribute float textStartAngleBottomLeft
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textStartAngleBottomLeft="180.0"
     *</pre>
     *
     * @param angle Bottom left text start angle (degrees clockwise from 3 o'clock)
     */
    public void setTextStartAngleBottomLeft(final float angle) {
        mStartAngleLowerLeft = angle;
        recalculateView();
    }

    /**
     * Retrieve start angle for bottom right text.
     *
     * @return Start angle for bottom right text (degrees clockwise from 3 o'clock)
     */
    public float getTextStartAngleBottomRight() {
        return mStartAngleLowerRight;
    }
    /**
     * Modify start angle for bottom right text.
     * Positive values are clockwise; negative values are counter-clockwise.
     *<p>
     * Corresponds to XML attribute float textStartAngleBottomRight
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textStartAngleBottomRight="80.0"
     *</pre>
     *
     * @param angle Bottom right text start angle (degrees clockwise from 3 o'clock)
     */
    public void setTextStartAngleBottomRight(final float angle) {
        mStartAngleLowerRight = angle;
        recalculateView();
    }

    /**
     * Retrieve sweep angle for top text.
     *
     * @return Sweep angle for top text (degrees clockwise from 3 o'clock)
     */
    public float getTextSweepAngleTop() {
        return mSweepAngleUpper;
    }
    /**
     * Modify sweep angle for top text.
     * Positive values are clockwise; negative values are counter-clockwise.
     *<p>
     * Corresponds to XML attribute float textSweepAngleTop
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textSweepAngleTop="180.0"
     *</pre>
     *
     * @param angle Top text sweep angle (degrees clockwise from 3 o'clock)
     */
    public void setTextSweepAngleTop(final float angle) {
        mSweepAngleUpper = angle;
        recalculateView();
    }

    /**
     * Retrieve sweep angle for bottom left text.
     *
     * @return Sweep angle for bottom left text (degrees clockwise from 3 o'clock)
     */
    public float getTextSweepAngleBottomLeft() {
        return mSweepAngleLowerLeft;
    }

    /**
     * Modify sweep angle for bottom left text.
     * Positive values are clockwise; negative values are counter-clockwise.
     *<p>
     * Corresponds to XML attribute float textSweepAngleBottomLeft
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textSweepAngleBottomLeft="-100.0"
     *</pre>
     *
     * @param angle Bottom left text sweep angle (degrees clockwise from 3 o'clock)
     */
    public void setTextSweepAngleBottomLeft(final float angle) {
        mSweepAngleLowerLeft = angle;
        recalculateView();
    }

    /**
     * Retrieve sweep angle for bottom right text.
     *
     * @return Sweep angle for bottom right text (degrees clockwise from 3 o'clock)
     */
    public float getTextSweepAngleBottomRight() {
        return mSweepAngleLowerRight;
    }
    /**
     * Modify sweep angle for bottom right text.
     * Positive values are clockwise; negative values are counter-clockwise.
     *<p>
     * Corresponds to XML attribute float textSweepAngleBottomRight
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textSweepAngleBottomRight="-80.0"
     *</pre>
     *
     * @param angle Bottom right text sweep angle (degrees clockwise from 3 o'clock)
     */
    public void setTextSweepAngleBottomRight(final float angle) {
        mSweepAngleLowerRight = angle;
        recalculateView();
    }

    /**
     * Retrieve size of middle text.
     *
     * @return Size of middle text (pixels)
     */
    public float getTextSizeMiddle() {
        return mTextSizeCenter;
    }
    /**
     * Modify size of middle text.
     *<p>
     * Corresponds to XML attribute dimension textSizeMiddle
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textSizeMiddle="15.0sp"
     *</pre>
     *
     * @param size Middle text size (pixels)
     */
    public void setTextSizeMiddle(final float size) {
        mTextSizeCenter = size;
        recalculateView();
    }

    /**
     * Retrieve size of top text.
     *
     * @return Size of top text (pixels)
     */
    public float getTextSizeTop() {
        return mTextSizeUpper;
    }
    /**
     * Modify size of top text.
     *<p>
     * Corresponds to XML attribute dimension textSizeTop
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textSizeTop="10.0sp"
     *</pre>
     *
     * @param size Top text size (pixels)
     */
    public void setTextSizeTop(final float size) {
        mTextSizeUpper = size;
        recalculateView();
    }

    /**
     * Retrieve size of bottom left text.
     *
     * @return Size of bottom left text (pixels)
     */
    public float getTextSizeBottomLeft() {
        return mTextSizeLowerLeft;
    }

    /**
     * Modify size of bottom left text.
     *<p>
     * Corresponds to XML attribute dimension textSizeBottomLeft
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textSizeBottomLeft="12.0sp"
     *</pre>
     *
     * @param size Bottom left text size (pixels)
     */
    public void setTextSizeBottomLeft(final float size) {
        mTextSizeLowerLeft = size;
        recalculateView();
    }

    /**
     * Retrieve size of bottom right text.
     *
     * @return Size of bottom right text (pixels)
     */
    public float getTextSizeBottomRight() {
        return mTextSizeLowerRight;
    }
    /**
     * Modify size of bottom right text.
     *<p>
     * Corresponds to XML attribute dimension textSizeBottomRight
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textSizeBottomRight="12.0sp"
     *</pre>
     *
     * @param size Bottom right text size (pixels)
     */
    public void setTextSizeBottomRight(final float size) {
        mTextSizeLowerRight = size;
        recalculateView();
    }

    /**
     * Retrieve color of middle text.
     *
     * @return Color of middle text
     */
    public int getTextColorMiddle() {
        return mTextColorCenter;
    }
    /**
     * Modify color of middle text.
     *<p>
     * Corresponds to XML attribute color textColorMiddle
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textColorMiddle="#FF999999"
     *</pre>
     *
     * @param color Middle text color
     */
    public void setTextColorMiddle(final int color) {
        mTextColorCenter = color;
        recalculateView();
    }

    /**
     * Retrieve color of top text.
     *
     * @return Color of top text
     */
    public int getTextColorTop() {
        return mTextColorUpper;
    }
    /**
     * Modify color of top text.
     *<p>
     * Corresponds to XML attribute color textColorTop
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textColorTop="#FF999999"
     *</pre>
     *
     * @param color Top text color
     */
    public void setTextColorTop(final int color) {
        mTextColorUpper = color;
        recalculateView();
    }

    /**
     * Retrieve color of bottom left text.
     *
     * @return Color of bottom left text
     */
    public int getTextColorBottomLeft() {
        return mTextColorLowerLeft;
    }
    /**
     * Modify color of bottom left text.
     *<p>
     * Corresponds to XML attribute color textColorBottomLeft
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textColorBottomLeft="#FFAAAAAA"
     *</pre>
     *
     * @param color Bottom left text color
     */
    public void setTextColorBottomLeft(final int color) {
        mTextColorLowerLeft = color;
        recalculateView();
    }

    /**
     * Retrieve color of bottom right text.
     *
     * @return Color of bottom right text
     */
    public int getTextColorBottomRight() {
        return mTextColorLowerRight;
    }
    /**
     * Modify color of bottom right text.
     *<p>
     * Corresponds to XML attribute color textColorBottomRight
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textColorBottomRight="#FFEEEEEE"
     *</pre>
     *
     * @param color Bottom right text color.
     */
    public void setTextColorBottomRight(final int color) {
        mTextColorLowerRight = color;
        recalculateView();
    }

    /**
     * Retrieve whether middle text is bold.
     *
     * @return true if bold; false otherwise
     */
    public boolean isTextBoldMiddle() {
        return mIsTextBoldCenter;
    }
    /**
     * Modify whether middle text is bold.
     *<p>
     * Corresponds to XML attribute boolean isTextBoldMiddle
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:isTextBoldMiddle="false"
     *</pre>
     *
     * @param isBold true or false
     */
    public void setIsTextBoldMiddle(final boolean isBold) {
        mIsTextBoldCenter = isBold;
        recalculateView();
    }

    /**
     * Retrieve whether top text is bold.
     *
     * @return true if bold; false otherwise
     */
    public boolean isTextBoldTop() {
        return mIsTextBoldUpper;
    }
    /**
     * Modify whether top text is bold.
     *<p>
     * Corresponds to XML attribute boolean isTextBoldTop
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:isTextBoldTop="false"
     *</pre>
     *
     * @param isBold true or false
     */
    public void setIsTextBoldTop(final boolean isBold) {
        mIsTextBoldUpper = isBold;
        recalculateView();
    }

    /**
     * Retrieve whether bottom left text is bold.
     *
     * @return true if bold; false otherwise
     */
    public boolean isTextBoldBottomLeft() {
        return mIsTextBoldLowerLeft;
    }

    /**
     * Modify whether bottom left text is bold.
     *<p>
     * Corresponds to XML attribute boolean isTextBoldBottomLeft
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:sTextBoldBottomLeft="false"
     *</pre>
     *
     * @param isBold true or false
     */
    public void setIsTextBoldBottomLeft(final boolean isBold) {
        mIsTextBoldLowerLeft = isBold;
        recalculateView();
    }

    /**
     * Retrieve whether bottom right text is bold.
     *
     * @return true if bold; false otherwise
     */
    public boolean isTextBoldBottomRight() {
        return mIsTextBoldLowerRight;
    }
    /**
     * Modify whether bottom right text is bold.
     *<p>
     * Corresponds to XML attribute boolean isTextBoldBottomRight
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:isTextBoldBottomRight="true"
     *</pre>
     *
     * @param isBold true or false
     */
    public void setIsTextBoldBottomRight(final boolean isBold) {
        mIsTextBoldLowerRight = isBold;
        recalculateView();
    }

    /**
     * Retrieve offset of middle text.
     *
     * @return Offset of middle text (pixels downward)
     */
    public float getTextOffsetMiddle() {
        return mTextOffsetFromDefaultCenter;
    }
    /**
     * Modify offset of middle text.
     * Positive values are downward; negative values are upwards.
     *<p>
     * Corresponds to XML attribute dimension textOffsetMiddle
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textOffsetMiddle="0.0dp"
     *</pre>
     *
     * @param offset Middle text offset (pixels downward)
     */
    public void setTextOffsetMiddle(final float offset) {
        mTextOffsetFromDefaultCenter = offset;
        recalculateView();
    }

    /**
     * Retrieve offset of top text.
     *
     * @return Offset of top text (pixels towards center)
     */
    public float getTextOffsetTop() {
        return mTextOffsetFromDefaultUpper;
    }
    /**
     * Modify offset of top text.
     *<p>
     * Positive values are towards center; negative values are away from center.
     * Note: If you put top text on the bottom of the oval or vice versa,
     * then positive is away from center and negative is towards center.
     *<p>
     * Corresponds to XML attribute dimension textOffsetTop
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textOffsetTop="3.0dp"
     *</pre>
     *
     * @param offset Top text offset (pixels towards center)
     */
    public void setTextOffsetTop(final float offset) {
        mTextOffsetFromDefaultUpper = offset;
        recalculateView();
    }

    /**
     * Retrieve offset of bottom left text.
     *
     * @return Offset of bottom left text (pixels towards center)
     */
    public float getTextOffsetBottomLeft() {
        return mTextOffsetFromDefaultLowerLeft;
    }
    /**
     * Modify offset of bottom left text.
     *<p>
     * Positive values are towards center; negative values are away from center.
     * Note: If you put top text on the bottom of the oval or vice versa,
     * then positive is away from center and negative is towards center.
     *<p>
     * Corresponds to XML attribute dimension textOffsetBottomLeft
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textOffsetBottomLeft="4.0dp"
     *</pre>
     *
     * @param offset Bottom left text offset (pixels towards center)
     */
    public void setTextOffsetBottomLeft(final float offset) {
        mTextOffsetFromDefaultLowerLeft = offset;
        recalculateView();
    }

    /**
     * Retrieve offset of bottom right text.
     *
     * @return Offset of bottom right text (pixels towards center)
     */
    public float getTextOffsetBottomRight() {
        return mTextOffsetFromDefaultLowerRight;
    }
    /**
     * Modify offset of bottom right text.
     *<p>
     * Positive values are towards center; negative values are away from center.
     * Note: If you put top text on the bottom of the oval or vice versa,
     * then positive is away from center and negative is towards center.
     *<p>
     * Corresponds to XML attribute dimension textOffsetBottomRight
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textOffsetBottomRight="4.0dp"
     *</pre>
     *
     * @param offset Bottom right text offset (pixels towards center)
     */
    public void setTextOffsetBottomRight(final float offset) {
        mTextOffsetFromDefaultLowerRight = offset;
        recalculateView();
    }

    /**
     * Retrieve text alignment of middle text.
     *
     * @return Text alignment of middle text
     */
    public Paint.Align getTextAlignMiddle() {
        return mTextAlignCenter.getPaintAlign();
    }
    /**
     * Modify text alignment of middle text.
     *<p>
     * Corresponds to XML attribute enum textAlignMiddle {center, left, right}
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textAlignMiddle="center"
     *</pre>
     *
     * @param align Middle text alignment
     */
    public void setTextAlignMiddle(final Paint.Align align) {
        mTextAlignCenter = TextAlignmentEnum.fromPaintAlign(align);
        recalculateView();
    }

    /**
     * Retrieve text alignment of top text.
     *
     * @return Text alignment of top text
     */
    public Paint.Align getTextAlignTop() {
        return mTextAlignUpper.getPaintAlign();
    }
    /**
     * Modify text alignment of top text.
     *<p>
     * Corresponds to XML attribute enum textAlignTop {center, left, right}
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textAlignTop="center"
     *</pre>
     *
     * @param align Top text alignment
     */
    public void setTextAlignTop(final Paint.Align align) {
        mTextAlignUpper = TextAlignmentEnum.fromPaintAlign(align);
        recalculateView();
    }

    /**
     * Retrieve text alignment of bottom left text.
     *
     * @return Text alignment of bottom left text
     */
    public Paint.Align getTextAlignBottomLeft() {
        return mTextAlignLowerLeft.getPaintAlign();
    }
    /**
     * Modify text alignment of bottom left text.
     *<p>
     * Corresponds to XML attribute enum textAlignBottomLeft {center, left, right}
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:="right"
     *</pre>
     *
     * @param align Bottom left text alignment
     */
    public void setTextAlignBottomLeft(final Paint.Align align) {
        mTextAlignLowerLeft = TextAlignmentEnum.fromPaintAlign(align);
        recalculateView();
    }

    /**
     * Retrieve text alignment of bottom right text.
     *
     * @return Text alignment of bottom right text
     */
    public Paint.Align getTextAlignBottomRight() {
        return mTextAlignLowerRight.getPaintAlign();
    }
    /**
     * Modify text alignment of bottom right text.
     *<p>
     * Corresponds to XML attribute enum textAlignBottomRight {center, left, right}
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textAlignBottomRight="left"
     *</pre>
     *
     * @param align Bottom right text alignment
     */
    public void setTextAlignBottomRight(final Paint.Align align) {
        mTextAlignLowerRight = TextAlignmentEnum.fromPaintAlign(align);
        recalculateView();
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        if (isInEditMode()) {
            super.onDraw(canvas);
            return;
        }

        // Draw curved text
        canvas.drawTextOnPath(mTextUpper, mPathTextUpper, 0f, mTextOffsetUpper, mPaintTextUpper);
        canvas.drawTextOnPath(mTextLower, mPathTextLower, 0f, -mTextOffsetLower, mPaintTextLower);
        canvas.drawTextOnPath(mTextLowerLeft, mPathTextLowerLeft, 0f, -mTextOffsetLowerLeft, mPaintTextLowerLeft);
        canvas.drawTextOnPath(mTextLowerRight, mPathTextLowerRight, 0f, -mTextOffsetLowerRight, mPaintTextLowerRight);
        canvas.drawTextOnPath(mTextCenter, mPathTextCenter, 0f, mTextOffsetCenter, mPaintTextCenter);
        invalidate();
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mViewWidth, mViewHeight);
    }

 	/* MHIV-510 : Extend existing custom circle button layout so that
	one complete string could be written in the bottom from left
	which is center aligned with white foreground text color */

	/**
     * Retrieve color of lower text.
     *
     * @return Color of lower text
     */
    public int getTextColorLower() {
        return mTextColorLower;
    }

     /**
     * Modify color of lower text.
     *<p>
     * Corresponds to XML attribute color textColorLower
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textColorLower="#FF999999"
     *</pre>
     *
     * @param color Lower text color
     */
    public void setTextColorLower(final int color) {
        mTextColorLower = color;
        recalculateView();
    }

    /**
     * Retrieve start angle for lower text.
     *
     * @return Start angle for lower text (degrees clockwise from 3 o'clock)
     */
    public float getTextStartAngleLower() {
        return mStartAngleLower;
    }
    /**
     * Modify start angle for lower text.
     * Positive values are clockwise; negative values are counter-clockwise.
     *<p>
     * Corresponds to XML attribute float textStartAngleLower
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textStartAngleLower="180.0"
     *</pre>
     *
     * @param angle Bottom left text start angle (degrees clockwise from 3 o'clock)
     */
    public void setTextStartAngleLower(final float angle) {
        mStartAngleLower = angle;
        recalculateView();
    }
    /**
     * Retrieve size of bottom text.
     *
     * @return Size of bottom text (pixels)
     */
    public float getTextSizeLower() {
        return mTextSizeLower;
    }
    /**
     * Modify size of lower text.
     *<p>
     * Corresponds to XML attribute dimension textSizeLower
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textSizeLower="10.0sp"
     *</pre>
     *
     * @param size Lower text size (pixels)
     */
    public void setTextSizeLower(final float size) {
        mTextSizeLower = size;
        recalculateView();
    }

     /**
     * Retrieve text in lower.
     *
     * @return Text in lower.
     */
    public String getTextLower() {
        return mTextLower;
    }
    /**
     * Modify text lower
     *<p>
     * Corresponds to XML attribute string textLower
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textLower="Lower text"
     *</pre>
     *
     * @param text Top text
     */
    public void setTextLower(final String text) {
        mTextLower = text;
        recalculateView();
    }
    /**
     * Modify text lower.
     *<p>
     * Corresponds to XML attribute string text lower
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textLower="Lower text"
     *</pre>
     *
     * @param resId Top text resource ID
     */
    public void setTextLower(final int resId) {
        mTextLower = getResources().getString(resId);
        recalculateView();
    }

    /**
     * Retrieve sweep angle for lower
     *
     * @return Sweep angle for lower text (degrees clockwise from 3 o'clock)
     */
    public float getTextSweepAngleLower() {
        return mSweepAngleLower;
    }
    /**
     * Modify sweep angle for lower text.
     * Positive values are clockwise; negative values are counter-clockwise.
     *<p>
     * Corresponds to XML attribute float textSweepAngleLower
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textSweepAngleLower="-180.0"
     *</pre>
     *
     * @param angle Bottom left text sweep angle (degrees clockwise from 3 o'clock)
     */
    public void setTextSweepAngleLower(final float angle) {
        mSweepAngleLower = angle;
        recalculateView();
    }

 	/**
     * Retrieve whether lower text is bold.
     *
     * @return true if bold; false otherwise
     */
    public boolean isTextBoldLower() {
        return mIsTextBoldLower;
    }

    /**
     * Retrieve offset of lower
     *
     * @return Offset of lower (pixels towards center)
     */
    public float getTextOffsetLower() {
        return mTextOffsetFromDefaultLower;
    }

    /**
     * Modify offset of lower text.
     *<p>
     * Positive values are towards center; negative values are away from center.
     * Note: If you put top text on the bottom of the oval or vice versa,
     * then positive is away from center and negative is towards center.
     *<p>
     * Corresponds to XML attribute dimension textOffsetLower
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:textOffsetLower="4.0dp"
     *</pre>
     *
     * @param offset Bottom left text offset (pixels towards center)
     */
    public void setTextOffsetLower(final float offset) {
        mTextOffsetFromDefaultLower = offset;
        recalculateView();
    }

    /**
     * Modify text alignment of lower text.
     *<p>
     * Corresponds to XML attribute enum textAlignBottomLeft {center, left, right}
     * in namespace
     *<pre>
     * xmlns:circlebutton="http://schemas.android.com/apk/res/com.samsung.music"
     *</pre>
     *<p>
     * For example,
     *<pre>
     * circlebutton:="center"
     *</pre>
     *
     * @param align Lower text alignment
     */
    public void setTextAlignLower(final Paint.Align align) {
        mTextAlignLower = TextAlignmentEnum.fromPaintAlign(align);
        recalculateView();
    }

}
