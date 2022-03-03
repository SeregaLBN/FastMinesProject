package fmg.core.img;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import fmg.common.Color;
import fmg.common.HSV;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.DoubleExt;
import fmg.common.geom.PointDouble;
import fmg.common.geom.SizeDouble;

/** MVC: model for FastMines logo image */
public class LogoModel2 implements IImageModel2 {

    private SizeDouble size = new SizeDouble(ImageHelper.DEFAULT_IMAGE_SIZE, ImageHelper.DEFAULT_IMAGE_SIZE);

    /** inside padding */
    private BoundDouble padding = new BoundDouble(ImageHelper.DEFAULT_PADDING);

    private Color borderColor = Color.Maroon().darker(0.5);

    private double borderWidth = 3;

    public static final HSV[] DEFAULT_PALETTE = { new HSV(  0, 100, 100), new HSV( 45, 100, 100), new HSV( 90, 100, 100), new HSV(135, 100, 100),
                                                  new HSV(180, 100, 100), new HSV(225, 100, 100), new HSV(270, 100, 100), new HSV(315, 100, 100) };
    private final HSV[] palette = { new HSV(DEFAULT_PALETTE[0]), new HSV(DEFAULT_PALETTE[1]), new HSV(DEFAULT_PALETTE[2]), new HSV(DEFAULT_PALETTE[3]),
                                    new HSV(DEFAULT_PALETTE[4]), new HSV(DEFAULT_PALETTE[5]), new HSV(DEFAULT_PALETTE[6]), new HSV(DEFAULT_PALETTE[7]) };

    private boolean useGradient = true;

    /** owner rays points */
    private final List<PointDouble> rays = new ArrayList<>();
    /** inner octahedron */
    private final List<PointDouble> inn = new ArrayList<>();
    /** central octahedron */
    private final List<PointDouble> oct = new ArrayList<>();

    private Consumer<String> changedCallback;


    @Override
    public SizeDouble getSize() {
        return size;
    }

    @Override
    public void setSize(SizeDouble size) {
        if (this.size.equals(size))
            return;

        ImageHelper.checkSize(size);

        SizeDouble oldSize = new SizeDouble(this.size);
        this.size.width  = size.width;
        this.size.height = size.height;

        rays.clear();
        inn.clear();
        oct.clear();

        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_NAME_SIZE);

