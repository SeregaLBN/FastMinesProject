package fmg.android.app;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.Toast;

import java.beans.PropertyChangeEvent;
import java.util.function.Supplier;

import fmg.android.app.databinding.MainActivityBinding;
import fmg.android.app.model.MosaicInitDataExt;
import fmg.android.app.model.dataSource.MosaicGroupDataSource;
import fmg.android.app.model.dataSource.MosaicSkillDataSource;
import fmg.android.app.model.items.MosaicGroupDataItem;
import fmg.android.app.model.items.MosaicSkillDataItem;
import fmg.android.app.presentation.MainMenuViewModel;
import fmg.android.app.presentation.SmoothHelper;
import fmg.android.utils.Cast;
import fmg.common.LoggerSimple;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.SizeDouble;
import fmg.common.ui.UiInvoker;
import fmg.core.mosaic.MosaicInitData;
import fmg.core.types.EMosaicGroup;
import fmg.core.types.ESkillLevel;

public class MainActivity extends AppCompatActivity {

    public static final int MenuTextWidthDp = 95; // dp

    private MainActivityBinding binding;
    /** View-Model */
    private MainMenuViewModel viewModel;
    private MosaicGroupListViewAdapter mosaicGroupListViewAdapter;
    private MosaicSkillListViewAdapter mosaicSkillListViewAdapter;
    private SizeDouble cachedSizeActivity = new SizeDouble(-1, -1);

    public MosaicInitData getInitData() { return MosaicInitDataExt.getSharedData(); }
    //public void setInitData(MosaicInitData initData) { MosaicInitDataExt.getSharedData().copyFrom(initData); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.main_activity);
        viewModel = ViewModelProviders.of(this).get(MainMenuViewModel.class);
        binding.setViewModel(viewModel);
        binding.executePendingBindings();

        binding.rvMenuMosaicGroupItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMenuMosaicSkillItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMenuMosaicGroupItems.setAdapter(mosaicGroupListViewAdapter = new MosaicGroupListViewAdapter(viewModel.getMosaicGroupDS(), this::onMenuMosaicGroupItemClick));
        binding.rvMenuMosaicSkillItems.setAdapter(mosaicSkillListViewAdapter = new MosaicSkillListViewAdapter(viewModel.getMosaicSkillDS(), this::onMenuMosaicSkillItemClick));

        binding.panelMenuMosaicGroupHeader.setOnClickListener(this::onMenuMosaicGroupHeaderClick);
        binding.panelMenuMosaicSkillHeader.setOnClickListener(this::onMenuMosaicSkillHeaderClick);

        binding.rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(this::onGlobalLayoutListener);


        viewModel.getMosaicGroupDS().addListener(this::onMosaicGroupDsPropertyChanged);
        viewModel.getMosaicSkillDS().addListener(this::onMosaicSkillDsPropertyChanged);

        viewModel.getMosaicGroupDS().setCurrentItem(viewModel.getMosaicGroupDS().getDataSource().stream().filter(x -> x.getMosaicGroup() == getInitData().getMosaicType().getGroup()).findFirst().get());
        viewModel.getMosaicSkillDS().setCurrentItem(viewModel.getMosaicSkillDS().getDataSource().stream().filter(x -> x.getSkillLevel()  == getInitData().getSkillLevel()           ).findFirst().get());

        if (savedInstanceState == null) {
            // initial setup
            // navigate to SelectMosaicFragment
            SelectMosaicFragment smf = new SelectMosaicFragment();
            Bundle intentBundle = getIntent().getExtras();
            smf.setArguments(intentBundle);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.rightFrame, smf)
                    .commit();
        }


