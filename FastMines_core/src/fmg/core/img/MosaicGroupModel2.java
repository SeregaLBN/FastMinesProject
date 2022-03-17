package fmg.core.img;

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

    public MosaicGroupModel2(EMosaicGroup mosaicGroup) { this.mosaicGroup = mosaicGroup; }


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

    /** animation of polar lights */
    private boolean polarLights = true;

    /** animation direction (example: clockwise or counterclockwise for simple rotation) */
    private boolean animeDirection = true;

    /** Image is animated? */
    private boolean animated;

    /** Overall animation period (in milliseconds) */
    private long animatePeriod = 3000;

    /** Total frames of the animated period */
    private int totalFrames = 30;

    private int currentFrame = 0;

    private boolean burgerShow = true;
    private boolean burgerHorizontal = true;
    private int burgerLayers = 3;
    private BoundDouble burgerPad;




    public static final boolean varMosaicGroupAsValueOthers1 = false;
    /** triangle -> quadrangle -> hexagon -> anew triangle -> ... */
    private final int[] nmArray = { 3, 4, 6 };
    private int nmIndex1 = 0;
    private int nmIndex2 = 1;
    private double incrementSpeedAngle;

    public EMosaicGroup getMosaicGroup() { return mosaicGroup; }
    public void setMosaicGroup(EMosaicGroup value) {
        if (this.mosaicGroup == value)
            return;

        this.mosaicGroup = value;
        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_NAME_MOSAIC_GROUP);
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

        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_NAME_SIZE);

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

        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_NAME_OTHER);
    }


    /** Image is animated? */
    public boolean isAnimated() { return animated; }
    public void setAnimated(boolean value) {
        if (this.animated == value)
            return;

        this.animated = value;

        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_NAME_OTHER);
    }

    /** Overall animation period (in milliseconds) */
    public long getAnimatePeriod() { return animatePeriod; }
    /** Overall animation period (in milliseconds) */
    public void setAnimatePeriod(long value) {
        if (this.animatePeriod == value)
            return;

        this.animatePeriod = value;

        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_NAME_OTHER);
    }

    /** Total frames of the animated period */
    public int getTotalFrames() { return totalFrames; }
    public void setTotalFrames(int value) {
        if (this.totalFrames == value)
            return;

        this.totalFrames = value;

        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_NAME_OTHER);

        setCurrentFrame(0);
    }

    public int getCurrentFrame() { return currentFrame; }
    public void setCurrentFrame(int value) {
        if (this.currentFrame == value)
            return;

        this.currentFrame = value;

        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_NAME_OTHER);
    }


    public Color getForegroundColor() { return foregroundColor; }
    public void setForegroundColor(Color value) {
        if (this.foregroundColor.equals(value))
            return;

        this.foregroundColor = value;

        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_NAME_OTHER);
    }

    /** background fill color */
    public Color getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(Color value) {
        if (this.backgroundColor.equals(value))
            return;

        this.backgroundColor = value;

        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_NAME_OTHER);
    }

    public Color getBorderColor() { return borderColor; }
    public void setBorderColor(Color value) {
        if (this.borderColor.equals(value))
            return;

        this.borderColor = value;

        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_NAME_OTHER);
    }

    public double getBorderWidth() { return borderWidth; }
    public void setBorderWidth(double value) {
        if (DoubleExt.almostEquals(this.borderWidth, value))
            return;

        this.borderWidth = value;

        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_NAME_OTHER);
    }

    /** 0° .. +360° */
    public double getRotateAngle() { return rotateAngle; }
    public void setRotateAngle(double value) {
        value = fixAngle(value);
        if (DoubleExt.almostEquals(this.rotateAngle, value))
            return;

        this.rotateAngle = value;

        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_NAME_OTHER);
    }

    /** to diapason (0° .. +360°] */
    public static double fixAngle(double value) {
        return (value >= 360)
             ?              (value % 360)
             : (value < 0)
                ?           (value % 360) + 360
                :            value;
    }

    public boolean isPolarLights() { return polarLights; }
    public void setPolarLights(boolean polarLights) {
        if (this.polarLights == polarLights)
            return;

        this.polarLights = polarLights;

        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_NAME_OTHER);
    }

    public boolean getAnimeDirection() { return animeDirection; }
    public void setAnimeDirection(boolean animeDirection) {
        if (this.animeDirection = animeDirection)
            return;

        this.animeDirection = animeDirection;

        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_NAME_OTHER);
    }


    public boolean isBurgerShow() {
        return burgerShow;
    }

    public void setBurgerShow(boolean burgerShow) {
        if (this.burgerShow = burgerShow)
            return;

        this.burgerShow = burgerShow;

        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_NAME_OTHER);
    }

    public boolean isBurgerHorizontal() {
        return burgerHorizontal;
    }

    public void setBurgerHorizontal(boolean burgerHorizontal) {
        if (this.burgerHorizontal = burgerHorizontal)
            return;

        this.burgerHorizontal = burgerHorizontal;

        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_NAME_OTHER);
    }

    public int getBurgerLayers() {
        return burgerLayers;
    }

    public void setBurgerLayers(int burgerLayers) {
        if (this.burgerLayers == burgerLayers)
            return;

        this.burgerLayers = burgerLayers;

        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_NAME_OTHER);
    }

    public BoundDouble getBurgerPadding() {
        return burgerPad;
    }

    public void setBurgerPadding(BoundDouble burgerPadding) {
        if (this.burgerPad.equals(burgerPadding))
            return;

        this.burgerPad = burgerPadding;

        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_NAME_OTHER);
    }


    protected int[] getNmArray() { return nmArray; }

    protected double getIncrementSpeedAngle() { return incrementSpeedAngle; }
    protected void setIncrementSpeedAngle(double incrementSpeedAngle) { this.incrementSpeedAngle = incrementSpeedAngle; }

    protected int getNmIndex1() { return nmIndex1; }
    protected void setNmIndex1(int nmIndex1) { this.nmIndex1 = nmIndex1; }

    protected int getNmIndex2() { return nmIndex2; }
    protected void setNmIndex2(int nmIndex2) { this.nmIndex2 = nmIndex2; }

    public Stream<Pair<Color, Stream<PointDouble>>> getCoords() {
        EMosaicGroup mosaicGroup = getMosaicGroup();
        return (mosaicGroup == null)
            ? getCoords_MosaicGroupAsType()
            : (mosaicGroup != EMosaicGroup.eOthers)
                ? Stream.of(new Pair<>(getForegroundColor(), getCoords_MosaicGroupAsValue()))
                : MosaicGroupModel2.varMosaicGroupAsValueOthers1
                    ? getCoords_MosaicGroupAsValueOthers1()
                    : getCoords_MosaicGroupAsValueOthers2();
    }

    private Stream<PointDouble> getCoords_MosaicGroupAsValue() {
        BoundDouble pad = getPadding();
        double sq = Math.min( // size inner square
                                getSize().width  - pad.getLeftAndRight(),
                                getSize().height - pad.getTopAndBottom());
        EMosaicGroup mosaicGroup = getMosaicGroup();
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

      //Pair<Integer, Integer> nm = getNM(getNmIndex1());
      //return                           FigureHelper.getFlowingToTheRightPolygonCoordsByRadius(nm.first, nm.second, sq / 2, center, getIncrementSpeedAngle(), ra);
      //return FigureHelper.rotateBySide(FigureHelper.getFlowingToTheRightPolygonCoordsByRadius(nm.first, nm.second, sq / 2, center, getIncrementSpeedAngle(), 0), 2, center, ra);
    }

    private Stream<Pair<Color, Stream<PointDouble>>> getCoords_MosaicGroupAsValueOthers1() {
        BoundDouble pad = getPadding();
        double sq = Math.min( // size inner square
                                getSize().width  - pad.getLeftAndRight(),
                                getSize().height - pad.getTopAndBottom());
        PointDouble center = new PointDouble(getSize().width / 2.0, getSize().height / 2.0);


        Pair<Integer, Integer> nm1 = getNM(getNmIndex1());
        Pair<Integer, Integer> nm2 = getNM(getNmIndex2());
        double isa = getIncrementSpeedAngle();
        double ra = getRotateAngle();
        int sideNum = 2;
        double radius = sq / 3.7; // подобрал.., чтобы не вылазило за периметр изображения
        double sizeSide = sq / 3.5; // подобрал.., чтобы не вылазило за периметр изображения

        final boolean byRadius = false;
        // высчитываю координаты двух фигур.
        // с одинаковым размером одной из граней.
        Stream<PointDouble> resS1 = byRadius
                ? FigureHelper.getFlowingToTheRightPolygonCoordsByRadius(nm1.first, nm1.second, radius, center, isa, 0)
                : FigureHelper.getFlowingToTheRightPolygonCoordsBySide(nm1.first, nm1.second, sizeSide, sideNum, center, isa, 0);
        List<PointDouble> res1 = FigureHelper.rotateBySide(resS1, sideNum, center, ra)
                                            .collect(Collectors.toList());
        Stream<PointDouble> resS2 = byRadius
                ? FigureHelper.getFlowingToTheRightPolygonCoordsByRadius(nm2.first, nm2.second, radius, center, isa, 0)
                : FigureHelper.getFlowingToTheRightPolygonCoordsBySide(nm2.first, nm2.second, sizeSide, sideNum, center, isa, 0);
        List<PointDouble> res2 = FigureHelper.rotateBySide(resS2, sideNum, center, ra+180) // +180° - разворачиваю вторую фигуру, чтобы не пересекалась с первой фигурой
                                            .collect(Collectors.toList());

        // и склеиваю грани:
        //  * нахожу середины граней
        PointDouble p11 = res1.get(sideNum - 1); PointDouble p12 = res1.get(sideNum);
        PointDouble p21 = res2.get(sideNum - 1); PointDouble p22 = res2.get(sideNum);
        PointDouble centerPoint1 = new PointDouble((p11.x + p12.x) / 2, (p11.y + p12.y) / 2);
        PointDouble centerPoint2 = new PointDouble((p21.x + p22.x) / 2, (p21.y + p22.y) / 2);

        //  * и совмещаю их по центру изображения
        PointDouble offsetToCenter1 = new PointDouble(center.x - centerPoint1.x, center.y - centerPoint1.y);
        PointDouble offsetToCenter2 = new PointDouble(center.x - centerPoint2.x, center.y - centerPoint2.y);
        Color fgClr = getForegroundColor();
        boolean pl = isPolarLights();
        return Stream.of(
                new Pair<>(        fgClr                       , FigureHelper.move(res1.stream(), offsetToCenter1)),
                new Pair<>(pl ?
                           new HSV(fgClr).addHue(180).toColor()
                           :       fgClr                       , FigureHelper.move(res2.stream(), offsetToCenter2))
            );
    }

    private Pair<Integer, Integer> getNM(int index) {
        int[] nmArrayLocal = getNmArray();
        int n = nmArrayLocal[index];
        int m = nmArrayLocal[(index + 1) % nmArrayLocal.length];

        // Во вторую половину вращения фиксирую значение N равно M.
        // Т.к. в прервую половину, с 0 до 180, N стремится к M - см. описание FigureHelper.getFlowingToTheRightPolygonCoordsByXxx...
        // Т.е. при значении 180 значение N уже достигло M.
        // Фиксирую для того, чтобы при следующем инкременте параметра index, значение N не менялось. Т.о. обеспечиваю плавность анимации.
        if (getIncrementSpeedAngle() >= 180) {
            if (getAnimeDirection())
                n = m;
        } else {
            if (!getAnimeDirection())
                n = m;
        }
        return new Pair<>(n, m);
    }

    private Stream<Pair<Color, Stream<PointDouble>>> getCoords_MosaicGroupAsType() {
        final boolean accelerateRevert = true; // ускорение под конец анимации, иначе - в начале...

        int shapes = 4; // 3х-, 4х-, 5ти- и 6ти-угольники

        double angle = getRotateAngle();
      //double[] angleAccumulative = { angle };
        double anglePart = 360.0/shapes;

        BoundDouble pad = getPadding();
        double sqMax = Math.min( // размер квадрата куда будет вписана фигура при 0°
                                    getSize().width  - pad.getLeftAndRight(),
                                    getSize().height - pad.getTopAndBottom());
        double sqMin = sqMax / 7; // размер квадрата куда будет вписана фигура при 360°
        double sqDiff = sqMax - sqMin;

        PointDouble center = new PointDouble(pad.left + (getSize().width  - pad.getLeftAndRight()) / 2.0,
                                             pad.top  + (getSize().height - pad.getTopAndBottom()) / 2.0);

        Color fgClr = getForegroundColor();
        boolean pl = isPolarLights();
        Stream<Pair<Double, Pair<Color, Stream<PointDouble>>>> res = IntStream.range(0, shapes)
            .mapToObj(shapeNum -> {
                int vertices = 3+shapeNum;
                double angleShape = fixAngle(angle + shapeNum * anglePart);
              //angleAccumulative[0] = Math.sin(FigureHelper.toRadian(angle/4))*angleAccumulative[0]; // accelerate / ускоряшка..

                double sq = angleShape * sqDiff / 360;
                // (un)comment next line to view result changes...
                sq = Math.sin(FigureHelper.toRadian(angleShape/4))*sq; // accelerate / ускоряшка..
                sq = accelerateRevert
                    ? sqMin + sq
                    : sqMax - sq;

                double radius = sq/1.8;

                Color clr = !pl
                    ? fgClr
                    : new HSV(fgClr).addHue(+angleShape).toColor(); // try: -angleShape

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

    private Stream<Pair<Color, Stream<PointDouble>>> getCoords_MosaicGroupAsValueOthers2() {
        BoundDouble pad = getPadding();
        double sq = Math.min( // size inner square
                                getSize().width  - pad.getLeftAndRight(),
                                getSize().height - pad.getTopAndBottom());
        double radius = sq/2.7;

        int shapes = 3; // мозаики из группы EMosaicGroup.eOthers состоят из 3 типов фигур: треугольники, квадраты и шестигранники

        double angle = getRotateAngle();
        double anglePart = 360.0/shapes;

        final PointDouble center = new PointDouble(getSize().width / 2.0, getSize().height / 2.0);
        final PointDouble zero = new PointDouble(0, 0);
        Color fgClr = getForegroundColor();
        boolean pl = isPolarLights();
        Stream<Pair<Double, Pair<Color, Stream<PointDouble>>>> res = IntStream.range(0, shapes)
            .mapToObj(shapeNum -> {
                double angleShape = angle*shapeNum;

                // adding offset
                PointDouble offset = FigureHelper.getPointOnCircle(sq / 5, angleShape + shapeNum * anglePart, zero);
                PointDouble centerStar = new PointDouble(center.x + offset.x, center.y + offset.y);

                Color clr = !pl
                    ? fgClr
                    : new HSV(fgClr).addHue(shapeNum * anglePart).toColor();

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
                        FigureHelper.getRegularPolygonCoords(vertices, radius, centerStar, -angle)));
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

}
