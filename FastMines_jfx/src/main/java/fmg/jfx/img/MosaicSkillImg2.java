package fmg.jfx.img;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.scene.canvas.GraphicsContext;

import fmg.common.Color;
import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.core.img.BurgerMenuModel2;
import fmg.core.img.MosaicSkillController2;
import fmg.core.img.MosaicSkillModel2;
import fmg.core.types.ESkillLevel;
import fmg.jfx.utils.Cast;

/** Representable {@link fmg.core.types.ESkillLevel} as image */
public final class MosaicSkillImg2 {
    private MosaicSkillImg2() {}

    private static void draw(GraphicsContext g, MosaicSkillModel2 m, BurgerMenuModel2 bm) {
        var size = m.getSize();
        { // fill background
            Color bkClr = m.getBackgroundColor();
            if (!bkClr.isOpaque())
                g.clearRect(0, 0, size.width, size.height);
            if (!bkClr.isTransparent()) {
                g.setFill(Cast.toColor(bkClr));
                g.fillRect(0, 0, size.width, size.height);
            }
        }

        double bw = m.getBorderWidth();
        boolean needDrawPerimeterBorder = (!m.getBorderColor().isTransparent() && (bw > 0));
        javafx.scene.paint.Color borderColor = !needDrawPerimeterBorder ? null : Cast.toColor(m.getBorderColor());
        if (needDrawPerimeterBorder)
            g.setLineWidth(bw);
        Stream<Pair<Color, Stream<PointDouble>>> shapes = m.getCoords();
        shapes.forEach(pair -> {
            List<PointDouble> poly = pair.second.collect(Collectors.toList());
            double[] polyX = Cast.toPolygon(poly, true);
            double[] polyY = Cast.toPolygon(poly, false);
            if (!pair.first.isTransparent()) {
                g.setFill(Cast.toColor(pair.first));
                g.fillPolygon(polyX, polyY, polyX.length);
            }

            // draw perimeter border
            if (needDrawPerimeterBorder) {
                g.setStroke(borderColor);
                g.strokePolygon(polyX, polyY, polyX.length);
            }
        });

        // draw burger menu
        if (m.getMosaicSkill() == null)
            bm.getCoords()
                .forEach(li -> {
                    g.setLineWidth(li.penWidht);
                    g.setStroke(Cast.toColor(li.clr));
                    g.strokeLine(li.from.x, li.from.y, li.to.x, li.to.y);
                });
    }

    /** MosaicSkill image controller implementation for {@link javafx.scene.canvas.Canvas} */
    public static class MosaicSkillJfxCanvasController extends MosaicSkillController2<javafx.scene.canvas.Canvas, JfxCanvasView<MosaicSkillModel2>> {

        public MosaicSkillJfxCanvasController(ESkillLevel skill) {
            var model = new MosaicSkillModel2(skill);
            var view = new JfxCanvasView<>(model, this::draw);
            init(model, view);
        }

        private void draw(GraphicsContext g, MosaicSkillModel2 m) {
            MosaicSkillImg2.draw(g, m, getBurgerModel());
        }

    }

    /** MosaicSkill image controller implementation for {@link javafx.scene.image.Image} */
    public static class MosaicSkillJfxImageController extends MosaicSkillController2<javafx.scene.image.Image, JfxImageView<MosaicSkillModel2>> {

        public MosaicSkillJfxImageController(ESkillLevel skill) {
            var model = new MosaicSkillModel2(skill);
            var view = new JfxImageView<>(model, this::draw);
            init(model, view);
        }

        private void draw(GraphicsContext g, MosaicSkillModel2 m) {
            MosaicSkillImg2.draw(g, m, getBurgerModel());
        }

    }

}
