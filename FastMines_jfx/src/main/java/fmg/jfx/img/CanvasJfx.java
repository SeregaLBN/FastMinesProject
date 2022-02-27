package fmg.jfx.img;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import fmg.common.geom.SizeDouble;
import fmg.core.img.IImageModel;
import fmg.core.img.IImageView;

/** Internal wrapper-image implementation over javafx.scene.canvas.Canvas */
@Deprecated
public class CanvasJfx {

    private IImageView<?, ? extends IImageModel> imageView;
    private Canvas canvas;

    public CanvasJfx(IImageView<?, ? extends IImageModel> imageView) {
        this.imageView = imageView;
    }

    public Canvas create() {
        SizeDouble s = imageView.getSize();
        if (canvas == null)
            canvas = new Canvas(s.width, s.height);
        else {
            canvas. setWidth(s.width);
            canvas.setHeight(s.height);
        }
        return canvas;
    }

    public GraphicsContext getGraphics() { return canvas.getGraphicsContext2D(); }

}
