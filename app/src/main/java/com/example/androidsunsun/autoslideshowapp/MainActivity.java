package com.example.androidsunsun.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    Button forwardButton;
    Button backButton;
    Button startButton;
    Cursor cursor;
    private Timer mTimer;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //各ボタンをactivity.mainから読み込んでくる
        forwardButton = (Button) findViewById(R.id.forwardButton);
        forwardButton.setOnClickListener(this);
        backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(this);
        startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(this);
        //ボタンの有効化、無効化
        setButtonState(true, true, true);
        //パーミッションボタンを押した時、許可を求めるダイアログを表示する
        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                } else  if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // 外部ストレージの読み込みが許可された。
                    getContentsInfo();
                }else{
                    //許可されなかったので、再度許可ダイアログを表示する。
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
                }
                break;
            default:
                break;
        }
    }
    // 画像の情報を取得する
    private void getContentsInfo() {
        ContentResolver resolver = getContentResolver();
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );
    }
    //ボタンの有効化、無効化
    public void setButtonState(boolean forward, boolean back, boolean start) {
        forwardButton.setEnabled(forward);
        backButton.setEnabled(back);
        startButton.setEnabled(start);
    }
    @Override
    public void onClick(View v) {
        //進むボタン
        if (v.getId() == R.id.forwardButton) {
            if (cursor.moveToNext()) {
            } else {
                cursor.moveToFirst();
            }
            setImageView();
            setButtonState(true, true, true);
        //戻るボタン
        } else if (v.getId() == R.id.backButton) {
            if (cursor.moveToPrevious()) {
            } else {
                cursor.moveToLast();
            }
            setImageView();
            setButtonState(true, true, true);
        //再生or停止ボタン（nullであれば再生する）
        } else if (v.getId() == R.id.startButton) {
            if (mTimer == null) {
                setButtonState(false, false, true);
                startButton.setText("停止");
                mTimer = new Timer();
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (cursor.moveToNext()) {
                                } else {
                                    cursor.moveToFirst();
                                }
                                setImageView();
                            }
                        });
                    }
                }, 2000, 2000);
            }
            //nullでなければ停止する
            else if (mTimer != null) {
                setButtonState(true, true, true);
                startButton.setText("再生");
                mTimer.cancel();
                mTimer = null;
            }
        }
    }
    private void setImageView() {
        // indexからIDを取得し、そのIDから画像のURIを取得する
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
        //imageUriをImageViewに表示する
        Log.d("ANDROID", "URI : " + imageUri.toString());
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageURI(imageUri);
    }
}












