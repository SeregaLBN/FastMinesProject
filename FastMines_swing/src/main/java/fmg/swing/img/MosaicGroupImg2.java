package fmg.swing.img;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.core.img.BurgerMenuModel2;
import fmg.core.img.MosaicGroupController2;
import fmg.core.img.MosaicGroupModel2;
import fmg.core.types.EMosaicGroup;
import fmg.swing.utils.Cast;

/** Representable {@link fmg.core.types.EMosaicGroup} as image */
public final class MosaicGroupImg2 {
    private MosaicGroupImg2() {}

    private static void draw(Graphics2D g, MosaicGroupModel2 m, BurgerMenuModel2 bm) {
        var size = m.getSize();
        { // fill background
            g.setComposite(AlphaComposite.Src);
            g.setColor(Cast.toColor(m.getBackgroundColor()));
            g.fillRect(0, 0, (int)size.width, (int)size.height);
        }

        g.setComposite(AlphaComposite.SrcOver);
        double bw = m.getBorderWidth();
        boolean needDrawPerimeterBorder = (!m.getBorderColor().isTransparent() && (bw > 0));
        java.awt.Color borderColor = !needDrawPerimeterBorder ? null : Cast.toColor(m.getBorderColor());
        BasicStroke bs = !needDrawPerimeterBorder ? null : new BasicStroke((float)bw);
        Stream<Pair<Color, Stream<PointDouble>>> shapes = m.getCoords();
        shapes.forEach(pair -> {
            Polygon poly = Cast.toPolygon(pair.second.collect(Collectors.toList()));
            if (!pair.first.isTransparent()) {
                g.setColor(Cast.toColor(pair.first));
                g.fillPolygon(poly);
            }

            // draw perimeter border
            if (needDrawPerimeterBorder) {
                g.setColor(borderColor);
                g.setStroke(bs);
                g.drawPolygon(poly);
            }
        });

        // draw burger menu
        if (m.getMosaicGroup() == null)
            bm.getCoords()
                .forEach(li -> {
                    g.setStroke(new BasicStroke((float)li.penWidht));
                    g.setColor(Cast.toColor(li.clr));
                    g.drawLine((int)li.from.x, (int)li.from.y, (int)li.to.x, (int)li.to.y);
                });
    }

    /** MosaicGroup image controller implementation for {@link javax.swing.Icon} */
    public static class MosaicGroupSwingIconController extends MosaicGroupController2<javax.swing.Icon, SwingIconView<MosaicGroupModel2>> {

        public MosaicGroupSwingIconController(EMosaicGroup group) {
            var model = new MosaicGroupModel2(group);
            var view = new SwingIconView<>(model, this::draw);
            init(model, view);
        }

        private void draw(Graphics2D g, MosaicGroupModel2 m) {
            MosaicGroupImg2.draw(g, m, getBurgerModel());
        }

    }

    /** MosaicGroup image controller implementation for {@link java.awt.Image} */
    public static class MosaicGroupAwtImageController extends MosaicGroupController2<java.awt.Image, AwtImageView<MosaicGroupModel2>> {

        public MosaicGroupAwtImageController(EMosaicGroup group) {
            var model = new MosaicGroupModel2(group);
            var view = new AwtImageView<>(model, this::draw);
            init(model, view);
        }

        private void draw(Graphics2D g, MosaicGroupModel2 m) {
            MosaicGroupImg2.draw(g, m, getBurgerModel());
        }

    }

}
