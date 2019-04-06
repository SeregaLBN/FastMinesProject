package fmg.android.app;

import android.arch.lifecycle.ViewModelProviders;
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
import java.util.concurrent.TimeUnit;

import fmg.android.app.databinding.SelectMosaicFragmentBinding;
import fmg.android.app.model.MosaicInitDataExt;
import fmg.android.app.model.dataSource.MosaicDataSource;
import fmg.android.app.model.items.MosaicDataItem;
import fmg.android.app.presentation.MosaicsViewModel;
import fmg.android.utils.Cast;
import fmg.common.LoggerSimple;
import fmg.common.geom.Size;
import fmg.common.geom.SizeDouble;
import fmg.common.ui.UiInvoker;
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
    private Subject<Size> subjSizeChanged;
    private Disposable sizeChangedObservable;
    private Size cachedSize = new Size(-1, -1);
    private static final double TileMinSize = Cast.dpToPx(30);
    private static final double TileMaxSize = Cast.dpToPx(90);

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
        mosaicListViewAdapter = new MosaicListViewAdapter(viewModel.getMosaicDS().getDataSource(), this::onMosaicItemClick);
        binding.rvMosaicItems.setAdapter(mosaicListViewAdapter);

        binding.panelMosaicHeader.setOnClickListener(this::onMosaicHeaderClick);

        binding.rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(this::onGlobalLayoutListener);
        viewModel.getMosaicDS().addListener(this::onMosaicDsPropertyChanged);

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        viewModel = ViewModelProviders.of(this).get(MosaicsViewModel.class);
        // TODO: Use the ViewModel
    }

    @Override
    public void onDestroy() {
        viewModel.getMosaicDS().removeListener(this::onMosaicDsPropertyChanged);
        binding.rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this::onGlobalLayoutListener);
        if (sizeChangedObservable != null)
            sizeChangedObservable.dispose();
        super.onDestroy();
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

        //onFragmentSizeChanged(newSize);
        if (sizeChangedObservable == null) {
            subjSizeChanged = PublishSubject.create();
            sizeChangedObservable = subjSizeChanged.debounce(200, TimeUnit.MILLISECONDS)
                    .subscribe(ev -> {
//                        LoggerSimple.put("  SelectMosaicFragment::onGlobalLayoutListener: Debounce: onNext: ev=" + ev);
                        UiInvoker.DEFERRED.accept(() -> onFragmentSizeChanged(ev));
                    }, ex -> {
                        LoggerSimple.put("  SelectMosaicFragment::onGlobalLayoutListener: Debounce: onError: " + ex);
                    });
        }

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


    void onMosaicHeaderClick(View v) {
    }

    void onMosaicItemClick(View v, int position) {
        Toast.makeText(this.getContext(), "onMosaicItemClick " + position, Toast.LENGTH_LONG).show();

        viewModel.getMosaicDS().setCurrentItemPos(position); // change current item before call listener

        EMosaic selectedMosaic = getInitData().getMosaicGroup().getMosaics().get(position);
        getInitData().setMosaicType(selectedMosaic);
    }

    // TODO bind to double click
    private void onMosaicItemDoubleClick(/*DoubleTappedRoutedEventArgs e*/) {
        StartNewGame();
    }

    // TODO bind to button
    private void onClickBttnStartGame(/*object sender, RoutedEventArgs ev*/) {
        //LoggerSimple.put("> SelectMosaicFragment::OnClickBttnStartGame");
        StartNewGame();
    }

    private void StartNewGame() {
//        //Frame frame = this.Frame;
//        Frame frame = Window.Current.Content as Frame;
//        System.Diagnostics.Debug.Assert(frame != null);
//
//        var eMosaic = CurrentItem.MosaicType;
//        frame.Navigate(typeof(MosaicPage), InitData);
//
//        //Window.Current.Content = new MosaicPage();
//        //// Ensure the current window is active
//        //Window.Current.Activate();
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
                mosaicListViewAdapter.notifyItemChanged(oldPos);
                mosaicListViewAdapter.notifyItemChanged(newPos);
            }
            break;
        case MosaicDataSource.PROPERTY_DATA_SOURCE:
            mosaicListViewAdapter.updateItems(viewModel.getMosaicDS().getDataSource());
//            mosaicListViewAdapter.notifyDataSetChanged();
            break;
        }
    }

}
