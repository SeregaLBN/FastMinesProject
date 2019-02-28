package fmg.android.app.presentation;

import android.content.res.Resources;
import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageButton;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import fmg.android.app.MainActivity;
import fmg.android.utils.Cast;
import fmg.common.LoggerSimple;
import fmg.common.geom.SizeDouble;

public final class Converters {

    private Converters() {}


    @BindingAdapter("layout_mainMenuWidth")
    public static void setLayoutMainMenuWidth(View view, SizeDouble size) {
        float additionDp = MainActivity.MenuIsFullWidth
                ? MainActivity.MenuTextWidthDp + Cast.pxToDp(12) // vertical scrollbar width
                : Cast.pxToDp(2);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = (int)(size.width + Cast.dpToPx(additionDp));
        view.setLayoutParams(layoutParams);
        LoggerSimple.put("Converters::setLayoutMainMenuWidth: size={0}, set layout.width={1} for {2}.id={3}", size, layoutParams.width, view.getClass().getSimpleName(), view.getId());
    }

    @BindingAdapter("layout_sizeToWidth")
    public static void setLayoutSizeToWidth(View view, SizeDouble size) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = (int)size.width;
        view.setLayoutParams(layoutParams);
        LoggerSimple.put("Converters::setLayoutSizeToWidth: size={0}, set layout.width={1} for {2}.id={3}", size, layoutParams.width, view.getClass().getSimpleName(), view.getId());
    }

    @BindingAdapter("layout_sizeToHeight")
    public static void setLayoutSizeToHeight(View view, SizeDouble size) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int)size.height;
        view.setLayoutParams(layoutParams);
        LoggerSimple.put("Converters::setLayoutSizeToHeight: size={0}, set layout.height={1} for {2}.id={3}", size, layoutParams.height, view.getClass().getSimpleName(), view.getId());
    }

    @BindingAdapter("android:imageBitmap")
    public static void loadImage(ImageView iv, Bitmap bitmap) {
        iv.setImageBitmap(bitmap);
    }

//    @BindingAdapter("headerImage")
//    public static void headerImage(Button bttn, Bitmap bitmap) {
//        Drawable img = new BitmapDrawable(Resources.getSystem(), bitmap);
//
////        bttn.setBackground(img);
//
////        img.setBounds(0, 0, 60, 60);
////        bttn.setCompoundDrawables(img, null, null, null);
//
//        bttn.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
//    }
//
//    @BindingAdapter("headerImage")
//    public static void headerImage(AppCompatImageButton bttn, Bitmap bitmap) {
//        Drawable img = new BitmapDrawable(Resources.getSystem(), bitmap);
//        bttn.setImageDrawable(img);
//    }

}
