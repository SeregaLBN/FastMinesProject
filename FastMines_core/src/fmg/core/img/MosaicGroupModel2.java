package fmg.core.img;

import static fmg.core.img.PropertyConst.PROPERTY_BACKGROUND_ANGLE;
import static fmg.core.img.PropertyConst.PROPERTY_BACKGROUND_COLOR;
import static fmg.core.img.PropertyConst.PROPERTY_BORDER_COLOR;
import static fmg.core.img.PropertyConst.PROPERTY_BORDER_WIDTH;
import static fmg.core.img.PropertyConst.PROPERTY_FOREGROUND_ANGLE;
import static fmg.core.img.PropertyConst.PROPERTY_FOREGROUND_COLOR;
import static fmg.core.img.PropertyConst.PROPERTY_PADDING;
import static fmg.core.img.PropertyConst.PROPERTY_ROTATE_ANGLE;
import static fmg.core.img.PropertyConst.PROPERTY_SIZE;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.HSV;
import fmg.common.Pair;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.DoubleExt;
import fmg.common.geom.PointDouble;
import fmg.common.geom.SizeDouble;
import fmg.common.geom.util.FigureHelper;
import fmg.core.types.EMosaicGroup;

/** MVC model of {@link EMosaicGroup} representable as image */
public class MosaicGroupModel2 implements IImageModel2 {

    public static final String PROPERTY_MOSAIC_GROUP = "MosaicGroup";

    public MosaicGroupModel2(EMosaicGroup mosaicGroup) {
        this.mosaicGroup = mosaicGroup;
    }


    private EMosaicGroup mosaicGroup;

    private final SizeDouble size = new SizeDouble(ImageHelper.DEFAULT_IMAGE_SIZE, ImageHelper.DEFAULT_IMAGE_SIZE);
    /** inside padding */
    private final BoundDouble pad = new BoundDouble(ImageHelper.DEFAULT_PADDING);
    private Consumer<String> changedCallback;


    private Color foregroundColor = ImageHelper.DEFAULT_FOREGROUND_COLOR;

    /** background fill color */
    private Color backgroundColor = ImageHelper.DEFAULT_BK_COLOR;

    private Color borderColor = Color.Maroon().darker(0.5);

    private double borderWidth = 3;

    /** 0° .. +360° */
    private double rotateAngle;
    /** 0° .. +360° foreground color angle-offset */
    private double foregroundAngle;
    /** 0° .. +360° background color angle-offset */
    private double backgroundAngle;


    public EMosaicGroup getMosaicGroup() { return mosaicGroup; }
    public void setMosaicGroup(EMosaicGroup value) {
        if (this.mosaicGroup == value)
            return;

        this.mosaicGroup = value;
        firePropertyChanged(PROPERTY_MOSAIC_GROUP);
    }

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


    public Color getForegroundColor() { return foregroundColor; }
    public void setForegroundColor(Color value) {
        if (this.foregroundColor.equals(value))
            return;

        this.foregroundColor = value;

        firePropertyChanged(PROPERTY_FOREGROUND_COLOR);
    }

    /** background fill color */
    public Color getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(Color value) {
        if (this.backgroundColor.equals(value))
            return;

        this.backgroundColor = value;

        firePropertyChanged(PROPERTY_BACKGROUND_COLOR);
    }

    public Color getBorderColor() { return borderColor; }
    public void setBorderColor(Color value) {
        if (this.borderColor.equals(value))
            return;

        this.borderColor = value;

        firePropertyChanged(PROPERTY_BORDER_COLOR);
    }

    public double getBorderWidth() { return borderWidth; }
    public void setBorderWidth(double value) {
        if (DoubleExt.almostEquals(this.borderWidth, value))
            return;

        this.borderWidth = value;

        firePropertyChanged(PROPERTY_BORDER_WIDTH);
    }


    /** 0° .. +360° */
    public double getRotateAngle() { return rotateAngle; }
    public void setRotateAngle(double value) {
        value = ImageHelper.fixAngle(value);
        if (DoubleExt.almostEquals(this.rotateAngle, value))
            return;

        this.rotateAngle = value;

        firePropertyChanged(PROPERTY_ROTATE_ANGLE);
    }

