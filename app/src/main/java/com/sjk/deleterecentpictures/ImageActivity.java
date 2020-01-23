package com.sjk.deleterecentpictures;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;
import java.util.List;

public class ImageActivity extends AppCompatActivity {
    
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
        
        if (MainActivity.theLatestImages.size() == 0) {
            int l = 10;
            if (MainActivity.imagePaths.size() < 10) {
                l = MainActivity.imagePaths.size();
            }
            for (int i = 0; i < l; i++) {
                Bitmap bitmap = BitmapFactory.decodeFile(MainActivity.imagePaths.get(i));
                MainActivity.theLatestImages.add(bitmap);
            }
        }
        
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter();
        viewPagerAdapter.setImages(MainActivity.theLatestImages);
        viewPagerAdapter.setActivity(this);
        
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(viewPagerAdapter);
        
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
}

class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.ViewPagerViewHolder> {
    private List<Bitmap> images = new ArrayList<>();
    private Activity context;
    
    public List<Bitmap> getImages() {
        return images;
    }
    
    void setImages(List<Bitmap> images) {
        this.images = images;
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
        return new ViewPagerViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_view_pager_item, parent, false));
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewPagerViewHolder holder, int position) {
        holder.photoView.setImageBitmap(images.get(position));
    }
    
    @Override
    public int getItemCount() {
        return images.size();
    }
    
    class ViewPagerViewHolder extends RecyclerView.ViewHolder {
        PhotoView photoView;
        
        ViewPagerViewHolder(@NonNull View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.photoView);
            photoView.setMaximumScale(10);
            photoView.setMediumScale(4);
            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.finish();
                }
            });
        }
    }
}
