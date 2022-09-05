package fmg.swing.img;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;

import fmg.common.HSV;
import fmg.common.geom.PointDouble;
import fmg.core.img.LogoController2;
import fmg.core.img.LogoModel2;
import fmg.swing.utils.Cast;

/** Main logo image */
public final class Logo2 {
    private Logo2() {}

    private static final Color TRANSPARENT = new Color(0xFF, 0xFF, 0xFF, 0);

    private static void draw(Graphics2D g, LogoModel2 lm) {
        var size = lm.getSize();

        { // fill background
            g.setComposite(AlphaComposite.Src);
            g.setColor(TRANSPARENT);
            g.fillRect(0, 0, (int)size.width, (int)size.height);
        }

        g.setComposite(AlphaComposite.SrcOver);
        List<PointDouble> rays0 = lm.getRays();
        List<PointDouble> inn0  = lm.getInn();
        List<PointDouble> oct0  = lm.getOct();

        Point2D.Double[] rays = rays0.stream().map(Cast::toPoint).toArray(s -> new Point2D.Double[s]);
        Point2D.Double[] inn  = inn0 .stream().map(Cast::toPoint).toArray(s -> new Point2D.Double[s]);
        Point2D.Double[] oct  = oct0 .stream().map(Cast::toPoint).toArray(s -> new Point2D.Double[s]);
        Point2D.Double center = new Point2D.Double(size.width/2.0, size.height/2.0);

        HSV[] hsvPalette = lm.getPalette();
        Color[] palette = Arrays.stream(hsvPalette)
            .map(hsv -> Cast.toColor(hsv.toColor()))
            .toArray(s -> new Color[s]);

        // paint owner gradient rays
        for (int i=0; i<8; i++) {
            if (!lm.isUseGradient()) {
                g.setColor(Cast.toColor(hsvPalette[i].toColor().darker()));
                fillPolygon(g, rays[i], oct[i], inn[i], oct[(i+5)%8]);
            } else {
                // emulate triangle gradient (see BmpLogo.cpp C++ source code)
                // over linear gragients

                g.setPaint(new GradientPaint(rays[i], palette[(i+1)%8], inn[i], palette[(i+6)%8]));
                fillPolygon(g, rays[i], oct[i], inn[i], oct[(i+5)%8]);

                Point2D.Double p1 = oct[i];
                Point2D.Double p2 = oct[(i+5)%8];
                Point2D.Double p = new Point2D.Double((p1.getX()+p2.getX())/2, (p1.getY()+p2.getY())/2); // середина линии oct[i]-oct[(i+5)%8]. По факту - пересечение линий rays[i]-inn[i] и oct[i]-oct[(i+5)%8]

                Color clr;// = new Color(255,255,255,0); //  Cast.toColor(fmg.common.Color.Transparent);
                if (true) {
                    HSV c1 = hsvPalette[(i+1)%8];
                    HSV c2 = hsvPalette[(i+6)%8];
                    double diff = c1.h - c2.h;
                    HSV cP = new HSV(c1.toColor());
                    cP.h += diff/2; // цвет в точке p (пересечений линий...)
                    cP.a = 0;
                    clr = Cast.toColor(cP.toColor());
                }

                g.setPaint(new GradientPaint(oct[i], palette[(i+3)%8], p, clr));
                fillPolygon(g, rays[i], oct[i], inn[i]);

                g.setPaint(new GradientPaint(oct[(i+5)%8], palette[(i+0)%8], p, clr));
                fillPolygon(g, rays[i], oct[(i+5)%8], inn[i]);
            }
        }

        // paint star perimeter
        double zoomAverage = (lm.getZoomX() + lm.getZoomY())/2;
        final double penWidth = lm.getBorderWidth() * zoomAverage;
        if (penWidth > 0.1) {
            g.setStroke(new BasicStroke((float)penWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
            for (int i=0; i<8; i++) {
                Point2D.Double p1 = rays[(i + 7)%8];
                Point2D.Double p2 = rays[i];
                g.setColor(palette[i].darker());
                g.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
            }
        }

        // paint inner gradient triangles
        for (int i=0; i<8; i++) {
            if (lm.isUseGradient()) {
                Point2D.Double p1 = inn[(i+0)%8];
                Point2D.Double p2 = inn[(i+3)%8];
                Point2D.Double p = new Point2D.Double((p1.getX()+p2.getX())/2, (p1.getY()+p2.getY())/2); // center line of p1-p2
                g.setPaint(new GradientPaint(
                    p, palette[(i+6)%8],
                    center, ((i & 1) == 1) ? Color.BLACK : Color.WHITE));
            } else
                g.setColor(((i & 1) == 1)
                    ? Cast.toColor(hsvPalette[(i + 6)%8].toColor().brighter())
                    : Cast.toColor(hsvPalette[(i + 6)%8].toColor().darker()));
            fillPolygon(g, inn[(i + 0)%8], inn[(i + 3)%8], center);
        }
    }

    private static void fillPolygon(Graphics2D g, Point2D.Double... p) {
        g.fillPolygon(
            Arrays.stream(p).mapToInt(s -> (int)s.x).toArray(),
            Arrays.stream(p).mapToInt(s -> (int)s.y).toArray(),
            p.length);
    }


    /** Logo image controller implementation for {@link javax.swing.Icon} */
    public static class LogoSwingIconController extends LogoController2<javax.swing.Icon, SwingIconView<LogoModel2>> {

        public LogoSwingIconController() {
            var model = new LogoModel2();
            var view = new SwingIconView<>(model, g -> Logo2.draw(g, model));
            init(model, view);
        }

    }

    /** Logo image controller implementation for {@link java.awt.Image} */
    public static class LogoAwtImageController extends LogoController2<java.awt.Image, AwtImageView<LogoModel2>> {

        public LogoAwtImageController() {
            var model = new LogoModel2();
            var view = new AwtImageView<>(model, g -> Logo2.draw(g, model));
            init(model, view);
        }

    }

}
