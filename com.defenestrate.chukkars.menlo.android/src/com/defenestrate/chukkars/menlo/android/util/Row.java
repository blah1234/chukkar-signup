package com.defenestrate.chukkars.menlo.android.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;

import com.defenestrate.chukkars.menlo.android.R;


/**
 * Helper methods to populate a simple row.
 * In additional to what's in the base view,
 * this view has a third label and a context menu button.
 */
public class Row extends BaseRow {

	///////////////////////////// CONSTRUCTORS /////////////////////////////////
    public Row(Context context) {
        super(context);
    }


	//////////////////////////////// METHODS ///////////////////////////////////
    /**
     * Gets the view for a row.
     *
     * @param convertView           The view that may be recycled.
     * @param parent                The parent of the view.
     * @return                      The view for the row.
     *                              Call <code>View.getTag()</code> to get the {@link BaseViewHolder}.
     */
    public View getView(View convertView, ViewGroup parent) {
        final View v;
        if (convertView != null) {
            v = convertView;
        } else {
            v = mInflater.inflate(R.layout.list_item, parent, false);
            final BaseViewHolder holder = new ViewHolder(
                mContext,
                (ImageSwitcher) v.findViewById(R.id.icon),
                (TextView) v.findViewById(R.id.label1),
                (TextView) v.findViewById(R.id.label2),
                (TextView) v.findViewById(R.id.label3));

            v.setTag(holder);
        }

        return v;
    }

    /**
     * Populates a row.
     *
     * @param holder            The view holder of the row to populate.
     * @param icon              The row icon, <code>null</code> if none.
     * @param labels            The labels, <code>null</code> if none.
     */
    public void initRow(ViewHolder holder, Drawable icon, String... labels) {
        super.initRow(holder, icon, labels);

        if (labels != null) {
            final int len = labels.length;
            if (len >= 3) {
                holder.label3.setText(labels[2]);
                if (labels[2] != null && !labels[2].isEmpty()) {
                    holder.label3.setVisibility(View.VISIBLE);
                } else {
                    holder.label3.setVisibility(View.GONE);
                }
            } else {
                holder.label3.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Caches the child views in a row,
     * so that we don't need to call <code>View.findViewById()</code> more than once.
     * In additional to what's in the base view,
     * this view has a third label and a context menu button.
     */
    public static class ViewHolder extends BaseViewHolder {

        /** The tertiary label. */
        public final TextView label3;

        public ViewHolder(
                Context context,
                ImageSwitcher icon, TextView label1, TextView label2, TextView label3) {

            super(context, icon, label1, label2);

            this.label3 = label3;
        }

        /**
         * Convenience method to get the current <code>Drawable</code>
         * in {@link #icon}.
         *
         * @return          The current <code>Drawable</code> in
         *                  {@link #icon}.
         */
        public Drawable getIconDrawable() {
            return (icon != null) ? ((ImageView) icon.getCurrentView()).getDrawable() : null;
        }

        /**
         * Convenience method to set the <code>Drawable</code> in an <code>ImageSwitcher</code>,
         * with fade animation.
         *
         * @param switcher  The image switcher.
         * @param icon      The new icon image.
         *                  If <code>null</code>, then <code>switcher</code>'s visibility
         *                  is set to <code>View.GONE</code>.
         * @param doFade    <code>true</code> to fade in the icon.
         */
        public void setIconDrawable(ImageSwitcher switcher, Drawable d, boolean doFade) {
            final Drawable currentIcon = getIconDrawable();
            if (currentIcon != d && switcher != null) {
                if (null == d) {
                    ((ImageView) switcher.getCurrentView()).setImageDrawable(d);
                    switcher.setVisibility(View.GONE);
                } else {
                    switcher.setVisibility(View.VISIBLE);

                    if (!doFade || currentIcon == null) {
                        ((ImageView) switcher.getCurrentView()).setImageDrawable(d);
                    } else {
                        switcher.setImageDrawable(d);
                    }
                }
            }
        }

        @Override
        public View makeView() {
            return mInflater.inflate(R.layout.list_item_icon, icon, false);
        }

        /**
         * Resets the content of the view holder.
         */
        public void reset() {
            tag = null;

            if (icon != null) {
                icon.setImageDrawable(null);
            }
            if (label1 != null) {
                label1.setText(null);
            }
            if (label2 != null) {
                label2.setText(null);
            }
            if (label3 != null) {
                label3.setText(null);
            }
        }
    }

}
