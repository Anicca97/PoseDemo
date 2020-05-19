package com.example.pose;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.MediaCodec;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Main4Activity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private Button button;
    private Timer timer;
    private Camera camera;
    private MediaPlayer player;
    private boolean sendFrame = false;
    private boolean corrected = false;
    private String pUsername = "XZY";
    private String serverUrl = "166.111.71.59";
    private String lastRecord1 = "9";
    private String lastRecord2 = "9";
    private String record1 = "9";
    private String record2 = "9";
    private byte[] report;
    private int serverPort = 18899;
    private SurfaceView surfaceView;
    private SurfaceHolder mSurfaceHolder;
    private int cameraId = 1; // 0是后置摄像头,1是前置摄像头
    private TextView textView;
    private TextView textView2;
    private Intent intent2;
    private Camera.Size mSize;
    private int VideoQuality=85;
    private int VideoWidth = 640;
    private int VideoHeight = 480;
    private int VideoFormatIndex = 0;
    private int timeCount = -1;
    private boolean isStop;
    private boolean stopSend;
    private String action;
    private static final int MAX_TIME = 80;
    private static final int COUNT = 1;
    private static final int MAX_SIZE = 300;
    private static final String text_plank = "平板支撑 . ";
    private static final String text_bent = "Y字俯身伸展 . ";
    private static final String text_squat = "靠墙深蹲 . ";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        textView = (TextView) findViewById(R.id.textView_start);
        textView2 = (TextView) findViewById(R.id.textView2_start);

        Intent intent = getIntent();
        action = intent.getStringExtra("action");
        String firstGroup = "第1组";
        timeCount = intent.getIntExtra("time", 0);
        switch (action) {
            case "plank":
                textView.setText(text_plank + firstGroup);
                break;
            case "bend_over":
                textView.setText(text_bent + firstGroup);
                break;
            case "squat":
                textView.setText(text_squat + firstGroup);
                break;
        }
        isStop = false;
        stopSend = false;

        intent2 = new Intent(Main4Activity.this, Main5Activity.class);
        intent2.putExtra("action", action);

        button = (Button) findViewById(R.id.button_start);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (isStop) {
//                    button.setText("Pause");
//                }
//                else {
//                    button.setText("Start");
//                }
                isStop = ! isStop;
            }
        });

        player = new MediaPlayer();

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView_start);
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.addCallback(this);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(COUNT);
            }
        }, 0, 1000);
    }


    //打开照相机
    public void CameraOpen() {
        try {
            //打开摄像机
            camera = Camera.open(cameraId);
            camera.setDisplayOrientation(90);
            Camera.Parameters parameters = camera.getParameters();
            mSize = parameters.getPreviewSize();
            List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
            boolean set2min = true;
            Camera.Size psize;
            Camera.Size min_size = mSize;
            Camera.Size my_size = mSize;
            for (int i = 0; i < previewSizes.size(); i++) {
                psize = previewSizes.get(i);
                Log.i("previewSize",psize.width+" x "+psize.height);
                if (psize.width == 640 && psize.height == 480) {
                    set2min = false;
                    my_size = psize;
                }
                if (min_size.width >= psize.width) {
                    min_size = psize;
                }
            }
            if (set2min) {
                mSize = min_size;
            }
            else {
                mSize = my_size;
            }
            VideoWidth = mSize.width;
            VideoHeight = mSize.height;
            Log.i("final_previewSize",mSize.width+" x "+mSize.height);
            parameters.setPreviewSize(VideoWidth, VideoHeight);
            VideoFormatIndex=parameters.getPreviewFormat();
            camera.setParameters(parameters);
            //绑定Surface并开启预览
            camera.setPreviewDisplay(mSurfaceHolder);
            camera.startPreview();
        }
        catch (IOException e) {
            camera.release();
            camera = null;
            Toast.makeText(Main4Activity.this, "surface created failed", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        //检查权限
        if (ContextCompat.checkSelfPermission(Main4Activity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
        }
        else {
            CameraOpen();
        }
    }


    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        camera.stopPreview();
        camera.setPreviewCallback(this);
        camera.setDisplayOrientation(90);
        camera.startPreview();
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
    }


    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (stopSend)
            return;
        sendFrame = !sendFrame;
        if (true) {
            try {
                if(data != null)
                {
                    if (report == null) {
                        report = data;
                    }
                    YuvImage image = new YuvImage(data, VideoFormatIndex, VideoWidth, VideoHeight,null);
                    if(image != null)
                    {
                        timeCount++;
                        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
                        //在此设置图片的尺寸和质量
                        image.compressToJpeg(new Rect(0, 0, VideoWidth, VideoHeight), VideoQuality, outstream);
                        outstream.flush();
                        //启用线程将图像数据发送出去
                        Thread th = new MySendFileThread(outstream, VideoWidth, VideoHeight, pUsername, serverUrl, serverPort, timeCount);
                        th.start();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void speakMP3() {
        String r1 = record1;
        Log.i("record1", record1);
        if (Integer.parseInt(lastRecord1) != 1 && Integer.parseInt(lastRecord1) != 9 && Integer.parseInt(lastRecord1) != 10 && Integer.parseInt(r1) == 1) {
            r1 = "10";
            corrected = true;
        }
        if (Integer.parseInt(lastRecord2) != 1 && Integer.parseInt(lastRecord2) != 9 && Integer.parseInt(lastRecord2) != 10 && Integer.parseInt(record2) == 1) {
            r1 = "10";
            corrected = true;
        }
        lastRecord1 = r1;
        if (player == null) {
            return;
        }
        if (r1 != "9")  {
            String path1 = String.format("%s.mp3", r1);
            AssetFileDescriptor fd = null;
            try {
                while (player.isPlaying()) {
                    continue;
                }
                player.reset();
                fd = getAssets().openFd(path1);
                player.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
                player.prepareAsync();
                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fd != null) {
                    try {
                        fd.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    public void speakMP32() {
        String r2 = record2;
        Log.i("record2", record2);
        lastRecord2 = r2;
        if (corrected) {
            corrected = false;
            return;
        }
        if (player == null) {
            return;
        }
        if (r2 != "9") {
            String path2 = String.format("%s.mp3", r2);
            AssetFileDescriptor fd2 = null;
            try {
                fd2 = getAssets().openFd(path2);
                while (player.isPlaying()) {
                    continue;
                }
                player.reset();
                player.setDataSource(fd2.getFileDescriptor(), fd2.getStartOffset(), fd2.getLength());
                player.prepareAsync();
                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fd2 != null) {
                    try {
                        fd2.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    public void speakNotice(int now) {
        if (player == null) {
            return;
        }
        String path;
        if (now == 1 || now == 40) {
            path = String.format("%s%d.mp3", action, now);
        }
        else {
            path = String.format("%d.mp3", now);
        }
        AssetFileDescriptor fd = null;
        try {
            while (player.isPlaying()) {
                continue;
            }
            player.reset();
            fd = getAssets().openFd(path);
            player.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            player.prepareAsync();
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fd != null) {
                try {
                    fd.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    class MySendFileThread extends Thread{
        private String username;
        private String ipname;
        private int port;
        private int width;
        private int height;
        private int time_count;
        private byte byteBuffer[] = new byte[1024];
        private DataOutputStream outsocket;
        private BufferedReader insocket;
        private ByteArrayOutputStream myoutputstream;

        public MySendFileThread(ByteArrayOutputStream myoutputstream, int width, int height, String username, String ipname, int port, int time_count){
            this.myoutputstream = myoutputstream;
            this.username = username;
            this.ipname = ipname;
            this.port = port;
            this.width = width;
            this.height = height;
            this.time_count = time_count;
            try {
                myoutputstream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try{
                //将图像数据通过Socket发送出去
                Socket tempSocket = new Socket(ipname, port);
                insocket = new BufferedReader(new InputStreamReader(tempSocket.getInputStream()));
                outsocket = new DataOutputStream(tempSocket.getOutputStream());

                ByteArrayInputStream inputstream = new ByteArrayInputStream(myoutputstream.toByteArray());
                int size = inputstream.available();

                String comm_s = String.valueOf(1);
                while (comm_s.length() < 10) {
                    comm_s = comm_s + " ";
                }
                byte[] bytes = comm_s.getBytes();
                outsocket.write(bytes);
                outsocket.flush();

                String time_s = String.valueOf(time_count);
                while (time_s.length() < 10) {
                    time_s = time_s + " ";
                }
                bytes = time_s.getBytes();
                outsocket.write(bytes);
                outsocket.flush();

                String act_s = action;
                while (act_s.length() < 10) {
                    act_s = act_s + " ";
                }
                bytes = act_s.getBytes();
                outsocket.write(bytes);
                outsocket.flush();

                String size_s = String.valueOf(size);
                while (size_s.length() < 10) {
                    size_s = size_s + " ";
                }
                bytes = size_s.getBytes();
                outsocket.write(bytes);
                outsocket.flush();

                String height_s = String.valueOf(height);
                while (height_s.length() < 10) {
                    height_s = height_s + " ";
                }
                bytes = height_s.getBytes();
                outsocket.write(bytes);
                outsocket.flush();

                String width_s = String.valueOf(width);
                while (width_s.length() < 10) {
                    width_s = width_s + " ";
                }
                bytes = width_s.getBytes();
                outsocket.write(bytes);
                outsocket.flush();

                int amount = 0;
                while ((amount = inputstream.read(byteBuffer)) > 0) {
                    outsocket.write(byteBuffer, 0, amount);
                    outsocket.flush();
                }
                myoutputstream.flush();
                myoutputstream.close();

                String result1 = insocket.readLine();
                while (result1 == null) {
                    result1 = insocket.readLine();
                }
                String result2 = insocket.readLine();
                while (result2 == null) {
                    result2 = insocket.readLine();
                }
                if (Integer.parseInt(result1) <= Integer.parseInt(result2)) {
                    record1 = result1;
                    record2 = result2;
                }
                else {
                    record1 = result2;
                    record2 = result1;
                }

                tempSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private Handler handler = new Handler() {
        int num = 0;

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case COUNT:
                    if (num == 1 || num == 15 || num == 28 || num == 30 ||
                        num == 33 || num == 40 || num == 80) {
                        speakNotice(num);
                        if (num == 30 || num == 80) {
                            textView.setText("休息一下");
                            stopSend = true;
                        }
                    }
                    else if (num == 50) {
                        switch (action) {
                            case "plank":
                                textView.setText(text_plank + "第2组");
                                break;
                            case "bend_over":
                                textView.setText(text_bent + "第2组");
                                break;
                            case "squat":
                                textView.setText(text_squat + "第2组");
                                break;
                        }
                        stopSend = false;
                    }
                    else if (num == 51 || num == 65 || num == 78) {
                        speakNotice(num-50);
                    }
                    else if (num == MAX_TIME+1) {
                        if (timer != null) {
                            timer.cancel();
                        }
                        intent2.putExtra("image", report);
                        surfaceDestroyed(mSurfaceHolder);
                        startActivity(intent2);
                        finish();
                    }
                    else if (num == 4 || num == 11 || num == 17 || num == 23 ||
                             num == 54 || num == 61 || num ==67 || num == 73) {
                        speakMP3();
                    }
                    else if (num == 6 || num == 13 || num == 19 || num == 25 ||
                            num == 56 || num == 63 || num ==69 || num == 75) {
                        speakMP32();
                    }

                    if (!isStop) {
                        num++;
                    }
                    if (num <= 30) {
                        textView2.setText(String.valueOf(num) + "\'\' / 30\'\'");
                    }
                    else if (num > 30 && num <= 50) {
                        textView2.setText(String.valueOf(num-30) + "\'\' / 20\'\'");
                    }
                    else if (num > 50) {
                        textView2.setText(String.valueOf(num-50) + "\'\' / 30\'\'");
                    }

                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
