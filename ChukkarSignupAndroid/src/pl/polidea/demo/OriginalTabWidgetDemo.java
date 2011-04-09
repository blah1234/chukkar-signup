package pl.polidea.demo;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class OriginalTabWidgetDemo extends TabActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Resources res = getResources();
        final TabHost tabHost = getTabHost();
        final Intent oneIntent = new Intent().setClass(this, ActivityOne.class);
        final TabSpec oneSpec = tabHost.newTabSpec("One")
                .setIndicator("One", res.getDrawable(R.drawable.ic_tab_artists)).setContent(oneIntent);
        tabHost.addTab(oneSpec);
        final Intent twoIntent = new Intent().setClass(this, ActivityTwo.class);
        final TabSpec twoSpec = tabHost.newTabSpec("Two").setIndicator("Two").setContent(twoIntent);
        tabHost.addTab(twoSpec);

        final Intent threeIntent = new Intent().setClass(this, ActivityThree.class);
        final TabSpec threeSpec = tabHost.newTabSpec("Three").setIndicator("Three").setContent(threeIntent);
        tabHost.addTab(threeSpec);

        tabHost.setCurrentTab(2);
    }
}