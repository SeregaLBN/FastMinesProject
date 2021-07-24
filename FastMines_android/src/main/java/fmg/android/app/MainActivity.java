package fmg.android.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.function.Supplier;

import fmg.android.app.databinding.MainActivityBinding;
import fmg.android.app.model.dataSource.MosaicGroupDataSource;
import fmg.android.app.model.dataSource.MosaicSkillDataSource;
import fmg.android.app.model.items.MosaicGroupDataItem;
import fmg.android.app.model.items.MosaicSkillDataItem;
import fmg.android.app.presentation.MainMenuViewModel;
import fmg.android.app.presentation.MenuSettings;
import fmg.android.app.presentation.SmoothHelper;
import fmg.android.app.recyclerView.MosaicGroupListViewAdapter;
import fmg.android.app.recyclerView.MosaicSkillListViewAdapter;
import fmg.android.utils.Cast;
import fmg.common.Color;
import fmg.common.Logger;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.SizeDouble;
import fmg.common.ui.UiInvoker;
import fmg.core.types.EMosaicGroup;
import fmg.core.types.ESkillLevel;
import fmg.core.types.model.MosaicInitData;

/** Main window activity */
public class MainActivity extends AppCompatActivity {

    public static final int MenuTextWidthDp = 95; // dp
    public static final String BUNDLE_KEY__HEADER_SIZE_HEIGHT = "headerSizeHeight";

    private MainActivityBinding binding;
    /** View-Model */
    private MainMenuViewModel viewModel;
    private MosaicGroupListViewAdapter mosaicGroupListViewAdapter;
    private MosaicSkillListViewAdapter mosaicSkillListViewAdapter;
    private SizeDouble cachedSizeActivity = new SizeDouble(-1, -1);
    private EActivityStatus activityStatus = EActivityStatus.eLaunched;
    private final PropertyChangeListener onMosaicGroupDsPropertyChangedListener = this::onMosaicGroupDsPropertyChanged;
    private final PropertyChangeListener onMosaicSkillDsPropertyChangedListener = this::onMosaicSkillDsPropertyChanged;

    public MosaicInitData getInitData()     { return FastMinesApp.get().getMosaicInitData(); }
    public MenuSettings   getMenuSettings() { return FastMinesApp.get().getMenuSettings(); }

    enum EActivityStatus {
        eLaunched,
        eCreated,
        eStarted,
        eResumed,
        eRunning,
        ePaused,
        eStopped,
        eDestroyed,
        eRestarted;

        boolean isRunning() { return this == eResumed || this == eRunning; }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.info("MainActivity.onCreate: this.hash={0}", this.hashCode());
        super.onCreate(savedInstanceState);
        activityStatus = EActivityStatus.eCreated;

        // Remove title bar
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = DataBindingUtil.setContentView(this, R.layout.main_activity);
        viewModel = ViewModelProviders.of(this).get(MainMenuViewModel.class);
        viewModel.getSplitViewPane().setOpen(getMenuSettings().isSplitPaneOpen());
        binding.setViewModel(viewModel);
        binding.executePendingBindings();

        binding.rvMenuMosaicGroupItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMenuMosaicSkillItems.setLayoutManager(new LinearLayoutManager(this));

        viewModel.getMosaicGroupDS().getHeader().getEntity().getBurgerMenuModel().setHorizontal(viewModel.getSplitViewPane().isOpen());

        viewModel.getMosaicGroupDS().setCurrentItem(viewModel.getMosaicGroupDS().getDataSource().stream().filter(x -> x.getMosaicGroup() == getInitData().getMosaicType().getGroup()).findFirst().get());
        viewModel.getMosaicSkillDS().setCurrentItem(viewModel.getMosaicSkillDS().getDataSource().stream().filter(x -> x.getSkillLevel()  == getInitData().getSkillLevel()           ).findFirst().get());

        Color bkHeaderColor = getBackgroundHeaderColor();
        viewModel.getMosaicGroupDS().getHeader().getEntity().getModel().setBackgroundColor(bkHeaderColor);
        viewModel.getMosaicSkillDS().getHeader().getEntity().getModel().setBackgroundColor(bkHeaderColor);
        binding.panelMenuMosaicGroupHeader.setBackgroundColor(Cast.toColor(bkHeaderColor));
        binding.panelMenuMosaicSkillHeader.setBackgroundColor(Cast.toColor(bkHeaderColor));

