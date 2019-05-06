package fmg.android.app;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.TimeUnit;

import fmg.android.app.databinding.SelectMosaicFragmentBinding;
import fmg.android.app.model.MosaicInitDataExt;
import fmg.android.app.model.dataSource.MosaicDataSource;
import fmg.android.app.model.items.MosaicDataItem;
import fmg.android.app.presentation.MosaicsViewModel;
import fmg.android.app.recyclerView.MosaicListViewAdapter;
import fmg.android.app.recyclerView.RecyclerItemDoubleClickListener;
import fmg.android.img.Logo;
import fmg.android.utils.AsyncRunner;
import fmg.android.utils.Cast;
import fmg.common.Color;
import fmg.common.HSV;
import fmg.common.LoggerSimple;
import fmg.common.geom.Size;
import fmg.common.geom.SizeDouble;
import fmg.common.ui.UiInvoker;
import fmg.core.img.AnimatedImageModel;
import fmg.core.img.LogoModel;
import fmg.core.mosaic.MosaicInitData;
import fmg.core.types.EMosaic;
import fmg.core.types.ESkillLevel;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class SelectMosaicFragment extends Fragment {

    private SelectMosaicFragmentBinding binding;
    /** View-Model */
    private MosaicsViewModel viewModel;
    private MosaicListViewAdapter mosaicListViewAdapter;
    private RecyclerItemDoubleClickListener recyclerItemDoubleClickListener;
    private Subject<Size> subjSizeChanged;
    private Disposable sizeChangedObservable;
    private Size cachedSize = new Size(-1, -1);
    private boolean rotateBkColorOfGameBttn = true;
    private static final double TileMinSize = Cast.dpToPx(30);
    private static final double TileMaxSize = Cast.dpToPx(90);
    private final PropertyChangeListener onMosaicDsPropertyChangedListener = this::onMosaicDsPropertyChanged;

    public MosaicInitData getInitData() { return MosaicInitDataExt.getSharedData(); }
    //public void setInitData(MosaicInitData initData) { MosaicInitDataExt.getSharedData().copyFrom(initData); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.select_mosaic_fragment, container, false);

        binding = DataBindingUtil.inflate(inflater, R.layout.select_mosaic_fragment, container, false);
        viewModel = ViewModelProviders.of(this).get(MosaicsViewModel.class);
        updateViewModel();

        binding.setViewModel(viewModel);
        binding.executePendingBindings();

        binding.rvMosaicItems.setLayoutManager(new GridLayoutManager(this.getContext(), 2));

        { // setup header
            Logo.BitmapController logoController = viewModel.getMosaicDS().getHeader().getEntity();
            logoController.usePolarLightFgTransforming(true);
            LogoModel logoModel = logoController.getModel();
            logoModel.setRotateMode(LogoModel.ERotateMode.classic);
            logoModel.setAnimatePeriod(30000);
            logoModel.setTotalFrames(700);
            logoModel.setUseGradient(true);
            logoModel.setAnimated(true);
            logoModel.setBorderWidth(1);
            logoModel.setBorderColor(Color.BlueViolet());

            binding.panelMosaicHeader.setBackgroundColor(Cast.toColor(MainActivity.getBackgroundHeaderColor()));

            Double headerSizeHeight = getArguments().getDouble(MainActivity.BUNDLE_KEY__HEADER_SIZE_HEIGHT);
            if (headerSizeHeight != null)
                updateHeader(headerSizeHeight);
        }

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        // subscribe all
        rotateBkColorOfGameBttn = true;
        {
            HSV hsv = new HSV(AnimatedImageModel.DefaultForegroundColor);
            hsv.s = 80;
            hsv.v = 70;
            hsv.a = 170;

            Runnable run = () -> {
                if (viewModel.getMosaicDS().getCurrentItemPos() < 0)
                    return;
                hsv.h += 10;
                binding.layoutBttnStartGame.setBackgroundColor(Cast.toColor(hsv.toColor()));
            };
            try {
                AsyncRunner.Repeat(run, 100, () -> !rotateBkColorOfGameBttn);
            } catch (Exception ex) {
                LoggerSimple.put("SelectMosaicFragment::onResume: AsyncRunner.Repeat: {0}", ex);
            }
        }

        mosaicListViewAdapter = new MosaicListViewAdapter(viewModel.getMosaicDS().getDataSource());
        mosaicListViewAdapter.setOnItemClick(this::onMosaicItemClick);
        mosaicListViewAdapter.setOnItemLongClick(this::onMosaicItemLongClick);
        binding.rvMosaicItems.setAdapter(mosaicListViewAdapter);

        recyclerItemDoubleClickListener = new RecyclerItemDoubleClickListener(this.getContext());
        recyclerItemDoubleClickListener.setOnItemDoubleClick(this::onMosaicItemDoubleClick);
        binding.rvMosaicItems.addOnItemTouchListener(recyclerItemDoubleClickListener);

        binding.panelMosaicHeader.setOnClickListener(this::onMosaicHeaderClick);
        binding.bttnBeginGame.setOnClickListener(this::onClickBttnBeginGame);
        viewModel.getMosaicDS().addListener(onMosaicDsPropertyChangedListener);

        { // onFragmentSizeChanged(newSize);
            subjSizeChanged = PublishSubject.create();
            sizeChangedObservable = subjSizeChanged.debounce(200, TimeUnit.MILLISECONDS)
                    .subscribe(ev -> {
//                        LoggerSimple.put("  SelectMosaicFragment::onGlobalLayoutListener: Debounce: onNext: ev=" + ev);
                        UiInvoker.DEFERRED.accept(() -> onFragmentSizeChanged(ev));
                    }, ex -> {
                        LoggerSimple.put("  SelectMosaicFragment: sizeChangedObservable: Debounce: onError: " + ex);
                    });
            binding.rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(this::onGlobalLayoutListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // unsubscribe all
        rotateBkColorOfGameBttn = false;

        mosaicListViewAdapter.setOnItemClick(null);
        mosaicListViewAdapter.setOnItemLongClick(null);
        mosaicListViewAdapter = null;
        binding.rvMosaicItems.setAdapter(null);

        binding.rvMosaicItems.removeOnItemTouchListener(recyclerItemDoubleClickListener);
        recyclerItemDoubleClickListener.setOnItemDoubleClick(null);
        recyclerItemDoubleClickListener = null;

        binding.panelMosaicHeader.setOnClickListener(null);
        binding.bttnBeginGame.setOnClickListener(null);
        viewModel.getMosaicDS().removeListener(onMosaicDsPropertyChangedListener);

        binding.rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this::onGlobalLayoutListener);
        sizeChangedObservable.dispose();
        subjSizeChanged = null;
    }

    private void onGlobalLayoutListener() {
        int w = binding.rootLayout.getWidth();
        int h = binding.rootLayout.getHeight();
        if ((w <= 0) || (h <= 0))
            return;

        Size newSize = new Size(w, h);

        if (cachedSize.equals(newSize))
            return;
        cachedSize = newSize;
        subjSizeChanged.onNext(newSize);
    }

    private void onFragmentSizeChanged(Size newSize) {
//        LoggerSimple.put("> SelectMosaicFragment::onFragmentSizeChanged: newSize={0}", newSize);

        int size = Math.min(newSize.height, newSize.width);
        double size2 = size / 3.9;
        double wh = Math.min(Math.max(TileMinSize, size2), TileMaxSize);
//        LoggerSimple.put("Math.min(Math.max(TileMinSize={0}, size2={1}), TileMaxSize={2}) = {3}", TileMinSize, size2, TileMaxSize, wh);
        viewModel.setImageSize(new SizeDouble(wh, wh));
//        LoggerSimple.put("< SelectMosaicFragment::onFragmentSizeChanged: imageSize={0}", wh);

//        mosaicListViewAdapter.notifyItemRangeChanged(0, viewModel.getMosaicDS().getDataSource().size());
    }


    void onMosaicHeaderClick(View v) { }

    void onMosaicItemClick(View v, int position) {
        Toast.makeText(this.getContext(), "onMosaicItemClick " + position, Toast.LENGTH_LONG).show();

        viewModel.getMosaicDS().setCurrentItemPos(position); // change current item before call listener

        EMosaic selectedMosaic = getInitData().getMosaicGroup().getMosaics().get(position);
        getInitData().setMosaicType(selectedMosaic);
    }

    private void onMosaicItemDoubleClick(View view, int position) {
        //LoggerSimple.put("> SelectMosaicFragment::onMosaicItemDoubleClick");
        StartNewGame();
    }

    private void onMosaicItemLongClick(View view, int position) {
        //LoggerSimple.put("> SelectMosaicFragment::onMosaicItemLongClick");
        StartNewGame();
    }

    private void onClickBttnBeginGame(View v) {
        //LoggerSimple.put("> SelectMosaicFragment::onClickBttnBeginGame");
        StartNewGame();
    }

    private void StartNewGame() {
        Intent intent = new Intent(this.getContext(), MosaicActivity.class);
        startActivity(intent);
    }

    public void updateHeader(double headerSizeHeight) {
        viewModel.getMosaicDS().getHeader().setSize(new SizeDouble(headerSizeHeight, headerSizeHeight));
    }

    public void updateViewModel() {
        ESkillLevel skill = getInitData().getSkillLevel();
        EMosaic mosaicType = getInitData().getMosaicType();
        viewModel.getMosaicDS().setSkillLevel(skill);
        viewModel.getMosaicDS().setMosaicGroup(mosaicType.getGroup());
        MosaicDataItem newItem = viewModel.getMosaicDS().getDataSource().stream().filter(x -> x.getMosaicType() == mosaicType).findAny().get();
        viewModel.getMosaicDS().setCurrentItem(newItem);
    }

    private void onMosaicDsPropertyChanged(PropertyChangeEvent ev) {
        switch(ev.getPropertyName()) {
        case MosaicDataSource.PROPERTY_CURRENT_ITEM_POS: {
                //LoggerSimple.put("  MenuMosaicListViewAdapter::onMosaicDsPropertyChanged: ev=" + ev);
                int oldPos = (Integer) ev.getOldValue();
                int newPos = (Integer) ev.getNewValue();

    //            // Below line is just like a safety check, because sometimes holder could be null,
    //            // in that case, getAdapterPosition() will return RecyclerView.NO_POSITION
    //            if (newPos == RecyclerView.NO_POSITION)
    //                return;

                // Updating old as well as new positions
                if (mosaicListViewAdapter != null) {
                    mosaicListViewAdapter.notifyItemChanged(oldPos);
                    mosaicListViewAdapter.notifyItemChanged(newPos);
                }
            }
            break;
        case MosaicDataSource.PROPERTY_DATA_SOURCE:
            if (mosaicListViewAdapter != null) {
                mosaicListViewAdapter.updateItems(viewModel.getMosaicDS().getDataSource());
//                mosaicListViewAdapter.notifyDataSetChanged();
            }
            break;
        }
    }

}
