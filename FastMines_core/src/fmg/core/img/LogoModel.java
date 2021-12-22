package fmg.core.img;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import fmg.common.Color;
import fmg.common.HSV;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.PointDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.types.Property;

/** MVC: model for FastMines logo image */
public class LogoModel extends AnimatedImageModel {

    public static final String PROPERTY_USE_GRADIENT = "UseGradient";
    public static final String PROPERTY_ROTATE_MODE  = "RotateMode";

    public enum ERotateMode {
        /** rotate image */
        classic,

        /** rotate color Palette */
        color,

        /** {@link #color} + {@link #classic} */
        combi
    }

    private final HSV[] palette = { new HSV(  0, 100, 100), new HSV( 45, 100, 100), new HSV( 90, 100, 100), new HSV(135, 100, 100),
                                    new HSV(180, 100, 100), new HSV(225, 100, 100), new HSV(270, 100, 100), new HSV(315, 100, 100) };

    @Property(PROPERTY_USE_GRADIENT)
    private boolean useGradient;

    @Property(PROPERTY_ROTATE_MODE)
    private ERotateMode rotateMode = ERotateMode.combi;

    /** owner rays points */
    private final List<PointDouble> rays = new ArrayList<>();
    /** inner octahedron */
    private final List<PointDouble> inn = new ArrayList<>();
    /** central octahedron */
    private final List<PointDouble> oct = new ArrayList<>();

    private final PropertyChangeListener onPropertyChangedListener = this::onPropertyChanged;


    public LogoModel() {
        setBackgroundColor(Color.Transparent());
        notifier.addListener(onPropertyChangedListener);
    }

    public BoundDouble getInnerPadding() {
        BoundDouble pad = new BoundDouble(super.getPadding());
        SizeDouble s = getSize();
        double innerX = s.width  - pad.getLeftAndRight();
        double innerY = s.height - pad.getTopAndBottom();
        if (innerX != innerY) {
            double add = (innerX - innerY) / 2;
            if (innerX > innerY) {
                pad.left   += add;
                pad.right  += add;
            } else {
                pad.top    -= add;
                pad.bottom -= add;
            }
        }
        return pad;
    }

    public HSV[] getPalette() {
        return palette;
    }

    public static void toMineModel(LogoModel m) {
        m.setUseGradient(false);
        for (HSV item : m.getPalette())
            //item.v = 75;
            item.grayscale();
    }

    public boolean isUseGradient() { return useGradient; }
    public void setUseGradient(boolean value) {
        notifier.setProperty(useGradient, value, PROPERTY_USE_GRADIENT);
    }

    public ERotateMode getRotateMode() { return rotateMode; }
    public void setRotateMode(ERotateMode value) {
        notifier.setProperty(rotateMode, value, PROPERTY_ROTATE_MODE);
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

    protected void onPropertyChanged(PropertyChangeEvent ev) {
        switch (ev.getPropertyName()) {
        case PROPERTY_SIZE:
        case PROPERTY_PADDING:
            rays.clear();
            inn.clear();
            oct.clear();
            break;
        default:
            // none
        }
    }

    @Override
    public void close() {
        notifier.removeListener(onPropertyChangedListener);
        super.close();
    }

}
