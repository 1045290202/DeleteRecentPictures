package com.sjk.deleterecentpictures;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ImageActivity extends AppCompatActivity {
    
    private static final String TAG = "ImageActivity";
    
    private int maximum = 10;
    
    private ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter();
    
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (Objects.requireNonNull(ImageActivityHandlerMsgWhat.getByValue(msg.what))) {
                case NOTIFY_DATA_SET_CHANGED: {
                    //刷新数据
                    Log.d(TAG, "handleMessage: " + "刷新数据");
                    viewPagerAdapter.notifyDataSetChanged();
                    break;
                }
                default: {
                    break;
                }
            }
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_image);
        
        init();
    }
    
    private void init() {
//        List<Bitmap> images = new ArrayList<>();
//        images.add(MainActivity.theLatestImage);
        
        if (MainActivity.imagePaths.size() == 0) {
            Toast.makeText(this, "无图片", Toast.LENGTH_SHORT).show();
            
            finish();
            return;
        }
        
        /*if (MainActivity.theLatestImages.size() == 0) {
            int l = 1;
            if (MainActivity.imagePaths.size() < 1) {
                l = MainActivity.imagePaths.size();
            }
            for (int i = 0; i < l; i++) {
                Bitmap bitmap = BitmapFactory.decodeFile(MainActivity.imagePaths.get(i));
                MainActivity.theLatestImages.add(bitmap);
            }
        }*/
        
        if (MainActivity.imagePaths.size() < maximum) {
            maximum = MainActivity.imagePaths.size();
        }
        
        Log.d(TAG, "init: " + maximum);
        
        viewPagerAdapter.setImages(MainActivity.theLatestImages);
        viewPagerAdapter.setImagePaths(MainActivity.imagePaths);
        viewPagerAdapter.getItemCount();
        viewPagerAdapter.setActivity(this);
        
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
//                Log.d(TAG, "onPageScrolled: " + position);
                if (position < maximum - 1) {
                    createNewImage(position + 1);
                }
            }
            
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0) {
                    createNewImage(position);
                }
            }
        });
    }
    
    private void buttonClickEventBind() {
    
    }
    
    private void createNewImage(int position) {
        try {
            Bitmap bitmap = MainActivity.theLatestImages.get(position);
        } catch (Exception e) {
            new Thread(() -> {
                Bitmap bitmap = BitmapFactory.decodeFile(MainActivity.imagePaths.get(position));
                MainActivity.theLatestImages.add(bitmap);
                
                Message message = new Message();
                message.what = ImageActivityHandlerMsgWhat.NOTIFY_DATA_SET_CHANGED.getIndex();
                handler.sendMessage(message);
            }).run();
        }
    }
    
    public void setFullScreen() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS |
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION); //允许页面可以拉伸到顶部状态栏并且定义顶部状态栏透名
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |  //设置全屏显示
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        window.setStatusBarColor(Color.TRANSPARENT); //设置状态栏为透明
        window.setNavigationBarColor(Color.parseColor("#44000000")); //设置虚拟键为透明
    }
    
    public int getNavigationBarHeight() {
        Resources resources = getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }
    
    class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.ViewPagerViewHolder> {
        public List<ViewPagerViewHolder> viewPagerViewHolders = new ArrayList<>();
        
        private List<Bitmap> images = new ArrayList<>();
        private List<String> imagePaths = new ArrayList<>();
        private Activity context;
        
        public List<Bitmap> getImages() {
            return images;
        }
        
        void setImages(List<Bitmap> images) {
            this.images = images;
        }
        
        public List<String> getImagePaths() {
            return imagePaths;
        }
        
        public void setImagePaths(List<String> imagePaths) {
            this.imagePaths = imagePaths;
        }
        
        
        public Activity getActivity() {
            return context;
        }
        
        void setActivity(Activity context) {
            this.context = context;
        }
        
        
        @NonNull
        @Override
        public ViewPagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ViewPagerViewHolder viewPagerViewHolder = new ViewPagerViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_view_pager_item, parent, false));
            viewPagerViewHolders.add(viewPagerViewHolder);
            return viewPagerViewHolder;
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewPagerViewHolder holder, int position) {
            if (position < images.size()) {
                holder.photoView.setImageBitmap(images.get(position));
            }
            holder.imagePath = imagePaths.get(position);
        }
        
        @Override
        public int getItemCount() {
            return maximum;
        }
        
        class ViewPagerViewHolder extends RecyclerView.ViewHolder {
            PhotoView photoView;
            String imagePath;
            
            ViewPagerViewHolder(@NonNull View itemView) {
                super(itemView);
                photoView = itemView.findViewById(R.id.photoView);
                photoView.setMaximumScale(10);
                photoView.setMediumScale(4);
                photoView.setOnClickListener(v -> context.finish());
            }
        }
    }
    
    enum ImageActivityHandlerMsgWhat {
        //错误
        NOTIFY_DATA_SET_CHANGED(0);
        
        private final int index;
        
        ImageActivityHandlerMsgWhat(int index) {
            this.index = index;
        }
        
        public int getIndex() {
            return index;
        }
        
        public static ImageActivityHandlerMsgWhat getByValue(int what) {
            for (ImageActivityHandlerMsgWhat handlerMsgWhat : values()) {
                if (handlerMsgWhat.getIndex() == what) {
                    return handlerMsgWhat;
                }
            }
            return null;
        }
    }
}
