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

import fmg.common.Color;
import fmg.common.Pair;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.img.AnimatedImageModel;
import fmg.core.img.BurgerMenuModel;
import fmg.core.img.MosaicGroupModel;
import fmg.core.img.MosaicSkillModel;
import fmg.core.img.WithBurgerMenuView;
import fmg.android.utils.Cast;
import fmg.android.utils.StaticInitializer;

/**
 * MVC: view. Abstract Android representable {@link fmg.core.types.ESkillLevel} or {@link fmg.core.types.EMosaicGroup} as image
 * @param <TImage> platform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageModel> {@link MosaicSkillModel} or {@link MosaicGroupModel}
 */
abstract class MosaicSkillOrGroupView<TImage, TImageModel extends AnimatedImageModel> extends WithBurgerMenuView<TImage, TImageModel> {

    protected MosaicSkillOrGroupView(TImageModel imageModel) {
        super(imageModel);
    }

    /** get paint information of drawing basic image model */
    protected abstract Stream<Pair<Color, Stream<PointDouble>>> getCoords();


    protected void draw(Canvas g) {
        TImageModel m = getModel();

        { // fill background
            Color bkClr = m.getBackgroundColor();
            if (!bkClr.isOpaque())
                g.drawColor(android.graphics.Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            if (!bkClr.isTransparent())
                g.drawColor(Cast.toColor(bkClr));
        }

        /** / // debug
        Rect rc = new Rect(0,0, (int)getModel().getSize().width, (int)getModel().getSize().height);
        g.drawRect(rc, new Paint(Paint.ANTI_ALIAS_FLAG) {{
            this.setStyle(Paint.Style.STROKE);
            setStrokeWidth(1.5f);
            setColor(android.graphics.Color.RED);}});
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

        Stream<Pair<Color, Stream<PointDouble>>> shapes = getCoords();
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
        BurgerMenuModel burgerModel = getBurgerMenuModel();
        List<BurgerMenuModel.LineInfo> coords = burgerModel.getCoords().collect(Collectors.toList());
        if (!coords.isEmpty()) {
            boolean simple = false;
            if (simple) {
                coords.forEach(li -> {
                    paintFill.setStrokeWidth((float)li.penWidht);
                    paintFill.setColor(Cast.toColor(li.clr));
                    g.drawLine((float)li.from.x, (float)li.from.y, (float)li.to.x, (float)li.to.y, paintFill);
                });
            } else {
                SizeDouble size = burgerModel.getSize();
                BoundDouble pad = burgerModel.getPadding();
                double width  = size.width  - pad.getLeftAndRight();
                double height = size.height - pad.getTopAndBottom();
                Bitmap bmpBurger = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);
                Canvas canvasBurger = new Canvas(bmpBurger);
                double penWidth = 0;
                for (BurgerMenuModel.LineInfo li : coords) {
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
                boolean horiz = burgerModel.isHorizontal();
                RectDouble sourceRc = new RectDouble(
                        horiz ? 0 : offset / 2,
                        horiz ? offset / 2 : 0,
                        width + (horiz ? 0 : -offset),
                        height + (horiz ? -offset : 0));
                g.drawBitmap(bmpBurger, Cast.toRectInt(sourceRc), Cast.toRect(destinationRc), null);
            }
        }
    }

}
