package fmg.core.img;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import fmg.common.geom.*;
import fmg.common.geom.util.FigureHelper;
import fmg.core.mosaic.MosaicModel2;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.shape.BaseShape;

/** Representable {@link fmg.core.types.EMosaic} as animated image */
public class MosaicImageModel2 extends MosaicModel2 {

    /** Mosaic rotate mode */
    public enum ERotateMode {
        /** rotate full matrix (all cells) */
        FULL_MATRIX,
        /** rotate some cells (independently of each other) */
        SOME_CELLS
    }


    private ERotateMode rotateMode = ERotateMode.FULL_MATRIX;

    /** 0° .. +360° */
    private double rotateAngle;
    /** 0° .. +360° background color angle-offset */
    private double backgroundAngle;

    /** list of offsets rotation angles prepared for cells */
    private final List<Double /* angle offset */ > prepareList = new ArrayList<>();
    private final List<RotatedCellContext> rotatedElements = new ArrayList<>();


    public MosaicImageModel2() {
        super(false);
    }

    public ERotateMode getRotateMode() { return rotateMode; }
    public void setRotateMode(ERotateMode value) {
        if (this.rotateMode == value)
            return;

        this.rotateMode = value;

        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_OTHER);

        prepareList.clear();
        rotatedElements.clear();
    }

    /** 0° .. +360° */
    public double getRotateAngle() { return rotateAngle; }
    public void setRotateAngle(double value) {
        value = ImageHelper.fixAngle(value);
        if (DoubleExt.almostEquals(this.rotateAngle, value))
            return;

        this.rotateAngle = value;

        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_OTHER);
    }

    /** 0° .. +360° */
    public double getBackgroundAngle() { return backgroundAngle; }
    public void setBackgroundAngle(double value) {
        value = ImageHelper.fixAngle(value);
        if (DoubleExt.almostEquals(this.backgroundAngle, value))
            return;

        this.backgroundAngle = value;

        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_OTHER);
    }

    public List<RotatedCellContext> getRotatedElements() { return rotatedElements; }

    @Override
    public void setSizeField(Matrisize newSizeField) {
        super.setSizeField(newSizeField);

        if (rotateMode == ERotateMode.SOME_CELLS)
            randomRotateElemenIndex();
    }

    /** ///////////// ================= PART {@link ERotateMode#FULL_MATRIX} ======================= ///////////// */

    public void rotateMatrix() {
        SizeDouble size = getShape().getSize(getSizeField());
        PointDouble center = new PointDouble(size.width  / 2,
                                             size.height / 2);
        for (BaseCell cell : getMatrix()) {
            cell.init(); // restore base coords

            FigureHelper.rotateCollection(cell.getRegion().getPoints(), rotateAngle, center);
        }

        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_OTHER); // PROPERTY_MATRIX
    }

    /** ///////////// ================= PART {@link ERotateMode#SOME_CELLS} ======================= ///////////// */

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
        final double area = shape.getArea();
        final double angle = rotateAngle;

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

            // modify
            var callback = changedCallback;
            changedCallback = null; // lock to fire changing model
            shape.setArea(cntxt.area);

            // rotate
            cell.init();
            PointDouble centerNew = cell.getCenter();
            PointDouble delta = new PointDouble(center.x - centerNew.x, center.y - centerNew.y);
            FigureHelper.moveCollection(FigureHelper.rotateCollection(cell.getRegion().getPoints(), (((coord.x + coord.y) & 1) == 0) ? +angle2 : -angle2, rotateCellAlterantive ? center : centerNew), delta);

            // restore
            shape.setArea(area);
            changedCallback = callback;
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

    public List<BaseCell> getRotatedCells() {
        if (rotatedElements.isEmpty())
            return Collections.emptyList();

        List<BaseCell> matrix = getMatrix();
        List<BaseCell> rotatedCells = new ArrayList<>(rotatedElements.size());
        for (RotatedCellContext cntxt : rotatedElements)
            rotatedCells.add(matrix.get(cntxt.index));
        return rotatedCells;
    }

    private void randomRotateElemenIndex() {
        prepareList.clear();
        rotatedElements.clear();

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
        double area = getShape().getArea();

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

                    if (changedCallback != null)
                        changedCallback.accept(ImageHelper.PROPERTY_OTHER);
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

            if (changedCallback != null)
                changedCallback.accept(ImageHelper.PROPERTY_OTHER);
        }
    }

}
