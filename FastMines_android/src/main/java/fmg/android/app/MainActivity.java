package fmg.android.app;

import android.arch.lifecycle.ViewModelProviders;
import android.content.res.Resources;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.databinding.ObservableField;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.concurrent.ThreadLocalRandom;

import fmg.android.app.databinding.MainActivityBinding;
import fmg.android.app.presentation.MainMenuViewModel;
import fmg.android.utils.AsyncRunner;
import fmg.android.utils.Cast;
import fmg.android.utils.StaticInitializer;
import fmg.common.Color;
import fmg.common.LoggerSimple;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.DoubleExt;
import fmg.common.geom.SizeDouble;
import fmg.common.geom.util.FigureHelper;
import fmg.core.img.AnimatedImageModel;

public class MainActivity extends AppCompatActivity {

    public static final int MenuTextWidthDp = 90; // dp
    public static boolean MenuIsFullWidth = true;

    private MainActivityBinding binding;
    private MainMenuViewModel viewModel;
    private MenuMosaicGroupListViewAdapter menuMosaicGroupListViewAdapter;
    private MenuMosaicSkillListViewAdapter menuMosaicSkillListViewAdapter;

    SizeDouble cachedSizeActivity = new SizeDouble(-1, -1);

    static {
        StaticInitializer.init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.main_activity);
        viewModel = ViewModelProviders.of(this).get(MainMenuViewModel.class);
        binding.setViewModel(viewModel);
        binding.executePendingBindings();

        binding.rvMosaicGroupItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMosaicSkillItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMosaicGroupItems.setAdapter(menuMosaicGroupListViewAdapter = new MenuMosaicGroupListViewAdapter(viewModel.getMosaicGroupDS(), this::onMenuMosaicGroupItemClick));
        binding.rvMosaicSkillItems.setAdapter(menuMosaicSkillListViewAdapter = new MenuMosaicSkillListViewAdapter(viewModel.getMosaicSkillDS(), this::onMenuMosaicSkillItemClick));

        binding.panelMosaicGroupHeader.setOnClickListener(this::onMosaicGroupHeaderClick);
        binding.panelMosaicSkillHeader.setOnClickListener(this::onMosaicSkillHeaderClick);

