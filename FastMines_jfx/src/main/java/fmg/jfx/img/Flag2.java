package fmg.jfx.img;

import fmg.common.geom.BoundDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.img.FlagModel2;
import fmg.core.img.ImageController2;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/** Flag image */
public final class Flag2 {
    private Flag2() {}

    private static void draw(GraphicsContext g, FlagModel2 model) {
        SizeDouble size = model.getSize();
        BoundDouble padding = model.getPadding();

        g.clearRect(0,0, size.width, size.height);

        double width  = size.width  - padding.left - padding.right;
        double height = size.height - padding.top  - padding.bottom;
        double offsetX = padding.left;
        double offsetY = padding.top ;
        double w = width  / 100.0;
        double h = height / 100.0;
//        g.setEffect(new BoxBlur());

        // test
//        g.setLineWidth(1);
//        g.setStroke(Color.RED);
//        g.strokeRect(0, 0, size.width, size.height);

        // perimeter figure points
        Point2D[] p = new Point2D[] {
            new Point2D(offsetX + 13.50 * w, offsetY + 90 * h),
            new Point2D(offsetX + 17.44 * w, offsetY + 51 * h),
            new Point2D(offsetX + 21.00 * w, offsetY + 16 * h),
            new Point2D(offsetX + 85.00 * w, offsetY + 15 * h),
            new Point2D(offsetX + 81.45 * w, offsetY + 50 * h)};

        g.setLineWidth(Math.max(1, 7*(w+h)/2));
        g.setStroke(Color.BLACK);
        g.strokeLine(p[0].getX(), p[0].getY(), p[1].getX(), p[1].getY());

        g.setStroke(Color.RED);
        g.beginPath();
        g.moveTo(p[2].getX(), p[2].getY());
        g.bezierCurveTo(offsetX + 95.0 * w, offsetY +  0 * h,
                        offsetX + 19.3 * w, offsetY + 32 * h,
                        p[3].getX(), p[3].getY());
        g.bezierCurveTo(offsetX + 77.80 * w, offsetY + 32.89 * h,
                        offsetX + 88.05 * w, offsetY + 22.73 * h,
                        p[4].getX(), p[4].getY());
        g.bezierCurveTo(offsetX + 15.83 * w, offsetY + 67 * h,
                        offsetX + 91.45 * w, offsetY + 35 * h,
                        p[1].getX(), p[1].getY());
        g.lineTo(p[2].getX(), p[2].getY());
        g.closePath();

        g.stroke();
    }

    /** Flag image controller implementation for {@link javafx.scene.canvas.Canvas} */
    public static class FlagJfxCanvasController extends ImageController2<javafx.scene.canvas.Canvas, FlagModel2, JfxCanvasView<FlagModel2>> {

        public FlagJfxCanvasController() {
            var model = new FlagModel2();
            var view = new JfxCanvasView<>(model, Flag2::draw);
            init(model, view);
        }

    }

    /** Flag image controller implementation for {@link javafx.scene.image.Image} */
    public static class FlagJfxImageController extends ImageController2<javafx.scene.image.Image, FlagModel2, JfxImageView<FlagModel2>> {

        public FlagJfxImageController() {
            var model = new FlagModel2();
            var view = new JfxImageView<>(model, Flag2::draw);
            init(model, view);
        }

    }

}