        if (savedInstanceState == null) {
            // initial setup

            /*
            // navigate to SelectMosaicFragment
            SelectMosaicFragment smf = new SelectMosaicFragment();
            Bundle intentBundle = getIntent().getExtras();
            smf.setArguments(intentBundle);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.rightFrame, smf)
                    .commit();
            */
        }
    }

    @Override
    protected void onStart() {
        Logger.info("MainActivity.onStart: this.hash={0}", this.hashCode());
        super.onStart();
        activityStatus = EActivityStatus.eStarted;
    }

    @Override
    protected void onRestart() {
        Logger.info("MainActivity.onRestart: this.hash={0}", this.hashCode());
        super.onRestart();
        activityStatus = EActivityStatus.eRestarted;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Logger.info("MainActivity.onSaveInstanceState: this.hash={0}", this.hashCode());
        // TODO save...  new AppDataSerializer().save(savedInstanceState);
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public void onResume() {
        Logger.info("MainActivity.onResume: this.hash={0}", this.hashCode());
        super.onResume();
        activityStatus = EActivityStatus.eResumed;
        UiInvoker.DEFERRED.accept(() -> {
            if (activityStatus != EActivityStatus.eResumed)
                return;
            activityStatus = EActivityStatus.eRunning;
            Logger.info("MainActivity.onResume: this.hash={0}: activityStatus={1}", this.hashCode(), activityStatus);
        });

        // subscribe all
        mosaicGroupListViewAdapter = new MosaicGroupListViewAdapter(viewModel.getMosaicGroupDS().getDataSource());
        mosaicSkillListViewAdapter = new MosaicSkillListViewAdapter(viewModel.getMosaicSkillDS().getDataSource());
        mosaicGroupListViewAdapter.setOnItemClick(this::onMenuMosaicGroupItemClick);
        mosaicSkillListViewAdapter.setOnItemClick(this::onMenuMosaicSkillItemClick);
        binding.rvMenuMosaicGroupItems.setAdapter(mosaicGroupListViewAdapter);
        binding.rvMenuMosaicSkillItems.setAdapter(mosaicSkillListViewAdapter);

        binding.panelMenuMosaicGroupHeader.setOnClickListener(this::onMenuMosaicGroupHeaderClick);
        binding.panelMenuMosaicSkillHeader.setOnClickListener(this::onMenuMosaicSkillHeaderClick);

        binding.rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(this::onGlobalLayoutListener);

        viewModel.getMosaicGroupDS().addListener(onMosaicGroupDsPropertyChangedListener);
        viewModel.getMosaicSkillDS().addListener(onMosaicSkillDsPropertyChangedListener);
    }

    @Override
    public void onPause() {
        Logger.info("MainActivity.onPause: this.hash={0}", this.hashCode());
        super.onPause();
        activityStatus = EActivityStatus.ePaused;

        // unsubscribe all
        viewModel.getMosaicGroupDS().removeListener(onMosaicGroupDsPropertyChangedListener);
        viewModel.getMosaicSkillDS().removeListener(onMosaicSkillDsPropertyChangedListener);

        binding.rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this::onGlobalLayoutListener);

        binding.panelMenuMosaicGroupHeader.setOnClickListener(null);
        binding.panelMenuMosaicSkillHeader.setOnClickListener(null);

        binding.rvMenuMosaicGroupItems.setAdapter(null);
        binding.rvMenuMosaicSkillItems.setAdapter(null);
        mosaicGroupListViewAdapter.setOnItemClick(null);
        mosaicSkillListViewAdapter.setOnItemClick(null);
        mosaicGroupListViewAdapter = null;
        mosaicSkillListViewAdapter = null;
    }

    @Override
    protected void onStop() {
        Logger.info("MainActivity.onStop: this.hash={0}", this.hashCode());
        super.onStop();
        activityStatus = EActivityStatus.eStopped;
    }

