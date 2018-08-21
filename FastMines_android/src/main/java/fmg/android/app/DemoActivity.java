package fmg.android.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;


import fmg.android.img.Flag;
import fmg.android.img.Logo;
import fmg.android.img.Mine;
import fmg.android.img.MosaicGroupImg;
import fmg.android.img.MosaicImg;
import fmg.android.img.MosaicSkillImg;
import fmg.android.img.Smile;
import fmg.android.mosaic.MosaicViewController;
import fmg.android.utils.Cast;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.img.ATestDrawing;
import fmg.core.img.IImageController;
import fmg.core.mosaic.MosaicDrawModel;

public class DemoActivity extends Activity {

   class TestDrawing extends ATestDrawing {
      public TestDrawing() { super("Android"); }
   }

   TestDrawing _td;
   ATestDrawing.CellTilingResult _ctr;
   private DemoView _demoView;
   static final int margin = 10; // panel margin - padding to inner images
   Runnable _onCloseImages;
   Runnable[] _onCreateImages; // images factory
   int _nextCreateImagesIndex;
   List<IImageController<?,?,?>> _images; // current image controllers
   boolean _testTransparent;
   boolean _imgIsControl;

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
      int iii = R.layout.demo_activity;

      _demoView = (DemoView)findViewById(R.id.demo_view);
      _demoView._onDraw = this::onDraw;
      Button nextImagesBtn = (Button)findViewById(R.id.next_images);
      nextImagesBtn.setOnClickListener(view -> onNextImages());

      _td = new TestDrawing();

      _onCreateImages = new Runnable[] { this::testMosaicControl, this::testMosaicImg, this::testMosaicSkillImg, this::testMosaicGroupImg, this::testSmiles, this::testLogos, this::testMines, this::testFlags };

      _demoView.setOnTouchListener((v, ev) -> onTouch(ev));

      _demoView.post(this::onNextImages);
   }

   @Override
   protected void onDestroy() {
      _onCloseImages.run();
      super.onDestroy();
   }

   private boolean onTouch(MotionEvent ev) {
      onCellTilingHandler(true, false);
      return true;
   }

   void testApp(Supplier<List<IImageController<?,?,?>>> funcGetImages) {
      _images = funcGetImages.get();
      setTitle(_td.getTitle(_images));

      _imgIsControl = _images.get(0).getImage() instanceof View;

      onCellTilingHandler(true, true);

      Consumer<PropertyChangeEvent> onChangeImage = ev -> {
         if (ev.getPropertyName().equals(IImageController.PROPERTY_IMAGE)) {
            _demoView.invalidate();
         }
      };
      if (_imgIsControl) {
         _demoView.addChildrenForAccessibility(new ArrayList<>(_images.stream().map(x -> (View) x.getImage()).collect(Collectors.toList())));
         _demoView.invalidate(); // clean previous drawed images
      } else {
         _images.forEach(img -> {
            img.addListener(onChangeImage::accept);
         });
      }
      _onCloseImages = () -> {
         if (_imgIsControl)
            _demoView.addChildrenForAccessibility(null);
         else
            _images.forEach(img -> {
               img.removeListener(onChangeImage::accept);
            });
         _images.forEach(IImageController::close);
       //_images.clear(); // unmodifiable list
         _images = null;
      };

   }

   void onCellTilingHandler(boolean applySettings, boolean resized) {
      resized = resized || applySettings;

      if (applySettings) {
         _testTransparent = _td.bl();
         _images.forEach(img -> _td.applySettings(img, _testTransparent));
      }

      double sizeW = _demoView.getWidth();
      double sizeH = _demoView.getHeight();
      RectDouble rc = new RectDouble(margin, margin, sizeW - margin * 2, sizeH - margin * 2); // inner rect where drawing images as tiles

      _ctr = _td.cellTiling(rc, _images, _testTransparent);
      SizeDouble imgSize = _ctr.imageSize;
      if (resized)
         _images.forEach(imgObj ->  imgObj.getModel().setSize(imgSize));
   }

   void onDraw(Canvas canvas) {
      //canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
      canvas.drawColor(Cast.toColor(MosaicDrawModel.DefaultBkColor));

      if (_imgIsControl)
         return;

      double sizeW = _demoView.getWidth();  if (sizeW <= 0) sizeW = 100;
      double sizeH = _demoView.getHeight(); if (sizeH <= 0) sizeH = 100;
      RectDouble rc = new RectDouble(margin, margin, sizeW - margin * 2, sizeH - margin * 2); // inner rect where drawing images as tiles

      Paint paint = new Paint();
      paint.setColor(Color.BLACK);
      paint.setStrokeWidth(1);
      paint.setStyle(Paint.Style.STROKE);
      canvas.drawRect(Cast.toRect(rc), paint);

      if (_ctr == null)
         return;
      Function<IImageController<?,?,?> /* imageControllers */, ATestDrawing.CellTilingInfo> callback = _ctr.itemCallback;
      for (IImageController<?,?,?> img : _images) {
         ATestDrawing.CellTilingInfo cti = callback.apply(img);
         PointDouble offset = cti.imageOffset;

         Object image = img.getImage();
         if (image instanceof Bitmap) {
            Bitmap bmp = (Bitmap)image;
            canvas.drawBitmap(bmp, (float)offset.x, (float)offset.y, null);
         } else {
            throw new RuntimeException("Unsupported image type: " + image.getClass().getSimpleName());
         }
      }
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
