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
        String[] titles = new String[]{"URL", "Video ID", "Channel ID", "Test ID"};
        ((SegmentedControl) findViewById(R.id.segc)).setTitles(titles, 1);
        ((SegmentedControl) findViewById(R.id.segc)).setSelectionChangeListener(index -> {
            ((TextView) findViewById(R.id.tv)).setText(titles[index]);
        });
    }
}