    @Override
    protected void onDestroy() {
        Logger.info("MainActivity.onDestroy: this.hash={0}", this.hashCode());
        super.onDestroy();
        activityStatus = EActivityStatus.eDestroyed;
    }

    void onMenuMosaicGroupHeaderClick(View v) {
        if (isFailedStatus("onMenuMosaicGroupHeaderClick"))
            return;

        SmoothHelper.runColorSmoothTransition(viewModel.getMosaicGroupDS().getHeader().getEntity().getModel());

        if (binding.rvMenuMosaicGroupItems.getVisibility() == View.GONE) {
            SmoothHelper.applySmoothVisibilityOverScale(binding.rvMenuMosaicGroupItems, true, this::getLvGroupHeight, null);
            viewModel.getMosaicGroupDS().getHeader().getEntity().getBurgerMenuModel().setHorizontal(false);
        } else {
            boolean isSplitPaneOpen = !viewModel.getSplitViewPane().isOpen();
            getMenuSettings().setSplitPaneOpen(isSplitPaneOpen);
            viewModel.getSplitViewPane().setOpen(isSplitPaneOpen);
            viewModel.getMosaicGroupDS().getHeader().getEntity().getModel().setAnimeDirection(
                    !viewModel.getMosaicGroupDS().getHeader().getEntity().getModel().getAnimeDirection());
        }
    }

    double getLvGroupHeight() { return EMosaicGroup.values().length * (viewModel.getMosaicGroupDS().getImageSize().height + Cast.dpToPx(2) /* padding */); }
    double getLvSkillHeight() { return ESkillLevel .values().length * (viewModel.getMosaicSkillDS().getImageSize().height + Cast.dpToPx(2) /* padding */); }

    void onMenuMosaicSkillHeaderClick(View v) {
        if (isFailedStatus("onMenuMosaicSkillHeaderClick"))
            return;

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
        if (isFailedStatus("onMenuMosaicGroupItemClick"))
            return;

        viewModel.getMosaicGroupDS().setCurrentItemPos(position);
    }

    void onMenuMosaicSkillItemClick(View v, int position) {
        if (isFailedStatus("onMenuMosaicSkillItemClick"))
            return;

        viewModel.getMosaicSkillDS().setCurrentItemPos(position);
    }

