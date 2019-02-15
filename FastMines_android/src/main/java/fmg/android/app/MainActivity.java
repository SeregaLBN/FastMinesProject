package fmg.android.app;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;

import fmg.android.app.databinding.MainActivityBinding;
import fmg.android.app.presentation.MainMenuViewModel;
import fmg.android.utils.StaticInitializer;
import fmg.common.LoggerSimple;

public class MainActivity extends AppCompatActivity {

    private MainActivityBinding binding;
    private MainMenuViewModel viewModel;

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

        binding.mosaicGroupItems.setLayoutManager(new LinearLayoutManager(this));
        binding.mosaicGroupItems.setAdapter(new MenuMosaicGroupListViewAdapter(viewModel.getMosaicGroupDS().getDataSource(), this::onMenuMosaicGroupItemClick));


//        Intent intent = new Intent(this, DemoActivity.class);
//        startActivity(intent);
    }

    void onMenuMosaicGroupItemClick(View view, int position) {
        LoggerSimple.put("  onMenuMosaicGroupItemClick: pos={0}", position);
        viewModel.getMosaicGroupDS().setCurrentItemPos(position);
    }

    @BindingAdapter("android:imageBitmap")
    public static void loadImage(ImageView iv, Bitmap bitmap) {
        iv.setImageBitmap(bitmap);
    }

}
