package fmg.android.app;

import android.app.Activity;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;

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
import fmg.core.img.TestDrawing;
import fmg.core.img.IImageController;
import fmg.core.img.SmileModel;
import fmg.core.mosaic.MosaicView;
import fmg.core.types.EMosaic;
import fmg.core.types.EMosaicGroup;
import fmg.core.types.ESkillLevel;

/** live UI test application */
public class DemoActivity extends /*AppCompat*/Activity {

    private static final int MARGIN = 10; // panel margin - padding to inner images

    private TestDrawing _td;
    private DemoActivityBinding activityBinding;
    private Runnable _onCloseImages;
    private Runnable[] _onCreateImages; // images factory
    private DemoViewModel viewModel;
    public static class DemoViewModel extends ViewModel {
        private int _nextCreateImagesIndex;
    }


    // #region images Fabrica
    public void testMosaicControl () {
        MosaicView._DEBUG_DRAW_FLOW = true;
        testApp(() -> {
            MosaicViewController mosaicController = new MosaicViewController(this);

            if (ThreadLocalRandom.current().nextBoolean()) {
                // unmodified controller test
            } else {
                EMosaic mosaicType = EMosaic.fromOrdinal(ThreadLocalRandom.current().nextInt(EMosaic.values().length));
                ESkillLevel skill  = ESkillLevel.eBeginner;

                mosaicController.setMosaicType(mosaicType);
                mosaicController.setSizeField(skill.getDefaultSize());
                mosaicController.setMinesCount(skill.getNumberMines(mosaicType));
                mosaicController.gameNew();
            }
            return Stream.of(mosaicController);

        }
    );}
    public void testMosaicImg     () { testApp(() ->
                                                   //// test single
                                                   //Stream.of(new MosaicImg.ControllerBitmap() { { setMosaicType(EMosaic.eMosaicSquare1); }})

                                                   // test all
                                                   Stream.of(EMosaic.values())
                                                         .map(e -> new MosaicImg.ControllerBitmap() { { setMosaicType(e); }})
                                     );
    }
    public void testMosaicGroupImg() { testApp(() -> Stream.concat(Stream.of((EMosaicGroup)null), Stream.of(EMosaicGroup.values()))
                                              .map(e -> new Pair<>(new MosaicGroupImg.ControllerBitmap(e),
                                                                   new MosaicGroupImg.ControllerBitmap(e)))
                                              .flatMap(x -> Stream.of(x.first, x.second)));
    }
    public void testMosaicSkillImg() { testApp(() -> Stream.concat(Stream.of((ESkillLevel)null), Stream.of(ESkillLevel.values()))
                                              .map(e -> new Pair<>(new MosaicSkillImg.ControllerBitmap(e),
                                                                   new MosaicSkillImg.ControllerBitmap(e)))
                                              .flatMap(x -> Stream.of(x.first, x.second)));
    }
    public void testLogo          () { testApp(() -> Stream.of(new Logo.ControllerBitmap()
                                                             , new Logo.ControllerBitmap()
                                                             , new Logo.ControllerBitmap()
                                                             , new Logo.ControllerBitmap())); }
    public void testMine          () { testApp(() -> Stream.of(new Mine.ControllerBitmap()
                                                             , new Mine.ControllerBitmap())); }
    public void testFlag          () { testApp(() -> Stream.of(new Flag.ControllerBitmap()
                                                             , new Flag.ControllerBitmap())); }
    public void testSmile         () { testApp(() -> Stream.of(SmileModel.EFaceType.values())
                                                         .map(e -> new Smile.ControllerBitmap(e)));
    }
    // #endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityBinding = DataBindingUtil.setContentView(this, R.layout.demo_activity);
        viewModel = new DemoViewModel(); // ViewModelProviders.of(this).get(DemoViewModel.class);
        activityBinding.setViewModel(viewModel);
        activityBinding.executePendingBindings();

        _td = new TestDrawing("Android");

        _onCreateImages = new Runnable[] {
            this::testMosaicControl,
            this::testMosaicImg,
            this::testMosaicSkillImg,
            this::testMosaicGroupImg,
            this::testSmile,
            this::testLogo,
            this::testMine,
            this::testFlag
        };

