package fmg.jfx.mosaic;

import java.util.Collection;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

import fmg.common.geom.RectDouble;
import fmg.core.mosaic.MosaicDrawModel;
import fmg.core.mosaic.MosaicGameModel;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.cells.Square1;
import fmg.jfx.img.CanvasJfx;
import fmg.jfx.img.Flag;
import fmg.jfx.img.Mine;
import fmg.jfx.utils.ImgUtils;

/** MVC: view. JavaFX implementation over node-control {@link Canvas} */
public class MosaicCanvasView extends MosaicJfxView<Canvas, Image, MosaicDrawModel<Image>> {

    private CanvasJfx _canvas = new CanvasJfx(this);
    private Flag.ControllerImage _imgFlag = new Flag.ControllerImage();
    private Mine.ControllerImage _imgMine = new Mine.ControllerImage();

    public MosaicCanvasView() {
        super(new MosaicDrawModel<Image>());
        changeSizeImagesMineFlag();
    }

    @Override
    protected Canvas createImage() { return _canvas.create(); }

    @Override
    protected void drawModified(Collection<BaseCell> modifiedCells) {
        if (modifiedCells == null) {
            drawJfx(_canvas.getGraphics(), getModel().getMatrix(), true);
            return;
        }
        if (modifiedCells.iterator().next() instanceof Square1) { // optimize
            drawJfx(_canvas.getGraphics(), modifiedCells, false);
            return;
        }
        double minX=0, minY=0, maxX=0, maxY=0;
        boolean first = true;
        for (BaseCell cell : modifiedCells) {
            RectDouble rc = cell.getRcOuter();
            if (first) {
                first = false;
                minX = rc.x;
                minY = rc.y;
                maxX = rc.right();
                maxY = rc.bottom();
            } else {
                minX = Math.min(minX, rc.x);
                minY = Math.min(minY, rc.y);
                maxX = Math.max(maxX, rc.right());
                maxY = Math.max(maxY, rc.bottom());
            }
        }
        RectDouble rcClip = new RectDouble(minX, minY, maxX-minX, maxY-minY);
//        if (_DEBUG_DRAW_FLOW)
//            System.out.println("MosaicViewJfx.draw: repaint=" + rcClip);
        drawJfx(_canvas.getGraphics(), toDrawCells(rcClip), false);
    }

    @Override
    protected void onPropertyChanged(Object oldValue, Object newValue, String propertyName) {
        super.onPropertyChanged(oldValue, newValue, propertyName);
        if (propertyName.equals(PROPERTY_IMAGE))
            getImage(); // implicit call draw() -> drawBegin() -> drawModified() -> drawJfx()
    }

    @Override
    protected void onPropertyModelChanged(Object oldValue, Object newValue, String propertyName) {
        super.onPropertyModelChanged(oldValue, newValue, propertyName);
        switch (propertyName) {
        case MosaicGameModel.PROPERTY_MOSAIC_TYPE:
        case MosaicGameModel.PROPERTY_AREA:
            changeSizeImagesMineFlag();
            break;
        }
    }

    /** переустанавливаю заного размер мины/флага для мозаики */
    protected void changeSizeImagesMineFlag() {
        MosaicDrawModel<Image> model = getModel();
        double sq = model.getCellAttr().getSq(model.getPenBorder().getWidth());
        if (sq <= 0) {
            System.err.println("Error: too thick pen! There is no area for displaying the flag/mine image...");
            sq = 3; // ат балды...
        }

        final int max = 30;
        if (sq > max) {
            _imgFlag.getModel().setSize(sq);
            _imgMine.getModel().setSize(sq);
            model.setImgFlag(_imgFlag.getImage());
            model.setImgMine(_imgMine.getImage());
        } else {
            _imgFlag.getModel().setSize(max);
            _imgMine.getModel().setSize(max);
            model.setImgFlag(ImgUtils.zoom(_imgFlag.getImage(), sq, sq));
            model.setImgMine(ImgUtils.zoom(_imgMine.getImage(), sq, sq));
        }
    }

    @Override
    public void close() {
        getModel().close();
        super.close();
        _canvas = null;
        _imgFlag.close();
        _imgMine.close();
    }

}
