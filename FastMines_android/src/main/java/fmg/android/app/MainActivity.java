package fmg.android.app;

import android.arch.lifecycle.ViewModelProviders;
import android.content.res.Resources;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.databinding.ObservableField;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import java.util.concurrent.ThreadLocalRandom;

import fmg.android.app.databinding.MainActivityBinding;
import fmg.android.app.presentation.MainMenuViewModel;
import fmg.android.utils.StaticInitializer;
import fmg.common.LoggerSimple;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.SizeDouble;

public class MainActivity extends AppCompatActivity {

    public static final int MenuTextWidth = 110;
    public static boolean MenuIsFullWidth = true;

    private MainActivityBinding binding;
    private MainMenuViewModel viewModel;
    private MenuMosaicGroupListViewAdapter menuMosaicGroupListViewAdapter;

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
        binding.rvMosaicGroupItems.setAdapter(menuMosaicGroupListViewAdapter = new MenuMosaicGroupListViewAdapter(viewModel.getMosaicGroupDS(), this::onMenuMosaicGroupItemClick));

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
        // does something very interesting
        int s = 100 + ThreadLocalRandom.current().nextInt(100);
        viewModel.getMosaicGroupDS().setImageSize(new SizeDouble(s, s));
    }

    void onMenuMosaicGroupItemClick(View view, int position) {
    }

    private void onGlobalLayoutListener() {
        SizeDouble newSize = new SizeDouble(binding.rootLayout.getWidth(), binding.rootLayout.getHeight());
        if (cachedSizeActivity.equals(newSize))
            return;

        onActivitySizeChanged(cachedSizeActivity = newSize);
    }

    private void onActivitySizeChanged(SizeDouble newSize) {
        LoggerSimple.put("> MainActivity::onActivitySizeChanged: newSize={0}", newSize);
        final int minSize = 50;
        final int topElemHeight = 48;
        final int pad = 3;
        assert (topElemHeight <= minSize);

        double size = Math.min(newSize.height, newSize.width);
        double size1 = size/7;
        double wh = Math.min(Math.max(minSize, size1), 100); // TODO: DPI dependency
        viewModel.getMosaicGroupDS().setImageSize(new SizeDouble(wh, wh));
        viewModel.getMosaicSkillDS().setImageSize(new SizeDouble(wh, wh));

        viewModel.getMosaicGroupDS().getHeader().setSize(new SizeDouble(wh, topElemHeight));
        viewModel.getMosaicSkillDS().getHeader().setSize(new SizeDouble(wh, topElemHeight));
        viewModel.getMosaicGroupDS().getHeader().setPadding(new BoundDouble(pad, pad, wh - topElemHeight + pad, pad)); // left margin
        viewModel.getMosaicSkillDS().getHeader().setPadding(new BoundDouble(pad, pad, wh - topElemHeight + pad, pad)); // left margin

        double whBurger = topElemHeight / 2 + Math.min(topElemHeight / 2 - pad, Math.max(0, (wh - 1.5 * topElemHeight)));
        BoundDouble padBurger = new BoundDouble(wh - whBurger, topElemHeight - whBurger, pad, pad);
        viewModel.getMosaicGroupDS().getHeader().setPaddingBurgerMenu(padBurger); // right-bottom margin
        viewModel.getMosaicSkillDS().getHeader().setPaddingBurgerMenu(padBurger); // right-bottom margin
    }

}
