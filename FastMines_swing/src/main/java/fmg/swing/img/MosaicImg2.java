package fmg.swing.img;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import fmg.common.Color;
import fmg.common.HSV;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.RegionDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.img.MosaicImageController2;
import fmg.core.img.MosaicImageModel2;
import fmg.core.mosaic.MosaicModel2;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EClose;
import fmg.core.types.EOpen;
import fmg.core.types.EState;
import fmg.core.types.draw.PenBorder2;
import fmg.swing.utils.Cast;

/** Representable {@link fmg.core.types.EMosaic} as image */
public final class MosaicImg2 {
    private MosaicImg2() {}

    private static void draw(Graphics2D g, MosaicImageModel2 m, Supplier<Object> mineImage, Supplier<Object> flagImage) {
        Color bkClr = new HSV(m.getBackgroundColor())
                        .addHue(m.getBackgroundAngle())
                        .toColor();
        switch (m.getRotateMode()) {
        case FULL_MATRIX:
            draw(g, m, m.getMatrix(), true, () -> bkClr, mineImage, flagImage);
            break;

        case SOME_CELLS:
            // 1. draw static part
            draw(g, m, m.getNotRotatedCells(), true, () -> bkClr, mineImage, flagImage);

            // 2. draw rotated part
            PenBorder2 pb = m.getPenBorder();
            // save
            double borderWidth = pb.getWidth();
            Color colorLight  = pb.getColorLight();
            Color colorShadow = pb.getColorShadow();

            // unset notifier (щоб не призводило до малювання із методу малювання)
            var callback = m.getListener();
            m.setListener(null); // lock to fire changing model
            // modify
            pb.setWidth(2 * borderWidth);
            pb.setColorLight(colorLight.darker(0.5));
            pb.setColorShadow(colorShadow.darker(0.5));

            draw(g, m, m.getRotatedCells(), false, () -> bkClr, mineImage, flagImage);

            // restore
            pb.setWidth(borderWidth);
            pb.setColorLight(colorLight);
            pb.setColorShadow(colorShadow);
            m.setListener(callback);

            break;

        default:
            throw new IllegalArgumentException();
        }
    }

    private static void draw(Graphics2D g, MosaicModel2 m, Collection<BaseCell> toDrawCells, boolean drawBk, Supplier<Color> bkColor, Supplier<Object> mineImage, Supplier<Object> flagImage) {
        SizeDouble size = m.getSize();

        // save
        Shape oldShape = g.getClip();
        java.awt.Color oldColor = g.getColor();
        Stroke oldStroke = g.getStroke();
        Font oldFont = g.getFont();

        // 1. background color
        Color bkClr = bkColor.get();
        if (drawBk) {
            g.setComposite(AlphaComposite.Src);
            g.setColor(Cast.toColor(bkClr));
            g.fillRect(0, 0, (int)size.width, (int)size.height);
        }

        // 2. paint cells
        g.setComposite(AlphaComposite.SrcOver);
        g.setFont(getFont(m));
        PenBorder2 pen = m.getPenBorder();
        g.setStroke(new BasicStroke((float)pen.getWidth()));
        SizeDouble offset = m.getMosaicOffset();
        boolean isSimpleDraw = pen.getColorLight().equals(pen.getColorShadow());
        Color cellColor = m.getCellColor();

        if (toDrawCells == null)
            toDrawCells = m.getMatrix();

        int fillMode = m.getFillMode();
        var imgMine = (mineImage==null) ? null : mineImage.get();
        var imgFlag = (flagImage==null) ? null : flagImage.get();
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
                if (!drawBk || !bkClrCell.equals(bkClr)) {
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
                        drawText(g, m, szCaption, rcInner);
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

    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
    private static final Map<String /* font name*/, Map<Boolean /* bold? */, Map<Integer /* size */, Font>>> CACHED_FONT = new HashMap<>();
    /** cached TextLayout for quick drawing */
    private static final Map<Font, Map<String /* text */, TextLayout>> CACHED_TEXT_LAYOUT = new HashMap<>();

    private static Font getFont(MosaicModel2 m) {
        var fi = m.getFontInfo();
        var map2 = CACHED_FONT.computeIfAbsent(fi.getName(), fontName -> new HashMap<>());
        var map3 = map2.computeIfAbsent(fi.isBold(), isBold -> new HashMap<>());
        return map3.computeIfAbsent((int)fi.getSize(), size -> new Font(fi.getName(), fi.isBold() ? Font.BOLD : Font.PLAIN, size));
    }

    private static Rectangle2D getStringBounds(MosaicModel2 m, String text) {
        var font = getFont(m);
        var map2 = CACHED_TEXT_LAYOUT.computeIfAbsent(font, f -> new HashMap<>());
        var tl = map2.computeIfAbsent(text, t -> new TextLayout(t, font, FRC));
        return tl.getBounds();
//        return font.getStringBounds(text, new FontRenderContext(null, true, true));
    }

    private static void drawText(Graphics g, MosaicModel2 m, String text, RectDouble rc) {
        if ((text == null) || text.trim().isEmpty())
            return;
        Rectangle2D bnd = getStringBounds(m, text);
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


    /** Mosaic image controller implementation for {@link javax.swing.Icon} */
    public static class MosaicImageSwingIconController extends MosaicImageController2<javax.swing.Icon, SwingIconView<MosaicImageModel2>> {

        public MosaicImageSwingIconController() {
            var model = new MosaicImageModel2();
            var view = new SwingIconView<>(model, this::draw);
            init(model, view);
        }

        private void draw(Graphics2D g, MosaicImageModel2 m) {
            MosaicImg2.draw(g, m, null, null);
        }

    }

    /** Mosaic image controller implementation for {@link java.awt.Image} */
    public static class MosaicImageAwtImageController extends MosaicImageController2<java.awt.Image, AwtImageView<MosaicImageModel2>> {

        public MosaicImageAwtImageController() {
            var model = new MosaicImageModel2();
            var view = new AwtImageView<>(model, this::draw);
            init(model, view);
        }

        private void draw(Graphics2D g, MosaicImageModel2 m) {
            MosaicImg2.draw(g, m, null, null);
        }

    }

}
