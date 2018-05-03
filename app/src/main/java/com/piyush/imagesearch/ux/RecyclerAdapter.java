package com.piyush.imagesearch.ux;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.piyush.imagesearch.R;
import com.piyush.imagesearch.dataObj.ImageObj;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Created by eye0008 on 28-04-2018.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.adapter_viewholder> {
    Context context;
    ArrayList<ImageObj> list = new ArrayList<>();

    public RecyclerAdapter(Context context, ArrayList<ImageObj> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public adapter_viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_result, parent, false);
        return new adapter_viewholder(v);
    }

    @Override
    public void onBindViewHolder(final adapter_viewholder holder, final int position) {
        String img = list.get(position).getThumb_url();

        Glide.with(context)
                .load(img)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_placeholder)
                .into(holder.image);


        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ImageActivity.class);

                BitmapDrawable drawable = (BitmapDrawable) holder.image.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 50, bs);
                intent.putExtra("byteArray", bs.toByteArray());
                intent.putExtra("preview_image", list.get(position).getPreview_url());

                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation((Activity) context,
                                holder.image,
                                ViewCompat.getTransitionName(holder.image));
                context.startActivity(intent, options.toBundle());
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void add() {
        notifyItemInserted(list.size() - 1);
    }

    public class adapter_viewholder extends RecyclerView.ViewHolder {

        ImageView image;

        public adapter_viewholder(View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.image);
        }
    }
}
