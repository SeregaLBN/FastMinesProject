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
import fmg.core.types.ESkillLevel;

/** MVC model of {@link ESkillLevel} representable as image */
public class MosaicSkillModel2 implements IImageModel2 {

    public static final String PROPERTY_MOSAIC_SKILL = "MosaicSkill";

    public MosaicSkillModel2(ESkillLevel mosaicSkill) {
        this.mosaicSkill = mosaicSkill;
    }


    private ESkillLevel mosaicSkill;

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


    public ESkillLevel getMosaicSkill() { return mosaicSkill; }
    public void setMosaicSkill(ESkillLevel value) {
        if (this.mosaicSkill == value)
            return;

        this.mosaicSkill = value;
        if (changedCallback != null)
            changedCallback.accept(PROPERTY_MOSAIC_SKILL);
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
            changedCallback.accept(PROPERTY_SIZE);

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
            changedCallback.accept(PROPERTY_PADDING);
    }


    public Color getForegroundColor() { return foregroundColor; }
    public void setForegroundColor(Color value) {
        if (this.foregroundColor.equals(value))
            return;

        this.foregroundColor = value;

        if (changedCallback != null)
            changedCallback.accept(PROPERTY_FOREGROUND_COLOR);
    }

    /** background fill color */
    public Color getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(Color value) {
        if (this.backgroundColor.equals(value))
            return;

        this.backgroundColor = value;

        if (changedCallback != null)
            changedCallback.accept(PROPERTY_BACKGROUND_COLOR);
    }

    public Color getBorderColor() { return borderColor; }
    public void setBorderColor(Color value) {
        if (this.borderColor.equals(value))
            return;

        this.borderColor = value;

        if (changedCallback != null)
            changedCallback.accept(PROPERTY_BORDER_COLOR);
    }

    public double getBorderWidth() { return borderWidth; }
    public void setBorderWidth(double value) {
        if (DoubleExt.almostEquals(this.borderWidth, value))
            return;

        this.borderWidth = value;

        if (changedCallback != null)
            changedCallback.accept(PROPERTY_BORDER_WIDTH);
    }


    /** 0° .. +360° */
    public double getRotateAngle() { return rotateAngle; }
    public void setRotateAngle(double value) {
        value = ImageHelper.fixAngle(value);
        if (DoubleExt.almostEquals(this.rotateAngle, value))
            return;

        this.rotateAngle = value;

        if (changedCallback != null)
            changedCallback.accept(PROPERTY_ROTATE_ANGLE);
    }

    /** 0° .. +360° */
    public double getForegroundAngle() { return foregroundAngle; }
    public void setForegroundAngle(double value) {
        value = ImageHelper.fixAngle(value);
        if (DoubleExt.almostEquals(this.foregroundAngle, value))
            return;

        this.foregroundAngle = value;

        if (changedCallback != null)
            changedCallback.accept(PROPERTY_FOREGROUND_ANGLE);
    }

    /** 0° .. +360° */
    public double getBackgroundAngle() { return backgroundAngle; }
    public void setBackgroundAngle(double value) {
        value = ImageHelper.fixAngle(value);
        if (DoubleExt.almostEquals(this.backgroundAngle, value))
            return;

        this.backgroundAngle = value;

        if (changedCallback != null)
            changedCallback.accept(PROPERTY_BACKGROUND_ANGLE);
    }

    public Stream<Pair<Color, Stream<PointDouble>>> getCoords() {
        return (getMosaicSkill() == null)
            ? getCoordsSkillLevelAsType()
            : getCoordsSkillLevelAsValue();
    }

    private Stream<Pair<Color, Stream<PointDouble>>> getCoordsSkillLevelAsType() {
        final boolean bigMaxStar = !true; // true - большая звезда - вне картинки; false - большая звезда - внутри картинки.
        final boolean accelerateRevert = !true; // ускорение под конец анимации, иначе - в начале...

        int rays = 5;
        int stars = bigMaxStar ? 6 : 8;

        double sqMax = Math.min( // размер квадрата куда будет вписана звезда при 0°
                                size.width  - pad.getLeftAndRight(),
                                size.height - pad.getTopAndBottom());
        double sqMin = 1;//sqMax / (bigMaxStar ? 17 : 7); // размер квадрата куда будет вписана звезда при 360°
        double sqExt = sqMax * 3;

        PointDouble centerMax = new PointDouble(pad.left + (size.width  - pad.getLeftAndRight()) / 2.0,
                                                pad.top  + (size.height - pad.getTopAndBottom()) / 2.0);
        PointDouble centerMin = new PointDouble(pad.left + sqMin/2, pad.top + sqMin/2);
        PointDouble centerExt = new PointDouble(size.width * 1.5, size.height * 1.5);

        return Stream.concat(
            getCoordsSkillLevelAsType2(true , bigMaxStar, accelerateRevert, rays, stars/2, sqMin, sqMax, centerMin, centerMax),
            getCoordsSkillLevelAsType2(false, bigMaxStar, accelerateRevert, rays, stars/2, sqMax, sqExt, centerMax, centerExt));
      //return getCoords_SkillLevelAsType2(false, bigMaxStar, accelerateRevert, rays, stars/2, angle, sqMin, sqMax, centerMin, centerMax); // old
    }

