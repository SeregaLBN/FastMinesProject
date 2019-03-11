package fmg.android.app;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.beans.PropertyChangeEvent;
import java.util.concurrent.TimeUnit;

import fmg.android.app.databinding.SelectMosaicFragmentBinding;
import fmg.android.app.presentation.MainMenuViewModel;
import fmg.android.app.presentation.MosaicsViewModel;
import fmg.common.LoggerSimple;
import fmg.common.geom.SizeDouble;
import fmg.common.ui.Factory;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class SelectMosaicFragment extends Fragment {

    private SelectMosaicFragmentBinding binding;
    private MosaicsViewModel viewModel;
    private MosaicListViewAdapter mosaicListViewAdapter;
    private Subject<SizeDouble> subjSizeChanged;
    private Disposable sizeChangedObservable;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.select_mosaic_fragment, container, false);

        binding = DataBindingUtil.inflate(inflater, R.layout.select_mosaic_fragment, container, false);
        viewModel = ViewModelProviders.of(this).get(MosaicsViewModel.class);
        binding.setViewModel(viewModel);
        binding.executePendingBindings();

        binding.rvMosaicItems.setLayoutManager(new GridLayoutManager(this.getContext(), 2));
        binding.rvMosaicItems.setAdapter(mosaicListViewAdapter = new MosaicListViewAdapter(viewModel.getMosaicDS(), this::onMosaicItemClick));

        binding.panelMosaicHeader.setOnClickListener(this::onMosaicHeaderClick);

        binding.rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(this::onGlobalLayoutListener);

        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        viewModel = ViewModelProviders.of(this).get(MosaicsViewModel.class);
        // TODO: Use the ViewModel
    }

    @Override
    public void onDestroy() {
        binding.rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this::onGlobalLayoutListener);
        mosaicListViewAdapter.close();
        if (sizeChangedObservable != null)
            sizeChangedObservable.dispose();
        super.onDestroy();
    }

    void onMosaicHeaderClick(View v) {
    }

    void onMosaicItemClick(View v, int position) {
        Toast.makeText(this.getContext(), "onMosaicItemClick " + position, Toast.LENGTH_LONG).show();
    }


    private void onGlobalLayoutListener() {
        SizeDouble newSize = new SizeDouble(binding.rootLayout.getWidth(), binding.rootLayout.getHeight());

        //onFragmentSizeChanged(newSize);
        if (sizeChangedObservable == null) {
            subjSizeChanged = PublishSubject.create();
            sizeChangedObservable = subjSizeChanged.debounce(200, TimeUnit.MILLISECONDS)
                    .subscribe(ev -> {
//                        LoggerSimple.put("  SelectMosaicFragment::onGlobalLayoutListener: Debounce: onNext: ev=" + ev);
                        Factory.DEFERR_INVOKER.accept(() -> onFragmentSizeChanged(ev));
                    }, ex -> {
                        LoggerSimple.put("  SelectMosaicFragment::onGlobalLayoutListener: Debounce: onError: " + ex);
                    });
        }

        subjSizeChanged.onNext(newSize);
    }

    private void onFragmentSizeChanged(SizeDouble newSize) {
//        LoggerSimple.put("> SelectMosaicFragment::onFragmentSizeChanged: newSize={0}", newSize);
    }

}
