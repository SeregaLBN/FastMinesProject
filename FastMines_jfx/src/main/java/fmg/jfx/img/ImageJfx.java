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

    private IImageView<Image, ? extends IImageModel> imageView;
    private Canvas canvas;

    ImageJfx(IImageView<Image, ? extends IImageModel> imageView) {
        this.imageView = imageView;
    }

    public void createCanvas() {
        SizeDouble s = imageView.getSize();
        if (canvas == null)
            canvas = new Canvas(s.width, s.height);
        else {
            canvas. setWidth(s.width);
            canvas.setHeight(s.height);
        }
    }

    public Image createImage() {
        return ImgUtils.toImage(canvas);
    }

    public GraphicsContext getGraphics() { return canvas.getGraphicsContext2D(); }

}
