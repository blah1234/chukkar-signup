/**
 * @author			<a href="mailto:lwang@mspot.com">Larry Wang</a>
 * @copyright		Copyright (c) 2012 mSpot, Inc. All rights reserved.
 */
package com.defenestrate.chukkars.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.defenestrate.chukkars.android.R;


/**
 * <code>ImageView</code> that adjusts
 * its dimensions to maintain a square aspect ratio
 * (for cover art images).
 * <p>
 * In the layout xml, specify <code>app:square="true"</code>.
 * to scale the image to fit in the square.
 */
public class SquareImageView extends ImageView {

    /**
     * <code>true</code> to make the measured dimensions a square.
     */
    protected final boolean mIsSquare;

    public SquareImageView(Context context) {
        super(context);
        mIsSquare = false;
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mIsSquare = initSquareFlag(context, attrs);
        if (mIsSquare) {
            setScaleType(ScaleType.MATRIX);
        }
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mIsSquare = initSquareFlag(context, attrs);
        if (mIsSquare) {
            setScaleType(ScaleType.MATRIX);
        }
    }

    /**
     * Initialize the square flag from the xml attributes (if any).
     *
     * @param context           The context.
     * @param attrs             The xml attributes.
     * @return                  The square, <code>false</code>
     *                          if none specified in the xml attributes.
     */
    protected boolean initSquareFlag(Context context, AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SquareImageView);
        boolean square = false;
        for (int i = a.getIndexCount() - 1; i >= 0; i--) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.SquareImageView_square) {
                square = a.getBoolean(attr, false);
                break;
            }
        }
        a.recycle();

        return square;
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        updateMatrix();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        updateMatrix();
    }

    /**
     * Updates the scale matrix if necessary.
     */
    protected void updateMatrix() {
        if (mIsSquare) {
            final Drawable d = getDrawable();
            if (d != null) {
                final float scale = (float) getMeasuredWidth() / (float) getDrawable().getIntrinsicWidth();
                final Matrix m = new Matrix();
                m.postScale(scale, scale);
                setImageMatrix(m);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mIsSquare) {
            final int w = getMeasuredWidth();
            setMeasuredDimension(w, w);
            updateMatrix();
        }
    }

}
