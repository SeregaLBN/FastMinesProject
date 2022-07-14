package fmg.android.app;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.android.app.databinding.DemoActivity2Binding;
import fmg.android.img.Flag;
import fmg.android.img.Flag2;
import fmg.android.img.Logo;
import fmg.android.img.Logo2;
import fmg.android.img.Mine;
import fmg.android.img.MosaicGroupImg;
import fmg.android.img.MosaicGroupImg2;
import fmg.android.img.MosaicImg;
import fmg.android.img.MosaicSkillImg;
import fmg.android.img.MosaicSkillImg2;
import fmg.android.img.Smile;
import fmg.android.img.Smile2;
import fmg.android.mosaic.MosaicViewController;
import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.img.IImageController;
import fmg.core.img.IImageController2;
import fmg.core.img.SmileModel;
import fmg.core.img.SmileModel2;
import fmg.core.img.TestDrawing2;
import fmg.core.mosaic.MosaicView;
import fmg.core.types.EMosaic;
import fmg.core.types.EMosaicGroup;
import fmg.core.types.ESkillLevel;

/** live UI test application */
public class DemoActivity2 extends AppCompatActivity {

    private static final int MARGIN = 10; // panel margin - padding to inner images

    private TestDrawing2 td;
    private DemoActivity2Binding activityBinding;
    private Runnable onCloseImages;
    private Runnable[] onCreateImages; // images factory
    public static class DemoViewModel extends ViewModel {
        private List<IImageController2<?,?>> images;
        private int nextCreateImagesIndex;
    }


    // #region images Fabrica
//    public void testMosaicControl () {
//        MosaicView.DEBUG_DRAW_FLOW = true;
//        testApp(() -> {
//            MosaicViewController mosaicController = new MosaicViewController(this);
//
//            if (ThreadLocalRandom.current().nextBoolean()) {
//                // unmodified controller test
//            } else {
//                EMosaic mosaicType = EMosaic.fromOrdinal(ThreadLocalRandom.current().nextInt(EMosaic.values().length));
//                ESkillLevel skill  = ESkillLevel.eBeginner;
//
//                mosaicController.setMosaicType(mosaicType);
//                mosaicController.setSizeField(skill.getDefaultSize());
//                mosaicController.setCountMines(skill.getNumberMines(mosaicType));
//                mosaicController.gameNew();
//            }
//            return Stream.of(mosaicController);
//
//        }
//    );}
//    public void testMosaicImg     () { testApp(() ->
//                                                   //// test single
//                                                   //Stream.of(new MosaicImg.BitmapController() { { setMosaicType(EMosaic.eMosaicSquare1); }})
//
//                                                   // test all
//                                                   Stream.of(EMosaic.values())
//                                                         .map(e -> new MosaicImg.BitmapController() { { setMosaicType(e); }})
//                                     );
//    }
    public void testMosaicGroupImg() { testApp(() -> Stream.concat(Stream.of((EMosaicGroup)null), Stream.of(EMosaicGroup.values()))
                                              .map(e -> new Pair<>(new MosaicGroupImg2.MosaicGroupAndroidBitmapController(e),
                                                                   new MosaicGroupImg2.MosaicGroupAndroidBitmapController(e)))
                                              .flatMap(x -> Stream.of(x.first, x.second)));
    }
    public void testMosaicSkillImg() { testApp(() -> Stream.concat(Stream.of((ESkillLevel)null), Stream.of(ESkillLevel.values()))
                                              .map(e -> new Pair<>(new MosaicSkillImg2.MosaicSkillAndroidBitmapController(e),
                                                                   new MosaicSkillImg2.MosaicSkillAndroidBitmapController(e)))
                                              .flatMap(x -> Stream.of(x.first, x.second)));
    }
    public void testLogo          () { testApp(() -> Stream.of(new Logo2.LogoAndroidBitmapController()
                                                             , new Logo2.LogoAndroidBitmapController()
                                                             , new Logo2.LogoAndroidBitmapController().asMine()
                                                             , new Logo2.LogoAndroidBitmapController().asMine())); }
    public void testFlag          () { testApp(() -> Stream.of(new Flag2.FlagAndroidBitmapController()
                                                             , new Flag2.FlagAndroidBitmapController())); }
    public void testSmile         () { testApp(() -> Stream.of(SmileModel2.EFaceType.values())
                                                           .map(e -> new Smile2.SmileAndroidBitmapController(e)));
    }
    // #endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityBinding = DataBindingUtil.setContentView(this, R.layout.demo_activity_2);
        DemoViewModel viewModel = ViewModelProviders.of(this).get(DemoViewModel.class);
        activityBinding.setViewModel(viewModel);
        activityBinding.executePendingBindings();

        td = new TestDrawing2("Android");

        onCreateImages = new Runnable[] {
//            this::testMosaicControl,
//            this::testMosaicImg,
            this::testMosaicSkillImg,
            this::testMosaicGroupImg,
            this::testSmile,
            this::testLogo,
            this::testFlag
        };

