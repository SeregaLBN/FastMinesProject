package fmg.android.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.TimeUnit;

import fmg.android.app.databinding.SelectMosaicFragmentBinding;
import fmg.android.app.model.dataSource.MosaicDataSource;
import fmg.android.app.model.items.MosaicDataItem;
import fmg.android.app.presentation.MosaicDsViewModel;
import fmg.android.app.recyclerView.MosaicListViewAdapter;
import fmg.android.app.recyclerView.RecyclerItemDoubleClickListener;
import fmg.android.img.Logo;
import fmg.android.utils.AsyncRunner;
import fmg.android.utils.Cast;
import fmg.common.Color;
import fmg.common.HSV;
import fmg.common.Logger;
import fmg.common.geom.Size;
import fmg.common.geom.SizeDouble;
import fmg.common.ui.UiInvoker;
import fmg.core.img.AnimatedImageModel;
import fmg.core.img.LogoModel;
import fmg.core.types.EMosaic;
import fmg.core.types.ESkillLevel;
import fmg.core.app.model.MosaicInitData;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class SelectMosaicFragment extends Fragment {

    private SelectMosaicFragmentBinding binding;
    /** View-Model */
    private MosaicDsViewModel viewModel;
    private MosaicListViewAdapter mosaicListViewAdapter;
    private RecyclerItemDoubleClickListener recyclerItemDoubleClickListener;
    private GridLayoutManager gridLayoutManager;
    private Subject<Size> subjSizeChanged;
    private Disposable sizeChangedObservable;
    private Size cachedSize = new Size(-1, -1);
    private boolean rotateBkColorOfGameBttn = true;
    private static final double TileMinSize = Cast.dpToPx(30);
    private static final double TileMaxSize = Cast.dpToPx(90);
    private final PropertyChangeListener onMosaicDsPropertyChangedListener = this::onMosaicDsPropertyChanged;

    public MosaicInitData getInitData() { return FastMinesApp.get().getMosaicInitData(); }
    //public void setInitData(MosaicInitData initData) { MosaicInitDataExt.getSharedData().copyFrom(initData); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.select_mosaic_fragment, container, false);

        binding = DataBindingUtil.inflate(inflater, R.layout.select_mosaic_fragment, container, false);
        viewModel = ViewModelProviders.of(this).get(MosaicDsViewModel.class);
        updateViewModel();

        binding.setViewModel(viewModel);
        binding.executePendingBindings();

        gridLayoutManager = new GridLayoutManager(this.getContext(), 2);
        binding.rvMosaicItems.setLayoutManager(gridLayoutManager);

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
            HSV hsv = new HSV(AnimatedImageModel.DEFAULT_FOREGROUND_COLOR);
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
                Logger.info("SelectMosaicFragment::onResume: AsyncRunner.Repeat: {0}", ex);
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
//                        Logger.info("  SelectMosaicFragment::onGlobalLayoutListener: Debounce: onNext: ev=" + ev);
                        UiInvoker.DEFERRED.accept(() -> onFragmentSizeChanged(ev));
                    }, ex -> {
                        Logger.info("  SelectMosaicFragment: sizeChangedObservable: Debounce: onError: " + ex);
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
//        Logger.info("> SelectMosaicFragment::onFragmentSizeChanged: newSize={0}", newSize);

//        int size = Math.min(newSize.height, newSize.width);
//        double size2 = size / 3.9;
//        double wh = Math.min(Math.max(TileMinSize, size2), TileMaxSize);
////        Logger.info("Math.min(Math.max(TileMinSize={0}, size2={1}), TileMaxSize={2}) = {3}", TileMinSize, size2, TileMaxSize, wh);
//        viewModel.setImageSize(new SizeDouble(wh, wh));


        float minTileWidth = Cast.dpToPx(48);
        float maxTileWidth = Cast.dpToPx(ProjSettings.isMobile ? 90 : 140);
        double cardViewItemBorderWidth =
              //2 * Cast.dpToPx(0)  // mosaic_item.xml: <<android.support.v7.widget.CardView <LinearLayout android:layout_margin="0dp"
                2 * Cast.dpToPx(1); // mosaic_item.xml: <<android.support.v7.widget.CardView <LinearLayout <ImageView android:layout_margin="1dp"
        double widthBetweenItems = 2 * Cast.dpToPx(8); // mosaic_item.xml: <android.support.v7.widget.CardView android:layout_margin="8dp"

        double fragmentBorderWidth = 0;
              //2 * Cast.dpToPx(0)  // select_mosaic_fragment.xml: <LinearLayout android:layout_margin="0dp"
              //2 * Cast.dpToPx(0); // select_mosaic_fragment.xml: <LinearLayout <RelativeLayout <androidx.recyclerview.widget.RecyclerView android:layout_margin="0dp"

        double size = newSize.width; // Math.Min(newSize.width, newSize.height);
        double spaceToItems = size - fragmentBorderWidth;

        int rows = 1;
        for (; rows <= EMosaic.values().length; ++rows) {
            double size2 = maxTileWidth * rows + (rows - 0/* ! */) * widthBetweenItems;
            if (size2 > spaceToItems)
                break;
        }

        double spaceToItemsClear = spaceToItems - (rows - 1) * widthBetweenItems;
        double tileWidth = spaceToItemsClear / rows;
        double tileWidth2 = Math.min(Math.max(tileWidth, minTileWidth), maxTileWidth);
        double imageSize = tileWidth2 - cardViewItemBorderWidth;
        //Logger.info("tileWidth={0}, tileWidth2={1}, imageSize={2}", tileWidth, tileWidth2, imageSize);
        viewModel.setImageSize(new SizeDouble(imageSize, imageSize));
//        gridLayoutManager.setSpanCount(rows);


//        Logger.info("< SelectMosaicFragment::onFragmentSizeChanged: imageSize={0}", wh);

//        mosaicListViewAdapter.notifyItemRangeChanged(0, viewModel.getMosaicDS().getDataSource().size());
    }


    void onMosaicHeaderClick(View v) { }

    void onMosaicItemClick(View v, int position) {
        //Toast.makeText(this.getContext(), "onMosaicItemClick " + position, Toast.LENGTH_LONG).show();

        viewModel.getMosaicDS().setCurrentItemPos(position); // change current item before call listener

        EMosaic selectedMosaic = getInitData().getMosaicGroup().getMosaics().get(position);
        getInitData().setMosaicType(selectedMosaic);
    }

    private void onMosaicItemDoubleClick(View view, int position) {
        //Logger.info("> SelectMosaicFragment::onMosaicItemDoubleClick");
        StartNewGame();
    }

    private void onMosaicItemLongClick(View view, int position) {
        //Logger.info("> SelectMosaicFragment::onMosaicItemLongClick");
        StartNewGame();
    }

    private void onClickBttnBeginGame(View v) {
        //Logger.info("> SelectMosaicFragment::onClickBttnBeginGame");
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
                //Logger.info("  MenuMosaicListViewAdapter::onMosaicDsPropertyChanged: ev=" + ev);
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
