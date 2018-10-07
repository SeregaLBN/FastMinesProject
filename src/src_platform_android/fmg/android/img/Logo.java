package fmg.android.img;

import java.util.Arrays;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Shader;

import fmg.common.Color;
import fmg.common.HSV;
import fmg.common.geom.PointDouble;
import fmg.core.img.IImageController;
import fmg.core.img.ImageView;
import fmg.core.img.LogoController;
import fmg.core.img.LogoModel;
import fmg.android.utils.Cast;
import fmg.android.utils.StaticInitializer;

/** Main logos image - base Logo image view implementation */
public abstract class Logo<TImage> extends ImageView<TImage, LogoModel> {

    protected Logo() {
        super(new LogoModel());
    }

    static {
        StaticInitializer.init();
    }

    protected void draw(Canvas g) {
        LogoModel lm = this.getModel();

        { // fill background
            Color bkClr = lm.getBackgroundColor();
            if (!bkClr.isOpaque())
                g.drawColor(android.graphics.Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            if (!bkClr.isTransparent())
                g.drawColor(Cast.toColor(bkClr));
        }

        List<PointDouble> rays0 = lm.getRays();
        List<PointDouble> inn0  = lm.getInn();
        List<PointDouble> oct0  = lm.getOct();

        PointF[] rays = rays0.stream().map(p -> Cast.toPoint(p)).toArray(size -> new PointF[size]);
        PointF[] inn  = inn0 .stream().map(p -> Cast.toPoint(p)).toArray(size -> new PointF[size]);
        PointF[] oct  = oct0 .stream().map(p -> Cast.toPoint(p)).toArray(size -> new PointF[size]);
        PointF center = new PointF((float)(getSize().width/2.0), (float)(getSize().height/2.0));

        HSV[] hsvPalette = lm.getPalette();
        Color[] palette = Arrays.stream(hsvPalette)
            .map(hsv -> hsv.toColor())
            .toArray(size -> new Color[size]);

        // paint owner gradient rays
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        for (int i=0; i<8; i++) {
            if (!lm.isUseGradient()) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Cast.toColor(hsvPalette[i].toColor().darker()));
                fillPolygon(g, paint, rays[i], oct[i], inn[i], oct[(i+5)%8]);
            } else {
                // emulate triangle gradient (see BmpLogo.cpp C++ source code)
                // over linear gragients

                paint.setShader(makeLinearGradient(rays[i], palette[(i+1)%8], inn[i], palette[(i+6)%8]));
                fillPolygon(g, paint, rays[i], oct[i], inn[i], oct[(i+5)%8]);

                PointF p1 = oct[i];
                PointF p2 = oct[(i+5)%8];
                PointF p = new PointF((p1.x+p2.x)/2, (p1.y+p2.y)/2); // середина линии oct[i]-oct[(i+5)%8]. По факту - пересечение линий rays[i]-inn[i] и oct[i]-oct[(i+5)%8]

                Color clr;// = new Color(255,255,255,0); //  Cast.toColor(fmg.common.Color.Transparent);
                if (true) {
                    HSV c1 = hsvPalette[(i+1)%8];
                    HSV c2 = hsvPalette[(i+6)%8];
                    double diff = c1.h - c2.h;
                    HSV cP = new HSV(c1.toColor());
                    cP.h += diff/2; // цвет в точке p (пересечений линий...)
                    cP.a = 0;
                    clr = cP.toColor();
                }

                paint.setShader(makeLinearGradient(oct[i], palette[(i+3)%8], p, clr));
                fillPolygon(g, paint, rays[i], oct[i], inn[i]);

                paint.setShader(makeLinearGradient(oct[(i+5)%8], palette[(i+0)%8], p, clr));
                fillPolygon(g, paint, rays[i], oct[(i+5)%8], inn[i]);
            }
        }

        // paint star perimeter
        double zoomAverage = (lm.getZoomX() + lm.getZoomY())/2;
        final double penWidth = lm.getBorderWidth() * zoomAverage;
        if (penWidth > 0.1) {
            paint.setShader(null); // reset gradient shader
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth((float) penWidth);
            for (int i = 0; i < 8; i++) {
                PointF p1 = rays[(i + 7) % 8];
                PointF p2 = rays[i];
                paint.setColor(Cast.toColor(palette[i].darker()));
                g.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
            }
        }

        // paint inner gradient triangles
        paint.setStyle(Paint.Style.FILL);
        for (int i=0; i<8; i++) {
            if (lm.isUseGradient()) {
                PointF p1 = inn[(i+0)%8];
                PointF p2 = inn[(i+3)%8];
                PointF p = new PointF((p1.x+p2.x)/2, (p1.y+p2.y)/2); // center line of p1-p2
                paint.setShader(makeLinearGradient(
                        p, palette[(i+6)%8],
                        center, ((i & 1) == 1) ? Color.Black() : Color.White()));
            } else {
                paint.setColor(((i & 1) == 1)
                        ? Cast.toColor(hsvPalette[(i + 6) % 8].toColor().brighter())
                        : Cast.toColor(hsvPalette[(i + 6) % 8].toColor().darker()));
            }
            fillPolygon(g, paint, inn[(i + 0)%8], inn[(i + 3)%8], center);
        }
    }

    private static void fillPolygon(Canvas g, Paint paint, PointF... p) {
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(p[0].x, p[0].y);
        for (int i=1; i<p.length; ++i)
            path.lineTo(p[i].x, p[i].y);
        path.close();
        g.drawPath(path, paint);
    }

    private static Shader makeLinearGradient(PointF start, Color startClr, PointF end, Color endClr) {
        return new LinearGradient(start.x, start.y, end.x, end.y, Cast.toColor(startClr), Cast.toColor(endClr), Shader.TileMode.CLAMP);
    }

    @Override
    public void close() {
        getModel().close();
        super.close();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    //    custom implementations
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    /** Logo image view implementation over {@link android.graphics.Bitmap} */
    static class Bitmap extends Logo<android.graphics.Bitmap> {

        private BmpCanvas wrap = new BmpCanvas();

        @Override
        protected android.graphics.Bitmap createImage() {
            return wrap.createImage(getModel().getSize());
        }

        @Override
        protected void drawBody() {
            draw(wrap.getCanvas());
        }

        @Override
        public void close() {
            wrap.close();
        }

    }

    /** Logo image controller implementation for {@link Logo.Bitmap} */
    public static class ControllerBitmap extends LogoController<android.graphics.Bitmap, Logo.Bitmap> {

        public ControllerBitmap() {
            super(new Logo.Bitmap());
        }

        @Override
        public void close() {
            getView().close();
            super.close();
        }

    }

}
