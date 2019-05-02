package fmg.android.app;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.android.app.databinding.DemoActivityBinding;
import fmg.android.app.databinding.MosaicActivityBinding;
import fmg.android.img.Flag;
import fmg.android.img.Logo;
import fmg.android.img.Mine;
import fmg.android.img.MosaicGroupImg;
import fmg.android.img.MosaicImg;
import fmg.android.img.MosaicSkillImg;
import fmg.android.img.Smile;
import fmg.android.mosaic.MosaicViewController;
import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.img.IImageController;
import fmg.core.img.SmileModel;
import fmg.core.img.TestDrawing;
import fmg.core.mosaic.MosaicView;
import fmg.core.types.EMosaic;
import fmg.core.types.EMosaicGroup;
import fmg.core.types.ESkillLevel;

/** general activity of project */
public class MosaicActivity extends AppCompatActivity {

    private MosaicViewController mosaicController;
    private MosaicActivityBinding binding;

    /** Mosaic controller */
    public MosaicViewController getMosaicController() {
        if (mosaicController == null)
            setMosaicController(new MosaicViewController(this));
        return mosaicController;
    }
    private void setMosaicController(MosaicViewController mosaicController) {
        if (this.mosaicController != null) {
            this.mosaicController.addListener(this::onMosaicControllerPropertyChanged);
            this.mosaicController.close();
        }
        this.mosaicController = mosaicController;
        if (this.mosaicController != null) {
            this.mosaicController.removeListener(this::onMosaicControllerPropertyChanged);
        }
    }

    private void onMosaicControllerPropertyChanged(PropertyChangeEvent ev) {
    }

}
