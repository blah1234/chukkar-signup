package com.defenestrate.chukkars.android.util;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.defenestrate.chukkars.android.R;

public class CoverArtUtil {

	/////////////////////////////// CONSTANTS //////////////////////////////////
	static private final int MAX_NUM_COVER_ART = 13;
	static private final String LOG_TAG = CoverArtUtil.class.getSimpleName();


	/////////////////////////// MEMBER VARIABLES ///////////////////////////////
	static private final Random mRand = new Random();
	static private final Set<Integer> sUsedCoverArtIds = new TreeSet<Integer>();


	//////////////////////////////// METHODS ///////////////////////////////////
	static public CoverArtData getRandomCoverArt(Resources res) {
    	int coverArtId = getRandomWithExclusion(
    		mRand,
    		1,
    		MAX_NUM_COVER_ART,
    		sUsedCoverArtIds.toArray(new Integer[sUsedCoverArtIds.size()]) );

		sUsedCoverArtIds.add(coverArtId);

		Drawable coverArtDrawable = getAssignedCoverArt(res, coverArtId);

		CoverArtData ret = new CoverArtData();
		ret.mCoverArtDrawable = coverArtDrawable;
		ret.mCoverArtId = coverArtId;

		return ret;
    }

    static public Drawable getAssignedCoverArt(Resources res, int coverArtId) {
    	int id = getAssignedCoverArtResourceId(coverArtId);
    	return res.getDrawable(id);
    }

    static public int getAssignedCoverArtResourceId(int coverArtId) {
    	String fieldName = "cover" + coverArtId;
		int id;

		try {
			Field coverArtField = R.drawable.class.getField(fieldName);
			id = coverArtField.getInt(null);
		} catch (NoSuchFieldException e) {
			//should never happen
			Log.e(LOG_TAG, e.getMessage(), e);
			id = R.drawable.cover1;
		} catch (IllegalArgumentException e) {
			//should never happen because the field is static
			Log.e(LOG_TAG, e.getMessage(), e);
			id = R.drawable.cover1;
		} catch (IllegalAccessException e) {
			//should never happen because the field is public
			Log.e(LOG_TAG, e.getMessage(), e);
			id = R.drawable.cover1;
		}

		return id;
    }

    /**
     * Generates a random number (int) between start and end (both inclusive) and
     * does not return any number which is contained in the array exclude. All
     * other numbers occur with equal probability. Note, that the following
     * constrains must hold: exclude is sorted in ascending order and all numbers
     * are within the range provided and all of them are mutually exclusive.
     */
    static private int getRandomWithExclusion(Random rnd, int start, int end, Integer... exclude) {
        int random = start + rnd.nextInt(end - start + 1 - exclude.length);
        for (int ex : exclude) {
            if (random < ex) {
                break;
            }
            random++;
        }

        return random;
    }

    static public void freeCoverArtId(int coverArtId) {
    	sUsedCoverArtIds.remove(coverArtId);
    }


	//////////////////////////// INNER CLASSES /////////////////////////////////
    static public class CoverArtData {
    	public Drawable mCoverArtDrawable;
    	public int mCoverArtId;
    }
}