        activityBinding.prevImagesBtn.setOnClickListener(view -> onNextImages(false));
        activityBinding.refreshButton.setOnClickListener(view -> onNextImages(null));
        activityBinding.nextImagesBtn.setOnClickListener(view -> onNextImages(true));
        activityBinding.innerLayout.post(                ()   -> onNextImages(null));
    }

    @Override
    protected void onDestroy() {
        _onCloseImages.run();
        super.onDestroy();
    }

    @FunctionalInterface
    public interface Proc3Bool{
        void apply(boolean t1, boolean t2, boolean t3);
    }

    void testApp(Supplier<Stream<IImageController<?,?,?>>> funcGetImages) {
        List<IImageController<?,?,?>> images = funcGetImages.get().collect(Collectors.toList());
        setTitle(_td.getTitle(images));
        FrameLayout innerLayout = activityBinding.innerLayout;
        innerLayout.removeAllViews();

        List<View> imgControls = new ArrayList<>(images.size());
        boolean[] testTransparent = { false };
        boolean isMosaicGameController = images.get(0).getImage() instanceof View;
        Map<IImageController<?,?,?>, PropertyChangeListener> binding = new HashMap<>();

        Proc3Bool onCellTilingHandler = (applySettings, createImgControls, resized) -> {
            if (isMosaicGameController) // when is this game field...
                applySettings = false;  // ... then test as is
            resized = resized || applySettings;

            if (applySettings) {
                testTransparent[0] = _td.bl();
                images.forEach(img -> _td.applySettings(img, testTransparent[0]));
            }

            double sizeW = innerLayout.getWidth();  // _innerLayout.getMeasuredWidth();
            double sizeH = innerLayout.getHeight(); // _innerLayout.getMeasuredHeight();
            RectDouble rc = new RectDouble(MARGIN, MARGIN, sizeW - MARGIN * 2, sizeH - MARGIN * 2); // inner rect where drawing images as tiles

            TestDrawing.CellTilingResult ctr = _td.cellTiling(rc, images, testTransparent[0]);
            SizeDouble imgSize = ctr.imageSize;
            if (imgSize.width <= 0 || imgSize.height <= 0)
                return;
            if (createImgControls)
                imgControls.clear();

            Function<IImageController<?,?,?>, TestDrawing.CellTilingInfo> callback = ctr.itemCallback;
            for (IImageController<?,?,?> imgObj : images) {
                TestDrawing.CellTilingInfo cti = callback.apply(imgObj);
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
                        PropertyChangeListener onChangeImage = ev -> {
                            if (ev.getPropertyName().equals(IImageController.PROPERTY_IMAGE))
                                imgControl2.invalidate();
                        };
                        imgObj.addListener(onChangeImage);
                        binding.put(imgObj, onChangeImage);
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
//        _innerLayout.getViewTreeObserver().addOnGlobalLayoutListener(onSizeChanged);
        if (isMosaicGameController)
            innerLayout.setOnClickListener(onClick);
        else
            innerLayout.setOnTouchListener(onTouch);

        _onCloseImages = () -> {
//            _innerLayout.getViewTreeObserver().removeOnGlobalLayoutListener(onSizeChanged);
            if (isMosaicGameController)
                innerLayout.setOnClickListener(null);
            else
                innerLayout.setOnTouchListener(null);
            images.forEach(imgObj -> {
                if (binding.containsKey(imgObj))
                    imgObj.removeListener(binding.get(imgObj));
                imgObj.close();
            });
            //images.clear(); // unmodifiable list
            //images = null; // not final
        };

    }

    void onNextImages(Boolean isNext) {
        if (_onCloseImages != null)
            _onCloseImages.run();

        if (isNext != null)
            if (isNext) {
                if (++viewModel._nextCreateImagesIndex >= _onCreateImages.length)
                    viewModel._nextCreateImagesIndex = 0;
            } else {
                if (--viewModel._nextCreateImagesIndex < 0)
                    viewModel._nextCreateImagesIndex = _onCreateImages.length - 1;
            }

        _onCreateImages[viewModel._nextCreateImagesIndex].run();
    }

}
