package fmg.android.img;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.android.app.ProjSettings;
import fmg.android.utils.Cast;
import fmg.common.Color;
import fmg.common.HSV;
import fmg.common.Pair;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.core.img.BurgerMenuModel2;
import fmg.core.img.MosaicSkillController2;
import fmg.core.img.MosaicSkillModel2;
import fmg.core.types.ESkillLevel;

/** Representable {@link ESkillLevel} as image */
public final class MosaicSkillImg2 {
    private MosaicSkillImg2() {}

    private static void draw(Canvas g, MosaicSkillModel2 m, BurgerMenuModel2 bm) {
        var size = m.getSize();
        { // fill background
            var bkClr = new HSV(m.getBackgroundColor()).addHue(m.getBackgroundAngle()).toColor();
            if (!bkClr.isOpaque())
                g.drawColor(android.graphics.Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            if (!bkClr.isTransparent())
                g.drawColor(Cast.toColor(bkClr));
        }

        /**/ // debug
        if (!ProjSettings.isDrawModeFull()) {
            // draw border only
            Rect rc = new Rect(0, 0, (int)size.width, (int)size.height);
            g.drawRect(rc, new Paint(Paint.ANTI_ALIAS_FLAG) {{
                this.setStyle(Style.STROKE);
                setStrokeWidth(1.5f);
                setColor(android.graphics.Color.RED);
            }});
            return;
        }
        /**/

        float bw = (float)m.getBorderWidth();
        boolean needDrawPerimeterBorder = (!m.getBorderColor().isTransparent() && (bw > 0));
        Paint[] paintStroke = { null };
        if (needDrawPerimeterBorder) {
            paintStroke[0] = new Paint(Paint.ANTI_ALIAS_FLAG);
            paintStroke[0].setStyle(Paint.Style.STROKE);
            paintStroke[0].setStrokeWidth(bw);
            paintStroke[0].setColor(Cast.toColor(m.getBorderColor()));
        }
        Paint paintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintFill.setStyle(Paint.Style.FILL);

        Stream<Pair<Color, Stream<PointDouble>>> shapes = m.getCoords();
        shapes.forEach(pair -> {
            Path poly = Cast.toPolygon(pair.second.collect(Collectors.toList()));
            if (!pair.first.isTransparent()) {
                paintFill.setColor(Cast.toColor(pair.first));
                g.drawPath(poly, paintFill);
            }

            // draw perimeter border
            if (needDrawPerimeterBorder) {
                g.drawPath(poly, paintStroke[0]);
            }
        });

        // draw burger menu
        if (m.getMosaicSkill() != null)
            return;
        List<BurgerMenuModel2.LineInfo> coords = bm.getCoords().collect(Collectors.toList());
        if (!coords.isEmpty()) {
            boolean simple = false;
            if (simple) {
                coords.forEach(li -> {
                    paintFill.setStrokeWidth((float)li.penWidht);
                    paintFill.setColor(Cast.toColor(li.clr));
                    g.drawLine((float)li.from.x, (float)li.from.y, (float)li.to.x, (float)li.to.y, paintFill);
                });
            } else {
                BoundDouble pad = bm.getPadding();
                double width  = size.width  - pad.getLeftAndRight();
                double height = size.height - pad.getTopAndBottom();
                Bitmap bmpBurger = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);
                Canvas canvasBurger = new Canvas(bmpBurger);
                double penWidth = 0;
                for (BurgerMenuModel2.LineInfo li : coords) {
                    penWidth = li.penWidht;
                    li.from.move(-pad.left, -pad.top);
                    li.to  .move(-pad.left, -pad.top);
                    paintFill.setStrokeWidth((float)li.penWidht);
                    paintFill.setColor(Cast.toColor(li.clr));
                    canvasBurger.drawLine(
                            (float)li.from.x, (float)li.from.y,
                            (float)li.to  .x, (float)li.to  .y,
                            paintFill);
                }

                RectDouble destinationRc = new RectDouble(pad.left, pad.top, width, height);
                double offset = penWidth;
                boolean horiz = bm.isHorizontal();
                RectDouble sourceRc = new RectDouble(
                        horiz ? 0 : offset / 2,
                        horiz ? offset / 2 : 0,
                        width + (horiz ? 0 : -offset),
                        height + (horiz ? -offset : 0));
                g.drawBitmap(bmpBurger, Cast.toRectInt(sourceRc), Cast.toRect(destinationRc), null);
            }
        }
    }

    /** MosaicSkill image controller implementation for {@link Bitmap} */
    public static class MosaicSkillAndroidBitmapController extends MosaicSkillController2<Bitmap, AndroidBitmapView<MosaicSkillModel2>> {

        public MosaicSkillAndroidBitmapController(ESkillLevel group) {
            var model = new MosaicSkillModel2(group);
            var view = new AndroidBitmapView<>(model, this::draw);
            init(model, view);
        }

        private void draw(Canvas g, MosaicSkillModel2 m) {
            MosaicSkillImg2.draw(g, m, getBurgerModel());
        }

    }

}
