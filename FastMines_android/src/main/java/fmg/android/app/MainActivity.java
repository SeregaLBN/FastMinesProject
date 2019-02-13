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

public class MainActivity extends AppCompatActivity {

    private MainActivityBinding activityBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityBinding = DataBindingUtil.setContentView(this, R.layout.main_activity);
        MosaicGroupDataSource mosaicGroupDS = ViewModelProviders.of(this).get(MosaicGroupDataSource.class);
        activityBinding.setMosaicGroupDS(mosaicGroupDS);
        activityBinding.executePendingBindings();

//        Intent intent = new Intent(this, DemoActivity.class);
//        startActivity(intent);
    }

}
