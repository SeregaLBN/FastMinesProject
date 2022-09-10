package fmg.jfx.img;

import java.util.function.Consumer;

import javafx.scene.canvas.GraphicsContext;

import fmg.common.geom.DoubleExt;
import fmg.common.geom.SizeDouble;
import fmg.core.img.IImageModel2;
import fmg.core.img.IImageView2;
import fmg.jfx.utils.ImgUtils;

/** Image view implementation over {@link javafx.scene.image.Image} */
public class JfxImageView<TModel extends IImageModel2> implements IImageView2<javafx.scene.image.Image> {

    private final TModel model;
    private final Consumer<GraphicsContext> draw;
    private boolean valid;
    private javafx.scene.canvas.Canvas canvas;
    private javafx.scene.image.Image img;

    public JfxImageView(TModel model, Consumer<GraphicsContext> draw) {
        this.model = model;
        this.draw = draw;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public void invalidate() {
        valid = false;
    }

    @Override
    public javafx.scene.image.Image getImage() {
        SizeDouble s = model.getSize();
        if (canvas == null) {
            canvas = new javafx.scene.canvas.Canvas(s.width, s.height);
            valid = false;
        } else {
            if (!DoubleExt.almostEquals(canvas.getWidth() , s.width) &&
                !DoubleExt.almostEquals(canvas.getHeight(), s.height))
            {
                canvas.setWidth (s.width);
                canvas.setHeight(s.height);
                valid = false;
            }
        }

        if (!valid) {
            draw.accept(canvas.getGraphicsContext2D());
            img = null;
            valid = true;
        }
        if (img == null) {
            img = ImgUtils.toImage(canvas);
        }
        return img;
    }

    @Override
    public void reset() {
        img = null;
        valid = false;
    }

}
