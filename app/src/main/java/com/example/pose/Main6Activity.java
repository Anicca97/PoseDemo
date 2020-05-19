package com.example.pose;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.widget.ImageView;

public class Main6Activity extends AppCompatActivity {
    private ImageView imageView;
    private Type.Builder yuvType, rgbaType;
    private RenderScript rs;
    private Allocation in, out;
    private Bitmap myImage = null;
    private Bitmap backgroundImage = null;
    private Resources resources;
    private ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main6);

        Intent intent = getIntent();
        final String action = intent.getStringExtra("action");
        final byte[] report = intent.getByteArrayExtra("image");

        rs = RenderScript.create(this);
        yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));

        resources = getResources();
        backgroundImage = BitmapFactory.decodeResource(resources, R.drawable.play);

        imageView = (ImageView) findViewById(R.id.imageView_final);

        myImage = yuvToBitmap(report, 640, 480);
        Matrix matrix = new Matrix();
        matrix.setRotate(270);
        matrix.postScale((float) 1.0, (float) 1.2);
        Bitmap newBM = Bitmap.createBitmap(myImage, 0, 0, 640, 480, matrix, false);

        Canvas canvas = new Canvas(newBM);
        canvas.drawBitmap(backgroundImage, 210, 360, null);

        imageView.setImageBitmap(newBM);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public Bitmap yuvToBitmap(byte[] yuv, int width, int height){
        if (yuvType == null){
            yuvType = new Type.Builder(rs, Element.U8(rs)).setX(yuv.length);
            in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);
            rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
            out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
        }
        in.copyFrom(yuv);
        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);
        Bitmap rgbout = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        out.copyTo(rgbout);
        return rgbout;
    }
}