        binding.rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(this::onGlobalLayoutListener);

//        ApplyViewColorSmoothTransition(binding.panelMosaicGroupHeader, viewModel.getMosaicGroupDS().getHeader().getEntity().getModel());

//        Intent intent = new Intent(this, DemoActivity.class);
//        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        binding.rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this::onGlobalLayoutListener);
        menuMosaicGroupListViewAdapter.close();
        super.onDestroy();
    }

    void onMosaicGroupHeaderClick(View v) {
        ApplyViewColorSmoothTransition(v, viewModel.getMosaicGroupDS().getHeader().getEntity().getModel());
        Toast.makeText(this, "onMosaicGroupHeaderClick", Toast.LENGTH_LONG).show();
        // does something very interesting
//        int s = 100 + ThreadLocalRandom.current().nextInt(100);
//        viewModel.getMosaicGroupDS().setImageSize(new SizeDouble(s, s));
    }

    void onMosaicSkillHeaderClick(View v) {
        Toast.makeText(this, "onMosaicSkillHeaderClick", Toast.LENGTH_LONG).show();
    }

    void onMenuMosaicSkillItemClick(View v, int position) {
        Toast.makeText(this, "onMenuMosaicSkillItemClick " + position, Toast.LENGTH_LONG).show();
    }

    void onMenuMosaicGroupItemClick(View v, int position) {
        Toast.makeText(this, "onMenuMosaicGroupItemClick " + position, Toast.LENGTH_LONG).show();
    }

    private static void ApplyViewColorSmoothTransition(View view, AnimatedImageModel model) {
        int[] flag = { 0 };
        Color clrFrom = model.getBackgroundColor(); //Cast.toColor(((ColorDrawable)view.getBackground()).getColor());//Color.Coral;
        Color clrTo = Color.BlueViolet();
        long fullTimeMsec = 350, repeatTimeMsec = 20;
        double[] currStepAngle = { 0 };
        double deltaStepAngle = 360.0 * repeatTimeMsec / fullTimeMsec;
        Runnable handler= () -> {
            Runnable r = () -> {
                Color clrCurr;
                if (flag[0] == 1) {
                    currStepAngle[0] += deltaStepAngle;
                    if (currStepAngle[0] >= 360) {
                        flag[0] = 2;  // start exited
                        clrCurr = clrTo;
                        LoggerSimple.put("+ ApplyViewColorSmoothTransition:  to={0}", clrTo);
                    } else {
                        double sin = Math.sin(FigureHelper.toRadian(currStepAngle[0] / 4));
                        clrCurr = new Color((int)(clrFrom.getA() + sin * (clrTo.getA() - clrFrom.getA())),
                                            (int)(clrFrom.getR() + sin * (clrTo.getR() - clrFrom.getR())),
                                            (int)(clrFrom.getG() + sin * (clrTo.getG() - clrFrom.getG())),
                                            (int)(clrFrom.getB() + sin * (clrTo.getB() - clrFrom.getB())));
                    }
                } else {
                    currStepAngle[0] -= deltaStepAngle;
                    if (currStepAngle[0] <= 0) {
                        flag[0] = 0;  // stop
                        clrCurr = clrFrom;
                        LoggerSimple.put("< ApplyViewColorSmoothTransition: frm={0}", clrFrom);
                    } else {
                        double cos = Math.cos(FigureHelper.toRadian(currStepAngle[0] / 4));
                        clrCurr = new Color((int)(clrTo.getA() - (1 - cos * (clrFrom.getA() - clrTo.getA()))),
                                            (int)(clrTo.getR() - (1 - cos * (clrFrom.getR() - clrTo.getR()))),
                                            (int)(clrTo.getG() - (1 - cos * (clrFrom.getG() - clrTo.getG()))),
                                            (int)(clrTo.getB() - (1 - cos * (clrFrom.getB() - clrTo.getB()))));
                    }
                }
//                LoggerSimple.put("  ApplyViewColorSmoothTransition: clr={0}", clrCurr);
                model.setBackgroundColor(clrCurr);
//                view.setBackgroundColor(Cast.toColor(clrCurr));
            };
            LoggerSimple.put("> ApplyViewColorSmoothTransition: frm={0}", clrFrom);
            AsyncRunner.Repeat(r, repeatTimeMsec, () -> flag[0] == 0);
        };
        /** /
        Runnable pointerEntered = () -> {
            flag[0] = 1; // start entered
            handler.run();
        };
        Runnable pointerExited = () -> {
            flag[0] = 2;
            handler.run();
        };
        view.setOnCapturedPointerListener((ev, ev3) -> {
            pointerEntered.run();
            return false;
        });
        /**/
        flag[0] = 1; // start entered
        handler.run();
    }

    private void onGlobalLayoutListener() {
        SizeDouble newSize = new SizeDouble(binding.rootLayout.getWidth(), binding.rootLayout.getHeight());
        if (cachedSizeActivity.equals(newSize))
            return;

        onActivitySizeChanged(cachedSizeActivity = newSize);
    }

    private void onActivitySizeChanged(SizeDouble newSize) {
        LoggerSimple.put("> MainActivity::onActivitySizeChanged: newSize={0}", newSize);
        final float minSize       = Cast.dpToPx(45);
        final float maxSize       = Cast.dpToPx(80);
        final float topElemHeight = Cast.dpToPx(40);
        final float pad           = Cast.dpToPx(2);
        assert (topElemHeight <= minSize);

        double size = Math.min(newSize.height, newSize.width);
        double size1 = size/7;
        double wh = Math.min(Math.max(minSize, size1), maxSize);

        SizeDouble sizeItem = new SizeDouble(wh, wh);
        viewModel.getMosaicGroupDS().setImageSize(sizeItem);
        viewModel.getMosaicSkillDS().setImageSize(sizeItem);

        SizeDouble sizeHeader = new SizeDouble(wh, topElemHeight);
        viewModel.getMosaicGroupDS().getHeader().setSize(sizeHeader);
        viewModel.getMosaicSkillDS().getHeader().setSize(sizeHeader);

        BoundDouble padHeader = new BoundDouble(pad, pad, wh - topElemHeight + pad, pad); // left margin
        viewModel.getMosaicGroupDS().getHeader().setPadding(padHeader);
        viewModel.getMosaicSkillDS().getHeader().setPadding(padHeader);

        double whBurger = topElemHeight / 2 + Math.min(topElemHeight / 2 - pad, Math.max(0, (wh - 1.5 * topElemHeight)));
        BoundDouble padBurger = new BoundDouble(wh - whBurger, topElemHeight - whBurger, pad, pad);
        viewModel.getMosaicGroupDS().getHeader().setPaddingBurgerMenu(padBurger); // right-bottom margin
        viewModel.getMosaicSkillDS().getHeader().setPaddingBurgerMenu(padBurger); // right-bottom margin

        LoggerSimple.put("< MainActivity::onActivitySizeChanged: " +
                "sizeItem={0}, " +
                "sizeHeader={1}/{4}, " +
                "padHeader={2}, " +
                "padBurger={3}",
                sizeItem, sizeHeader, padHeader, padBurger,
                viewModel.getMosaicGroupDS().getHeader().getSize()
        );
    }

}
