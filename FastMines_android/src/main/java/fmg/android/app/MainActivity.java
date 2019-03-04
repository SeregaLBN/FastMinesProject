package fmg.android.app;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import fmg.android.app.databinding.MainActivityBinding;
import fmg.android.app.presentation.MainMenuViewModel;
import fmg.android.app.presentation.SmoothHelper;
import fmg.android.utils.AsyncRunner;
import fmg.android.utils.Cast;
import fmg.android.utils.StaticInitializer;
import fmg.common.Color;
import fmg.common.LoggerSimple;
import fmg.common.geom.BoundDouble;
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
//        binding.rvMosaicGroupItems.setNestedScrollingEnabled(false);
//        binding.rvMosaicSkillItems.setNestedScrollingEnabled(false);
        binding.rvMosaicGroupItems.setAdapter(menuMosaicGroupListViewAdapter = new MenuMosaicGroupListViewAdapter(viewModel.getMosaicGroupDS(), this::onMenuMosaicGroupItemClick));
        binding.rvMosaicSkillItems.setAdapter(menuMosaicSkillListViewAdapter = new MenuMosaicSkillListViewAdapter(viewModel.getMosaicSkillDS(), this::onMenuMosaicSkillItemClick));

        binding.panelMosaicGroupHeader.setOnClickListener(this::onMosaicGroupHeaderClick);
        binding.panelMosaicSkillHeader.setOnClickListener(this::onMosaicSkillHeaderClick);

        binding.rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(this::onGlobalLayoutListener);

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
        SmoothHelper.runColorSmoothTransition(v, viewModel.getMosaicGroupDS().getHeader().getEntity().getModel());
        Toast.makeText(this, "onMosaicGroupHeaderClick", Toast.LENGTH_LONG).show();

//        int s = 100 + ThreadLocalRandom.current().nextInt(100);
//        viewModel.getMosaicGroupDS().setImageSize(new SizeDouble(s, s));
    }

    void onMosaicSkillHeaderClick(View v) {
        SmoothHelper.runColorSmoothTransition(v, viewModel.getMosaicSkillDS().getHeader().getEntity().getModel());
        Toast.makeText(this, "onMosaicSkillHeaderClick", Toast.LENGTH_LONG).show();
    }

    void onMenuMosaicSkillItemClick(View v, int position) {
        Toast.makeText(this, "onMenuMosaicSkillItemClick " + position, Toast.LENGTH_LONG).show();
    }

    void onMenuMosaicGroupItemClick(View v, int position) {
        Toast.makeText(this, "onMenuMosaicGroupItemClick " + position, Toast.LENGTH_LONG).show();
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
