package fmg.jfx.mosaic;

import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javafx.geometry.Bounds;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import fmg.common.Color;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.RegionDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.mosaic.BackgroundFill;
import fmg.core.mosaic.MosaicDrawModel;
import fmg.core.mosaic.MosaicView;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EClose;
import fmg.core.types.EOpen;
import fmg.core.types.EState;
import fmg.core.types.draw.FontInfo;
import fmg.core.types.draw.PenBorder;
import fmg.jfx.utils.Cast;

/** MVC: view. Abstract JFX implementation */
public abstract class MosaicJfxView<TImage,
                                    TImageInner,
                                    TMosaicModel extends MosaicDrawModel<TImageInner>>
                 extends MosaicView<TImage, TImageInner, TMosaicModel>
{

    private Font _font;
    /** cached Text for quick drawing */
    private final Map<String /* text */, Text> _mapText = new HashMap<>();
    protected boolean _alreadyPainted = false;

    protected MosaicJfxView(TMosaicModel mosaicModel) {
        super(mosaicModel);
    }

    protected void drawJfx(GraphicsContext g, Collection<BaseCell> toDrawCells, boolean drawBk) {
        assert !_alreadyPainted;
        _alreadyPainted = true;

        TMosaicModel model = getModel();
        SizeDouble size = model.getSize();

        // save
        Paint oldFill = g.getFill();
        Paint oldStroke = g.getStroke();
        Font oldFont = g.getFont();

        // 1. background color
        Color bkClr = model.getBackgroundColor();
        if (drawBk) {
            if (!bkClr.isOpaque())
                g.clearRect(0, 0, size.width, size.height);
            if (!bkClr.isTransparent()) {
                g.setFill(Cast.toColor(bkClr));
                g.fillRect(0, 0, size.width, size.height);
            }
        }

        // 2. paint cells
        g.setFont(getFont());
        PenBorder pen = model.getPenBorder();
        g.setLineWidth(pen.getWidth());
        SizeDouble offset = model.getMosaicOffset();
        boolean isSimpleDraw = pen.getColorLight().equals(pen.getColorShadow());
        BackgroundFill bkFill = model.getBackgroundFill();

        if (_DEBUG_DRAW_FLOW)
            System.out.println("MosaicJfxView.drawJfx: " + ((toDrawCells==null) ? "all" : ("cnt=" + toDrawCells.size()))
                                                         + "; drawBk=" + drawBk);

        /** HINT: Using the {@link GraphicsContext#clip} method slows down drawing (animation).
            * Especially noticeable at startup demo {@link MosaicCanvasController#main} */
        boolean useClip = !isSimpleDraw;

        if (toDrawCells == null)
            toDrawCells = model.getMatrix();
        for (BaseCell cell: toDrawCells) {
            if (useClip)
                g.save();

            RectDouble rcInner = cell.getRcInner(pen.getWidth()).moveXY(offset);
            RegionDouble poly = RegionDouble.moveXY(cell.getRegion(), offset);

            if (useClip) {
                // ограничиваю рисование только границами своей фигуры
                g.beginPath();
//                    if (false) {
//                       // variant 1
//                       StringJoiner sj = new StringJoiner(" L ", "M ", " z");
//                       poly.getPoints().forEach(p -> sj.add(String.format(Locale.US, "%.2f %.2f", p.x, p.y)));
//                       g.appendSVGPath(sj.toString());
//                    } else
                {
                    // variant 2
                    boolean first = true;
                    for (PointDouble p : poly.getPoints()) {
                        if (first) {
                            first = false;
                            g.moveTo(p.x, p.y);
                        } else {
                            g.lineTo(p.x, p.y);
                        }
                    }
                }
                g.closePath();
                g.clip();
            }

            double[] polyX = null;
            double[] polyY = null;
            { // 2.1. paint component

                // 2.1.1. paint cell background
                //if (!isIconicMode) // когда русуется иконка, а не игровое поле, - делаю попроще...
                {
                    Color bkClrCell = cell.getBackgroundFillColor(bkFill.getMode(),
                                                                  bkClr,
                                                                  bkFill.getColors());
                    if (!drawBk || !bkClrCell.equals(bkClr)) {
                        g.setFill(Cast.toColor(bkClrCell));
                        polyX = Cast.toPolygon(poly, true);
                        polyY = Cast.toPolygon(poly, false);
                        g.fillPolygon(polyX, polyY, polyX.length);
                    }
                }

//                    g.setStroke(Cast.toColor(Color.Magenta));
//                    g.strokeRect(rcInner.x, rcInner.y, rcInner.width, rcInner.height);

                Consumer<TImageInner> paintImage = img -> {
                    if (img instanceof Image) {
                        g.drawImage((Image)img, rcInner.x, rcInner.y);
                    } else {
                        throw new RuntimeException("Unsupported image type " + img.getClass().getSimpleName());
                    }
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
                        g.setFill(Cast.toColor(model.getColorText().getColorClose(cell.getState().getClose().ordinal())));
                        szCaption = cell.getState().getClose().toCaption();
                      //szCaption = cell.getCoord().x + ";" + cell.getCoord().y; // debug
                      //szCaption = ""+cell.getDirection(); // debug
                    } else {
                        g.setFill(Cast.toColor(model.getColorText().getColorOpen(cell.getState().getOpen().ordinal())));
                        szCaption = cell.getState().getOpen().toCaption();
                    }
                    if ((szCaption != null) && (szCaption.length() > 0) && (model.getFontInfo().getSize() >= 1)) {
                        if (cell.getState().isDown())
                            rcInner.moveXY(pen.getWidth(), pen.getWidth());
                        drawText(g, szCaption, rcInner);
                        //{ // test
                        //    Paint clrOld = g.getStroke(); // test
                        //    g.setStroke(Cast.toColor(Color.Red));
                        //    g.strokeRect(rcInner.x, rcInner.y, rcInner.width, rcInner.height);
                        //    g.setStroke(clrOld);
                        //}
                    }
                }

            }

            // 2.2. paint border
            {
                // draw border lines
                boolean down = cell.getState().isDown() || (cell.getState().getStatus() == EState._Open);
                g.setStroke(Cast.toColor(down
                                           ? pen.getColorLight()
                                           : pen.getColorShadow()));
                if (isSimpleDraw) {
                    if (polyX == null)
                        polyX = Cast.toPolygon(poly, true);
                    if (polyY == null)
                        polyY = Cast.toPolygon(poly, false);
                    g.strokePolygon(polyX, polyY, polyX.length);
                } else {
                    int s = cell.getShiftPointBorderIndex();
                    int v = cell.getAttr().getVertexNumber(cell.getDirection());
                    for (int i=0; i<v; i++) {
                        PointDouble p1 = cell.getRegion().getPoint(i);
                        PointDouble p2 = (i != (v-1))
                                            ? cell.getRegion().getPoint(i+1)
                                            : cell.getRegion().getPoint(0);
                        if (i==s)
                            g.setStroke(Cast.toColor(down
                                                        ? pen.getColorShadow()
                                                        : pen.getColorLight()));
                        g.strokeLine(p1.x+offset.width, p1.y+offset.height, p2.x+offset.width, p2.y+offset.height);
                    }
                }

                // debug - визуально проверяю верность вписанного квадрата (проверять при ширине пера около 21)
                //g.setColor(java.awt.Color.MAGENTA);
                //g.drawRect(rcInner.x, rcInner.y, rcInner.width, rcInner.height);
            }

            if (useClip)
               g.restore();
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
            g.drawRect(padding.left,
                       padding.top,
                       size.width  - padding.getLeftAndRight(),
                       size.height - padding.getTopAndBottom());

            // test margin
            g.setStroke(new BasicStroke(3));
            clr = Color.DarkGreen.clone();
            clr.setA(120);
            g.setColor(Cast.toColor(clr));
            g.drawRect(padding.left + margin.left,
                       padding.top  + margin.top,
                       size.width  - padding.getLeftAndRight() - margin.getLeftAndRight(),
                       size.height - padding.getTopAndBottom() - margin.getTopAndBottom());
        }
        /**/

        if (_DEBUG_DRAW_FLOW)
            System.out.println("-------------------------------");

        // restore
        g.setFont(oldFont);
        g.setStroke(oldStroke);
        g.setFill(oldFill);

        _alreadyPainted = false;
    }

    private Bounds getStringBounds(String text) {
        Text t = _mapText.get(text);
        if (t == null) {
            t = new Text(text);
            t.setFont(getFont());
          //t.setTextAlignment(TextAlignment.CENTER);
          //t.setTextOrigin(VPos.CENTER);
            _mapText.put(text, t);
        }
        return t.getLayoutBounds();
    }

    private void drawText(GraphicsContext g, String text, RectDouble rc) {
        if ((text == null) || text.trim().isEmpty())
            return;
        Bounds bnd = getStringBounds(text);
//        { // test
//            Paint clrOld = g.getStroke();
//            g.setLineWidth(1);
//            g.setStroke(Cast.toColor(Color.Blue));
//            g.strokeRect(bnd.getMinX()+rc.x, bnd.getMinY()+rc.y, bnd.getWidth(), bnd.getHeight());
//            g.setStroke(Cast.toColor(Color.Red));
//            g.strokeRect(rc.x, rc.y, rc.width, rc.height);
//            g.setStroke(clrOld);
//        }
        g.setTextAlign(TextAlignment.LEFT);
        g.setTextBaseline(VPos.TOP);
        g.fillText(text,
                   rc.x+(rc.width -bnd.getWidth ())/2.,
                   rc.y+(rc.height-bnd.getHeight())/2.);
    }

    protected Font getFont() {
        if (_font == null) {
            FontInfo fi = getModel().getFontInfo();
            _font = new Font(fi.getName(), /*fi.isBold() ? Font.BOLD : Font.PLAIN, */fi.getSize());
        }
        return _font;
    }

    @Override
    protected void onModelPropertyChanged(PropertyChangeEvent ev) {
        super.onModelPropertyChanged(ev);
        if (MosaicDrawModel.PROPERTY_FONT_INFO.equals(ev.getPropertyName())) {
            _font = null;
            _mapText.clear();
        }
    }

    @Override
    public void close() {
        _mapText.clear();
        super.close();
    }

}