    private void showSelectMosaicFragment() {
        Logger.info("MainActivity.showSelectMosaicFragment: this.hash={0}", this.hashCode());
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.rightFrame);
        if (fragment instanceof SelectMosaicFragment) {
            SelectMosaicFragment smf = (SelectMosaicFragment)fragment;
            smf.updateViewModel();
        } else {
            Bundle bundle = new Bundle();
            bundle.putDouble(BUNDLE_KEY__HEADER_SIZE_HEIGHT, viewModel.getMosaicGroupDS().getHeader().getSize().height + Cast.dpToPx((float)viewModel.getMenuGroupPaddingInDip().dip));
            SelectMosaicFragment smf = new SelectMosaicFragment();
            smf.setArguments(bundle);

            // Execute a transaction, replacing any existing fragment with this one inside the frame.
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.rightFrame, smf);
            if (fragment != null)
                ft.detach(fragment);
            ft.addToBackStack(null);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
        }
    }

    private void showCustomSkillFragment() {
        Logger.info("TODO:  redirect to CustomSkillFragment...");
        Toast.makeText(this, "TODO:  redirect to CustomSkillFragment...", Toast.LENGTH_LONG).show();
    }

    private void showHypnosisLogoFragment() {
        Logger.info("TODO:  redirect to HypnosisLogoFragment...");
        Toast.makeText(this, "TODO:  redirect to HypnosisLogoFragment...", Toast.LENGTH_LONG).show();
    }

    private void onMenuCurrentItemChanged(boolean senderIsMosaicGroup, MosaicGroupDataItem currentGroupItem, MosaicSkillDataItem currentSkillItem) {
        if ((currentGroupItem == null) || (currentSkillItem == null)) {
            showHypnosisLogoFragment();
            return;
        }

        getInitData().setMosaicGroup(currentGroupItem.getMosaicGroup());
        if (currentSkillItem.getSkillLevel() != ESkillLevel.eCustom)
            getInitData().setSkillLevel(currentSkillItem.getSkillLevel());

        if (!senderIsMosaicGroup && (currentSkillItem.getSkillLevel() == ESkillLevel.eCustom)) {
            showCustomSkillFragment();
        } else {
            //Logger.info("> MainActivity.onMenuCurrentItemChanged: " + currentGroupItem.getMosaicGroup());
            showSelectMosaicFragment();
        }
    }

    private void onMosaicGroupDsPropertyChanged(PropertyChangeEvent ev) {
        if (isFailedStatus("onMosaicGroupDsPropertyChanged"))
            return;

        //Logger.info("> MainActivity.onMosaicGroupDsPropertyChanged: ev.Name=" + ev.getPropertyName());
        switch (ev.getPropertyName()) {
        case MosaicGroupDataSource.PROPERTY_CURRENT_ITEM_POS:
//            Logger.info("  MainActivity.onMosaicGroupDsPropertyChanged: ev=" + ev);
            int oldPos = (Integer)ev.getOldValue();
            int newPos = (Integer)ev.getNewValue();

//            // Below line is just like a safety check, because sometimes holder could be null,
//            // in that case, getAdapterPosition() will return RecyclerView.NO_POSITION
//            if (newPos == RecyclerView.NO_POSITION)
//                return;

            // Updating old as well as new positions
            if (mosaicGroupListViewAdapter != null) {
                mosaicGroupListViewAdapter.notifyItemChanged(oldPos);
                mosaicGroupListViewAdapter.notifyItemChanged(newPos);
            }
            break;
        case MosaicGroupDataSource.PROPERTY_CURRENT_ITEM:
            MosaicGroupDataItem currentGroupItem = ((MosaicGroupDataSource)ev.getSource()).getCurrentItem();
            onMenuCurrentItemChanged(true, currentGroupItem, viewModel.getMosaicSkillDS().getCurrentItem());
            break;
        }
    }

    private void onMosaicSkillDsPropertyChanged(PropertyChangeEvent ev) {
        if (isFailedStatus("onMosaicSkillDsPropertyChanged"))
            return;

        //Logger.info("> MainActivity.onMosaicSkillDsPropertyChanged: ev.Name=" + ev.getPropertyName());
        switch (ev.getPropertyName()) {
        case MosaicSkillDataSource.PROPERTY_CURRENT_ITEM_POS:
            //Logger.info("  MainActivity.onMosaicSkillDsPropertyChanged: ev=" + ev);
            int oldPos = (Integer)ev.getOldValue();
            int newPos = (Integer)ev.getNewValue();

//            // Below line is just like a safety check, because sometimes holder could be null,
//            // in that case, getAdapterPosition() will return RecyclerView.NO_POSITION
//            if (newPos == RecyclerView.NO_POSITION)
//                return;

            // Updating old as well as new positions
            if (mosaicSkillListViewAdapter != null) {
                mosaicSkillListViewAdapter.notifyItemChanged(oldPos);
                mosaicSkillListViewAdapter.notifyItemChanged(newPos);
            }
            break;
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
//        Logger.info("> MainActivity.onActivitySizeChanged: newSize={0}", newSize);
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

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.rightFrame);
        if (fragment instanceof SelectMosaicFragment) {
            SelectMosaicFragment smf = (SelectMosaicFragment)fragment;
            smf.updateHeader(sizeHeader.height + Cast.dpToPx((float)viewModel.getMenuGroupPaddingInDip().dip));
        }

//        Logger.info("< MainActivity.onActivitySizeChanged: " +
//                "sizeItem={0}, " +
//                "sizeHeader={1}/{4}, " +
//                "padHeader={2}, " +
//                "padBurger={3}",
//                sizeItem, sizeHeader, padHeader, padBurger,
//                viewModel.getMosaicGroupDS().getHeader().getSize()
//        );
    }

    public static Color getBackgroundHeaderColor() {
        return Color.LightSeaGreen().updateA(140);
    }

    private boolean isFailedStatus(String methodName) {
        if (activityStatus.isRunning())
            return false;

        Logger.info("MainActivity.{0}: this.hash={1}; failed activityStatus={2}", methodName, this.hashCode(), activityStatus);
        return true;
    }

}