        activityBinding.prevImagesBtn.setOnClickListener(view -> onNextImages(false));
        activityBinding.refreshButton.setOnClickListener(view -> onNextImages(null));
        activityBinding.nextImagesBtn.setOnClickListener(view -> onNextImages(true));
        activityBinding.innerLayout.post(                ()   -> onNextImages(null));
    }

    @Override
    protected void onDestroy() {
        onCloseImages.run();
        super.onDestroy();
    }

    @FunctionalInterface
    public interface Proc3Bool{
        void apply(boolean t1, boolean t2, boolean t3);
    }

    void testApp(Supplier<Stream<IImageController2<?,?>>> funcGetImages) {
        List<IImageController2<?,?>> images = activityBinding.getViewModel().images = funcGetImages.get().collect(Collectors.toList());
        setTitle(td.getTitle(images));
        FrameLayout innerLayout = activityBinding.innerLayout;
        innerLayout.removeAllViews();

        List<View> imgControls = new ArrayList<>(images.size());
        boolean[] testTransparent = { false };
        boolean isMosaicGameController = images.get(0).getImage() instanceof View;

        Proc3Bool onCellTilingHandler = (applySettings, createImgControls, resized) -> {
            if (isMosaicGameController) // when is this game field...
                applySettings = false;  // ... then test as is
            resized = resized || applySettings;

            if (applySettings) {
                testTransparent[0] = td.bl();
                images.forEach(img -> td.changeSettings(img, testTransparent[0]));
            }

            double sizeW = innerLayout.getWidth();  // innerLayout.getMeasuredWidth();
            double sizeH = innerLayout.getHeight(); // innerLayout.getMeasuredHeight();
            RectDouble rc = new RectDouble(MARGIN, MARGIN, sizeW - MARGIN * 2, sizeH - MARGIN * 2); // inner rect where drawing images as tiles

            TestDrawing2.CellTilingResult2 ctr = td.cellTiling(rc, images, testTransparent[0]);
            SizeDouble imgSize = ctr.imageSize;
            if (imgSize.width <= 0 || imgSize.height <= 0)
                return;
            if (createImgControls)
                imgControls.clear();

            Function<IImageController2<?,?>, TestDrawing2.CellTilingInfo> callback = ctr.itemCallback;
            for (IImageController2<?,?> imgObj : images) {
                TestDrawing2.CellTilingInfo cti = callback.apply(imgObj);
                PointDouble offset = cti.imageOffset;

                if (createImgControls) {
                    Object img = imgObj.getImage();
                    View imgControl = null;
                    if (img instanceof View) {
                        imgControl = (View)img;
                    } else {
                        imgControl = new View(this) {
                            @Override
                            public void draw(Canvas canvas) {
                                super.draw(canvas);
                                Object img2 = imgObj.getImage(); // reload image!
                                if (img2 instanceof Bitmap) {
                                    Bitmap bmp = (Bitmap)img2;
                                    canvas.drawBitmap(bmp, 0,0, null);
                                } else {
                                    throw new RuntimeException("Unsupported image type: " + img2.getClass().getName());
                                }
                            }
                        };
                      //imgControl.setBackgroundColor(Cast.toColor(Color.RandomColor().brighter()));

                        View imgControl2 = imgControl;
                        Consumer<String> onChangeImage = propName -> {
                            if (propName.equals(IImageController.PROPERTY_IMAGE))
                                imgControl2.invalidate();
                        };
                        imgObj.setListener(onChangeImage);
                    }

                    FrameLayout.LayoutParams lpView = new FrameLayout.LayoutParams(0,0);
                    innerLayout.addView(imgControl, lpView);
                    imgControls.add(ctr.tableSize.width * cti.j + cti.i, imgControl);
                    resized = true; // to set real values to lpView
                }

                if (resized) {
                    imgObj.getModel().setSize(imgSize);
                    View imgControl = imgControls.get(ctr.tableSize.width * cti.j + cti.i);
                    FrameLayout.LayoutParams lpView = (FrameLayout.LayoutParams)imgControl.getLayoutParams();
                    lpView.leftMargin = (int)offset.x;
                    lpView.topMargin  = (int)offset.y;
                    lpView.width  = (int)imgSize.width;
                    lpView.height = (int)imgSize.height;
                    imgControl.setLayoutParams(lpView);
                }
            }
        };

        onCellTilingHandler.apply(true, true, true);


//        ViewTreeObserver.OnGlobalLayoutListener onSizeChanged = () -> {
//           onCellTilingHandler.apply(false, false, true);
//        };
        View.OnClickListener onClick = view -> {
            //onCellTilingHandler.apply(true, false, false);
        };
        View.OnTouchListener onTouch = (view, motionEvent) -> {
           onCellTilingHandler.apply(true, false, false);
           return true;
        };
//        innerLayout.getViewTreeObserver().addOnGlobalLayoutListener(onSizeChanged);
        if (isMosaicGameController)
            innerLayout.setOnClickListener(onClick);
        else
            innerLayout.setOnTouchListener(onTouch);

        onCloseImages = () -> {
//            innerLayout.getViewTreeObserver().removeOnGlobalLayoutListener(onSizeChanged);
            if (isMosaicGameController)
                innerLayout.setOnClickListener(null);
            else
                innerLayout.setOnTouchListener(null);
            images.forEach(IImageController2::close);
            //images.clear(); // unmodifiable list
            //images = null; // not final
        };

    }

    void onNextImages(Boolean isNext) {
        if (onCloseImages != null)
            onCloseImages.run();

        DemoViewModel viewModel = activityBinding.getViewModel();
        if (isNext != null)
            if (isNext) {
                if (++viewModel.nextCreateImagesIndex >= onCreateImages.length)
                    viewModel.nextCreateImagesIndex = 0;
            } else {
                if (--viewModel.nextCreateImagesIndex < 0)
                    viewModel.nextCreateImagesIndex = onCreateImages.length - 1;
            }

        onCreateImages[viewModel.nextCreateImagesIndex].run();
    }

}