    private Stream<Pair<Color, Stream<PointDouble>>> getCoordsSkillLevelAsType2(
        boolean accumulative,
        boolean bigMaxStar,
        boolean accelerateRevert,
        int rays,
        int stars,
        double sqMin,
        double sqMax,
        PointDouble centerMin,
        PointDouble centerMax
    ) {
        double[] angleAccumulative = { rotateAngle };
        double anglePart = 360.0/stars;
        double sqDiff = sqMax - sqMin;
        PointDouble centerDiff = new PointDouble(centerMax.x - centerMin.x, centerMax.y - centerMin.y);
        Color fgClr = getForegroundColor();
        Stream<Pair<Double, Pair<Color, Stream<PointDouble>>>> res = IntStream.range(0, stars)
            .mapToObj(starNum -> {
                double angleStar = ImageHelper.fixAngle(rotateAngle + starNum * anglePart);
                double frgndAngle = ImageHelper.fixAngle(foregroundAngle + starNum * anglePart);
                if (accumulative)
                    angleAccumulative[0] = Math.sin(FigureHelper.toRadian(rotateAngle/4))*angleAccumulative[0]; // accelerate / ускоряшка..

                double sq = angleStar * sqDiff / 360;
                // (un)comment next line to view result changes...
                sq = Math.sin(FigureHelper.toRadian(angleStar/4))*sq; // accelerate / ускоряшка..
                sq = accelerateRevert
                    ? sqMin + sq
                    : sqMax - sq;

                double r1 = bigMaxStar ? sq*2.2 : sq/2; // external radius
                double r2 = r1/2.6; // internal radius

                PointDouble centerStar = new PointDouble(angleStar * centerDiff.x / 360,
                                                        angleStar * centerDiff.y / 360);
                // (un)comment next 2 lines to view result changes...
                centerStar.x = Math.sin(FigureHelper.toRadian(angleStar/4))*centerStar.x; // accelerate / ускоряшка..
                centerStar.y = Math.sin(FigureHelper.toRadian(angleStar/4))*centerStar.y; // accelerate / ускоряшка..
                centerStar.x = accelerateRevert
                    ? centerMin.x + centerStar.x
                    : centerMax.x - centerStar.x;
                centerStar.y = accelerateRevert
                    ? centerMin.y + centerStar.y
                    : centerMax.y - centerStar.y;

                Color clr = new HSV(fgClr).addHue(+frgndAngle).toColor();// try: -frgndAngle

                return new Pair<>(sq, new Pair<>(
                    clr,
                    FigureHelper.getRegularStarCoords(rays,
                                                      r1, r2,
                                                      bigMaxStar ? centerMax : centerStar,
                                                      accumulative ? angleAccumulative[0] : 0
                                                     )));
            });

        List<Pair<Double, Pair<Color, Stream<PointDouble>>>> resL = res.collect(Collectors.toList());
        Collections.sort(resL, (o1, o2) -> {
            if (o1.first < o2.first) return bigMaxStar ?  1 : -1;
            if (o1.first > o2.first) return bigMaxStar ? -1 :  1;
            return 0;
        });
        return resL.stream().map(x -> x.second);
    }

    private Stream<Pair<Color, Stream<PointDouble>>> getCoordsSkillLevelAsValue() {
        double sq = Math.min( // size inner square
                              size.width  - pad.getLeftAndRight(),
                              size.height - pad.getTopAndBottom());
        double r1 = sq/7; // external radius
        double r2 = sq/12; // internal radius

        ESkillLevel skill = getMosaicSkill();
        int ordinal = skill.ordinal();
        int rays = 5 + ordinal; // rays count
        int stars = 4 + ordinal; // number of stars on the perimeter of the circle

        double[] angleAccumulative = { rotateAngle };
        double anglePart = 360.0/stars;

        final PointDouble center = new PointDouble(size.width / 2.0, size.height / 2.0);
        final PointDouble zero = new PointDouble(0, 0);
        Color fgClr = getForegroundColor();
        Stream<Pair<Color, Stream<PointDouble>>> res = IntStream.range(0, stars)
            .mapToObj(starNum -> {
                // (un)comment next line to view result changes...
                angleAccumulative[0] = Math.sin(FigureHelper.toRadian(angleAccumulative[0]/4))*angleAccumulative[0]; // accelerate / ускоряшка..

                // adding offset
                PointDouble offset = FigureHelper.getPointOnCircle(sq / 3, angleAccumulative[0] + starNum * anglePart, zero);
                PointDouble centerStar = new PointDouble(center.x + offset.x, center.y + offset.y);

                double frgndAngle = ImageHelper.fixAngle(foregroundAngle + starNum * anglePart);
                Color clr = new HSV(fgClr).addHue(frgndAngle).toColor();

                return new Pair<>(clr, (skill == ESkillLevel.eCustom)
                    ? FigureHelper.getRegularPolygonCoords(3 + (starNum % 4), r1, centerStar, -angleAccumulative[0])
                    : FigureHelper.getRegularStarCoords(rays, r1, r2, centerStar, -angleAccumulative[0]));
            });
        List<Pair<Color, Stream<PointDouble>>> resL = res.collect(Collectors.toList());
        Collections.reverse(resL); // reverse stars, to draw the first star of the latter. (pseudo Z-order). (un)comment line to view result changes...
        return resL.stream();
    }


    @Override
    public void setListener(Consumer<String> callback) {
        if ((callback != null) && (changedCallback != null))
            throw new IllegalArgumentException("Can only set the controller once");
        changedCallback = callback;
    }

}