        setPadding(ImageHelper.recalcPadding(padding, size, oldSize));
    }

    @Override
    public BoundDouble getPadding() {
        return padding;
    }

    @Override
    public void setPadding(BoundDouble padding) {
        if (this.padding.equals(padding))
            return;

        ImageHelper.checkPadding(size, padding);

        this.padding.left   = padding.left;
        this.padding.right  = padding.right;
        this.padding.top    = padding.top;
        this.padding.bottom = padding.bottom;

        rays.clear();
        inn.clear();
        oct.clear();

        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_NAME_OTHER);
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        if (this.borderColor.equals(borderColor))
            return;

        this.borderColor = borderColor;

        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_NAME_OTHER);
    }

    public double getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(double borderWidth) {
        if (DoubleExt.almostEquals(this.borderWidth, borderWidth))
            return;

        this.borderWidth = borderWidth;

        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_NAME_OTHER);
    }

    private BoundDouble getInnerPadding() {
        BoundDouble padInner = new BoundDouble(padding);
        SizeDouble s = getSize();
        double innerX = s.width  - padInner.getLeftAndRight();
        double innerY = s.height - padInner.getTopAndBottom();
        if (innerX != innerY) {
            double add = (innerX - innerY) / 2;
            if (innerX > innerY) {
                padInner.left   += add;
                padInner.right  += add;
            } else {
                padInner.top    -= add;
                padInner.bottom -= add;
            }
        }
        return padInner;
    }

    public HSV[] getPalette() {
        return palette;
    }

    public static void toMineModel(LogoModel2 m) {
        m.setUseGradient(false);
        for (HSV item : m.getPalette())
            //item.v = 75;
            item.grayscale();
    }

    public boolean isUseGradient() { return useGradient; }
    public void setUseGradient(boolean value) {
        if (this.useGradient == value)
            return;

        this.useGradient = value;

        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_NAME_OTHER);
    }

    public double getZoomX() { return (getSize().width  - getInnerPadding().getLeftAndRight()) / 200.0; }
    public double getZoomY() { return (getSize().height - getInnerPadding().getTopAndBottom()) / 200.0; }

    public List<PointDouble> getRays() {
        if (rays.isEmpty()) {
            BoundDouble pad = getInnerPadding();
            double pl = pad.left;
            double pt = pad.top;
            double zx = getZoomX();
            double zy = getZoomY();

            rays.add(new PointDouble(pl + 100.0000*zx, pt + 200.0000*zy));
            rays.add(new PointDouble(pl + 170.7107*zx, pt +  29.2893*zy));
            rays.add(new PointDouble(pl +   0.0000*zx, pt + 100.0000*zy));
            rays.add(new PointDouble(pl + 170.7107*zx, pt + 170.7107*zy));
            rays.add(new PointDouble(pl + 100.0000*zx, pt +   0.0000*zy));
            rays.add(new PointDouble(pl +  29.2893*zx, pt + 170.7107*zy));
            rays.add(new PointDouble(pl + 200.0000*zx, pt + 100.0000*zy));
            rays.add(new PointDouble(pl +  29.2893*zx, pt +  29.2893*zy));
        }
        return rays;
    }

    public List<PointDouble> getInn() {
        if (inn.isEmpty()) {
            BoundDouble pad = getInnerPadding();
            double pl = pad.left;
            double pt = pad.top;
            double zx = getZoomX();
            double zy = getZoomY();

            inn.add(new PointDouble(pl + 100.0346*zx, pt + 141.4070*zy));
            inn.add(new PointDouble(pl + 129.3408*zx, pt +  70.7320*zy));
            inn.add(new PointDouble(pl +  58.5800*zx, pt + 100.0000*zy));
            inn.add(new PointDouble(pl + 129.2500*zx, pt + 129.2500*zy));
            inn.add(new PointDouble(pl +  99.9011*zx, pt +  58.5377*zy));
            inn.add(new PointDouble(pl +  70.7233*zx, pt + 129.3198*zy));
            inn.add(new PointDouble(pl + 141.4167*zx, pt + 100.0000*zy));
            inn.add(new PointDouble(pl +  70.7500*zx, pt +  70.7500*zy));
        }
        return inn;
    }

    public List<PointDouble> getOct() {
        if (oct.isEmpty()) {
            BoundDouble pad = getInnerPadding();
            double pl = pad.left;
            double pt = pad.top;
            double zx = getZoomX();
            double zy = getZoomY();

            oct.add(new PointDouble(pl + 120.7053*zx, pt + 149.9897*zy));
            oct.add(new PointDouble(pl + 120.7269*zx, pt +  50.0007*zy));
            oct.add(new PointDouble(pl +  50.0034*zx, pt + 120.7137*zy));
            oct.add(new PointDouble(pl + 150.0000*zx, pt + 120.6950*zy));
            oct.add(new PointDouble(pl +  79.3120*zx, pt +  50.0007*zy));
            oct.add(new PointDouble(pl +  79.2624*zx, pt + 149.9727*zy));
            oct.add(new PointDouble(pl + 150.0000*zx, pt +  79.2737*zy));
            oct.add(new PointDouble(pl +  50.0034*zx, pt +  79.3093*zy));
        }
        return oct;
    }

    @Override
    public void setListener(Consumer<String> callback) {
        if ((callback != null) && (changedCallback != null))
            throw new IllegalArgumentException("Can only set the controller once");
        changedCallback = callback;
    }

}
