package com.example.libjpegdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Compressor {
    public static String compress(String path, int q, String saveDir){
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        String result = saveDir+"/"+System.currentTimeMillis()+".jpg";
        int i = CompressUtil.compressBitmap(bitmap, q, result);
        return i==1?result:"";
    }
}
