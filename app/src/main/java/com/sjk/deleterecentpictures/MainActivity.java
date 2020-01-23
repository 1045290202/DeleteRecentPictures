package com.sjk.deleterecentpictures;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.dialog.MaterialDialogs;
import com.sjk.deleterecentpictures.utils.FileUtil;
import com.sjk.deleterecentpictures.utils.ImageScanner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    
    public static List<String> imagePaths;
    private static String imagePath;
    public static List<Bitmap> theLatestImages;
    public static Bitmap theLatestImage;
    
    private final static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (Objects.requireNonNull(HandlerMsgWhat.getByValue(msg.what))) {
                case ERROR: {
                    //错
                    String string = msg.obj.toString();
                    Toast.makeText(getApplicationContext(), "出错了：" + string, Toast.LENGTH_SHORT).show();
                    break;
                }
                case REFRESH_TEXT: {
                    //设置文本
                    String imagePath = msg.obj.toString();
                    MainActivity.imagePath = imagePath;
                    Button latestPicturePathButton = findViewById(R.id.latestPicturePathButton);
                    latestPicturePathButton.setText(imagePath);
                    break;
                }
                case REFRESH_IMAGE: {
                    //设置图片
                    Bitmap bitmap = (Bitmap) msg.obj;
                    ImageView latestPictureImageView = findViewById(R.id.latestPictureImageView);
                    latestPictureImageView.setImageBitmap(bitmap);
                    latestPictureImageView.setContentDescription(imagePath);
                    break;
                }
                case DELETE_IMAGE_SUCCESS: {
                    Toast.makeText(getApplicationContext(), "删除图片 " + imagePath + " 成功", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                }
                case DELETE_IMAGE_FAIL: {
                    Toast.makeText(getApplicationContext(), "出错了：删除图片 " + imagePath + " 失败", Toast.LENGTH_SHORT).show();
                    break;
                }
                default: {
                    break;
                }
            }
        }
    };
    
    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //设置默认偏好
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);
        
        initView();
        requestWritePermission();
    }
    
    private void initView() {
        setContentView(R.layout.activity_main);
        buttonClickEventBind();
    }
    
    private void buttonClickEventBind() {
        final Button latestPicturePathButton = findViewById(R.id.latestPicturePathButton);
        latestPicturePathButton.setOnClickListener(v -> {
            //android:textIsSelectable="true"
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(getResources().getString(R.string.app_name), imagePath);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(), "已复制到剪切板", Toast.LENGTH_SHORT).show();
            } else {
                Message message = new Message();
                message.what = HandlerMsgWhat.ERROR.getIndex();
                message.obj = "复制失败";
                handler.sendMessage(message);
            }
        });
        
        Button openImageActivityButton = findViewById(R.id.openImageActivityButton);
        openImageActivityButton.setOnClickListener(v -> {
            //打开图片查看界面
            Intent intent = new Intent(getApplicationContext(), ImageActivity.class);
            startActivity(intent);
        });
        
        Button cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> finish());
        
        //删除按钮
        Button deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(v -> new MaterialAlertDialogBuilder(MainActivity.this)
                .setTitle("警告")
                .setMessage("请确认是否删除\n" + imagePath)
                .setPositiveButton("确定", (dialog, which) -> onDeleteButtonClick())
                .setNegativeButton("取消", (dialog, which) -> {
                
                })
                .show());
        deleteButton.setOnLongClickListener(v -> {
            onDeleteButtonClick();
            return true;
        });
        
        Button settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity2.class);
            startActivityForResult(intent, 1);
        });
    }
    
    /**
     * 点击删除按钮触发事件
     */
    private void onDeleteButtonClick() {
        new Thread(
                () -> {
                    Log.d("imagePath", "run: " + imagePath);
                    if (imagePath != null && !imagePath.equals("")) {
                        Message message = new Message();
                        //删除图片并判断
                        if (FileUtil.deleteFile(imagePath)) {
                            message.what = HandlerMsgWhat.DELETE_IMAGE_SUCCESS.getIndex();
                            FileUtil.updateFileFromDatabase(getApplicationContext(), imagePath);
                        } else {
                            message.what = HandlerMsgWhat.DELETE_IMAGE_FAIL.getIndex();
                        }
                        handler.sendMessage(message);
                    } else {
                        Message message = new Message();
                        message.what = HandlerMsgWhat.ERROR.getIndex();
                        message.obj = "没有获取到图片路径，删除失败";
                        handler.sendMessage(message);
                    }
                }
        ).run();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult: " + requestCode + " " + requestCode + " " + data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            boolean result = data.getBooleanExtra("preferenceChanged", false);
            if (result) {
                startActivity(new Intent(MainActivity.this, MainActivity.class));
                finish();
                
                Toast.makeText(this, "已重新加载", Toast.LENGTH_SHORT).show();
            }
        } else {
//            Toast.makeText(this, "无返回值", Toast.LENGTH_SHORT).show();
        }
        
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Message message = new Message();
                message.what = HandlerMsgWhat.ERROR.getIndex();
                message.obj = "没有获取到存储权限，自动退出";
                handler.sendMessage(message);
                finish();
                return;
            }
        }
