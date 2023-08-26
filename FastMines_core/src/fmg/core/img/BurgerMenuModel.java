package fmg.core.img;

import static fmg.core.img.PropertyConst.PROPERTY_HORIZONTAL;
import static fmg.core.img.PropertyConst.PROPERTY_LAYERS;
import static fmg.core.img.PropertyConst.PROPERTY_PADDING;
import static fmg.core.img.PropertyConst.PROPERTY_ROTATE_ANGLE;
import static fmg.core.img.PropertyConst.PROPERTY_SIZE;

import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.HSV;
import fmg.common.geom.*;

/** MVC: model of representable menu as horizontal or vertical lines */
public final class BurgerMenuModel implements IImageModel {

    private final SizeDouble size = new SizeDouble(ImageHelper.DEFAULT_IMAGE_SIZE, ImageHelper.DEFAULT_IMAGE_SIZE);
    private final BoundDouble pad = new BoundDouble(ImageHelper.DEFAULT_IMAGE_SIZE / 2.,
                                                    ImageHelper.DEFAULT_IMAGE_SIZE / 2.,
                                                    ImageHelper.DEFAULT_PADDING,
                                                    ImageHelper.DEFAULT_PADDING);
    /** 0° .. +360° */
    private double rotateAngle;
    private boolean horizontal = true;
    private int layers = 3;
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

        firePropertyChanged(PROPERTY_SIZE);

        setPadding(ImageHelper.recalcPadding(pad, size, oldSize));
    }

    @Override
    public BoundDouble getPadding() {
        return pad;
    }

    @Override
    public void setPadding(BoundDouble padding) {
        if (pad.equals(padding))
            return;

        ImageHelper.checkPadding(size, padding);

        this.pad.left   = padding.left;
        this.pad.right  = padding.right;
        this.pad.top    = padding.top;
        this.pad.bottom = padding.bottom;

        firePropertyChanged(PROPERTY_PADDING);
    }

    public boolean isHorizontal() { return horizontal; }
    public void   setHorizontal(boolean value) {
        this.horizontal = value;

        firePropertyChanged(PROPERTY_HORIZONTAL);
    }

    public int  getLayers() { return layers; }
    public void setLayers(int value) {
        this.layers = value;

        firePropertyChanged(PROPERTY_LAYERS);
    }

    /** 0° .. +360° */
    public double getRotateAngle() { return rotateAngle; }
    public void setRotateAngle(double value) {
        var old = this.rotateAngle;
        this.rotateAngle = ImageHelper.fixAngle(value);

        if (!DoubleExt.almostEquals(old, this.rotateAngle))
            firePropertyChanged(PROPERTY_ROTATE_ANGLE);
    }

    @Override
    public void setListener(Consumer<String> callback) {
        if ((callback != null) && (changedCallback != null))
            throw new IllegalArgumentException("Can only set the controller once");
        changedCallback = callback;
    }

    public static class LineInfo {
        public Color clr;
        public double penWidht;
        public PointDouble from; // start coord
        public PointDouble to;   // end   coord
    }

    /** get paint information of drawing burger menu model image */
    public Stream<LineInfo> getCoords() {
        RectDouble rc = new RectDouble(pad.left,
                                       pad.top,
                                       getSize().width  - pad.getLeftAndRight(),
                                        getSize().height - pad.getTopAndBottom());
        double penWidth = Math.max(1, (horizontal ? rc.height : rc.width) / (2.0 * layers));
        double stepAngle = 360.0 / layers;

        return IntStream.range(0, layers)
            .mapToObj(layerNum -> {
                double layerAlignmentAngle = ImageHelper.fixAngle(layerNum*stepAngle + rotateAngle);
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

    private void firePropertyChanged(String propertyName) {
        if (changedCallback != null)
            changedCallback.accept(propertyName);
    }

}