    /** 0° .. +360° */
    public double getForegroundAngle() { return foregroundAngle; }
    public void setForegroundAngle(double value) {
        value = ImageHelper.fixAngle(value);
        if (DoubleExt.almostEquals(this.foregroundAngle, value))
            return;

        this.foregroundAngle = value;

        firePropertyChanged(PROPERTY_FOREGROUND_ANGLE);
    }

    /** 0° .. +360° */
    public double getBackgroundAngle() { return backgroundAngle; }
    public void setBackgroundAngle(double value) {
        value = ImageHelper.fixAngle(value);
        if (DoubleExt.almostEquals(this.backgroundAngle, value))
            return;

        this.backgroundAngle = value;

        firePropertyChanged(PROPERTY_BACKGROUND_ANGLE);
    }

    public Stream<Pair<Color, Stream<PointDouble>>> getCoords() {
        if (mosaicGroup == null)
            return getCoordsMosaicGroupAsType();

        if (mosaicGroup != EMosaicGroup.eOthers)
            return Stream.of(new Pair<>(getForegroundColor(), getCoordsMosaicGroupAsValue()));

        return getCoordsMosaicGroupAsValueOthers();
    }

    private Stream<PointDouble> getCoordsMosaicGroupAsValue() {
        double sq = Math.min( // size inner square
                                getSize().width  - pad.getLeftAndRight(),
                                getSize().height - pad.getTopAndBottom());
        int vertices = 3 + mosaicGroup.ordinal(); // vertices count
        PointDouble center = new PointDouble(getSize().width / 2.0, getSize().height / 2.0);

        double ra = getRotateAngle();
        if (mosaicGroup != EMosaicGroup.eOthers)
            return FigureHelper.getRegularPolygonCoords(vertices, sq/2, center, ra);

        return FigureHelper.getRegularStarCoords(4, sq/2, sq/5, center, ra);

      //return                           FigureHelper.getFlowingToTheRightPolygonCoordsByRadius(3, vertices, sq / 2, center, ra, ra);
      //return FigureHelper.rotateBySide(FigureHelper.getFlowingToTheRightPolygonCoordsByRadius(3, vertices, sq / 2, center, ra, 0), 2, center, ra);

      //return                           FigureHelper.getFlowingToTheRightPolygonCoordsBySide(3, vertices, sq / 3.5, 2, center, ra, ra);
      //return FigureHelper.rotateBySide(FigureHelper.getFlowingToTheRightPolygonCoordsBySide(3, vertices, sq / 3.5, 2, center, ra, 0), 2, center, ra);
    }

