package pl.polidea.demo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public abstract class DemoListActivity extends Activity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, R.id.demo_text);
        for (int i = 0; i < 20; i++) {
            adapter.add(getName() + " : " + i);
        }
        setContentView(R.layout.list_view);
        final ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);
    }

    public abstract String getName();
}
