package fmg.android.mosaic;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;

import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.function.Consumer;

import fmg.common.Color;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.RegionDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.mosaic.MosaicDrawModel;
import fmg.core.mosaic.MosaicDrawModel.BackgroundFill;
import fmg.core.mosaic.MosaicView;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EClose;
import fmg.core.types.EOpen;
import fmg.core.types.EState;
import fmg.core.types.draw.FontInfo;
import fmg.core.types.draw.PenBorder;
import fmg.android.utils.Cast;
import fmg.android.utils.StaticInitializer;

/** MVC: view. Abstract android implementation
 * @param <TImage> platform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageInner> image type of flag/mine into mosaic field
 * @param <TMosaicModel> mosaic data model
 */
public abstract class MosaicAndroidView<TImage,
                                        TImageInner,
                                        TMosaicModel extends MosaicDrawModel<TImageInner>>
                     extends MosaicView<TImage, TImageInner, TMosaicModel>
{

    private final Paint _textPaint;
    protected boolean _alreadyPainted = false;

    protected MosaicAndroidView(TMosaicModel mosaicModel) {
        super(mosaicModel);
        _textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _textPaint.setStyle(Paint.Style.FILL);
    }


    static {
        StaticInitializer.init();
    }

    protected void drawAndroid(Canvas g, Collection<BaseCell> toDrawCells, boolean drawBk) {
        assert !_alreadyPainted;
        _alreadyPainted = true;

        TMosaicModel model = getModel();
        SizeDouble size = model.getSize();

        // save
        g.save();

        Paint paintStroke = new Paint(Paint.ANTI_ALIAS_FLAG); paintStroke.setStyle(Paint.Style.STROKE);
        Paint paintFill   = new Paint(Paint.ANTI_ALIAS_FLAG); paintFill  .setStyle(Paint.Style.FILL);

        // 1. background color
        Color bkClr = model.getBackgroundColor();
        if (drawBk) {
            if (!bkClr.isOpaque())
                g.drawColor(android.graphics.Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            if (!bkClr.isTransparent()) {
                paintFill.setColor(Cast.toColor(bkClr));
                g.drawColor(Cast.toColor(bkClr));//, PorterDuff.Mode.CLEAR);
            }
        }

        // 2. paint cells
        PenBorder pen = model.getPenBorder();
        paintStroke.setStrokeWidth((float)pen.getWidth());
        SizeDouble offset = model.getMosaicOffset();
        boolean isSimpleDraw = pen.getColorLight().equals(pen.getColorShadow());
        BackgroundFill bkFill = model.getBackgroundFill();

        if (_DEBUG_DRAW_FLOW)
            System.out.println("MosaicAndroidView.drawAndroid: " + ((toDrawCells==null) ? "all" : ("cnt=" + toDrawCells.size()))
                                                                 + "; drawBk=" + drawBk);
        if (toDrawCells == null)
            toDrawCells = model.getMatrix();
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
                //if (!isSimpleDraw) // когда русуется иконка, а не игровое поле, - делаю попроще...
                {
                    Color bkClrCell = cell.getBackgroundFillColor(bkFill.getMode(),
                                                                    bkClr,
                                                                    bkFill.getColors());
                    if (!drawBk || !bkClrCell.equals(bkClr)) {
                        paintFill.setColor(Cast.toColor(bkClrCell));
                        g.drawPath(poly, paintFill);
                    }
                }

              //g.setColor(java.awt.Color.MAGENTA);
              //g.drawRect((float)rcInner.x, (float)rcInner.y, (float)rcInner.width, (float)rcInner.height);

                Consumer<TImageInner> paintImage = img -> {
                    if (img instanceof android.graphics.Bitmap)
                        g.drawBitmap((android.graphics.Bitmap)img, (float)rcInner.x, (float)rcInner.y, null);
                    else
                        throw new RuntimeException("Unsupported image type " + img.getClass().getSimpleName());
                };

                // 2.1.2. output pictures
                if ((model.getImgFlag() != null) &&
                    (cell.getState().getStatus() == EState._Close) &&
                    (cell.getState().getClose()  == EClose._Flag))
                {
                    paintImage.accept(model.getImgFlag());
                } else
                if ((model.getImgMine() != null) &&
                    (cell.getState().getStatus() == EState._Open) &&
                    (cell.getState().getOpen()   == EOpen._Mine))
                {
                    paintImage.accept(model.getImgMine());
                } else
                // 2.1.3. output text
                {
                    String szCaption;
                    if (cell.getState().getStatus() == EState._Close) {
                        _textPaint.setColor(Cast.toColor(model.getColorText().getColorClose(cell.getState().getClose().ordinal())));
                        szCaption = cell.getState().getClose().toCaption();
                      //szCaption = cell.getCoord().x + ";" + cell.getCoord().y; // debug
                      //szCaption = ""+cell.getDirection(); // debug
                    } else {
                        _textPaint.setColor(Cast.toColor(model.getColorText().getColorOpen(cell.getState().getOpen().ordinal())));
                        szCaption = cell.getState().getOpen().toCaption();
                    }
                    if ((szCaption != null) && (szCaption.length() > 0)) {
                        if (cell.getState().isDown())
                            rcInner.moveXY(1, 1);
                        drawText(g, szCaption, rcInner);
                      //{ // test
                      //    java.awt.Color clrOld = g.getColor(); // test
                      //    g.setColor(java.awt.Color.red);
                      //    g.drawRect((float)rcInner.x, (float)rcInner.y, (float)rcInner.width, (float)rcInner.height);
                      //    g.setColor(clrOld);
                      //}
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
                    int v = cell.getAttr().getVertexNumber(cell.getDirection());
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

                // debug - визуально проверяю верность вписанного квадрата (проверять при ширине пера около 21)
              //g.setColor(java.awt.Color.MAGENTA);
              //g.drawRect((float)rcInner.x, (float)rcInner.y, (float)rcInner.width, (float)rcInner.height);
            }

            //if (!isSimpleDraw)
            {
                g.restore();
            }
        }

        /** /
        // test
        {
            g.setClip(oldShape);
            //g.setComposite(AlphaComposite.SrcOver);

            // test padding
            g.setStroke(new BasicStroke(5));
            Color clr = Color.DarkRed.clone();
            clr.setA(120);
            g.setColor(Cast.toColor(clr));
            g.drawRect((float)padding.left,
                       (float)padding.top,
                       (float)(size.width  - padding.getLeftAndRight()),
                       (float)(size.height - padding.getTopAndBottom()));

            // test margin
            g.setStroke(new BasicStroke(3));
            clr = Color.DarkGreen.clone();
            clr.setA(120);
            g.setColor(Cast.toColor(clr));
            g.drawRect((float)(padding.left + margin.left),
                       (float)(padding.top  + margin.top),
                       (float)(size.width  - padding.getLeftAndRight() - margin.getLeftAndRight()),
                       (float)(size.height - padding.getTopAndBottom() - margin.getTopAndBottom()));
        }
        /**/

        if (_DEBUG_DRAW_FLOW)
            System.out.println("-------------------------------");

        // restore
        g.restore();

        _alreadyPainted = false;
    }

    private RectDouble getStringBounds(String text) {
       RectDouble r;
       if (true) {
          Rect r2 = new Rect();
          _textPaint.getTextBounds(text, 0, text.length(), r2);
          r = new RectDouble(r2.left, r2.top, r2.width(), r2.height());
       } else {
          float textWidth = _textPaint.measureText(text);
          r = new RectDouble(textWidth, getModel().getFontInfo().getSize());
       }
       return r;
    }

    private void drawText(Canvas g, String text, RectDouble rc) {
        if ((text == null) || text.trim().isEmpty())
            return;
        RectDouble bnd = getStringBounds(text);
//        { // test
//            java.awt.Color clrOld = g.getColor();
//            g.setColor(java.awt.Color.BLUE);
//            g.fillRect((float)rc.x, (float)rc.y, (float)rc.width, (float)rc.height);
//            g.setColor(clrOld);
//        }
        g.drawText(text, (float)(rc.x       +(rc.width -bnd.width )/2.),
                         (float)(rc.bottom()-(rc.height-bnd.height)/2.), _textPaint);
    }

    @Override
    protected void onPropertyModelChanged(PropertyChangeEvent ev) {
        super.onPropertyModelChanged(ev);
        if (MosaicDrawModel.PROPERTY_FONT_INFO.equals(ev.getPropertyName())) {
            FontInfo fi = getModel().getFontInfo();
            Typeface tf;
            try {
                tf = Typeface.create(fi.getName(), fi.isBold() ? Typeface.BOLD : Typeface.NORMAL);
            } catch(Throwable ex) {
                ex.printStackTrace(System.err);
                tf = Typeface.create(Typeface.DEFAULT, fi.isBold() ? Typeface.BOLD : Typeface.NORMAL);
            }
            _textPaint.setTypeface(tf);
            _textPaint.setTextSize((float)fi.getSize());
        }
    }

}
