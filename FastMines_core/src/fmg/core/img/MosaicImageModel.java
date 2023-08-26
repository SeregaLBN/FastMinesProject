package fmg.core.img;

import static fmg.core.img.PropertyConst.PROPERTY_BACKGROUND_ANGLE;
import static fmg.core.img.PropertyConst.PROPERTY_ROTATE_ANGLE;
import static fmg.core.img.PropertyConst.PROPERTY_ROTATE_FULL_MATRIX;
import static fmg.core.img.PropertyConst.PROPERTY_ROTATE_MODE;
import static fmg.core.img.PropertyConst.PROPERTY_ROTATE_SOME_CELLS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import fmg.common.geom.*;
import fmg.common.geom.util.FigureHelper;
import fmg.core.mosaic.MosaicModel;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.shape.BaseShape;

/** Representable {@link fmg.core.types.EMosaic} as animated image */
public class MosaicImageModel extends MosaicModel {

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


    public MosaicImageModel() {
        super(false);

        var pen = getPenBorder();
        pen.setColorLight(pen.getColorShadow());
    }

    public ERotateMode getRotateMode() { return rotateMode; }
    public void setRotateMode(ERotateMode value) {
        if (this.rotateMode == value)
            return;

        this.rotateMode = value;

        firePropertyChanged(PROPERTY_ROTATE_MODE);

        applyRotateModeSomeCells();
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
    public double getBackgroundAngle() { return backgroundAngle; }
    public void setBackgroundAngle(double value) {
        value = ImageHelper.fixAngle(value);
        if (DoubleExt.almostEquals(this.backgroundAngle, value))
            return;

        this.backgroundAngle = value;

        firePropertyChanged(PROPERTY_BACKGROUND_ANGLE);
    }

    @Override
    public void setSizeField(Matrisize newSizeField) {
        super.setSizeField(newSizeField);

        applyRotateModeSomeCells();
    }

    private void applyRotateModeSomeCells() {
        if (rotateMode != ERotateMode.SOME_CELLS)
            return;

        prepareList.clear();
        rotatedElements.clear();

        // create random cells indexes  and  base rotate offset (negative)
        int len = getMatrix().size();
        for (int i = 0; i < len/4.5; ++i)
            addRandomToPrepareList(i==0);
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

        firePropertyChanged(PROPERTY_ROTATE_FULL_MATRIX);
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
    protected void rotateCells(double rotateAngleDelta) {
        // Step 1. updateAnglesOffsets
        final double angleNew = rotateAngle;
        final double angleOld = angleNew - rotateAngleDelta;

        BaseShape shape = getShape();
        final double area = shape.getArea();
        List<BaseCell> matrix = getMatrix();

        if (!prepareList.isEmpty()) {
            List<Double> copyList = new ArrayList<>(prepareList);
            for (int i = copyList.size()-1; i >= 0; --i) {
                double angleOffset = copyList.get(i);
                if ((rotateAngleDelta >= 0)
                    ?  ((angleOld <= angleOffset && angleOffset <  angleNew && angleOld < angleNew) || // example: old=10   offset=15   new=20
                        (angleOld <= angleOffset && angleOffset >  angleNew && angleOld > angleNew) || // example: old=350  offset=355  new=0
                        (angleOld >  angleOffset && angleOffset <= angleNew && angleOld > angleNew))   // example: old=355  offset=0    new=5
                    :  ((angleOld >= angleOffset && angleOffset >  angleNew && angleOld > angleNew) || // example: old=20   offset=15   new=10
                        (angleOld <  angleOffset && angleOffset >  angleNew && angleOld < angleNew) || // example: old=0    offset=355  new=350
                        (angleOld >= angleOffset && angleOffset <= angleNew && angleOld < angleNew)))  // example: old=5    offset=0    new=355
                {
                    prepareList.remove(i);
                    rotatedElements.add(new RotatedCellContext(nextRandomIndex(), angleOffset, area));
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
            double angle3 = angle2 + rotateAngleDelta;
            if ((angle3 >= 360) || (angle3 < 0)) {
                toRemove.add(cntxt);
            }
        });
        toRemove.forEach(cntxt -> {
                            matrix.get(cntxt.index).init(); // restore original region coords
                            rotatedElements.remove(cntxt);
                            if (rotatedElements.isEmpty())
                                rotateCellAlterantive = !rotateCellAlterantive;
                            addRandomToPrepareList(false);
                        });


        // Step 2. rotate cells
        rotatedElements.forEach(cntxt -> {
            assert (cntxt.angleOffset >= 0);
            double angle2 = angleNew - cntxt.angleOffset;
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
            shape.setArea(cntxt.area);

            // rotate
            cell.init();
            PointDouble centerNew = cell.getCenter();
            PointDouble delta = new PointDouble(center.x - centerNew.x, center.y - centerNew.y);
            FigureHelper.moveCollection(FigureHelper.rotateCollection(cell.getRegion().getPoints(), (((coord.x + coord.y) & 1) == 0) ? +angle2 : -angle2, rotateCellAlterantive ? center : centerNew), delta);

            // restore
            shape.setArea(area);
        });

        // Z-ordering
        Collections.sort(rotatedElements, (e1, e2) -> Double.compare(e1.area, e2.area));

        firePropertyChanged(PROPERTY_ROTATE_SOME_CELLS);
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

    private void addRandomToPrepareList(boolean zero) {
        double offset = (zero ? 0 : ThreadLocalRandom.current().nextInt(360)) + rotateAngle;
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

}
