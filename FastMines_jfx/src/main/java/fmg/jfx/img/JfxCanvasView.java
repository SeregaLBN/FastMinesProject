package fmg.jfx.img;

import java.util.function.BiConsumer;

import fmg.common.geom.DoubleExt;
import fmg.common.geom.SizeDouble;
import fmg.core.img.IImageModel2;
import fmg.core.img.IImageView2;
import javafx.scene.canvas.GraphicsContext;

/** Image view implementation over {@link javafx.scene.canvas.Canvas} */
public class JfxCanvasView<TModel extends IImageModel2> implements IImageView2<javafx.scene.canvas.Canvas> {

    private final TModel model;
    private final BiConsumer<GraphicsContext, TModel> draw;
    private boolean valid;
    private javafx.scene.canvas.Canvas canvas;

    public JfxCanvasView(TModel model, BiConsumer<GraphicsContext, TModel> draw) {
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
    public javafx.scene.canvas.Canvas getImage() {
        SizeDouble s = model.getSize();
        if (canvas == null) {
            canvas = new javafx.scene.canvas.Canvas(s.width, s.height);
            valid = false;
        } else {
            if (!DoubleExt.almostEquals(canvas.getWidth() , s.width) &&
                !DoubleExt.almostEquals(canvas.getHeight(), s.height))
            {
                canvas. setWidth(s.width);
                canvas.setHeight(s.height);
                valid = false;
            }
        }

        if (!valid) {
            draw.accept(canvas.getGraphicsContext2D(), model);
            valid = true;
        }
        return canvas;
    }

    @Override
    public void reset() {
        valid = false;
    }

}
