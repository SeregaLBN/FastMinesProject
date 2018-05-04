package fmg.jfx.draw.img;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import fmg.common.geom.Size;
import fmg.core.img.IImageModel;
import fmg.core.img.IImageView;

/** Internal wrapper-image imlementation over javafx.scene.canvas.Canvas */
class CanvasJfx {

   private IImageView<Canvas, ? extends IImageModel> _imageView;
   private Canvas _canvas;

   CanvasJfx(IImageView<Canvas, ? extends IImageModel> imageView) {
      this._imageView = imageView;
   }

   public Canvas create() {
      Size s = _imageView.getSize();
      _canvas = new Canvas(s.width, s.height);
      return _canvas;
   }

   public GraphicsContext getGraphics() { return _canvas.getGraphicsContext2D(); }

}
