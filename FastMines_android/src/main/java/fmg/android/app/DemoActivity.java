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
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


import fmg.android.img.Logo;
import fmg.android.utils.Cast;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.img.ATestDrawing;
import fmg.core.img.IImageController;

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

   // #region images Fabrica
   public void testLogos() {
      testApp(Logo::testData);
   }
   // #endregion

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.demo_view);

      _demoView = (DemoView)findViewById(R.id.demo_view);
      _demoView._onDraw = this::onDraw;
      Button changeColorBtn = (Button)findViewById(R.id.change_color);
      changeColorBtn.setOnClickListener(view -> onNextImages());

      _td = new TestDrawing();

      _onCreateImages = new Runnable[] { this::testLogos/*, TestMine, TestMosaicSkillImg, TestMosaicGroupImg, TestMosaicsImg, TestFlag, TestSmile*/ };

      _demoView.setOnTouchListener(this::onTouch);

      _demoView.post(this::onNextImages);
   }

   @Override
   protected void onDestroy() {
      _onCloseImages.run();
      super.onDestroy();
   }

   public boolean onTouch(View view, MotionEvent ev) {
      onCellTilingHandler(true, false);
      return true;
   }

   void testApp(Supplier<List<IImageController<?,?,?>>> funcGetImages) {
    //_demoView.children.clear();
      _images = funcGetImages.get();
      setTitle(_td.getTitle(_images));

      onCellTilingHandler(true, true);

      Consumer<PropertyChangeEvent> onChangeImage = ev -> {
         if (ev.getPropertyName().equals(IImageController.PROPERTY_IMAGE)) {
            _demoView.invalidate();
         }
      };
      _images.forEach(img -> {
         img.addListener(onChangeImage::accept);
      });
      _onCloseImages = () -> {
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
      canvas.drawColor(Color.WHITE);

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
