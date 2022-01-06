package fmg.core.img;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.HSV;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.SizeDouble;
import fmg.common.notifier.NotifyPropertyChanged;
import fmg.core.types.Property;

/** MVC: model of representable menu as horizontal or vertical lines */
public final class BurgerMenuModel implements IAnimatedModel {

    public static final String PROPERTY_SHOW       = "Show";
    public static final String PROPERTY_HORIZONTAL = "Horizontal";
    public static final String PROPERTY_LAYERS     = "Layers";

    private AnimatedImageModel generalModel;

    @Property(PROPERTY_SHOW)
    private boolean show = true;

    @Property(PROPERTY_HORIZONTAL)
    private boolean horizontal = true;

    @Property(PROPERTY_LAYERS)
    private int layers = 3;

    @Property(PROPERTY_PADDING)
    private BoundDouble padding;

    private NotifyPropertyChanged notifier = new NotifyPropertyChanged(this);
    private final PropertyChangeListener onGeneralModelPropertyChangedListener = this::onGeneralModelPropertyChanged;

    /** @param generalModel another basic model */
    protected BurgerMenuModel(AnimatedImageModel generalModel) {
        this.generalModel = generalModel;
        this.generalModel.addListener(onGeneralModelPropertyChangedListener);
    }

    /** image width and height in pixel */
    @Override
    public SizeDouble getSize() { return generalModel.getSize(); }
    @Override
    public void setSize(SizeDouble size) { generalModel.setSize(size); }

    @Override
    public boolean isAnimated() { return generalModel.isAnimated(); }
    @Override
    public void setAnimated(boolean value) { generalModel.setAnimated(value); }

    @Override
    public long getAnimatePeriod() { return generalModel.getAnimatePeriod(); }
    @Override
    public void setAnimatePeriod(long value) { generalModel.setAnimatePeriod(value); }

    @Override
    public int getTotalFrames() { return generalModel.getTotalFrames(); }
    @Override
    public void setTotalFrames(int value) { generalModel.setTotalFrames(value); }

    @Override
    public int getCurrentFrame() { return generalModel.getCurrentFrame(); }
    @Override
    public void setCurrentFrame(int value) { generalModel.setCurrentFrame(value); }


    public boolean isShow() { return show; }
    public void   setShow(boolean value) { notifier.setProperty(show, value, PROPERTY_SHOW); }

    public boolean isHorizontal() { return horizontal; }
    public void   setHorizontal(boolean value) { notifier.setProperty(horizontal, value, PROPERTY_HORIZONTAL); }

    public int  getLayers() { return layers; }
    public void setLayers(int value) { notifier.setProperty(layers, value, PROPERTY_LAYERS); }

    /** inside padding */
    @Override
    public BoundDouble getPadding() {
        if (padding == null)
            recalcPadding(null);
        return padding;
    }
    @Override
    public void setPadding(BoundDouble padding) {
        IImageModel.checkPadding(this, padding);
        notifier.setProperty(this.padding, new BoundDouble(padding), PROPERTY_PADDING);
    }
    private void recalcPadding(SizeDouble old) {
        SizeDouble size = getSize();
        BoundDouble paddingNew = (padding == null)
                ? new BoundDouble(size.width / 2,
                                  size.height / 2,
                                  generalModel.getPadding().right,
                                  generalModel.getPadding().bottom)
                : IImageModel.recalcPadding(padding, size, old);
        notifier.setProperty(padding, paddingNew, PROPERTY_PADDING);
    }

    public static class LineInfo {
        public Color clr;
        public double penWidht;
        public PointDouble from; // start coord
        public PointDouble to;   // end   coord
    }

    /** get paint information of drawing burger menu model image */
    public Stream<LineInfo> getCoords() {
        if (!isShow())
            return Stream.empty();

        boolean horizontal = isHorizontal();
        int layers = getLayers();
        BoundDouble pad = getPadding();
        RectDouble rc = new RectDouble(pad.left,
                                       pad.top,
                                       getSize().width  - pad.getLeftAndRight(),
                                        getSize().height - pad.getTopAndBottom());
        double penWidth = Math.max(1, (horizontal ? rc.height : rc.width) / (2.0 * layers));
        double rotateAngle = generalModel.getRotateAngle();
        double stepAngle = 360.0 / layers;

        return IntStream.range(0, layers)
            .mapToObj(layerNum -> {
                double layerAlignmentAngle = AnimatedImageModel.fixAngle(layerNum*stepAngle + rotateAngle);
                double offsetTop  = !horizontal ? 0 : layerAlignmentAngle*rc.height/360;
                double offsetLeft =  horizontal ? 0 : layerAlignmentAngle*rc.width /360;
                PointDouble start = new PointDouble(rc.left() + offsetLeft,
                                                    rc.top()  + offsetTop);
                PointDouble end   = new PointDouble((horizontal ? rc.right() : rc.left()) + offsetLeft,
                                                    (horizontal ? rc.top() : rc.bottom()) + offsetTop);

                HSV hsv = new HSV(Color.Gray());
                hsv.v *= Math.sin(layerNum*stepAngle / layers);

                LineInfo li = new LineInfo();
                li.clr = hsv.toColor();
                li.penWidht = penWidth;
                li.from = start;
                li.to = end;
                return li;
            });
    }

    private void onGeneralModelPropertyChanged(PropertyChangeEvent ev) {
        assert ev.getSource() == generalModel; // by reference
        if (IImageModel.PROPERTY_SIZE.equals(ev.getPropertyName()))
            recalcPadding((SizeDouble)ev.getOldValue());
    }

    @Override
    public void close() {
        generalModel.removeListener(onGeneralModelPropertyChangedListener);
        notifier.close();
        generalModel = null;
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        notifier.addListener(listener);
    }
    @Override
    public void removeListener(PropertyChangeListener listener) {
        notifier.removeListener(listener);
    }
}
