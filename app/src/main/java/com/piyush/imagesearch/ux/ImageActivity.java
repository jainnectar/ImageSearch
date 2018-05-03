package com.piyush.imagesearch.ux;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.piyush.imagesearch.R;
import com.piyush.imagesearch.views.CustomSnackbar;

public class ImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        ImageView preview_image = findViewById(R.id.preview_image);

        Bitmap _bitmap = BitmapFactory.decodeByteArray(
                getIntent().getByteArrayExtra("byteArray"), 0, getIntent().getByteArrayExtra("byteArray").length);
        preview_image.setImageBitmap(_bitmap);

        Glide.with(this)
                .load(getIntent().getStringExtra("preview_image"))
                .asBitmap()
                .placeholder(preview_image.getDrawable())
                .into(preview_image);

        if (!isNetworkAvailable()) {
            CustomSnackbar customSnackbar = new CustomSnackbar(this, findViewById(R.id.main_layout), getString(R.string.no_internet_info_text), "");
        }
    }

    public boolean isNetworkAvailable() {
        boolean state;
        ConnectivityManager cmg = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cmg.getActiveNetworkInfo();
        state = activeNetworkInfo != null && activeNetworkInfo.isConnected();

        if (state) {
            return true;
        } else {
            //NO interntet
            return false;
        }
    }
}
