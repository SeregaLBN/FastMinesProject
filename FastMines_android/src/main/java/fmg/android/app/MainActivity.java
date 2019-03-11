package fmg.android.app;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import java.util.function.Supplier;

import fmg.android.app.databinding.MainActivityBinding;
import fmg.android.app.presentation.MainMenuViewModel;
import fmg.android.app.presentation.SmoothHelper;
import fmg.android.utils.Cast;
import fmg.android.utils.StaticInitializer;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.types.EMosaicGroup;
import fmg.core.types.ESkillLevel;

public class MainActivity extends AppCompatActivity {

    public static final int MenuTextWidthDp = 95; // dp

    private MainActivityBinding binding;
    private MainMenuViewModel viewModel;
    private MosaicGroupListViewAdapter mosaicGroupListViewAdapter;
    private MosaicSkillListViewAdapter mosaicSkillListViewAdapter;
    private SizeDouble cachedSizeActivity = new SizeDouble(-1, -1);

    static {
        StaticInitializer.init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);

        binding = DataBindingUtil.setContentView(this, R.layout.main_activity);
        viewModel = ViewModelProviders.of(this).get(MainMenuViewModel.class);
        binding.setViewModel(viewModel);
        binding.executePendingBindings();

        binding.rvMosaicGroupItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMosaicSkillItems.setLayoutManager(new LinearLayoutManager(this));
//        binding.rvMosaicGroupItems.setNestedScrollingEnabled(false);
//        binding.rvMosaicSkillItems.setNestedScrollingEnabled(false);
        binding.rvMosaicGroupItems.setAdapter(mosaicGroupListViewAdapter = new MosaicGroupListViewAdapter(viewModel.getMosaicGroupDS(), this::onMenuMosaicGroupItemClick));
        binding.rvMosaicSkillItems.setAdapter(mosaicSkillListViewAdapter = new MosaicSkillListViewAdapter(viewModel.getMosaicSkillDS(), this::onMenuMosaicSkillItemClick));

        binding.panelMosaicGroupHeader.setOnClickListener(this::onMenuMosaicGroupHeaderClick);
        binding.panelMosaicSkillHeader.setOnClickListener(this::onMenuMosaicSkillHeaderClick);

        binding.rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(this::onGlobalLayoutListener);

//        Intent intent = new Intent(this, DemoActivity.class);
//        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        binding.rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this::onGlobalLayoutListener);
        mosaicGroupListViewAdapter.close();
        super.onDestroy();
    }

    void onMenuMosaicGroupHeaderClick(View v) {
        SmoothHelper.runColorSmoothTransition(viewModel.getMosaicGroupDS().getHeader().getEntity().getModel());

        if (binding.rvMosaicGroupItems.getVisibility() == View.GONE) {
            SmoothHelper.applySmoothVisibilityOverScale(binding.rvMosaicGroupItems, true, this::getLvGroupHeight, null);
            viewModel.getMosaicGroupDS().getHeader().getEntity().getBurgerMenuModel().setHorizontal(false);
        } else {
            viewModel.getSplitViewPane().setOpen(!viewModel.getSplitViewPane().isOpen());
            viewModel.getMosaicGroupDS().getHeader().getEntity().getModel().setAnimeDirection(
                    !viewModel.getMosaicGroupDS().getHeader().getEntity().getModel().getAnimeDirection());
        }
    }

    double getLvGroupHeight() { return EMosaicGroup.values().length * (viewModel.getMosaicGroupDS().getImageSize().height + Cast.dpToPx(2) /* padding */); }
    double getLvSkillHeight() { return ESkillLevel .values().length * (viewModel.getMosaicSkillDS().getImageSize().height + Cast.dpToPx(2) /* padding */); }

    void onMenuMosaicSkillHeaderClick(View v) {
        SmoothHelper.runColorSmoothTransition(viewModel.getMosaicSkillDS().getHeader().getEntity().getModel());

        Supplier<Boolean> isVisibleScrollerFunc = () -> {
            int viewHeight = binding.menuScroller.getMeasuredHeight();
            int contentHeight = binding.menuScroller.getChildAt(0).getHeight();
            boolean scrollable = (viewHeight - contentHeight < 0);
            return scrollable;
        };
        boolean isVisibleScroller = isVisibleScrollerFunc.get();
        if (binding.rvMosaicSkillItems.getVisibility() == View.GONE) {
            if (isVisibleScroller) {
                SmoothHelper.applySmoothVisibilityOverScale(binding.rvMosaicSkillItems, true , this::getLvSkillHeight, null);
                SmoothHelper.applySmoothVisibilityOverScale(binding.rvMosaicGroupItems, false, this::getLvGroupHeight, null);
            } else {
                SmoothHelper.applySmoothVisibilityOverScale(binding.rvMosaicSkillItems, true, this::getLvSkillHeight,
                        () -> {
                            if (isVisibleScrollerFunc.get())
                                SmoothHelper.applySmoothVisibilityOverScale(binding.rvMosaicGroupItems, false, this::getLvGroupHeight, null);
                        });
            }
            viewModel.getMosaicSkillDS().getHeader().getEntity().getModel().setAnimeDirection(
                    !viewModel.getMosaicSkillDS().getHeader().getEntity().getModel().getAnimeDirection());
        } else {
            if (isVisibleScroller && (binding.rvMosaicGroupItems.getVisibility() == View.VISIBLE)) {
                SmoothHelper.applySmoothVisibilityOverScale(binding.rvMosaicGroupItems, false, this::getLvGroupHeight, null);
                viewModel.getMosaicGroupDS().getHeader().getEntity().getBurgerMenuModel().setHorizontal(true);
            } else {
                SmoothHelper.applySmoothVisibilityOverScale(binding.rvMosaicSkillItems, false, this::getLvSkillHeight, null);
                viewModel.getMosaicSkillDS().getHeader().getEntity().getModel().setAnimeDirection(
                        !viewModel.getMosaicSkillDS().getHeader().getEntity().getModel().getAnimeDirection());
            }
        }
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
//        LoggerSimple.put("> MainActivity::onActivitySizeChanged: newSize={0}", newSize);
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

//        LoggerSimple.put("< MainActivity::onActivitySizeChanged: " +
//                "sizeItem={0}, " +
//                "sizeHeader={1}/{4}, " +
//                "padHeader={2}, " +
//                "padBurger={3}",
//                sizeItem, sizeHeader, padHeader, padBurger,
//                viewModel.getMosaicGroupDS().getHeader().getSize()
//        );
    }

}
