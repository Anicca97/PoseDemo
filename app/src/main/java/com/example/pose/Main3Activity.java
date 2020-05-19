package com.example.pose;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.AudioManager;
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
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class Main3Activity extends AppCompatActivity implements  SurfaceHolder.Callback, Camera.PreviewCallback {

    private Button button;
    private Timer timer;
    private TimerTask task;
    private Camera camera;
    private MediaPlayer player;
    private boolean sendFrame = false;
    private boolean haveStarted = false;
    private boolean rightAction = false;
    private String pUsername = "XZY";
    private String serverUrl = "166.111.71.59";
    private int serverPort = 18899;
    private SurfaceView surfaceView;
    private SurfaceHolder mSurfaceHolder;
    private String path_now = "0";
    private int cameraId = 1; // 0是后置摄像头,1是前置摄像头
    private Intent intent2;
    private Camera.Size mSize;
    private static final int SPEAK = 1;
    private int timeCount = -1;
    private int VideoQuality=85;
    private int VideoWidth = 640;
    private int VideoHeight = 480;
    private int VideoFormatIndex = 0;
    private String action;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        Intent intent = getIntent();
        action = intent.getStringExtra("action");

        intent2 = new Intent(Main3Activity.this, Main4Activity.class);
        intent2.putExtra("action", action);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView_view);
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.addCallback(this);

        button = (Button) findViewById(R.id.button_view);

        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        speakNotice(action);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(SPEAK);
            }
        }, 6000, 3000);
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
            Toast.makeText(Main3Activity.this, "surface created failed", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        //检查权限
        if (ContextCompat.checkSelfPermission(Main3Activity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
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
        Camera.Parameters parameters = camera.getParameters();
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
        if (haveStarted && rightAction) {
            return;
        }
        sendFrame = !sendFrame;
        if (true) {
            try {
                if(data != null)
                {
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


    public void speakNotice(String now) {
        if (player == null) {
            return;
        }
        String path = String.format("activity3_%s.mp3", now);
        AssetFileDescriptor fd = null;
        while (player.isPlaying()) {
            continue;
        }
        Log.i("Playing", path);
        try {
            player.reset();
            fd = getAssets().openFd(path);
            player.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            player.prepareAsync();
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (mp == player)
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

                String comm_s = String.valueOf(0);
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
                Log.i("result1", result1);
                Log.i("result2", result2);
                if (Integer.parseInt(result1) == 1) {
                    Log.i("result", "result1 == 1");
                    haveStarted = true;
                    if (Integer.parseInt(result2) == 1 && !rightAction) {
                        Log.i("result", "result2 == 1");
                        rightAction = true;
                        path_now = "11";
                    }
                    else {
                        path_now = "10";
                    }
                }

                tempSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private Handler handler = new Handler() {
        int count = 0;
        boolean startCount = false;

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SPEAK:
                    if (count == 1) {
                        speakNotice(action+"_start");
                        count++;
                        break;
                    }
                    if (count == 5) {
                        count++;
                        if (timer != null) {
                            timer.cancel();
                        }
                        surfaceDestroyed(mSurfaceHolder);
                        intent2.putExtra("time", timeCount);
                        startActivity(intent2);
                        finish();
                        break;
                    }
                    if (!startCount) {
                        if (haveStarted && rightAction) {
                            startCount = true;
                        }
                        speakNotice(path_now);
                    }

                    if (startCount)
                        count++;
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
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}