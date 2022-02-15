package fmg.core.img;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import fmg.common.Color;
import fmg.common.geom.Coord;
import fmg.common.geom.DoubleExt;
import fmg.common.geom.PointDouble;
import fmg.common.geom.SizeDouble;
import fmg.common.geom.util.FigureHelper;
import fmg.core.mosaic.MosaicDrawModel;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.shape.BaseShape;
import fmg.core.types.Property;
import fmg.core.types.draw.PenBorder;

/** Representable {@link fmg.core.types.EMosaic} as animated image */
public class MosaicAnimatedModel<TImageInner>
     extends MosaicDrawModel<TImageInner>
 implements IMosaicAnimatedModel<TImageInner>
{

    public static final String PROPERTY_ROTATE_ANGLE     = "RotateAngle";
    public static final String PROPERTY_ROTATE_MODE      = "RotateMode";
    public static final String PROPERTY_ROTATED_ELEMENTS = "RotatedElements";

    @Property(PROPERTY_ROTATE_MODE)
    private EMosaicRotateMode rotateMode = EMosaicRotateMode.fullMatrix;

    /** 0° .. +360° */
    @Property(PROPERTY_ROTATE_ANGLE)
    private double rotateAngle;

    /** list of offsets rotation angles prepared for cells */
    private final List<Double /* angle offset */ > prepareList = new ArrayList<>();

    @Property(PROPERTY_ROTATED_ELEMENTS)
    private final List<RotatedCellContext> rotatedElements = new ArrayList<>();

    private final AnimatedInnerModel innerModel = new AnimatedInnerModel();
    private boolean hackLock = false;
    private final PropertyChangeListener onInnerModelPropertyChangedListener = this::onInnerModelPropertyChanged;

    public MosaicAnimatedModel() {
        innerModel.addListener(onInnerModelPropertyChangedListener);
    }

    @Override
    public boolean isAnimated() { return innerModel.isAnimated(); }
    @Override
    public void setAnimated(boolean value) { innerModel.setAnimated(value); }

    /** Overall animation period (in milliseconds) */
    @Override
    public long getAnimatePeriod() { return innerModel.getAnimatePeriod(); }
    /** Overall animation period (in milliseconds) */
    @Override
    public void setAnimatePeriod(long value) { innerModel.setAnimatePeriod(value); }

    /** Total frames of the animated period */
    @Override
    public int getTotalFrames() { return innerModel.getTotalFrames(); }
    @Override
    public void setTotalFrames(int value) { innerModel.setTotalFrames(value); }

    @Override
    public int getCurrentFrame() { return innerModel.getCurrentFrame(); }
    @Override
    public void setCurrentFrame(int value) { innerModel.setCurrentFrame(value); }

    @Override
    public EMosaicRotateMode getRotateMode() { return rotateMode; }
    @Override
    public void setRotateMode(EMosaicRotateMode value) { notifier.setProperty(rotateMode, value, PROPERTY_ROTATE_MODE); }

    /** 0° .. +360° */
    @Override
    public double getRotateAngle() { return rotateAngle; }
    @Override
    public void setRotateAngle(double value) {
        notifier.setProperty(rotateAngle, AnimatedImageModel.fixAngle(value), PROPERTY_ROTATE_ANGLE);
    }

    public List<RotatedCellContext> getRotatedElements() { return rotatedElements; }

    @Override
    protected void onPropertyChanged(PropertyChangeEvent ev) {
        if (hackLock)
            return;
        super.onPropertyChanged(ev);
        switch (ev.getPropertyName()) {
        case PROPERTY_ROTATE_MODE:
        case PROPERTY_SIZE_FIELD:
            if (getRotateMode() == EMosaicRotateMode.someCells)
                randomRotateElemenIndex();
            break;
        default:
            // none
        }
    }

    /** ///////////// ================= PART {@link EMosaicRotateMode#fullMatrix} ======================= ///////////// */

    public void rotateMatrix() { rotateMatrix(true); }
    private void rotateMatrix(boolean reinit) {
        SizeDouble size = getShape().getSize(getSizeField());
        PointDouble center = new PointDouble(size.width  / 2,
                                                size.height / 2);
        double rotateAngle = getRotateAngle();
        for (BaseCell cell : getMatrix()) {
            cell.init(); // restore base coords

            FigureHelper.rotateCollection(cell.getRegion().getPoints(), rotateAngle, center);
        }
        notifier.firePropertyChanged(PROPERTY_MATRIX);
    }

    /** ///////////// ================= PART {@link EMosaicRotateMode#someCells} ======================= ///////////// */

    private boolean rotateCellAlterantive;

    public static final class RotatedCellContext {
        public RotatedCellContext(int index, double angleOffset, double area) {
            this.index = index;
            this.angleOffset = angleOffset;
            this.area = area;
        }
        public final int index;
        public final double angleOffset;
        public double area;
    }

    /** rotate BaseCell from original Matrix with modified Region */
    protected void rotateCells() {
        BaseShape shape = getShape();
        List<BaseCell> matrix = getMatrix();
        final double area = getArea();
        final double angle = getRotateAngle();

        rotatedElements.forEach(cntxt -> {
            assert (cntxt.angleOffset >= 0);
            double angle2 = angle - cntxt.angleOffset;
            if (angle2 < 0)
                angle2 += 360;
            assert (angle2 < 360);
            assert (angle2 >= 0);
            // (un)comment next line to look result changes...
            angle2 = Math.sin(FigureHelper.toRadian((angle2 / 4))) * angle2; // accelerate / ускоряшка..

            // (un)comment next line to look result changes...
            cntxt.area = area * (1 + Math.sin(FigureHelper.toRadian(angle2 / 2))); // zoom'ирую


            BaseCell cell = matrix.get(cntxt.index);

            cell.init();
            PointDouble center = cell.getCenter();
            Coord coord = cell.getCoord();

            hackLock = true;
            try {
                // modify
                shape.setArea(cntxt.area);

                // rotate
                cell.init();
                PointDouble centerNew = cell.getCenter();
                PointDouble delta = new PointDouble(center.x - centerNew.x, center.y - centerNew.y);
                FigureHelper.moveCollection(FigureHelper.rotateCollection(cell.getRegion().getPoints(), (((coord.x + coord.y) & 1) == 0) ? +angle2 : -angle2, rotateCellAlterantive ? center : centerNew), delta);

                // restore
                shape.setArea(area);
            } finally {
                hackLock = false;
            }
        });

        // Z-ordering
        Collections.sort(rotatedElements, (e1, e2) -> Double.compare(e1.area, e2.area));
    }

    public List<BaseCell> getNotRotatedCells() {
        if (rotatedElements.isEmpty())
            return getMatrix();

        List<BaseCell> matrix = getMatrix();
        List<Integer> indexes = rotatedElements.stream().map(cntxt -> cntxt.index).collect(Collectors.toList());
        List<BaseCell> notRotated = new ArrayList<>(matrix.size() - indexes.size());
        int i = 0;
        for (BaseCell cell : matrix) {
            if (!indexes.contains(i))
                notRotated.add(cell);
            ++i;
        }
        return notRotated;
    }

    public void getRotatedCells(Consumer<List<BaseCell>> rotatedCellsFunctor) {
        if (rotatedElements.isEmpty())
            return;

        PenBorder pb = getPenBorder();
        // save
        double borderWidth = pb.getWidth();
        Color colorLight  = pb.getColorLight();
        Color colorShadow = pb.getColorShadow();

        hackLock = true;
        try {
            // modify
            pb.setWidth(2 * borderWidth);
            pb.setColorLight(colorLight.darker(0.5));
            pb.setColorShadow(colorShadow.darker(0.5));

            List<BaseCell> matrix = getMatrix();
            List<BaseCell> rotatedCells = new ArrayList<>(rotatedElements.size());
            for (RotatedCellContext cntxt : rotatedElements)
                rotatedCells.add(matrix.get(cntxt.index));
            rotatedCellsFunctor.accept(rotatedCells);

            // restore
            pb.setWidth(borderWidth);
            pb.setColorLight(colorLight);
            pb.setColorShadow(colorShadow);
        } finally {
            hackLock = false;
        }
    }

    private void randomRotateElemenIndex() {
        prepareList.clear();
        if (!rotatedElements.isEmpty()) {
            rotatedElements.clear();
            notifier.firePropertyChanged(PROPERTY_ROTATED_ELEMENTS);
        }

//        if (!isAnimated())
//            return;

        // create random cells indexes  and  base rotate offset (negative)
        int len = getMatrix().size();
        for (int i = 0; i < len/4.5; ++i) {
            addRandomToPrepareList(i==0);
        }
    }

    private void addRandomToPrepareList(boolean zero) {
        double offset = (zero ? 0 : ThreadLocalRandom.current().nextInt(360)) + getRotateAngle();
        if (offset > 360)
            offset -= 360;
        prepareList.add(offset);
    }

    private int nextRandomIndex() {
        int len = getMatrix().size();
        assert (rotatedElements.size() < len);
        Random rand = ThreadLocalRandom.current();
        do {
            int index = rand.nextInt(len);
            if (rotatedElements.stream().anyMatch(ctxt -> ctxt.index == index))
                continue;
            return index;
        } while(true);
    }

    public void updateAnglesOffsets(double rotateAngleDelta) {
        double angleNew = getRotateAngle();
        double angleOld = angleNew - rotateAngleDelta;
        double rotateDelta = rotateAngleDelta;
        double area = getArea();

        if (!prepareList.isEmpty()) {
            List<Double> copyList = new ArrayList<>(prepareList);
            for (int i = copyList.size()-1; i >= 0; --i) {
                double angleOffset = copyList.get(i);
                if ((rotateDelta >= 0)
                    ?  ((angleOld <= angleOffset && angleOffset <  angleNew && angleOld < angleNew) || // example: old=10   offset=15   new=20
                        (angleOld <= angleOffset && angleOffset >  angleNew && angleOld > angleNew) || // example: old=350  offset=355  new=0
                        (angleOld >  angleOffset && angleOffset <= angleNew && angleOld > angleNew))   // example: old=355  offset=0    new=5
                    :  ((angleOld >= angleOffset && angleOffset >  angleNew && angleOld > angleNew) || // example: old=20   offset=15   new=10
                        (angleOld <  angleOffset && angleOffset >  angleNew && angleOld < angleNew) || // example: old=0    offset=355  new=350
                        (angleOld >= angleOffset && angleOffset <= angleNew && angleOld < angleNew)))  // example: old=5    offset=0    new=355
                {
                    prepareList.remove(i);
                    rotatedElements.add(new RotatedCellContext(nextRandomIndex(), angleOffset, area));
                    notifier.firePropertyChanged(PROPERTY_ROTATED_ELEMENTS);
                }
            }
        }

        List<RotatedCellContext> toRemove = new ArrayList<>();
        rotatedElements.forEach(cntxt -> {
            double angle2 = angleNew - cntxt.angleOffset;
            if (angle2 < 0)
                angle2 += 360;
            assert (angle2 < 360);
            assert (angle2 >= 0);

            // prepare to next step - exclude current cell from rotate and add next random cell
            double angle3 = angle2 + rotateDelta;
            if ((angle3 >= 360) || (angle3 < 0)) {
                toRemove.add(cntxt);
            }
        });
        if (!toRemove.isEmpty()) {
            List<BaseCell> matrix = getMatrix();
            toRemove.forEach(cntxt -> {
                                matrix.get(cntxt.index).init(); // restore original region coords
                                rotatedElements.remove(cntxt);
                                if (rotatedElements.isEmpty())
                                rotateCellAlterantive = !rotateCellAlterantive;
                                addRandomToPrepareList(false);
                            });
            notifier.firePropertyChanged(PROPERTY_ROTATED_ELEMENTS);
        }
    }

    @Override
    protected void onShapePropertyChanged(PropertyChangeEvent ev) {
        if (hackLock)
            return;

        super.onShapePropertyChanged(ev);

        String propName = ev.getPropertyName();
        if (BaseShape.PROPERTY_AREA.equals(propName))
            switch (getRotateMode()) {
            case fullMatrix:
                if (!DoubleExt.hasMinDiff(rotateAngle, 0))
                    rotateMatrix(false);
                break;
            case someCells:
                //updateAnglesOffsets(rotateAngleDelta);
                //rotateCells();
                break;
            default:
                throw new RuntimeException("Unsupported RotateMode=" + getRotateMode());
            }
    }

    protected void onInnerModelPropertyChanged(PropertyChangeEvent ev) {
        // refire
        notifier.firePropertyChanged(ev.getOldValue(), ev.getNewValue(), ev.getPropertyName());
    }

    @Override
    public void close() {
        innerModel.removeListener(onInnerModelPropertyChangedListener);
        super.close();
    }

}
