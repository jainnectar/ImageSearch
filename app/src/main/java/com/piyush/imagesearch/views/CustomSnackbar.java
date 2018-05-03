package com.piyush.imagesearch.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.piyush.imagesearch.R;
import com.piyush.imagesearch.ux.MainActivity;

/**
 * Created by user on 4/19/2017.
 */

public class CustomSnackbar {
    Context context;
    View view;
    Snackbar snackbar;

    public CustomSnackbar(final Context context, View view, String msg, final String action) {
        snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_LONG);
//                .setActionTextColor(ContextCompat.getColor(context, R.color.colorAccent));
//                .setAction(action, new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if (action.equals(context.getResources().getString(R.string.go_to_cart))) {
//                            Intent in = new Intent(context, MainActivity.class);
//                            in.putExtra("fragment", "cart");
//                            context.startActivity(in);
//                        }
//                    }
//                });
//        if (action.equals(context.getResources().getString(R.string.go_to_cart))) {
            View sbView = snackbar.getView();
            sbView.setBackgroundColor(context.getResources().getColor(R.color.cardview_dark_background));
            snackbar.setActionTextColor(context.getResources().getColor(R.color.white));
//        }
        TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }
}