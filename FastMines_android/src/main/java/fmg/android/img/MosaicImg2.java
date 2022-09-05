package fmg.android.img;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;

import java.util.function.Consumer;

import fmg.android.app.ProjSettings;
import fmg.android.utils.Cast;
import fmg.common.Color;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.RegionDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.img.MosaicDrawContext;
import fmg.core.img.MosaicImageController2;
import fmg.core.img.MosaicImageModel2;
import fmg.core.mosaic.MosaicModel2;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EClose;
import fmg.core.types.EOpen;
import fmg.core.types.EState;
import fmg.core.types.draw.PenBorder2;

/** Representable {@link fmg.core.types.EMosaic} as image */
public final class MosaicImg2 {
    private MosaicImg2() {}

    private static final Paint textPaint;

    static {
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStyle(Paint.Style.FILL);
    }

    private static void draw(Canvas g, MosaicImageModel2 m) {
        MosaicImageController2.<Void>draw(m, ctx -> draw(g, ctx));
    }

    public static <T> void draw(Canvas g, MosaicDrawContext<T> drawContext) {
        var m = drawContext.model;
        SizeDouble size = m.getSize();

        // save
        g.save();

        Paint paintStroke = new Paint(Paint.ANTI_ALIAS_FLAG); paintStroke.setStyle(Paint.Style.STROKE);
        Paint paintFill   = new Paint(Paint.ANTI_ALIAS_FLAG); paintFill  .setStyle(Paint.Style.FILL);

        // 1. background color
        Color bkClr = (drawContext.getBackgroundColor == null)
                ? m.getBackgroundColor()
                : drawContext.getBackgroundColor.get();
        if (drawContext.drawBackground) {
            if (!bkClr.isOpaque())
                g.drawColor(android.graphics.Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            if (!bkClr.isTransparent()) {
                int clr = Cast.toColor(bkClr);
                paintFill.setColor(clr);
                g.drawColor(clr);//, PorterDuff.Mode.CLEAR);
            }
        }

        /**/ // debug
        if (!ProjSettings.isDrawModeFull()) {
            // draw border only
            Rect rc = new Rect(0, 0, (int)size.width, (int)size.height);
            g.drawRect(rc, new Paint(Paint.ANTI_ALIAS_FLAG) {{
                this.setStyle(Paint.Style.STROKE);
                setStrokeWidth(1.5f);
                setColor(android.graphics.Color.RED);
            }});
            return;
        }

        // 2. paint cells
        PenBorder2 pen = m.getPenBorder();
        paintStroke.setStrokeWidth((float)pen.getWidth());
        SizeDouble offset = m.getMosaicOffset();
        boolean isSimpleDraw = pen.getColorLight().equals(pen.getColorShadow());
        Color cellColor = m.getCellColor();

        var toDrawCells = (drawContext.drawCells == null)
                ? m.getMatrix()
                : drawContext.drawCells.get();

        int fillMode = m.getFillMode();
        var imgMine = (drawContext.mineImage == null) ? null : drawContext.mineImage.get();
        var imgFlag = (drawContext.flagImage == null) ? null : drawContext.flagImage.get();
        for (BaseCell cell: toDrawCells) {
            RectDouble rcInner = cell.getRcInner(pen.getWidth()).moveXY(offset);
            Path poly = Cast.toPolygon(RegionDouble.moveXY(cell.getRegion(), offset));

            //if (!isSimpleDraw)
            {
                g.save();
                // ограничиваю рисование только границами своей фигуры
                g.clipPath(poly);
            }

            { // 2.1. paint component

                // 2.1.1. paint cell background
                //if (!isSimpleDraw) // когда рисуется иконка, а не игровое поле, - делаю попроще...
                {
                    Color bkClrCell = cell.getCellFillColor(fillMode,
                                                            cellColor,
                                                            m::getFillColor);
                    if (!drawContext.drawBackground || !bkClrCell.equals(bkClr)) {
                        paintFill.setColor(Cast.toColor(bkClrCell));
                        g.drawPath(poly, paintFill);
                    }
                }

                Consumer<Object> paintImage = img -> {
                    if (img instanceof android.graphics.Bitmap)
                        g.drawBitmap((android.graphics.Bitmap)img, (float)rcInner.x, (float)rcInner.y, null);
                    else
                        throw new RuntimeException("Unsupported image type " + img.getClass().getSimpleName());
                };

                // 2.1.2. output pictures
                if ((imgFlag != null) &&
                    (cell.getState().getStatus() == EState._Close) &&
                    (cell.getState().getClose()  == EClose._Flag))
                {
                    paintImage.accept(imgFlag);
                } else
                if ((imgMine != null) &&
                    (cell.getState().getStatus() == EState._Open) &&
                    (cell.getState().getOpen()   == EOpen._Mine))
                {
                    paintImage.accept(imgMine);
                } else
                // 2.1.3. output text
                {
                    String szCaption;
                    if (cell.getState().getStatus() == EState._Close) {
                        textPaint.setColor(Cast.toColor(cell.getState().getClose().asColor()));
                        szCaption = cell.getState().getClose().toCaption();
                    } else {
                        textPaint.setColor(Cast.toColor(cell.getState().getOpen().asColor()));
                        szCaption = cell.getState().getOpen().toCaption();
                    }
                    if ((szCaption != null) && (szCaption.length() > 0) && (m.getFontInfo().getSize() >= 1)) {
                        if (cell.getState().isDown())
                            rcInner.moveXY(pen.getWidth(), pen.getWidth());
                        drawText(g, m, szCaption, rcInner);
                    }
                }

            }

            // 2.2. paint border
            {
                // draw border lines
                boolean down = cell.getState().isDown() || (cell.getState().getStatus() == EState._Open);
                paintStroke.setColor(Cast.toColor(down
                        ? pen.getColorLight()
                        : pen.getColorShadow()));
                if (isSimpleDraw) {
                    g.drawPath(poly, paintStroke);
                } else {
                    int s = cell.getShiftPointBorderIndex();
                    int v = cell.getShape().getVertexNumber(cell.getDirection());
                    for (int i=0; i<v; i++) {
                        PointDouble p1 = cell.getRegion().getPoint(i);
                        PointDouble p2 = (i != (v-1))
                                ? cell.getRegion().getPoint(i+1)
                                : cell.getRegion().getPoint(0);
                        if (i==s)
                            paintStroke.setColor(Cast.toColor(down
                                    ? pen.getColorShadow()
                                    : pen.getColorLight()));
                        g.drawLine((float)(p1.x+offset.width), (float)(p1.y+offset.height), (float)(p2.x+offset.width), (float)(p2.y+offset.height), paintStroke);
                    }
                }
            }

            //if (!isSimpleDraw)
            {
                g.restore();
            }
        }

        // restore
        g.restore();
    }

    private static RectDouble getStringBounds(MosaicModel2 m, String text) {
        // variant 1
        Rect r2 = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), r2);
        return new RectDouble(r2.left, r2.top, r2.width(), r2.height());

//        // variant 2
//        float textWidth = textPaint.measureText(text);
//        return new RectDouble(textWidth, m.getFontInfo().getSize());
    }

    private static void drawText(Canvas g, MosaicModel2 m, String text, RectDouble rc) {
        if ((text == null) || text.trim().isEmpty())
            return;

        RectDouble bnd = getStringBounds(m, text);
        g.drawText(text, (float)(rc.x       +(rc.width -bnd.width )/2.),
                         (float)(rc.bottom()-(rc.height-bnd.height)/2.), textPaint);
    }


    /** Mosaic image controller implementation for {@link android.graphics.Bitmap} */
    public static class MosaicAndroidBitmapController extends MosaicImageController2<Bitmap, AndroidBitmapView<MosaicImageModel2>> {

        public MosaicAndroidBitmapController() {
            var model = new MosaicImageModel2();
            var view = new AndroidBitmapView<>(model, g -> MosaicImg2.draw(g, model));
            init(model, view);
        }

    }

}
