package fmg.swing.img;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import fmg.common.Color;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.RegionDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.img.MosaicDrawContext;
import fmg.core.img.MosaicImageController;
import fmg.core.img.MosaicImageModel;
import fmg.core.mosaic.MosaicModel;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EClose;
import fmg.core.types.EOpen;
import fmg.core.types.EState;
import fmg.core.types.draw.FontInfo;
import fmg.core.types.draw.PenBorder;
import fmg.swing.utils.Cast;

/** Representable {@link fmg.core.types.EMosaic} as image */
public final class MosaicImg {
    private MosaicImg() {}

    private static void draw(Graphics2D g, MosaicImageModel m) {
        MosaicImageController.<Void>draw(m, ctx -> draw(g, ctx));
    }

    public static <T> void draw(Graphics2D g, MosaicDrawContext<T> drawContext) {
        var m = drawContext.model;
        SizeDouble size = m.getSize();

        // save
        Shape oldShape = g.getClip();
        java.awt.Color oldColor = g.getColor();
        Stroke oldStroke = g.getStroke();
        Font oldFont = g.getFont();

        // 1. background color
        Color bkClr = (drawContext.getBackgroundColor == null)
                ? m.getBackgroundColor()
                : drawContext.getBackgroundColor.get();
        if (drawContext.drawBackground) {
            g.setComposite(AlphaComposite.Src);
            g.setColor(Cast.toColor(bkClr));
            g.fillRect(0, 0, (int)size.width, (int)size.height);
        }

        // 2. paint cells
        g.setComposite(AlphaComposite.SrcOver);
        g.setFont(getFont(m));
        PenBorder pen = m.getPenBorder();
        g.setStroke(new BasicStroke((float)pen.getWidth()));
        SizeDouble offset = m.getMosaicOffset();
        boolean isSimpleDraw = pen.getColorLight().equals(pen.getColorShadow());
        Color cellColor = m.getCellColor();

        var toDrawCells = (drawContext.drawCells == null)
                ? m.getMatrix()
                : drawContext.drawCells.get();
        if (toDrawCells.isEmpty())
            throw new IllegalArgumentException("Undefined drawing cells");

        int fillMode = m.getFillMode();
        var imgMine = (drawContext.mineImage == null) ? null : drawContext.mineImage.get();
        var imgFlag = (drawContext.flagImage == null) ? null : drawContext.flagImage.get();
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
                Color bkClrCell = cell.getCellFillColor(fillMode,
                                                        cellColor,
                                                        m::getFillColor);
                if (!drawContext.drawBackground || !bkClrCell.equals(bkClr)) {
                    g.setColor(Cast.toColor(bkClrCell));
                    g.fillPolygon(poly);
                }

                Consumer<Object> paintImage = img -> {
                    int x = (int)rcInner.x;
                    int y = (int)rcInner.y;
                    if (img instanceof javax.swing.Icon) {
                        ((javax.swing.Icon)img).paintIcon(null, g, x, y);
                    } else
                    if (img instanceof java.awt.Image) {
                        g.drawImage((java.awt.Image)img, x, y, null);
                    } else {
                        throw new IllegalArgumentException("Unsupported image type " + img.getClass().getSimpleName());
                    }
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
                        g.setColor(Cast.toColor(cell.getState().getClose().asColor()));
                        szCaption = cell.getState().getClose().toCaption();
                    } else {
                        g.setColor(Cast.toColor(cell.getState().getOpen().asColor()));
                        szCaption = cell.getState().getOpen().toCaption();
                    }
                    if ((szCaption != null) && (szCaption.length() > 0) && (m.getFontInfo().getSize() >= 1)) {
                        if (cell.getState().isDown())
                            rcInner.moveXY(pen.getWidth(), pen.getWidth());
                        drawText(g, szCaption, rcInner);
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
                    int v = cell.getShape().getVertexNumber(cell.getDirection());
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
            }
        }

        // restore
        g.setFont(oldFont);
        g.setStroke(oldStroke);
        g.setColor(oldColor);
        g.setClip(oldShape);
    }

    /** cached Font for {@link FontInfo} */
    private static final Map<String /* font name*/, Map<Boolean /* bold? */, Map<Integer /* size */, Font>>> CACHED_FONT = new HashMap<>();

    private static Font getFont(MosaicModel m) {
        var fi = m.getFontInfo();
        var map2 = CACHED_FONT.computeIfAbsent(fi.getName(), fontName -> new HashMap<>());
        var map3 = map2.computeIfAbsent(fi.isBold(), isBold -> new HashMap<>());
        return map3.computeIfAbsent((int)fi.getSize(), size -> new Font(fi.getName(), fi.isBold() ? Font.BOLD : Font.PLAIN, size));
    }

    private static void drawText(Graphics g, String text, RectDouble rc) {
        if ((text == null) || text.trim().isEmpty())
            return;

        // draw centered string
        FontMetrics metrics = g.getFontMetrics();
        var x = rc.x + (rc.width - metrics.stringWidth(text)) / 2;
        var y = rc.y + ((rc.height - metrics.getHeight()) / 2) + metrics.getAscent();
        g.drawString(text, (int)x, (int)y);
    }


    /** Mosaic image controller implementation for {@link javax.swing.Icon} */
    public static class MosaicSwingIconController extends MosaicImageController<javax.swing.Icon, SwingIconView<MosaicImageModel>> {

        public MosaicSwingIconController() {
            var model = new MosaicImageModel();
            var view = new SwingIconView<>(model, g -> MosaicImg.draw(g, model));
            init(model, view);
        }

    }

    /** Mosaic image controller implementation for {@link java.awt.Image} */
    public static class MosaicAwtImageController extends MosaicImageController<java.awt.Image, AwtImageView<MosaicImageModel>> {

        public MosaicAwtImageController() {
            var model = new MosaicImageModel();
            var view = new AwtImageView<>(model, g -> MosaicImg.draw(g, model));
            init(model, view);
        }

    }

}
