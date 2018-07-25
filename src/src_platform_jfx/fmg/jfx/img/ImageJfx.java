package fmg.jfx.img;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import fmg.common.geom.SizeDouble;
import fmg.core.img.IImageModel;
import fmg.core.img.IImageView;
import fmg.jfx.utils.ImgUtils;

/** Internal wrapper-image implementation over javafx.scene.image.Image */
class ImageJfx {

   private IImageView<Image, ? extends IImageModel> _imageView;
   private Canvas _canvas;

   ImageJfx(IImageView<Image, ? extends IImageModel> imageView) {
      this._imageView = imageView;
   }

   public void createCanvas() {
      SizeDouble s = _imageView.getSize();
      if (_canvas == null)
         _canvas = new Canvas(s.width, s.height);
      else {
         _canvas. setWidth(s.width);
         _canvas.setHeight(s.height);
      }
   }

   public Image createImage() {
      return ImgUtils.toImage(_canvas);
   }

   public GraphicsContext getGraphics() { return _canvas.getGraphicsContext2D(); }

}
