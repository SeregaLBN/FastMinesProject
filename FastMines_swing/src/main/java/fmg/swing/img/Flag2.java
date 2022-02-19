package fmg.swing.img;

import java.awt.*;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

import fmg.common.geom.BoundDouble;
import fmg.common.geom.SizeDouble;
import fmg.common.ui.UiInvoker;
import fmg.core.img.FlagModel2;
import fmg.core.img.IImageController2;
import fmg.core.img.IImageView2;
import fmg.core.img.ImageHelper;

/** Flag image */
public final class Flag2 {
    private Flag2() {}

    private static final Color TRANSPARENT = new Color(0xFF, 0xFF, 0xFF, 0);

    private static void draw(Graphics2D g, SizeDouble size, BoundDouble padding) {
        g.setComposite(AlphaComposite.Src);
        g.setColor(TRANSPARENT);
        g.fillRect(0, 0, (int)size.width, (int)size.height);

        double width  = size.width  - padding.left - padding.right;
        double height = size.height - padding.top  - padding.bottom;
        double offsetX = padding.left;
        double offsetY = padding.top ;
        double w = width  / 100.0;
        double h = height / 100.0;

        // perimeter figure points
        Point2D.Double[] p = new Point2D.Double[] {
            new Point2D.Double(offsetX + 13.5 *w, offsetY + 90*h),
            new Point2D.Double(offsetX + 17.44*w, offsetY + 51*h),
            new Point2D.Double(offsetX + 21   *w, offsetY + 16*h),
            new Point2D.Double(offsetX + 85   *w, offsetY + 15*h),
            new Point2D.Double(offsetX + 81.45*w, offsetY + 50*h)};

        double penWidth = 7 * (w + h) / 2;
        BasicStroke penLine = new BasicStroke((float)penWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
        g.setStroke(penLine);
        g.setColor(Color.BLACK);
        g.drawLine((int)p[0].x, (int)p[0].y, (int)p[1].x, (int)p[1].y);

        BasicStroke penCurve = new BasicStroke((float)penWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
        g.setStroke(penCurve);
        g.setColor(Color.RED);
        CubicCurve2D curve = new CubicCurve2D.Double(
            p[2].x, p[2].y,
            offsetX + 95.0*w, offsetY +  0*h,
            offsetX + 19.3*w, offsetY + 32*h,
            p[3].x, p[3].y);
        g.draw(curve);

        curve = new CubicCurve2D.Double(
            p[3].x, p[3].y,
            offsetX + 77.80*w, offsetY + 32.89*h,
            offsetX + 88.05*w, offsetY + 22.73*h,
            p[4].x, p[4].y);
        g.draw(curve);

        curve = new CubicCurve2D.Double(
            p[4].x, p[4].y,
            offsetX + 15.83*w, offsetY + 67*h,
            offsetX + 91.45*w, offsetY + 35*h,
            p[1].x, p[1].y);
        g.draw(curve);

        g.setStroke(penLine);
        g.drawLine((int)p[1].x, (int)p[1].y, (int)p[2].x, (int)p[2].y);
    }

    /** Flag image view implementation over {@link javax.swing.Icon} */
    private static class FlagSwingIconView implements IImageView2<javax.swing.Icon> {
        private final FlagModel2 model;
        private boolean valid;
        private javax.swing.Icon image;
        private Graphics2D gBuffImg;

        public FlagSwingIconView(FlagModel2 model) {
            this.model = model;
        }

        public boolean isValid() {
            return valid;
        }

        @Override
        public void invalidate() {
            valid = false;
        }

        @Override
        public javax.swing.Icon getImage() {
            if (image == null) {
                image = create((int)model.getSize().width, (int)model.getSize().height);
                valid = false;
            }
            if (!valid) {
                draw(gBuffImg, model.getSize(), model.getPadding());
                valid = true;
            }
            return image;
        }

        private javax.swing.Icon create(int width, int height) {
            if (gBuffImg != null)
                gBuffImg.dispose();

            var buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            gBuffImg = buffImg.createGraphics();
            gBuffImg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gBuffImg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            //gBuffImg.setClip(0, 0, s.width, s.height);

            return new javax.swing.Icon() {
                @Override
                public int getIconWidth() { return width; }
                @Override
                public int getIconHeight() { return height; }
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    g.drawImage(buffImg, x,y, c);
                }
            };
        }

        public void reset() {
            if (gBuffImg != null)
                gBuffImg.dispose();
            gBuffImg = null;
            image = null;
            valid = false;
        }

    }

    /** Flag image view implementation over {@link java.awt.Image} */
    private static class FlagAwtImageView implements IImageView2<java.awt.Image> {
        private final FlagModel2 model;
        private boolean valid;
        private BufferedImage image;

        public FlagAwtImageView(FlagModel2 model) {
            this.model = model;
        }

        public boolean isValid() {
            return valid;
        }

        @Override
        public void invalidate() {
            valid = false;
        }

        @Override
        public java.awt.Image getImage() {
            if (image == null) {
                image = create((int)model.getSize().width, (int)model.getSize().height);
                valid = false;
            }
            if (!valid) {
                Graphics2D g = image.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                draw(g, model.getSize(), model.getPadding());
                g.dispose();
                valid = true;
            }
            return image;
        }

        private BufferedImage create(int width, int height) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        public void reset() {
            image = null;
            valid = false;
        }

    }

    /** Flag image controller implementation for {@link javax.swing.Icon} */
    public static class FlagSwingIconController implements IImageController2<javax.swing.Icon, FlagModel2>, AutoCloseable {

        private final FlagModel2 model = new FlagModel2();
        private final FlagSwingIconView view = new FlagSwingIconView(model);
        private Consumer<String> changedCallback;

        public FlagSwingIconController() {
            model.setListener(this::onModelChanged);
        }

        @Override
        public FlagModel2 getModel() {
            return model;
        }

        @Override
        public javax.swing.Icon getImage() {
            return view.getImage();
        }

        @Override
        public void setListener(Consumer<String> callback) {
            changedCallback = callback;
            view.invalidate();
        }

        private void onModelChanged(String property) {
            var isValidBefore = view.isValid();
            var callback = changedCallback;

            if (ImageHelper.PROPERTY_NAME_SIZE.equals(property)) {
                view.reset();
                if (callback != null)
                    UiInvoker.Deferred.accept(() -> callback.accept(ImageHelper.PROPERTY_NAME_SIZE));
            }

            view.invalidate();

            if ((callback != null) && isValidBefore)
                UiInvoker.Deferred.accept(() -> callback.accept(ImageHelper.PROPERTY_NAME_IMAGE));
        }

        @Override
        public void close() throws Exception {
            changedCallback = null;
            model.setListener(null);
            view.reset();
        }

    }

    /** Flag image controller implementation for {@link java.awt.Image} */
    public static class FlagAwtImageController implements IImageController2<java.awt.Image, FlagModel2>, AutoCloseable {

        private final FlagModel2 model = new FlagModel2();
        private final FlagAwtImageView view = new FlagAwtImageView(model);
        private Consumer<String> changedCallback;

        public FlagAwtImageController() {
            model.setListener(this::onModelChanged);
        }

        @Override
        public FlagModel2 getModel() {
            return model;
        }

        @Override
        public java.awt.Image getImage() {
            return view.getImage();
        }

        @Override
        public void setListener(Consumer<String> callback) {
            changedCallback = callback;
            view.invalidate();
        }

        private void onModelChanged(String property) {
            var isValidBefore = view.isValid();
            var callback = changedCallback;

            if (ImageHelper.PROPERTY_NAME_SIZE.equals(property)) {
                view.reset();
                if (callback != null)
                    UiInvoker.Deferred.accept(() -> callback.accept(ImageHelper.PROPERTY_NAME_SIZE));
            }

            view.invalidate();

            if ((callback != null) && isValidBefore)
                UiInvoker.Deferred.accept(() -> callback.accept(ImageHelper.PROPERTY_NAME_IMAGE));
        }

        @Override
        public void close() throws Exception {
            changedCallback = null;
            model.setListener(null);
            view.reset();
        }

    }

}
