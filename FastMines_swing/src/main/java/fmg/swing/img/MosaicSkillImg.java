package fmg.swing.img;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.HSV;
import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.core.img.BurgerMenuModel;
import fmg.core.img.MosaicSkillController;
import fmg.core.img.MosaicSkillModel;
import fmg.core.types.ESkillLevel;
import fmg.swing.utils.Cast;

/** Representable {@link fmg.core.types.ESkillLevel} as image */
public final class MosaicSkillImg {
    private MosaicSkillImg() {}

    private static void draw(Graphics2D g, MosaicSkillModel m, BurgerMenuModel bm) {
        var size = m.getSize();
        { // fill background
            g.setComposite(AlphaComposite.Src);
            var bk = new HSV(m.getBackgroundColor()).addHue(m.getBackgroundAngle());
            g.setColor(Cast.toColor(bk.toColor()));
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
        if (m.getMosaicSkill() == null)
            bm.getCoords()
                .forEach(li -> {
                    g.setStroke(new BasicStroke((float)li.penWidht));
                    g.setColor(Cast.toColor(li.clr));
                    g.drawLine((int)li.from.x, (int)li.from.y, (int)li.to.x, (int)li.to.y);
                });
    }

    /** MosaicSkill image controller implementation for {@link javax.swing.Icon} */
    public static class MosaicSkillSwingIconController extends MosaicSkillController<javax.swing.Icon, SwingIconView<MosaicSkillModel>> {

        public MosaicSkillSwingIconController(ESkillLevel skill) {
            var model = new MosaicSkillModel(skill);
            var view = new SwingIconView<>(model, g -> MosaicSkillImg.draw(g, model, getBurgerMenuModel()));
            init(model, view);
        }

    }

    /** MosaicSkill image controller implementation for {@link java.awt.Image} */
    public static class MosaicSkillAwtImageController extends MosaicSkillController<java.awt.Image, AwtImageView<MosaicSkillModel>> {

        public MosaicSkillAwtImageController(ESkillLevel skill) {
            var model = new MosaicSkillModel(skill);
            var view = new AwtImageView<>(model, g -> MosaicSkillImg.draw(g, model, getBurgerMenuModel()));
            init(model, view);
        }

    }

}
