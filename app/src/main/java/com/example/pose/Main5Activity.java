package com.example.pose;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Main5Activity extends AppCompatActivity {
    private ImageView imageView;
    private TextView textView;
    private Button button;
    private static final String text_plank = "平板支撑";
    private static final String text_bent = "Y字俯身伸展";
    private static final String text_squat = "靠墙深蹲";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main5);

        Intent intent = getIntent();
        final String action = intent.getStringExtra("action");
        final byte[] report = intent.getByteArrayExtra("image");

        imageView = (ImageView) findViewById(R.id.imageView_stop);
        imageView.setBackgroundResource(R.drawable.main5);
        textView = (TextView) findViewById(R.id.textView_stop1);
        button = (Button) findViewById(R.id.button_stop);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent2 = new Intent(Main5Activity.this, Main6Activity.class);
                intent2.putExtra("action", action);
                intent2.putExtra("image", report);
                startActivity(intent2);
                finish();
            }
        });

        switch (action) {
            case "plank":
                textView.setText(text_plank);
                break;
            case "bend_over":
                textView.setText(text_bent);
                break;
            case "squat":
                textView.setText(text_squat);
                break;
        }
    }
}
