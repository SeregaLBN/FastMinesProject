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
import java.util.function.Function;
import java.util.function.Supplier;

import fmg.android.img.Flag;
import fmg.android.img.Logo;
import fmg.android.img.Mine;
import fmg.android.img.MosaicGroupImg;
import fmg.android.img.MosaicImg;
import fmg.android.img.MosaicSkillImg;
import fmg.android.img.Smile;
import fmg.android.mosaic.MosaicViewController;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.img.ATestDrawing;
import fmg.core.img.IImageController;

public class DemoActivity extends Activity {

   class TestDrawing extends ATestDrawing {
      TestDrawing() { super("Android"); }
   }

   private TestDrawing _td;
   private FrameLayout _innerLayout;
   private static final int margin = 10; // panel margin - padding to inner images
   private Runnable _onCloseImages;
   private Runnable[] _onCreateImages; // images factory
   private int _nextCreateImagesIndex;

   // #region images Fabrica
   public void testMosaicControl () { testApp(() -> Arrays.asList(MosaicViewController.getTestData(this))); }
   public void testMosaicImg     () { testApp(                    MosaicImg          ::getTestData); }
   public void testMosaicGroupImg() { testApp(                    MosaicGroupImg     ::getTestData); }
   public void testMosaicSkillImg() { testApp(                    MosaicSkillImg     ::getTestData); }
   public void testLogos         () { testApp(                    Logo               ::getTestData); }
   public void testMines         () { testApp(                    Mine               ::getTestData); }
   public void testFlags         () { testApp(                    Flag               ::getTestData); }
   public void testSmiles        () { testApp(                    Smile              ::getTestData); }
   // #endregion

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.demo_activity);

      _innerLayout = findViewById(R.id.inner_layout);

      Button nextImagesBtn = findViewById(R.id.next_images);
      nextImagesBtn.setOnClickListener(view -> onNextImages());

      _td = new TestDrawing();

      _onCreateImages = new Runnable[] { this::testMosaicControl,
              this::testMosaicImg, this::testMosaicSkillImg, this::testMosaicGroupImg, this::testSmiles, this::testLogos, this::testMines, this::testFlags };

      _innerLayout.post(this::onNextImages);
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
         resized = resized || applySettings;

         if (applySettings) {
            testTransparent[0] = _td.bl();
            images.forEach(img -> _td.applySettings(img, testTransparent[0]));
         }

         double sizeW = _innerLayout.getWidth();  // _innerLayout.getMeasuredWidth();
         double sizeH = _innerLayout.getHeight(); // _innerLayout.getMeasuredHeight();
         RectDouble rc = new RectDouble(margin, margin, sizeW - margin * 2, sizeH - margin * 2); // inner rect where drawing images as tiles

         ATestDrawing.CellTilingResult ctr = _td.cellTiling(rc, images, testTransparent[0]);
         SizeDouble imgSize = ctr.imageSize;
         if (createImgControls)
            imgControls.clear();

         Function<IImageController<?,?,?>, ATestDrawing.CellTilingInfo> callback = ctr.itemCallback;
         for (IImageController imgObj : images) {
            ATestDrawing.CellTilingInfo cti = callback.apply(imgObj);
            PointDouble offset = cti.imageOffset;

            if (createImgControls) {
               View imgControl = null;
               if (imgIsControl) {
                  imgControl = (View)imgObj.getImage();
               } else {
                  View imgControl2 = imgControl = new View(this) {
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

                  PropertyChangeListener onChangeImage = ev -> {
                     if (ev.getPropertyName().equals(IImageController.PROPERTY_IMAGE)) {
                        imgControl2.invalidate();
                     }
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


//      ViewTreeObserver.OnGlobalLayoutListener onSizeChanged = () -> {
//         onCellTilingHandler.apply(false, false, true);
//      };
      View.OnClickListener onClick = view -> {
         //onCellTilingHandler.apply(true, false, false);
      };
      View.OnTouchListener onTouch = (view, motionEvent) -> {
         onCellTilingHandler.apply(true, false, false);
         return true;
      };
//      _innerLayout.getViewTreeObserver().addOnGlobalLayoutListener(onSizeChanged);
      if (imgIsControl)
         _innerLayout.setOnClickListener(onClick);
      else
         _innerLayout.setOnTouchListener(onTouch);

      _onCloseImages = () -> {
//         _innerLayout.getViewTreeObserver().removeOnGlobalLayoutListener(onSizeChanged);
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

   void onNextImages() {
      if (_onCloseImages != null)
         _onCloseImages.run();

      Runnable onCreate = _onCreateImages[_nextCreateImagesIndex];
      if (++_nextCreateImagesIndex >= _onCreateImages.length)
         _nextCreateImagesIndex = 0;
      onCreate.run();
   }

}
