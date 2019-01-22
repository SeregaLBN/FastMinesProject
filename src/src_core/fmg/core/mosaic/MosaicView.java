package fmg.core.mosaic;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import fmg.common.LoggerSimple;
import fmg.common.geom.DoubleExt;
import fmg.common.geom.RectDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.img.ImageView;
import fmg.core.mosaic.cells.BaseCell;

/**
 * MVC: view. Base mosaic view implementation
 * @param <TImage> platform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageInner> image type of flag/mine into mosaic field
 * @param <TMosaicModel> mosaic data model
 */
public abstract class MosaicView<TImage,
                                 TImageInner,
                                 TMosaicModel extends MosaicDrawModel<TImageInner>>
               extends ImageView<TImage, TMosaicModel>
          implements IMosaicView<TImage, TImageInner, TMosaicModel>
{

    protected MosaicView(TMosaicModel mosaicModel) {
        super(mosaicModel);
    }

    public static boolean _DEBUG_DRAW_FLOW = false;
    private final Collection<BaseCell> _modifiedCells = new HashSet<>();

    protected Collection<BaseCell> toDrawCells(RectDouble invalidatedRect) {
        if (_DEBUG_DRAW_FLOW)
            LoggerSimple.put("<>MosaicView.toDrawCells: invalidatedRect=" + (invalidatedRect==null ? "null" : invalidatedRect.toString()));

        if (invalidatedRect == null)
            return null; // equals Model.Matrix

        // check to redraw all mosaic cells
        TMosaicModel model = getModel();
        if (DoubleExt.hasMinDiff(invalidatedRect.x, 0) && DoubleExt.hasMinDiff(invalidatedRect.y, 0)) {
            SizeDouble size = model.getSize();
            if (DoubleExt.hasMinDiff(invalidatedRect.width, size.width) && DoubleExt.hasMinDiff(invalidatedRect.height, size.height))
                return null; // equals Model.Matrix
        }

        SizeDouble offset = model.getMosaicOffset();

        // redraw only when needed...
        Collection<BaseCell> toDrawCells = model.getMatrix().stream()
            .filter(cell -> cell.getRcOuter()
                               .moveXY(offset.width, offset.height)
                               .intersection(invalidatedRect)) // ...when the cells and update region intersect
            .collect(Collectors.toList());

        if (_DEBUG_DRAW_FLOW)
            LoggerSimple.put("< MosaicView.toDrawCells: cnt=" + toDrawCells.size());
        return toDrawCells;
    }

    @Override
    public void invalidate(Collection<BaseCell> modifiedCells) {
        if (modifiedCells == null) // mark NULL if all mosaic is changed
            _modifiedCells.clear();
        else
            _modifiedCells.addAll(modifiedCells);
        if (_DEBUG_DRAW_FLOW)
            System.out.println("MosaicView.invalidate: " + ((modifiedCells==null) ? "all" : ("cnt=" + modifiedCells.size()) + ": " + modifiedCells.stream().limit(5).collect(Collectors.toList())));
        invalidate();
    }

    /** Draw modified mosaic cells
     * @param modifiedCells Cells to be redrawn. NULL - redraw the full mosaic.
     */
    protected abstract void drawModified(Collection<BaseCell> modifiedCells);

    /** repaint all */
    @Override
    protected void drawBody() {
        if (_DEBUG_DRAW_FLOW)
            System.out.println("MosaicView.drawBody: " + (_modifiedCells.isEmpty() ? "all" : ("cnt=" + _modifiedCells.size()) + ": " + _modifiedCells.stream().limit(5).collect(Collectors.toList())));
        drawModified(_modifiedCells.isEmpty() ? null : _modifiedCells);
        _modifiedCells.clear();
    }

}
