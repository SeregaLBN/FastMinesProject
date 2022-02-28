package fmg.jfx.img;

import java.util.Arrays;
import java.util.List;

import fmg.common.HSV;
import fmg.common.geom.PointDouble;
import fmg.core.img.LogoController2;
import fmg.core.img.LogoModel2;
import fmg.jfx.utils.Cast;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.StrokeLineCap;

/** Main logo image */
public final class Logo2 {
    private Logo2() {}

    private static void draw(GraphicsContext g, LogoModel2 lm) {
        var size = lm.getSize();
        // fill background
        g.clearRect(0, 0, size.width, size.height);

        List<PointDouble> rays0 = lm.getRays();
        List<PointDouble> inn0  = lm.getInn();
        List<PointDouble> oct0  = lm.getOct();

        Point2D [] rays = rays0.stream().map(Cast::toPoint).toArray(s -> new Point2D[s]);
        Point2D [] inn  = inn0 .stream().map(Cast::toPoint).toArray(s -> new Point2D[s]);
        Point2D [] oct  = oct0 .stream().map(Cast::toPoint).toArray(s -> new Point2D[s]);
        Point2D center = new Point2D(size.width/2.0, size.height/2.0);

        HSV[] hsvPalette = lm.getPalette();
        Color[] palette = Arrays.stream(hsvPalette)
            .map(hsv -> Cast.toColor(hsv.toColor()))
            .toArray(s -> new Color[s]);

        // paint owner gradient rays
        for (int i=0; i<8; i++) {
            if (!lm.isUseGradient()) {
                g.setFill(Cast.toColor(hsvPalette[i].toColor().darker()));
                fillPolygon(g, rays[i], oct[i], inn[i], oct[(i+5)%8]);
            } else {
                // emulate triangle gradient (see BmpLogo.cpp C++ source code)
                // over linear gragients

                setGradientFill(g, rays[i], palette[(i+1)%8], inn[i], palette[(i+6)%8]);
                fillPolygon(g, rays[i], oct[i], inn[i], oct[(i+5)%8]);

                Point2D p1 = oct[i];
                Point2D p2 = oct[(i+5)%8];
                Point2D p = new Point2D((p1.getX()+p2.getX())/2, (p1.getY()+p2.getY())/2); // середина линии oct[i]-oct[(i+5)%8]. По факту - пересечение линий rays[i]-inn[i] и oct[i]-oct[(i+5)%8]

                Color clr;// = new Color(1,1,1,0); //  Cast.toColor(fmg.common.Color.Transparent);
                if (true) {
                    HSV c1 = hsvPalette[(i+1)%8];
                    HSV c2 = hsvPalette[(i+6)%8];
                    double diff = c1.h - c2.h;
                    HSV cP = new HSV(c1.toColor());
                    cP.h += diff/2; // цвет в точке p (пересечений линий...)
                    cP.a = 0;
                    clr = Cast.toColor(cP.toColor());
                }

                setGradientFill(g, oct[i], palette[(i+3)%8], p, clr);
                fillPolygon(g, rays[i], oct[i], inn[i]);

                setGradientFill(g, oct[(i+5)%8], palette[(i+0)%8], p, clr);
                fillPolygon(g, rays[i], oct[(i+5)%8], inn[i]);
            }
        }

        // paint star perimeter
        double zoomAverage = (lm.getZoomX() + lm.getZoomY())/2;
        final double penWidth = lm.getBorderWidth() * zoomAverage;
        if (penWidth > 0.1) {
            g.setLineCap(StrokeLineCap.ROUND);
            g.setLineWidth(penWidth);
            for (int i=0; i<8; i++) {
                Point2D p1 = rays[(i + 7)%8];
                Point2D p2 = rays[i];
                g.setStroke(palette[i].darker());
                g.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            }
        }

        // paint inner gradient triangles
        for (int i=0; i<8; i++) {
            if (lm.isUseGradient()) {
                Point2D p1 = inn[(i+0)%8];
                Point2D p2 = inn[(i+3)%8];
                Point2D p = new Point2D((p1.getX()+p2.getX())/2, (p1.getY()+p2.getY())/2); // center line of p1-p2
                setGradientFill(g,
                    p, palette[(i+6)%8],
                    center, ((i & 1) == 1) ? Color.BLACK : Color.WHITE);
            } else {
                g.setFill(((i & 1) == 1)
                    ? Cast.toColor(hsvPalette[(i + 6)%8].toColor().brighter())
                    : Cast.toColor(hsvPalette[(i + 6)%8].toColor().darker()));
            }
            fillPolygon(g, inn[(i + 0)%8], inn[(i + 3)%8], center);
        }
    }

    private static void setGradientFill(GraphicsContext g, Point2D start, Color startClr, Point2D end, Color endClr) {
        g.setFill(new LinearGradient(start.getX(), start.getY(),
                                     end  .getX(), end  .getY(),
                                     false,
                                     CycleMethod.NO_CYCLE,
                                     new Stop[] {
                                        new Stop(0, startClr),
                                        new Stop(1, endClr)
                                    }));
    }

    private static void fillPolygon(GraphicsContext g, Point2D... p) {
        g.fillPolygon(
            Arrays.stream(p).mapToDouble(s -> s.getX()).toArray(),
            Arrays.stream(p).mapToDouble(s -> s.getY()).toArray(),
            p.length);
    }


    /** Logo image controller implementation for {@link javafx.scene.canvas.Canvas} */
    public static class LogoSwingIconController extends LogoController2<javafx.scene.canvas.Canvas, JfxCanvasView<LogoModel2>> {

        public LogoSwingIconController() {
            var model = new LogoModel2();
            var view = new JfxCanvasView<>(model, Logo2::draw);
            init(model, view);
        }

    }

    /** Logo image controller implementation for {@link javafx.scene.image.Image} */
    public static class LogoAwtImageController extends LogoController2<javafx.scene.image.Image, JfxImageView<LogoModel2>> {

        public LogoAwtImageController() {
            var model = new LogoModel2();
            var view = new JfxImageView<>(model, Logo2::draw);
            init(model, view);
        }

    }

}
