package com.mm.cureenglish;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    private ListView listView;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.list_view);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        load();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String filename = adapter.getItem(position);
        Intent intent = new Intent(this, PracticeActivity.class);
        intent.putExtra(PracticeActivity.EXTRA_FILENAME, filename);
        startActivity(intent);
    }

    private void load() {
        String[] files = getExternalFilesDir(Constant.DIR_CONTENT).list();
        adapter.addAll(files);
    }
}
