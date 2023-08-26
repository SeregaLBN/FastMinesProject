package fmg.jfx.img;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.scene.canvas.GraphicsContext;

import fmg.common.Color;
import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.core.img.BurgerMenuModel;
import fmg.core.img.MosaicGroupController;
import fmg.core.img.MosaicGroupModel;
import fmg.core.types.EMosaicGroup;
import fmg.jfx.utils.Cast;

/** Representable {@link fmg.core.types.EMosaicGroup} as image */
public final class MosaicGroupImg {
    private MosaicGroupImg() {}

    private static void draw(GraphicsContext g, MosaicGroupModel m, BurgerMenuModel bm) {
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
        if (m.getMosaicGroup() == null)
            bm.getCoords()
                .forEach(li -> {
                    g.setLineWidth(li.penWidht);
                    g.setStroke(Cast.toColor(li.clr));
                    g.strokeLine(li.from.x, li.from.y, li.to.x, li.to.y);
                });
    }

    /** MosaicGroup image controller implementation for {@link javafx.scene.canvas.Canvas} */
    public static class MosaicGroupJfxCanvasController extends MosaicGroupController<javafx.scene.canvas.Canvas, JfxCanvasView<MosaicGroupModel>> {

        public MosaicGroupJfxCanvasController(EMosaicGroup group) {
            var model = new MosaicGroupModel(group);
            var view = new JfxCanvasView<>(model, g -> MosaicGroupImg.draw(g, model, getBurgerModel()));
            init(model, view);
        }

    }

    /** MosaicGroup image controller implementation for {@link javafx.scene.image.Image} */
    public static class MosaicGroupJfxImageController extends MosaicGroupController<javafx.scene.image.Image, JfxImageView<MosaicGroupModel>> {

        public MosaicGroupJfxImageController(EMosaicGroup group) {
            var model = new MosaicGroupModel(group);
            var view = new JfxImageView<>(model, g -> MosaicGroupImg.draw(g, model, getBurgerModel()));
            init(model, view);
        }

    }

}
