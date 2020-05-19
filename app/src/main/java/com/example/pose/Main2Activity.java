package com.example.pose;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.Timer;
import java.util.TimerTask;

public class Main2Activity extends AppCompatActivity {

    private Button button;
    private boolean clicked = false;
    private Timer timer;
    private TimerTask task;
    private VideoView videoView;
    private MediaController mediaController;
    private static final String text_plank = "•双脚分开与肩同宽\n" +
            "•大腿与地面平行，保持住\n" +
            "•下背紧贴墙面，感受膝盖附近肌肉发力";
    private static final String text_bent = "•屈肘，小臂与前脚掌撑地，身体在一条直线上\n" +
            "•手肘朝脚的方向用力，脚尖用力向前勾起，与地面摩擦力对抗";
    private static final String text_squat = "•屈膝俯身，身体与地面呈30度至45度，双臂上提至与身体呈Y字\n" +
            "•挺直腰背，头部与脊柱处在一条直线上";
    private static final String error_plank = "•大腿未与地面平行\n" +
            "•下背没有贴住墙面";
    private static final String error_bent = "•屁股过高或过低\n" +
            "•腹肌力竭后仍坚持，导致腰部酸疼";
    private static final String error_squat = "•身体过高或过低\n" +
            "•弯腰弓背";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Intent intent = getIntent();
        final String action = intent.getStringExtra("action");

        final TextView textView = (TextView) findViewById(R.id.textView2_introduction);
        final TextView textView2 = (TextView) findViewById(R.id.textView4_introduction);
        videoView = (VideoView) findViewById(R.id.videoView_introduction);
        mediaController = new MediaController(this);

        timer = new Timer();

        String video_uri = "android.resource://" + getPackageName() + "/";

        switch (action) {
            case "plank":
                textView.setText(text_plank);
                textView2.setText(error_plank);
                video_uri = video_uri + R.raw.plank;
                break;
            case "bend_over":
                textView.setText(text_bent);
                textView2.setText(error_bent);
                video_uri = video_uri + R.raw.bentover;
                break;
            case "squat":
                textView.setText(text_squat);
                textView2.setText(error_squat);
                video_uri = video_uri + R.raw.wallsquat;
                break;
        }

        Uri uri = Uri.parse(video_uri);
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(uri);
        videoView.start();

        button = (Button) findViewById(R.id.button_introduction);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!clicked) {
                    clicked = true;
                    final Intent intent2 = new Intent(Main2Activity.this, Main3Activity.class);
                    intent2.putExtra("action", action);
                    startActivity(intent2);
                    finish();

//                    task = new TimerTask() {
//                        @Override
//                        public void run() {
//                            startActivity(intent2);
//                            finish();
//                        }
//                    };
//                    timer.schedule(task, 1000*5);
                }
            }
        });
    }
}
