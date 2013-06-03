package com.defenestrate.chukkars.menlo.android.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;

import com.defenestrate.chukkars.menlo.android.R;

/**
 * Helper methods to populate a simple row.
 * The base row is assumed to have an icon and two text labels.
 */
public class BaseRow {

	/////////////////////////// MEMBER VARIABLES ///////////////////////////////
	protected final Context mContext;
    /** A reference to the layout inflater for inflating the constituent views. */
    protected final LayoutInflater mInflater;


	///////////////////////////// CONSTRUCTORS /////////////////////////////////
    public BaseRow(Context context) {
    	mContext = context;
        mInflater = LayoutInflater.from(context);
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
            v = mInflater.inflate(R.layout.list_item_base, parent, false);
            final BaseViewHolder holder = new BaseViewHolder(
            	mContext,
                v.findViewById(R.id.icon_layout),
                (ImageSwitcher) v.findViewById(R.id.icon),
                (TextView) v.findViewById(R.id.label1),
                (TextView) v.findViewById(R.id.label2));

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
    public void initRow(BaseViewHolder holder, Drawable icon, String... labels) {
		if(holder.icon_layout != null && icon == null)
		{
			holder.icon_layout.setVisibility(View.GONE);
		}
        else if (holder.icon != null) {
            holder.setIconDrawable(holder.icon, icon, false);
        }

        if (labels != null) {
            final int len = labels.length;
            if (len >= 1) {
                holder.label1.setText(labels[0]);
                holder.label1.setVisibility(View.VISIBLE);

                if (len >= 2) {
                    holder.label2.setText(labels[1]);
                    if (labels[1] != null && !labels[1].isEmpty()) {
                        holder.label2.setVisibility(View.VISIBLE);
                    } else {
                        holder.label2.setVisibility(View.GONE);
                    }
                } else {
                    holder.label2.setVisibility(View.GONE);
                }
            } else {
                holder.label1.setVisibility(View.GONE);
            }

        }
    }

    /**
     * Caches the child views in a row,
     * so that we don't need to call <code>View.findViewById()</code> more than once.
     */
    public static class BaseViewHolder implements ViewFactory {

        /** The tag identifying the row. */
        public String tag;

		public final View icon_layout;
        /** The row icon. */
        public final ImageSwitcher icon;

        /** The primary label. */
        public final TextView label1;

        /** The secondary label. */
        public final TextView label2;

        protected final LayoutInflater mInflater;

        /** For when the mini-icon is necessary */
        public BaseViewHolder(
                Context context,
			View icon_layout ,ImageSwitcher icon, TextView label1, TextView label2) {

            mInflater = LayoutInflater.from(context);

			this.icon_layout = icon_layout;
            this.icon = icon;
            if (icon != null) {
                icon.setFactory(this);
                icon.setInAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_in));
            }

            this.label1 = label1;
            this.label2 = label2;
        }

        /** For when the mini-icon is not necessary */
        public BaseViewHolder(
                Context context,
                ImageSwitcher icon, TextView label1, TextView label2) {

        	mInflater = LayoutInflater.from(context);

			this.icon_layout = null;
            this.icon = icon;
            if (icon != null) {
                icon.setFactory(this);
                icon.setInAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_in));
            }

            this.label1 = label1;
            this.label2 = label2;
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
         * @param d      	The new icon image.
         *                  If <code>null</code>, then <code>switcher</code>'s visibility
         *                  is set to <code>View.GONE</code>.
         * @param doFade    <code>true</code> to fade in the icon.
         */
        public void setIconDrawable(ImageSwitcher switcher, Drawable d, boolean doFade) {
        	setIconDrawable(switcher, d, 0, doFade);
        }

        /**
         * Convenience method to set the <code>Drawable</code> in an <code>ImageSwitcher</code>,
         * with fade animation.
         *
         * @param switcher  The image switcher.
         * @param d      	The new icon image.
         *                  If <code>null</code>, then <code>switcher</code>'s visibility
         *                  is set to <code>View.GONE</code>.
         * @param backgroundResId resource id for an optional background for the icon image;
         * 					use 0 to specify none.
         * @param doFade    <code>true</code> to fade in the icon.
         */
        public void setIconDrawable(ImageSwitcher switcher, Drawable d, int backgroundResId, boolean doFade) {
            if (switcher != null) //PLM-1992
            {
	        	final Drawable currentIcon = getIconDrawable();
	            if (currentIcon != d) {
	                if (null == d) {
	                    ( (ImageView)switcher.getCurrentView() ).setImageDrawable(d);
	                    switcher.setVisibility(View.GONE);
	                } else {
	                    switcher.setVisibility(View.VISIBLE);

	                    if (!doFade || currentIcon == null) {
	                    	( (ImageView)switcher.getCurrentView() ).setImageDrawable(d);
	                    	( (ImageView)switcher.getCurrentView() ).setScaleType(ImageView.ScaleType.FIT_CENTER);
	                    } else {
	                        switcher.setImageDrawable(d);
	                        ( (ImageView)switcher.getCurrentView() ).setScaleType(ImageView.ScaleType.FIT_CENTER);
	                    }
	                }
	            }

	            // sometimes backgroundResource does not set even though it's needed
	            switcher.setBackgroundResource(backgroundResId);
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
        }
    }

}
