package fmg.android.app;

import android.arch.lifecycle.ViewModelProviders;
import android.content.res.Resources;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import fmg.android.app.databinding.MainActivityBinding;
import fmg.android.app.presentation.MainMenuViewModel;
import fmg.android.utils.StaticInitializer;
import fmg.common.LoggerSimple;

public class MainActivity extends AppCompatActivity {

    private MainActivityBinding binding;
    private MainMenuViewModel viewModel;
    private MenuMosaicGroupListViewAdapter menuMosaicGroupListViewAdapter;

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
        binding.mosaicGroupItems.setAdapter(menuMosaicGroupListViewAdapter = new MenuMosaicGroupListViewAdapter(viewModel.getMosaicGroupDS(), this::onMenuMosaicGroupItemClick));


//        Intent intent = new Intent(this, DemoActivity.class);
//        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        menuMosaicGroupListViewAdapter.close();
        super.onDestroy();
    }

    void onMenuMosaicGroupItemClick(View view, int position) {
    }

    @BindingAdapter("android:imageBitmap")
    public static void loadImage(ImageView iv, Bitmap bitmap) {
        iv.setImageBitmap(bitmap);
    }

    @BindingAdapter("headerImage")
    public static void headerImage(Button bttn, Bitmap bitmap) {
        Drawable img = new BitmapDrawable(Resources.getSystem(), bitmap);

//        bttn.setBackground(img);

//        img.setBounds(0, 0, 60, 60);
//        bttn.setCompoundDrawables(img, null, null, null);

        bttn.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
    }

}
