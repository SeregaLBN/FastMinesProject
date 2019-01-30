package fmg.swing.mosaic;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
import fmg.swing.utils.Cast;
import fmg.swing.utils.StaticInitializer;

/** MVC: view. Abstract SWING implementation
 * @param <TImage> platform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageInner> image type of flag/mine into mosaic field
 * @param <TMosaicModel> mosaic data model
 */
public abstract class MosaicSwingView<TImage,
                                      TImageInner,
                                      TMosaicModel extends MosaicDrawModel<TImageInner>>
              extends MosaicView<TImage, TImageInner, TMosaicModel>
{
    private Font _font;
    private static final FontRenderContext _frc = new FontRenderContext(null, true, true);
    /** cached TextLayout for quick drawing */
    private final Map<String /* text */, TextLayout> _mapTextLayout = new HashMap<>();
    protected boolean _alreadyPainted = false;

    protected MosaicSwingView(TMosaicModel mosaicModel) {
        super(mosaicModel);
    }


    static {
        StaticInitializer.init();
    }


    protected void drawSwing(Graphics2D g, Collection<BaseCell> toDrawCells, boolean drawBk) {
        assert !_alreadyPainted;
        _alreadyPainted = true;

        TMosaicModel model = getModel();
        SizeDouble size = model.getSize();

        // save
        Shape oldShape = g.getClip();
        java.awt.Color oldColor = g.getColor();
        Stroke oldStroke = g.getStroke();
        Font oldFont = g.getFont();

        // 1. background color
        Color bkClr = model.getBackgroundColor();
        if (drawBk) {
            g.setComposite(AlphaComposite.Src);
            g.setColor(Cast.toColor(bkClr));
            g.fillRect(0, 0, (int)size.width, (int)size.height);
        }

        // 2. paint cells
        g.setComposite(AlphaComposite.SrcOver);
        g.setFont(getFont());
        PenBorder pen = model.getPenBorder();
        g.setStroke(new BasicStroke((float)pen.getWidth()));
        SizeDouble offset = model.getMosaicOffset();
        boolean isSimpleDraw = pen.getColorLight().equals(pen.getColorShadow());
        BackgroundFill bkFill = model.getBackgroundFill();

        /**/
        { // DEBUG
            Stroke storkeOld = g.getStroke();

            g.setColor(Cast.toColor(Color.IndianRed()));
            Stroke dotted = new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4, 12}, 0);
            g.setStroke(dotted);
            g.drawRect(0, 0, (int)size.width, (int)size.height);

            g.setColor(Cast.toColor(Color.Chartreuse()));
            dotted = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
            g.setStroke(dotted);
            g.drawRect((int)offset.width, (int)offset.height, (int)model.getMosaicSize().width, (int)model.getMosaicSize().height);

            g.setStroke(storkeOld);// restore
            if (true)
                return;
        }
        /**/

        if (_DEBUG_DRAW_FLOW)
            System.out.println("MosaicSwingView.drawSwing: " + ((toDrawCells==null) ? "all" : ("cnt=" + toDrawCells.size()))
                                                             + "; drawBk=" + drawBk);
        if (toDrawCells == null)
            toDrawCells = model.getMatrix();
        for (BaseCell cell: toDrawCells) {
            RectDouble rcInner = cell.getRcInner(pen.getWidth()).moveXY(offset);
            Polygon poly = Cast.toPolygon(RegionDouble.moveXY(cell.getRegion(), offset));

            //if (!isIconicMode)
            {
                // ограничиваю рисование только границами своей фигуры
                g.setClip(poly);
            }

            { // 2.1. paint component

                // 2.1.1. paint cell background
                //if (!isIconicMode) // когда русуется иконка, а не игровое поле, - делаю попроще...
                {
                    Color bkClrCell = cell.getBackgroundFillColor(bkFill.getMode(),
                                                                  bkClr,
                                                                  bkFill.getColors());
                    if (!drawBk || !bkClrCell.equals(bkClr)) {
                        g.setColor(Cast.toColor(bkClrCell));
                        g.fillPolygon(poly);
                    }
                }

                //g.setColor(java.awt.Color.MAGENTA);
                //g.drawRect((int)rcInner.x, (int)rcInner.y, (int)rcInner.width, (int)rcInner.height);

                Consumer<TImageInner> paintImage = img -> {
                    int x = (int)rcInner.x;
                    int y = (int)rcInner.y;
                    if (img instanceof javax.swing.Icon) {
                        ((javax.swing.Icon)img).paintIcon(null/*p.getOwner()*/, g, x, y);
                    } else
                    if (img instanceof java.awt.Image) {
                        g.drawImage((java.awt.Image)img, x, y, null);
                    } else {
                        throw new IllegalArgumentException("Unsupported image type " + img.getClass().getSimpleName());
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
                        g.setColor(Cast.toColor(model.getColorText().getColorClose(cell.getState().getClose().ordinal())));
                        szCaption = cell.getState().getClose().toCaption();
                      //szCaption = cell.getCoord().x + ";" + cell.getCoord().y; // debug
                      //szCaption = ""+cell.getDirection(); // debug
                    } else {
                        g.setColor(Cast.toColor(model.getColorText().getColorOpen(cell.getState().getOpen().ordinal())));
                        szCaption = cell.getState().getOpen().toCaption();
                    }
                    if ((szCaption != null) && (szCaption.length() > 0)) {
                        if (cell.getState().isDown())
                            rcInner.moveXY(1, 1);
                        drawText(g, szCaption, rcInner);
                      //{ // test
                      //    java.awt.Color clrOld = g.getColor(); // test
                      //    g.setColor(java.awt.Color.red);
                      //    g.drawRect((int)rcInner.x, (int)rcInner.y, (int)rcInner.width, (int)rcInner.height);
                      //    g.setColor(clrOld);
                      //}
                    }
                }

            }

            // 2.2. paint border
            {
                // draw border lines
                boolean down = cell.getState().isDown() || (cell.getState().getStatus() == EState._Open);
                g.setColor(Cast.toColor(down
                                           ? pen.getColorLight()
                                           : pen.getColorShadow()));
                if (isSimpleDraw) {
                    g.drawPolygon(poly);
                } else {
                    int s = cell.getShiftPointBorderIndex();
                    int v = cell.getAttr().getVertexNumber(cell.getDirection());
                    for (int i=0; i<v; i++) {
                        PointDouble p1 = cell.getRegion().getPoint(i);
                        PointDouble p2 = (i != (v-1))
                                            ? cell.getRegion().getPoint(i+1)
                                            : cell.getRegion().getPoint(0);
                        if (i==s)
                            g.setColor(Cast.toColor(down
                                                        ? pen.getColorShadow()
                                                        : pen.getColorLight()));
                        g.drawLine((int)(p1.x+offset.width), (int)(p1.y+offset.height), (int)(p2.x+offset.width), (int)(p2.y+offset.height));
                    }
                }

                // debug - визуально проверяю верность вписанного квадрата (проверять при ширине пера около 21)
              //g.setColor(java.awt.Color.MAGENTA);
              //g.drawRect((int)rcInner.x, (int)rcInner.y, (int)rcInner.width, (int)rcInner.height);
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
            g.drawRect((int)padding.left,
                       (int)padding.top,
                       (int)(size.width  - padding.getLeftAndRight()),
                       (int)(size.height - padding.getTopAndBottom()));

            // test margin
            g.setStroke(new BasicStroke(3));
            clr = Color.DarkGreen.clone();
            clr.setA(120);
            g.setColor(Cast.toColor(clr));
            g.drawRect((int)(padding.left + margin.left),
                       (int)(padding.top  + margin.top),
                       (int)(size.width  - padding.getLeftAndRight() - margin.getLeftAndRight()),
                       (int)(size.height - padding.getTopAndBottom() - margin.getTopAndBottom()));
        }
        /**/

        if (_DEBUG_DRAW_FLOW)
            System.out.println("-------------------------------");

        // restore
        g.setFont(oldFont);
        g.setStroke(oldStroke);
        g.setColor(oldColor);
        g.setClip(oldShape);

        _alreadyPainted = false;
    }

    private Rectangle2D getStringBounds(String text) {
        TextLayout tl = _mapTextLayout.get(text);
        _mapTextLayout.computeIfAbsent(text, k -> new TextLayout(text, getFont(), _frc));
        return tl.getBounds();
//        return font.getStringBounds(text, new FontRenderContext(null, true, true));
    }

    private void drawText(Graphics g, String text, RectDouble rc) {
        if ((text == null) || text.trim().isEmpty())
            return;
        Rectangle2D bnd = getStringBounds(text);
//        { // test
//            java.awt.Color clrOld = g.getColor();
//            g.setColor(java.awt.Color.BLUE);
//            g.fillRect((int)rc.x, (int)rc.y, (int)rc.width, (int)rc.height);
//            g.setColor(clrOld);
//        }
        g.drawString(text,
                (int)(rc.x       +(rc.width -bnd.getWidth ())/2.),
                (int)(rc.bottom()-(rc.height-bnd.getHeight())/2.));
    }

    protected Font getFont() {
        if (_font == null) {
            FontInfo fi = getModel().getFontInfo();
            _font = new Font(fi.getName(), fi.isBold() ? Font.BOLD : Font.PLAIN, (int)fi.getSize());
        }
        return _font;
    }

    @Override
    protected void onPropertyModelChanged(PropertyChangeEvent ev) {
        super.onPropertyModelChanged(ev);
        if (MosaicDrawModel.PROPERTY_FONT_INFO.equals(ev.getPropertyName())) {
            _font = null;
            _mapTextLayout.clear();
        }
    }

    @Override
    public void close() {
        _mapTextLayout.clear();
        super.close();
    }

}
