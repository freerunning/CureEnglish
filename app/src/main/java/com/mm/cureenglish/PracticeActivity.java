package com.mm.cureenglish;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PracticeActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    public static final String EXTRA_FILENAME = "extra_filename";

    private static final int MSG_TIME = 1;

    private ListView contentListView;
    private PracticeAdapter contentAdapter;

    private ListView recordListView;
    private ArrayAdapter<String> recordAdapter;

    private ProgressBar playProgressBar;

    private TextView timeTextView;
    private Button recordButton;
    private Button timeButton;

    private int level;
    private PracticeData currentData;

    private String filename;

    private List<PracticeData> dataList = new ArrayList<>();

    private boolean recording;

    private boolean timing;
    private long startTime;

    private AudioRecorder audioRecorder;
    private FileWriterSafe fileWriterSafe;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_TIME:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timeButton.setText(elapsedTime());
                            if (timing) {
                                handler.sendEmptyMessageDelayed(MSG_TIME, 50);
                            }
                        }
                    });
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        filename = getIntent().getStringExtra(EXTRA_FILENAME);
        setTitle(filename);

        setContentView(R.layout.activity_practice);

        recordButton = findViewById(R.id.record_button);
        recordButton.setOnClickListener(this);

        timeTextView = findViewById(R.id.time_textview);
        timeButton = findViewById(R.id.time_button);
        timeButton.setOnClickListener(this);

        contentListView = findViewById(R.id.content_list);

        contentAdapter = new PracticeAdapter(this);
        contentListView.setAdapter(contentAdapter);
        contentListView.setOnItemClickListener(this);

        recordListView = findViewById(R.id.record_list);
        recordAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        recordListView.setAdapter(recordAdapter);
        recordListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = recordAdapter.getItem(position);
                playRecord(name);
            }
        });
        recordListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final String name = recordAdapter.getItem(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(PracticeActivity.this)
                        .setTitle(name)
                        .setMessage("是否删除录音")
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                removeRecord(name);
                            }
                        })
                        .setNegativeButton("否", null);
                builder.create().show();

                return false;
            }
        });

        playProgressBar = findViewById(R.id.play_progress);

        fileWriterSafe = new FileWriterSafe();

        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 0);

        load();

        loadRecord();
    }

    private void setTitle(String title) {
        getActionBar().setTitle(title);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.time_button:
                if (timing) {
                    timing = false;
                    handler.removeMessages(MSG_TIME);
                    timeButton.setText("计时");
                    timeTextView.setText(elapsedTime());
                } else {
                    timing = true;
                    startTime = System.currentTimeMillis();
                    handler.sendEmptyMessage(MSG_TIME);
                    timeButton.setText("正在计时...");
                }
                break;
            case R.id.record_button:
                if (recording) {
                    recording = false;
                    recordButton.setText("录音");
                    AudioRecorder.getInstance().stop();
                } else {
                    recording = true;
                    recordButton.setText("正在录音...");
                    final String name = filename + "-" + currentData.getTitle() + "-" + new SimpleDateFormat(Constant.DATE_FORMAT_PATTERN).format(new Date());
                    AudioRecorder.getInstance().setListener(new AudioRecorder.IRecordListener() {
                        @Override
                        public void onStarted() {
                            fileWriterSafe.open(getRecordFile(name));
                        }

                        @Override
                        public void onStopped() {
                            final String name = fileWriterSafe.getFileName();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(PracticeActivity.this)
                                            .setTitle(name)
                                            .setMessage("是否保存录音")
                                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int which) {
                                                    recordAdapter.add(name);
                                                }
                                            })
                                            .setNegativeButton("否", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int which) {
                                                    removeRecord(name);
                                                }
                                            });
                                    builder.create().show();
                                }
                            });
                            fileWriterSafe.close();
                        }

                        @Override
                        public void onAudioDataAvailable(byte[] data, int len) {
                            fileWriterSafe.write(data, len);
                        }
                    });
                    AudioRecorder.getInstance().start();
                }
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (level == 0) {
            level = 1;
            currentData = dataList.get(position);
            setTitle(currentData.getTitle());

            contentAdapter.clear();
            contentAdapter.addAll(currentData.getCnList());

            recordButton.setVisibility(View.VISIBLE);
            timeButton.setVisibility(View.VISIBLE);
        } else if (level == 1) {
            String en = currentData.getEn(position);
            Toast toast = Toast.makeText(this, en, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    @Override
    public void onBackPressed() {
        if (level == 1) {
            level = 0;
            setTitle(filename);

            contentAdapter.clear();
            for (PracticeData data : dataList) {
                contentAdapter.add(data.getTitle());
            }

            recordButton.setVisibility(View.GONE);
            timeButton.setVisibility(View.GONE);

            return;
        }
        super.onBackPressed();
    }

    private void load() {
        try {
            File file = new File(getExternalFilesDir(Constant.DIR_CONTENT), filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("<")) {
                    parseBlock(line.replace("<", ""), reader);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseBlock(String title, BufferedReader reader) throws IOException {
        PracticeData data = new PracticeData(title);

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.equals(">")) {
                break;
            }
            if (line.isEmpty()) {
                continue;
            }
            String i = line;
            String ch = reader.readLine();
            String en = reader.readLine();
            data.add(ch, en);
        }

        dataList.add(data);
        contentAdapter.add(title);
    }

    private String elapsedTime() {
        long time = System.currentTimeMillis() - startTime;
        SimpleDateFormat format = new SimpleDateFormat("mm:ss:SSS");
        Date date = new Date(time);
        String text = format.format(date);
        return text;
    }

    private File getRecordDir() {
        File dir = new File(getFilesDir(), Constant.DIR_RECORD);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }

    private File getRecordFile(String name) {
        return new File(getRecordDir(), name);
    }

    private void loadRecord() {
        File dir = getRecordDir();
        String[] files = dir.list();
        recordAdapter.addAll(files);
    }

    private void removeRecord(String name) {
        File file = getRecordFile(name);
        file.delete();
        recordAdapter.remove(name);
    }

    private void playRecord(String name) {
        final File file = getRecordFile(name);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    RawAudioPlayer player = new RawAudioPlayer();
                    player.init();

                    FileInputStream inputStream = new FileInputStream(file);
                    final int totalSize = inputStream.available();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            playProgressBar.setMax(totalSize);
                            playProgressBar.setProgress(0);
                        }
                    });

                    int currentSize = 0;
                    byte[] data = new byte[1024*8];
                    while (true) {
                        int readsize = inputStream.read(data);
                        if (readsize <= 0) {
                            break;
                        }

                        player.write(data, readsize);

                        currentSize += readsize;
                        setPlayProgressOnUiThread(currentSize);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void setPlayProgressOnUiThread(final int progress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playProgressBar.setProgress(progress);
            }
        });
    }
}
