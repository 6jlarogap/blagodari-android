package blagodarie.rating.ui;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.squareup.picasso.Picasso;

public final class BindingAdapters {

    @BindingAdapter({"imageUrl"})
    public static void loadImage (ImageView view, String url) {
        if (url != null && !url.isEmpty()) {
            Picasso.get().load(url).into(view);
        }
    }

    @BindingAdapter({"imageUrl", "placeholder"})
    public static void loadImage (ImageView view, String url, Drawable placeholder) {
        if (url != null && !url.isEmpty()) {
            Picasso.get().load(url).error(placeholder).into(view);
        } else {
            view.setImageDrawable(placeholder);
        }
    }

    @BindingAdapter({"imageBitmap"})
    public static void loadImage (ImageView view, Bitmap bitmap) {
        view.setImageBitmap(bitmap);
    }

    @BindingAdapter({"onRefresh"})
    public static void onRefresh (SwipeRefreshLayout swipeRefreshLayout, SwipeRefreshLayout.OnRefreshListener onRefreshListener) {
        swipeRefreshLayout.setOnRefreshListener(onRefreshListener);
    }

}
