package com.defenestrate.chukkars.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

/**
 * Renders a highlight over the image,
 * with support to ensure square dimensions.
 */
public class HighlightImageView extends SquareImageView {

    public HighlightImageView(Context context) {
        super(context);
    }

    public HighlightImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HighlightImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final Drawable background = getBackground();
        if (background != null) {
            background.draw(canvas);
        }
    }

}
