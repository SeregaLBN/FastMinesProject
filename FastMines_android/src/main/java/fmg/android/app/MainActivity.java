package fmg.android.app;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import fmg.android.app.databinding.DemoActivityBinding;
import fmg.android.app.databinding.MainActivityBinding;
import fmg.android.app.model.dataSource.MosaicGroupDataSource;
import fmg.android.app.model.items.MosaicGroupDataItem;
import fmg.android.app.presentation.MainMenuViewModel;

public class MainActivity extends AppCompatActivity {

    private MainActivityBinding activityBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityBinding = DataBindingUtil.setContentView(this, R.layout.main_activity);
        MainMenuViewModel viewModel = ViewModelProviders.of(this).get(MainMenuViewModel.class);
        activityBinding.setViewModel(viewModel);
        activityBinding.executePendingBindings();

//        Intent intent = new Intent(this, DemoActivity.class);
//        startActivity(intent);
    }

}
