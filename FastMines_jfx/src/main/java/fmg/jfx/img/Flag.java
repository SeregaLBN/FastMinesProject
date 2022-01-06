package fmg.jfx.img;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import fmg.common.geom.SizeDouble;
import fmg.core.img.FlagModel;
import fmg.core.img.ImageController;
import fmg.core.img.ImageView;

/** Flag image */
public final class Flag {
    private Flag() {}

    /** Flag image. Base image view JFX implementation
     *  @param <TImage> JFX specific image: {@link javafx.scene.canvas.Canvas} or {@link javafx.scene.image.Image}
     */
    public abstract static class JfxView<TImage> extends ImageView<TImage, FlagModel> {

        public JfxView() {
            super(new FlagModel());
        }

        protected void draw(GraphicsContext g) {
            SizeDouble size = getSize();
            g.clearRect(0,0, size.width, size.height);

            double h = size.height / 100.0;
            double w = size.width  / 100.0;
    //        g.setEffect(new BoxBlur());

            // test
    //        g.setLineWidth(1);
    //        g.setStroke(Color.RED);
    //        g.strokeRect(0, 0, size.width, size.height);

            // perimeter figure points
            Point2D[] p = new Point2D[] {
                new Point2D(13.50 * w, 90 * h),
                new Point2D(17.44 * w, 51 * h),
                new Point2D(21.00 * w, 16 * h),
                new Point2D(85.00 * w, 15 * h),
                new Point2D(81.45 * w, 50 * h)};

            g.setLineWidth(Math.max(1, 7*(w+h)/2));
            g.setStroke(Color.BLACK);
            g.strokeLine(p[0].getX(), p[0].getY(), p[1].getX(), p[1].getY());

            g.setStroke(Color.RED);
            g.beginPath();
            g.moveTo(p[2].getX(), p[2].getY());
            g.bezierCurveTo(95.0 * w,  0 * h,
                            19.3 * w, 32 * h,
                            p[3].getX(), p[3].getY());
            g.bezierCurveTo(77.80 * w, 32.89 * h,
                            88.05 * w, 22.73 * h,
                            p[4].getX(), p[4].getY());
            g.bezierCurveTo(15.83 * w, 67 * h,
                            91.45 * w, 35 * h,
                            p[1].getX(), p[1].getY());
            g.lineTo(p[2].getX(), p[2].getY());
            g.closePath();

            g.stroke();
        }

        @Override
        public void close() {
            super.close();
            getModel().close();
        }

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    //    custom implementations
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    /** Flag image view implementation over {@link javafx.scene.canvas.Canvas} */
    public static class CanvasView extends JfxView<javafx.scene.canvas.Canvas> {

        private CanvasJfx canvas = new CanvasJfx(this);

        @Override
        protected javafx.scene.canvas.Canvas createImage() { return canvas.create(); }

        @Override
        protected void drawBody() { draw(canvas.getGraphics()); }

    }

    /** Flag image view implementation over {@link javafx.scene.image.Image} */
    public static class ImageJfxView extends JfxView<javafx.scene.image.Image> {

        private ImageJfx img = new ImageJfx(this);

        @Override
        protected javafx.scene.image.Image createImage() {
            img.createCanvas();
            return null; // img.createImage(); // fake empty image
        }

        @Override
        protected void drawBody() {
            draw(img.getGraphics());
            setImage(img.createImage()); // real image
        }

    }

    /** Flag image controller implementation for {@link Flag.CanvasView} */
    public static class CanvasController extends ImageController<javafx.scene.canvas.Canvas, Flag.CanvasView, FlagModel> {

        public CanvasController() {
            super(new Flag.CanvasView());
        }

        @Override
        public void close() {
            super.close();
            getView().close();
        }

    }

    /** Flag image controller implementation for {@link Flag.ImageJfxView} */
    public static class ImageJfxController extends ImageController<javafx.scene.image.Image, Flag.ImageJfxView, FlagModel> {

        public ImageJfxController() {
            super(new Flag.ImageJfxView());
        }

        @Override
        public void close() {
            super.close();
            getView().close();
        }

    }

}
