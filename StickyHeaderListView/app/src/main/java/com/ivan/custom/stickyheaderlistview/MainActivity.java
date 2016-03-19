package com.ivan.custom.stickyheaderlistview;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ivan.custom.stickyheaderlistview.PinnedHeaderListView.PinnedHeaderListAdapter;
import com.ivan.custom.stickyheaderlistview.PinnedHeaderListView.PinnedHeaderListView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String[] sections = {"A","B","C","D"};
        final String[] contents = {"1","2","3","4","5","6"};

        PinnedHeaderListView listView = (PinnedHeaderListView) findViewById(R.id.listView);
        listView.setAdapter(new PinnedHeaderListAdapter() {
            @Override
            public boolean isSectionView(int positon) {
                if (positon % 7 == 0)   return true;
                return false;
            }

            @Override
            public int sectionOfItem(int position) {
                return position/7*7;
            }

            @Override
            public int getCount() {
                return sections.length + contents.length * sections.length;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv;
                if (convertView != null) {
                    tv = (TextView) convertView;
                } else {
                    tv = new TextView(MainActivity.this);
                    tv.setHeight(300);
                    tv.setBackgroundColor(Color.WHITE);
                }
                if (position % 7 == 0)  tv.setText("   "+sections[position/7]);
                else                    tv.setText("   "+contents[position%7-1]);
                return tv;
            }
        });
    }
}
