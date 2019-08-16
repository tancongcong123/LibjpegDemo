package com.example.libjpegdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {
    private final int REQUEST_CODE = 1001;
    private Button btnStart;
    private TextView tvContent;
    private HashSet<String> preFileNames;
    private HashSet<String> fileNames;
    private String originalPath = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES)+"/original";
    private String savePath = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES)+"/libjpeg";
    private long startTime = 0;
    private StringBuilder stringBuilder = new StringBuilder();
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            btnStart.setEnabled(true);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        tvContent = findViewById(R.id.sample_text);
        btnStart = findViewById(R.id.btn_start);
        btnStart.setEnabled(false);
        btnStart.setOnClickListener(view -> {
            startCompress();
        });
        getImages();
    }

    private void getImages() {
        new Thread(() -> {
            preFileNames = new HashSet<>();
            fileNames = new HashSet<>();
            getList();
            mHandler.sendMessage(mHandler.obtainMessage());

        }).start();
    }

    private void getList(){
        searchFile(new File(originalPath));
        fileNames.addAll(preFileNames);
    }

    private void searchFile(File file){
        if (file.isDirectory()){
            File[] files=file.listFiles();
            for (int i=0;i<files.length;i++){
                if (files[i].isDirectory()){
                    searchFile(files[i]);
                }else {
                    if (isImage(files[i].getName())){
                        preFileNames.add(files[i].getAbsolutePath());
                    }
                }
            }
        }else {
            if (isImage(file.getName())){
                preFileNames.add(file.getAbsolutePath());
            }
        }
    }

    private boolean isImage(String name){
        int index = name.indexOf(".");
        if (index<0){
            return false;
        }
        String suffix = name.substring(index);
        if (suffix.equalsIgnoreCase(".png")
                ||suffix.equalsIgnoreCase(".jpeg")
                ||suffix.equalsIgnoreCase(".bmp")
                ||suffix.equalsIgnoreCase(".jpg")){
            return true;
        }
        return false;
    }

    private void startCompress(){
        fileNames.clear();
        fileNames.addAll(preFileNames);
        stringBuilder = new StringBuilder();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }else {
                if (fileNames.iterator().hasNext())
                    compressor(fileNames.iterator().next());

            }
            //return;
        } else {
            if (fileNames.iterator().hasNext())
                compressor(fileNames.iterator().next());

        }
    }

    private boolean permissionGranted(int[] grantResults){
        for (int i = 0;i<grantResults.length;i++){
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {

            if (permissionGranted(grantResults)) {
                // Permission Granted
                try {
                    if (fileNames.iterator().hasNext())
                        compressor(fileNames.iterator().next());
                }catch (SecurityException e) {
                    e.printStackTrace();
                }
            } else {
                // Permission Denied
                Toast.makeText(this, "请开启权限", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void compressor(String originalImg){
        stringBuilder.append(originalImg+"\n");
        stringBuilder.append(CommonUtils.getReadableFileSize(new File(originalImg).length())+"----");
        CompressUtils.with(this)
                .load(originalImg)
                .setTargetDir(savePath)
                .setCompressListener(new OnCompressorListener() {
                    @Override
                    public void onStart() {
                        startTime = System.currentTimeMillis();
                        stringBuilder.append("onStart---"+startTime+"\n");
                    }

                    @Override
                    public void onSuccess(File file) {
                        long end = System.currentTimeMillis();
                        stringBuilder.append("onSuccess---"+end+"\n");
                        stringBuilder.append("compress size:"+CommonUtils.getReadableFileSize(file.length())+"\n");
                        stringBuilder.append("compress time:"+(end-startTime)+"ms \n");
                        fileNames.remove(originalImg);
                        tvContent.setText(stringBuilder.toString());
                        if (fileNames.iterator().hasNext()){
                            compressor(fileNames.iterator().next());
                        }else {
                            CommonUtils.writeToFile(stringBuilder.toString(), savePath, "compressLog.txt");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        stringBuilder.append("onError"+"\n"+e.getMessage()+"\n");
                        fileNames.remove(originalImg);
                        tvContent.setText(stringBuilder.toString());
                        if (fileNames.iterator().hasNext()){
                            compressor(fileNames.iterator().next());
                        }else {
                            CommonUtils.writeToFile(stringBuilder.toString(), savePath, "compressLog.txt");
                        }
                    }
                }).launch();
    }
}
