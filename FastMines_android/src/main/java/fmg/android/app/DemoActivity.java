package fmg.android.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

public class DemoActivity extends Activity {

    private TestDrawing _td;
    private FrameLayout _innerLayout;
    private static final int margin = 10; // panel margin - padding to inner images
    private Runnable _onCloseImages;
    private Runnable[] _onCreateImages; // images factory
    private int _nextCreateImagesIndex;

    // #region images Fabrica
    public void testMosaicControl () {
        MosaicView._DEBUG_DRAW_FLOW = true;
        testApp(() -> {
            MosaicViewController mosaicController = new MosaicViewController(this);

            if (ThreadLocalRandom.current().nextBoolean()) {
                // unmodified controller test
            } else {
                EMosaic mosaicType = EMosaic.eMosaicTrSq1;
                ESkillLevel skill  = ESkillLevel.eBeginner;

                mosaicController.setArea(1500);
                mosaicController.setMosaicType(mosaicType);
                mosaicController.setSizeField(skill.getDefaultSize());
                mosaicController.setMinesCount(skill.getNumberMines(mosaicType));
                mosaicController.gameNew();
            }
            return Arrays.asList(mosaicController);

        }
    );}
    public void testMosaicImg     () { testApp(() ->
                                                   //// test single
                                                   //Arrays.asList(new MosaicImg.ControllerBitmap() { { setMosaicType(EMosaic.eMosaicSquare1); }})

                                                   // test all
                                                   Stream.of(EMosaic.values())
                                                         .map(e -> new MosaicImg.ControllerBitmap() { { setMosaicType(e); }})
                                                         .collect(Collectors.toList())
                                     ); }
    public void testMosaicGroupImg() { testApp(() -> Stream.concat(Stream.of((EMosaicGroup)null), Stream.of(EMosaicGroup.values()))
                                              .map(e -> new Pair<>(new MosaicGroupImg.ControllerBitmap(e),
                                                                   new MosaicGroupImg.ControllerBitmap(e)))
                                              .flatMap(x -> Stream.of(x.first, x.second))
                                              .collect(Collectors.toList())); }
    public void testMosaicSkillImg() { testApp(() -> Stream.concat(Stream.of((ESkillLevel)null), Stream.of(ESkillLevel.values()))
                                              .map(e -> new Pair<>(new MosaicSkillImg.ControllerBitmap(e),
                                                                   new MosaicSkillImg.ControllerBitmap(e)))
                                              .flatMap(x -> Stream.of(x.first, x.second))
                                              .collect(Collectors.toList())); }
    public void testLogos         () { testApp(() -> Arrays.asList(new Logo.ControllerBitmap()
                                                                 , new Logo.ControllerBitmap()
                                                                 , new Logo.ControllerBitmap()
                                                                 , new Logo.ControllerBitmap())); }
    public void testMines         () { testApp(() -> Arrays.asList(new Mine.ControllerBitmap()
                                                                 , new Mine.ControllerBitmap())); }
    public void testFlags         () { testApp(() -> Arrays.asList(new Flag.ControllerBitmap()
                                                                 , new Flag.ControllerBitmap())); }
    public void testSmiles        () { testApp(() -> Arrays.asList(SmileModel.EFaceType.values())
                                                         .stream()
                                                         .map(e -> new Smile.ControllerBitmap(e))
                                                         .collect(Collectors.toList())); }
    // #endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_activity);

        _td = new TestDrawing("Android");

        _onCreateImages = new Runnable[] {
                this::testMosaicControl,
                this::testMosaicImg,
                this::testMosaicSkillImg,
                this::testMosaicGroupImg,
                this::testSmiles,
                this::testLogos,
                this::testMines,
                this::testFlags
        };

        _innerLayout = findViewById(R.id.inner_layout);

        Button prevImagesBtn = findViewById(R.id.prev_images);
        Button refresh       = findViewById(R.id.refresh_images);
        Button nextImagesBtn = findViewById(R.id.next_images);

        prevImagesBtn.setOnClickListener(view -> onNextImages(false));
        refresh      .setOnClickListener(view -> onNextImages(null));
        nextImagesBtn.setOnClickListener(view -> onNextImages(true));
        _innerLayout.post(               ()   -> onNextImages(null));
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

    void testApp(Supplier<List<IImageController<?,?,?>>> funcGetImages) {
        _innerLayout.removeAllViews();
        List<IImageController<?,?,?>> images = funcGetImages.get();
        setTitle(_td.getTitle(images));

        List<View> imgControls = new ArrayList<>(images.size());
        boolean[] testTransparent = { false };
        boolean imgIsControl = images.get(0).getImage() instanceof View;
        Map<IImageController<?,?,?>, PropertyChangeListener> binding = imgIsControl ? null : new HashMap<>(images.size());

        Proc3Bool onCellTilingHandler = (applySettings, createImgControls, resized) -> {
            if (images.size() == 1)     // if one image...
                applySettings = false;  // ... then test as is
            resized = resized || applySettings;

            if (applySettings) {
                testTransparent[0] = _td.bl();
                images.forEach(img -> _td.applySettings(img, testTransparent[0]));
            }

            double sizeW = _innerLayout.getWidth();  // _innerLayout.getMeasuredWidth();
            double sizeH = _innerLayout.getHeight(); // _innerLayout.getMeasuredHeight();
            RectDouble rc = new RectDouble(margin, margin, sizeW - margin * 2, sizeH - margin * 2); // inner rect where drawing images as tiles

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
                    View imgControl = null;
                    if (imgIsControl) {
                        imgControl = (View)imgObj.getImage();
                    } else {
                        imgControl = new View(this) {
                            @Override
                            public void draw(Canvas canvas) {
                                super.draw(canvas);
                                Object image = imgObj.getImage();
                                if (image instanceof Bitmap) {
                                    Bitmap bmp = (Bitmap)image;
                                    canvas.drawBitmap(bmp, 0,0, null);
                                } else {
                                    throw new RuntimeException("Unsupported image type: " + image.getClass().getSimpleName());
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
                    _innerLayout.addView(imgControl, lpView);
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
        if (imgIsControl)
            _innerLayout.setOnClickListener(onClick);
        else
            _innerLayout.setOnTouchListener(onTouch);

        _onCloseImages = () -> {
//            _innerLayout.getViewTreeObserver().removeOnGlobalLayoutListener(onSizeChanged);
            if (imgIsControl)
                _innerLayout.setOnClickListener(null);
            else
                _innerLayout.setOnTouchListener(null);
            images.forEach(imgObj -> {
                if (!imgIsControl)
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
                if (++_nextCreateImagesIndex >= _onCreateImages.length)
                    _nextCreateImagesIndex = 0;
            } else {
                if (--_nextCreateImagesIndex < 0)
                    _nextCreateImagesIndex = _onCreateImages.length - 1;
            }

        _onCreateImages[_nextCreateImagesIndex].run();
    }

}
