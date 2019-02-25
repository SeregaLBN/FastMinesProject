package fmg.android.app.presentation;

import android.content.res.Resources;
import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import fmg.android.app.MainActivity;
import fmg.common.LoggerSimple;
import fmg.common.geom.SizeDouble;

public final class Converters {

    private Converters() {}


    @BindingAdapter("layout_mainMenuWidth")
    public static void setLayoutMainMenuWidth(View view, SizeDouble size) {
        int addition = MainActivity.MenuIsFullWidth
                ? MainActivity.MenuTextWidth + 12 // vertical scrollbar width
                : 2;

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        LoggerSimple.put("Converters::setLayoutWidth: size={0}", size);
        layoutParams.width = (int)(size.width + addition);
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("layout_sizeToWidth")
    public static void setLayoutSizeToWidth(View view, SizeDouble size) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = (int)size.width;
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("layout_sizeToHight")
    public static void setLayoutSizeToHight(View view, SizeDouble size) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int)size.height;
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:imageBitmap")
    public static void loadImage(ImageView iv, Bitmap bitmap) {
        iv.setImageBitmap(bitmap);
    }

    @BindingAdapter("headerImage")
    public static void headerImage(Button bttn, Bitmap bitmap) {
        Drawable img = new BitmapDrawable(Resources.getSystem(), bitmap);

//        bttn.setBackground(img);

//        img.setBounds(0, 0, 60, 60);
//        bttn.setCompoundDrawables(img, null, null, null);

        bttn.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
    }


}