//        initView();
        read();
    }
    
    private void requestWritePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 0);
            } else {
//                initView();
                read();
            }
        }
    }
    
    private void read() {
        new Thread(
                () -> {
                    Message message = new Message();
                    imagePaths = ImageScanner.getImages(getApplicationContext(), getSelection());
                    if (imagePaths.size() == 0) {
                        message.what = HandlerMsgWhat.ERROR.getIndex();
                        message.obj = "没有找到图片";
                        handler.sendMessage(message);
                        return;
                    }
                    
                    String imagePath = imagePaths.get(0);
                    if (imagePath != null) {
                        Message textViewMessage = new Message();
                        textViewMessage.what = HandlerMsgWhat.REFRESH_TEXT.getIndex();
                        textViewMessage.obj = imagePath;
                        handler.sendMessage(textViewMessage);
                        
                        theLatestImages = new ArrayList<>();
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                                int thumbnailSize = sp.getInt("thumbnailSize", 512);
                                theLatestImage = ThumbnailUtils.createImageThumbnail(
                                        new File(imagePath),
                                        new Size(thumbnailSize, thumbnailSize),
                                        new CancellationSignal()
                                );
                                /*
                                * SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                                String thumbnailSizeStr = sp.getString("thumbnailSize", "512");
                                int thumbnailSize = Integer.parseInt(thumbnailSizeStr);
                                theLatestImage = ThumbnailUtils.createImageThumbnail(
                                        new File(imagePath),
                                        new Size(thumbnailSize, thumbnailSize),
                                        new CancellationSignal()
                                );*/
                            } else {
                                theLatestImage = BitmapFactory.decodeFile(imagePaths.get(0));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            message.what = HandlerMsgWhat.ERROR.getIndex();
                            message.obj = "读取图片失败";
                            handler.sendMessage(message);
                            return;
                        }
                        
                        message.what = HandlerMsgWhat.REFRESH_IMAGE.getIndex();
                        message.obj = theLatestImage;
                        handler.sendMessage(message);
                        
                    } else {
                        message.what = HandlerMsgWhat.ERROR.getIndex();
                        message.obj = "查找图片失败";
                        handler.sendMessage(message);
                    }
                }
        ).run();
    }
    
    private String getSelection() {
        String[] strings = getResources().getStringArray(R.array.path_values);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String pathType = sp.getString("path", strings[0]);
//        Log.d(TAG, "read: " + sp.getString("path", strings[0]));
        
        String selection = null;
        if (strings[0].equals(pathType)) {
            selection = null;
        } else if (strings[1].equals(pathType)) {
            selection = ImageScanner.getScreenshotsPath();
        }
        return selection;
    }
    
    /*public static Bitmap bitmapCompress(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        while (baos.toByteArray().length / 1024 > 10) {
            baos.reset();
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            options -= 10;
            if (options <= 0) {
                break;
            }
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        return BitmapFactory.decodeStream(isBm, null, null);
    }*/
}

enum HandlerMsgWhat {
    //错误
    ERROR(-1),
    //刷新文本
    REFRESH_TEXT(0),
    //刷新图片
    REFRESH_IMAGE(1),
    //删除图片成功
    DELETE_IMAGE_SUCCESS(2),
    //删除图片失败
    DELETE_IMAGE_FAIL(3);
    
    private final int index;
    
    HandlerMsgWhat(int index) {
        this.index = index;
    }
    
    public int getIndex() {
        return index;
    }
    
    public static HandlerMsgWhat getByValue(int what) {
        for (HandlerMsgWhat handlerMsgWhat : values()) {
            if (handlerMsgWhat.getIndex() == what) {
                return handlerMsgWhat;
            }
        }
        return null;
    }
}