    private Stream<Pair<Color, Stream<PointDouble>>> getCoordsMosaicGroupAsType() {
        final boolean accelerateRevert = true; // ускорение под конец анимации, иначе - в начале...

        int shapes = 4; // 3х-, 4х-, 5ти- и 6ти-угольники

      //double[] angleAccumulative = { rotateAngle };
        double anglePart = 360.0/shapes;

        double sqMax = Math.min( // размер квадрата куда будет вписана фигура при 0°
                                    getSize().width  - pad.getLeftAndRight(),
                                    getSize().height - pad.getTopAndBottom());
        double sqMin = sqMax / 7; // размер квадрата куда будет вписана фигура при 360°
        double sqDiff = sqMax - sqMin;

        PointDouble center = new PointDouble(pad.left + (getSize().width  - pad.getLeftAndRight()) / 2.0,
                                             pad.top  + (getSize().height - pad.getTopAndBottom()) / 2.0);

        Color fgClr = getForegroundColor();
        Stream<Pair<Double, Pair<Color, Stream<PointDouble>>>> res = IntStream.range(0, shapes)
            .mapToObj(shapeNum -> {
                int vertices = 3 + shapeNum;
                double rtAngleShape = ImageHelper.fixAngle(    rotateAngle + shapeNum * anglePart);
                double fgAngleShape = ImageHelper.fixAngle(foregroundAngle + shapeNum * anglePart);
              //angleAccumulative[0] = Math.sin(FigureHelper.toRadian(rotateAngle/4)) * angleAccumulative[0]; // accelerate / ускоряшка..

                double sq = rtAngleShape * sqDiff / 360;
                // (un)comment next line to view result changes...
                sq = Math.sin(FigureHelper.toRadian(rtAngleShape/4)) * sq; // accelerate / ускоряшка..
                sq = accelerateRevert
                    ? sqMin + sq
                    : sqMax - sq;

                double radius = sq/1.8;

                Color clr = new HSV(fgClr).addHue(+fgAngleShape).toColor(); // try: -fgAngleShape

                return new Pair<>(sq, new Pair<>(
                    clr,
                    FigureHelper.getRegularPolygonCoords(vertices,
                                                         radius,
                                                         center,
                                                         45 // try to view: angleAccumulative[0]
                                                        )));
            });

        List<Pair<Double, Pair<Color, Stream<PointDouble>>>> resL = res.collect(Collectors.toList());
        Collections.sort(resL, (o1, o2) -> {
            if (o1.first < o2.first) return 1;
            if (o1.first > o2.first) return -1;
            return 0;
        });
        return resL.stream().map(x -> x.second);
    }

    private Stream<Pair<Color, Stream<PointDouble>>> getCoordsMosaicGroupAsValueOthers() {
        double sq = Math.min( // size inner square
                                getSize().width  - pad.getLeftAndRight(),
                                getSize().height - pad.getTopAndBottom());
        double radius = sq/2.7;

        int shapes = 3; // мозаики из группы EMosaicGroup.eOthers состоят из 3 типов фигур: треугольники, квадраты и шестигранники

        double anglePart = 360.0/shapes;

        final PointDouble center = new PointDouble(getSize().width / 2.0, getSize().height / 2.0);
        final PointDouble zero = new PointDouble(0, 0);
        Color fgClr = getForegroundColor();
        Stream<Pair<Double, Pair<Color, Stream<PointDouble>>>> res = IntStream.range(0, shapes)
            .mapToObj(shapeNum -> {
                double rtAngleShape = ImageHelper.fixAngle(    rotateAngle + shapeNum * anglePart);
                double fgAngleShape = ImageHelper.fixAngle(foregroundAngle + shapeNum * anglePart);

                // adding offset
                PointDouble offset = FigureHelper.getPointOnCircle(sq / 5, rtAngleShape + shapeNum * anglePart, zero);
                PointDouble centerStar = new PointDouble(center.x + offset.x, center.y + offset.y);

                Color clr = new HSV(fgClr).addHue(+fgAngleShape).toColor(); // try: -fgAngleShape

                int vertices;
                switch (shapeNum) { // мозаики из группы EMosaicGroup.eOthers состоят из 3 типов фигур:
                case 0: vertices = 6; break; // шестигранники
                case 1: vertices = 4; break; // квадраты
                case 2: vertices = 3; break; // и треугольники
                default: throw new RuntimeException();
                }
                return new Pair<>(
                    1.0, // const value (no sorting). Provided for the future...
                    new Pair<>(
                        clr,
                        FigureHelper.getRegularPolygonCoords(vertices, radius, centerStar, -rotateAngle)));
            });

        List<Pair<Double, Pair<Color, Stream<PointDouble>>>> resL = res.collect(Collectors.toList());
        Collections.sort(resL, (o1, o2) -> {
            if (o1.first < o2.first) return 1;
            if (o1.first > o2.first) return -1;
            return 0;
        });
        return resL.stream().map(x -> x.second);
    }


    @Override
    public void setListener(Consumer<String> callback) {
        if ((callback != null) && (changedCallback != null))
            throw new IllegalArgumentException("Can only set the controller once");
        changedCallback = callback;
    }

    private void firePropertyChanged(String propertyName) {
        if (changedCallback != null)
            changedCallback.accept(propertyName);
    }

}
