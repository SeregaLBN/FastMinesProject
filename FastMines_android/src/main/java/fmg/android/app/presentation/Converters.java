package fmg.android.app.presentation;

import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import fmg.android.app.MainActivity;
import fmg.android.utils.Cast;
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
        float additionPx = splitViewPane.isOpen()
                ? Cast.dpToPx(MainActivity.MenuTextWidthDp) + 12 // vertical scrollbar width
                : Cast.dpToPx(2);

        SizeDouble size = splitViewPane.getImageSize();
        setViewWidth(view, size.width + additionPx);
//        LoggerSimple.put("Converters::setLayoutMainMenuWidth: size={0}, set layout.width={1} for {2}.id={3}", size, layoutParams.width, view.getClass().getSimpleName(), view.getId());
    }

    @BindingAdapter("layout_sizeToWidth")
    public static void setLayoutSizeToWidth(View view, SizeDouble size) {
        setViewWidth(view, size.width);
//        LoggerSimple.put("Converters::setLayoutSizeToWidth: size={0}, set layout.width={1} for {2}.id={3}", size, layoutParams.width, view.getClass().getSimpleName(), view.getId());
    }

    @BindingAdapter("layout_sizeToHeight")
    public static void setLayoutSizeToHeight(View view, SizeDouble size) {
        setViewHeight(view, size.height);
//        LoggerSimple.put("Converters::setLayoutSizeToHeight: size={0}, set layout.height={1} for {2}.id={3}", size, layoutParams.height, view.getClass().getSimpleName(), view.getId());
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