//        Intent intent = new Intent(this, DemoActivity.class);
//        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        MosaicInitDataExt.save(savedInstanceState, getInitData());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        viewModel.getMosaicGroupDS().removeListener(this::onMosaicGroupDsPropertyChanged);
        viewModel.getMosaicSkillDS().removeListener(this::onMosaicSkillDsPropertyChanged);

        binding.rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this::onGlobalLayoutListener);
        mosaicGroupListViewAdapter.close();
        super.onDestroy();
    }

    void onMenuMosaicGroupHeaderClick(View v) {
        SmoothHelper.runColorSmoothTransition(viewModel.getMosaicGroupDS().getHeader().getEntity().getModel());

        if (binding.rvMenuMosaicGroupItems.getVisibility() == View.GONE) {
            SmoothHelper.applySmoothVisibilityOverScale(binding.rvMenuMosaicGroupItems, true, this::getLvGroupHeight, null);
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
        if (binding.rvMenuMosaicSkillItems.getVisibility() == View.GONE) {
            if (isVisibleScroller) {
                SmoothHelper.applySmoothVisibilityOverScale(binding.rvMenuMosaicSkillItems, true , this::getLvSkillHeight, null);
                SmoothHelper.applySmoothVisibilityOverScale(binding.rvMenuMosaicGroupItems, false, this::getLvGroupHeight, null);
            } else {
                SmoothHelper.applySmoothVisibilityOverScale(binding.rvMenuMosaicSkillItems, true, this::getLvSkillHeight,
                        () -> {
                            if (isVisibleScrollerFunc.get())
                                SmoothHelper.applySmoothVisibilityOverScale(binding.rvMenuMosaicGroupItems, false, this::getLvGroupHeight, null);
                        });
            }
            viewModel.getMosaicSkillDS().getHeader().getEntity().getModel().setAnimeDirection(
                    !viewModel.getMosaicSkillDS().getHeader().getEntity().getModel().getAnimeDirection());
        } else {
            if (isVisibleScroller && (binding.rvMenuMosaicGroupItems.getVisibility() == View.VISIBLE)) {
                SmoothHelper.applySmoothVisibilityOverScale(binding.rvMenuMosaicGroupItems, false, this::getLvGroupHeight, null);
                viewModel.getMosaicGroupDS().getHeader().getEntity().getBurgerMenuModel().setHorizontal(true);
            } else {
                SmoothHelper.applySmoothVisibilityOverScale(binding.rvMenuMosaicSkillItems, false, this::getLvSkillHeight, null);
                viewModel.getMosaicSkillDS().getHeader().getEntity().getModel().setAnimeDirection(
                        !viewModel.getMosaicSkillDS().getHeader().getEntity().getModel().getAnimeDirection());
            }
        }
    }

    void onMenuMosaicGroupItemClick(View v, int position) {
        Toast.makeText(this, "onMenuMosaicGroupItemClick " + position, Toast.LENGTH_LONG).show();
    }

    void onMenuMosaicSkillItemClick(View v, int position) {
        Toast.makeText(this, "onMenuMosaicSkillItemClick " + position, Toast.LENGTH_LONG).show();

        MosaicSkillDataItem msd = viewModel.getMosaicSkillDS().getCurrentItem();
        getInitData().setSkillLevel(msd.getSkillLevel());
    }

    private void showSelectMosaicFragment(EMosaicGroup mosaicGroup) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.rightFrame);
        if (true)
            return;
        SelectMosaicFragment smf;
        if (fragment instanceof SelectMosaicFragment) {
            smf = (SelectMosaicFragment)fragment;
        } else {
            // Execute a transaction, replacing any existing fragment with this one inside the frame.
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.rightFrame, smf = new SelectMosaicFragment());
            if (fragment != null)
                ft.detach(fragment);
            ft.addToBackStack(null);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
        }
        UiInvoker.DEFERRED.accept(() -> {
//            smf.setCurrentMosaicGroup(mosaicGroup);
//            smf.setInitData(this.getInitData());
//            smf.setCurrentSkillLevel(this.initData.getSkillLevel());
//            if (this.getInitData().getMosaicType().getGroup() == mosaicGroup)
//                smf.setCurrentItem(smf.viewModel.getMosaicDS().getDataSource().stream().filter(x -> x.getMosaicType() == this.getInitData().getMosaicType()).findAny().get());
        });
    }

    private void showCustomSkillFragment() {
        LoggerSimple.put("TODO:  redirect to CustomSkillFragment...");
    }

    private void showHypnosisLogoFragment() {
        LoggerSimple.put("TODO:  redirect to HypnosisLogoFragment...");
    }

    private void onMenuCurrentItemChanged(boolean senderIsMosaicGroup, MosaicGroupDataItem currentGroupItem, MosaicSkillDataItem currentSkillItem) {
        if ((currentGroupItem == null) || (currentSkillItem == null)) {
            showHypnosisLogoFragment();
            return;
        }

        if (currentSkillItem.getSkillLevel() != ESkillLevel.eCustom)
            getInitData().setSkillLevel(currentSkillItem.getSkillLevel());

        if (!senderIsMosaicGroup && (currentSkillItem.getSkillLevel() == ESkillLevel.eCustom)) {
            showCustomSkillFragment();
        } else {
            //LoggerSimple.put("> MainActivity::onMenuCurrentItemChanged: " + currentGroupItem.getMosaicGroup());
            showSelectMosaicFragment(currentGroupItem.getMosaicGroup());
        }
    }

    private void onMosaicGroupDsPropertyChanged(PropertyChangeEvent ev) {
        //LoggerSimple.put("> MainActivity::onMosaicGroupDsPropertyChanged: ev.Name=" + ev.getPropertyName());
        switch (ev.getPropertyName()) {
        case MosaicGroupDataSource.PROPERTY_CURRENT_ITEM:
            MosaicSkillDataItem currentGroupItem = viewModel.getMosaicSkillDS().getCurrentItem();
            onMenuCurrentItemChanged(true, ((MosaicGroupDataSource)ev.getSource()).getCurrentItem(), currentGroupItem);
            break;
        }
    }

    private void onMosaicSkillDsPropertyChanged(PropertyChangeEvent ev) {
        //LoggerSimple.put("> MainActivity::onMosaicSkillDsPropertyChanged: ev.Name=" + ev.getPropertyName());
        switch (ev.getPropertyName()) {
        case MosaicSkillDataSource.PROPERTY_CURRENT_ITEM:
            MosaicSkillDataItem currentSkillItem = ((MosaicSkillDataSource)ev.getSource()).getCurrentItem();
            onMenuCurrentItemChanged(false, viewModel.getMosaicGroupDS().getCurrentItem(), currentSkillItem);
            break;
        }
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
