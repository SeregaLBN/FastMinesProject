package fmg.jfx.img;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javafx.geometry.Bounds;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;
import javafx.scene.text.*;

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
import fmg.core.types.draw.FontInfo2;
import fmg.core.types.draw.PenBorder2;
import fmg.jfx.utils.Cast;

/** Representable {@link fmg.core.types.EMosaic} as image */
public final class MosaicImg2 {
    private MosaicImg2() {}

    private static void draw(GraphicsContext g, MosaicImageModel2 m) {
        MosaicImageController2.<Void>draw(m, ctx -> draw(g, ctx));
    }

    public static <T> void draw(GraphicsContext g, MosaicDrawContext<T> drawContext) {
        var m = drawContext.model;
        SizeDouble size = m.getSize();

        // save
        Paint oldFill = g.getFill();
        Paint oldStroke = g.getStroke();
        Font oldFont = g.getFont();

        // 1. background color
        Color bkClr = (drawContext.getBackgroundColor == null)
                ? m.getBackgroundColor()
                : drawContext.getBackgroundColor.get();
        if (drawContext.drawBackground) {
            if (!bkClr.isOpaque())
                g.clearRect(0, 0, size.width, size.height);
            if (!bkClr.isTransparent()) {
                g.setFill(Cast.toColor(bkClr));
                g.fillRect(0, 0, size.width, size.height);
            }
        }

        // 2. paint cells
        g.setFont(getFont(m));
        PenBorder2 pen = m.getPenBorder();
        g.setLineWidth(pen.getWidth());
        SizeDouble offset = m.getMosaicOffset();
        boolean isSimpleDraw = pen.getColorLight().equals(pen.getColorShadow());
        Color cellColor = m.getCellColor();

        /** HINT: Using the {@link GraphicsContext#clip} method slows down drawing (animation).
            * Especially noticeable at startup demo {@link MosaicCanvasController#main} */
        boolean useClip = !isSimpleDraw;

        var toDrawCells = (drawContext.drawCells == null)
                ? m.getMatrix()
                : drawContext.drawCells.get();

        int fillMode = m.getFillMode();
        var imgMine = (drawContext.mineImage == null) ? null : drawContext.mineImage.get();
        var imgFlag = (drawContext.flagImage == null) ? null : drawContext.flagImage.get();
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
                    Color bkClrCell = cell.getCellFillColor(fillMode,
                                                            cellColor,
                                                            m::getFillColor);
                    if (!drawContext.drawBackground || !bkClrCell.equals(bkClr)) {
                        g.setFill(Cast.toColor(bkClrCell));
                        polyX = Cast.toPolygon(poly, true);
                        polyY = Cast.toPolygon(poly, false);
                        g.fillPolygon(polyX, polyY, polyX.length);
                    }
                }

//                    g.setStroke(Cast.toColor(Color.Magenta));
//                    g.strokeRect(rcInner.x, rcInner.y, rcInner.width, rcInner.height);

                Consumer<Object> paintImage = img -> {
                    if (img instanceof Image) {
                        g.drawImage((Image)img, rcInner.x, rcInner.y);
                    } else {
                        throw new RuntimeException("Unsupported image type " + img.getClass().getSimpleName());
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
                        g.setFill(Cast.toColor(cell.getState().getClose().asColor()));
                        szCaption = cell.getState().getClose().toCaption();
                    } else {
                        g.setFill(Cast.toColor(cell.getState().getOpen().asColor()));
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
                    int v = cell.getShape().getVertexNumber(cell.getDirection());
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
            }

            if (useClip)
               g.restore();
        }

        // restore
        g.setFont(oldFont);
        g.setStroke(oldStroke);
        g.setFill(oldFill);
    }

    /** cached Font for {@link FontInfo2} */
    private static final Map<String /* font name*/, Map<Boolean /* bold? */, Map<Integer /* size */, Font>>> CACHED_FONT = new HashMap<>();
    /** cached Text for quick drawing */
    private static final Map<Font, Map<String /* text */, Text>> CACHED_TEXT = new HashMap<>();

    private static Font getFont(MosaicModel2 m) {
        var fi = m.getFontInfo();
        var map2 = CACHED_FONT.computeIfAbsent(fi.getName(), fontName -> new HashMap<>());
        var map3 = map2.computeIfAbsent(fi.isBold(), isBold -> new HashMap<>());
        return map3.computeIfAbsent((int)fi.getSize(), size -> Font.font(fi.getName(),
                                                                         fi.isBold()
                                                                             ? FontWeight.BOLD
                                                                             : FontWeight.NORMAL,
                                                                         FontPosture.REGULAR,
                                                                         size));
    }

    private static Bounds getStringBounds(MosaicModel2 m, String text) {
        var font = getFont(m);
        var map2 = CACHED_TEXT.computeIfAbsent(font, f -> new HashMap<>());
        Text textResult = map2.computeIfAbsent(text, txt -> {
            var t = new Text(txt);
            t.setFont(getFont(m));
          //t.setTextAlignment(TextAlignment.CENTER);
          //t.setTextOrigin(VPos.CENTER);
            return t;
        });
        return textResult.getLayoutBounds();
    }

    private static void drawText(GraphicsContext g, MosaicModel2 m, String text, RectDouble rc) {
        if ((text == null) || text.trim().isEmpty())
            return;

        Bounds bnd = getStringBounds(m, text);
        g.setTextAlign(TextAlignment.LEFT);
        g.setTextBaseline(VPos.TOP);
        g.fillText(text,
                   rc.x+(rc.width -bnd.getWidth ())/2.,
                   rc.y+(rc.height-bnd.getHeight())/2.);
    }


    /** Mosaic image controller implementation for {@link javafx.scene.canvas.Canvas} */
    public static class MosaicJfxCanvasController extends MosaicImageController2<javafx.scene.canvas.Canvas, JfxCanvasView<MosaicImageModel2>> {

        public MosaicJfxCanvasController() {
            var model = new MosaicImageModel2();
            var view = new JfxCanvasView<>(model, MosaicImg2::draw);
            init(model, view);
        }

    }

    /** Mosaic image controller implementation for {@link javafx.scene.image.Image} */
    public static class MosaicJfxImageController extends MosaicImageController2<javafx.scene.image.Image, JfxImageView<MosaicImageModel2>> {

        public MosaicJfxImageController() {
            var model = new MosaicImageModel2();
            var view = new JfxImageView<>(model, MosaicImg2::draw);
            init(model, view);
        }

    }

}
