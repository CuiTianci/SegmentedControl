package com.ctc.segmentedcontrol;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ctc.segmen.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SegmentedControl segmentedControl = ((SegmentedControl) findViewById(R.id.segc));
        String[] titles = new String[]{"URL", "Video ID", "Channel ID", "Test ID"};
        segmentedControl.setTitles(titles, 2);
        segmentedControl.setSelectionChangeListener(index -> ((TextView) findViewById(R.id.tv)).setText(titles[index]));
        findViewById(R.id.withAnima).setOnClickListener(view -> {
            int currentSelected = segmentedControl.getSelectedIndex();
            int targetIndex = currentSelected == 0 ? titles.length - 1 : 0;
            segmentedControl.setSelectedIndex(targetIndex);
        });

        findViewById(R.id.withoutAnima).setOnClickListener(view -> {
            int currentSelected = segmentedControl.getSelectedIndex();
            int targetIndex = currentSelected == 0 ? titles.length - 1 : 0;
            segmentedControl.setSelectedIndex(targetIndex, false);
        });
    }
}