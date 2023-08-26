package fmg.swing.img;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;

import fmg.common.geom.BoundDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.img.FlagModel2;
import fmg.core.img.ImageController2;

/** Flag image */
public final class Flag2 {
    private Flag2() {}

    private static final Color TRANSPARENT = new Color(0xFF, 0xFF, 0xFF, 0);

    private static void draw(Graphics2D g, FlagModel2 model) {
        SizeDouble size = model.getSize();
        BoundDouble padding = model.getPadding();

        g.setComposite(AlphaComposite.Src);
        g.setColor(TRANSPARENT);
        g.fillRect(0, 0, (int)size.width, (int)size.height);

        double width  = size.width  - padding.left - padding.right;
        double height = size.height - padding.top  - padding.bottom;
        double offsetX = padding.left;
        double offsetY = padding.top ;
        double w = width  / 100.0;
        double h = height / 100.0;

        // perimeter figure points
        Point2D.Double[] p = new Point2D.Double[] {
            new Point2D.Double(offsetX + 13.5 *w, offsetY + 90*h),
            new Point2D.Double(offsetX + 17.44*w, offsetY + 51*h),
            new Point2D.Double(offsetX + 21   *w, offsetY + 16*h),
            new Point2D.Double(offsetX + 85   *w, offsetY + 15*h),
            new Point2D.Double(offsetX + 81.45*w, offsetY + 50*h)};

        double penWidth = 7 * (w + h) / 2;
        BasicStroke penLine = new BasicStroke((float)penWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
        g.setStroke(penLine);
        g.setColor(Color.BLACK);
        g.drawLine((int)p[0].x, (int)p[0].y, (int)p[1].x, (int)p[1].y);

        BasicStroke penCurve = new BasicStroke((float)penWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
        g.setStroke(penCurve);
        g.setColor(Color.RED);
        CubicCurve2D curve = new CubicCurve2D.Double(
            p[2].x, p[2].y,
            offsetX + 95.0*w, offsetY +  0*h,
            offsetX + 19.3*w, offsetY + 32*h,
            p[3].x, p[3].y);
        g.draw(curve);

        curve = new CubicCurve2D.Double(
            p[3].x, p[3].y,
            offsetX + 77.80*w, offsetY + 32.89*h,
            offsetX + 88.05*w, offsetY + 22.73*h,
            p[4].x, p[4].y);
        g.draw(curve);

        curve = new CubicCurve2D.Double(
            p[4].x, p[4].y,
            offsetX + 15.83*w, offsetY + 67*h,
            offsetX + 91.45*w, offsetY + 35*h,
            p[1].x, p[1].y);
        g.draw(curve);

        g.setStroke(penLine);
        g.drawLine((int)p[1].x, (int)p[1].y, (int)p[2].x, (int)p[2].y);
    }

    /** Flag image controller implementation for {@link javax.swing.Icon} */
    public static class FlagSwingIconController extends ImageController2<javax.swing.Icon, SwingIconView<FlagModel2>, FlagModel2> {

        public FlagSwingIconController() {
            var model = new FlagModel2();
            var view = new SwingIconView<>(model, g -> Flag2.draw(g, model));
            init(model, view);
        }

    }

    /** Flag image controller implementation for {@link java.awt.Image} */
    public static class FlagAwtImageController extends ImageController2<java.awt.Image, AwtImageView<FlagModel2>, FlagModel2> {

        public FlagAwtImageController() {
            var model = new FlagModel2();
            var view = new AwtImageView<>(model, g -> Flag2.draw(g, model));
            init(model, view);
        }

    }

}
