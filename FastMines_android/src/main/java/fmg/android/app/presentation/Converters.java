package fmg.android.app.presentation;

import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import fmg.common.geom.SizeDouble;

public final class Converters {
    private Converters() {}

    public static void setViewWidth(View view, double width) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = (int)width;
        view.setLayoutParams(layoutParams);
    }

    public static void setViewHeight(View view, double height) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int)height;
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("layout_mainMenuWidth")
    public static void setLayoutMainMenuWidth(View view, MainMenuViewModel.SplitViewPane splitViewPane) {
        setViewWidth(view, splitViewPane.getPaneWidth());
    }

    @BindingAdapter("layout_sizeToWidth")
    public static void setLayoutSizeToWidth(View view, SizeDouble size) {
        setViewWidth(view, size.width);
    }

    @BindingAdapter("layout_sizeToHeight")
    public static void setLayoutSizeToHeight(View view, SizeDouble size) {
        setViewHeight(view, size.height);
    }

    @BindingAdapter("android:imageBitmap")
    public static void loadImage(ImageView iv, Bitmap bitmap) {
        iv.setImageBitmap(bitmap);
    }

}
