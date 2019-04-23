package fmg.android.app.presentation;

import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

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

    @BindingAdapter("android:padding")
    public static void padding(LinearLayout ll, DipWrapper padWrapper) {
        int p = (int)Cast.dpToPx((float)padWrapper.dip);
        ll.setPadding(p,p,p,p);
    }

/*
    private static void margin(LinearLayout.LayoutParams lp, DipWrapper marginWrapper) {
        int m = (int)Cast.dpToPx((float)marginWrapper.dip);
        lp.setMargins(m,m,m,m);
    }
    @BindingAdapter("android:layout_margin")
    public static void margin(LinearLayout ll, DipWrapper marginWrapper) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)ll.getLayoutParams();
        margin(lp, marginWrapper);
    }
    @BindingAdapter("android:layout_margin")
    public static void margin(ImageView imageView, DipWrapper marginWrapper) {
        ViewGroup.LayoutParams lp = imageView.getLayoutParams();
        if (!(lp instanceof LinearLayout.LayoutParams)) {
            Log.w("fmg", "Converters::margin: Can not cast ViewGroup.LayoutParams to LinearLayout.LayoutParams: is it instance of " + lp.getClass());
            return;
        }
        margin((LinearLayout.LayoutParams)lp, marginWrapper);
    }
*/

    public static class DipWrapper {
        public DipWrapper(double dip) { this.dip = dip; }
        public final double dip;
    }

}
