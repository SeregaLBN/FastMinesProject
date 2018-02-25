package fmg.swing.draw.img;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import fmg.common.geom.Size;
import fmg.core.img.IImageModel;
import fmg.core.img.IImageView;

/** Internal wrapper-image imlementation over java.awt.Image
 * @param <TImageModel> - model of image
 */
class ImageAwt<TImageModel extends IImageModel> {

   private IImageView<java.awt.Image, TImageModel> _imageView;

   ImageAwt(IImageView<java.awt.Image, TImageModel> imageView) {
      this._imageView = imageView;
   }

   public java.awt.Image createImage() {
      Size s = _imageView.getSize();
      return new BufferedImage(s.width, s.height, BufferedImage.TYPE_INT_ARGB);
   }

   public void draw(java.util.function.Consumer<Graphics2D> drawBody) {
      BufferedImage img = (BufferedImage)_imageView.getImage();
      Graphics2D g = img.createGraphics();
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      drawBody.accept(g);
      g.dispose();
   }


}
