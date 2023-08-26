package fmg.jfx.mosaic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import fmg.core.img.MosaicDrawContext;
import fmg.core.mosaic.IMosaicView;
import fmg.core.mosaic.MosaicModel;
import fmg.core.mosaic.cells.BaseCell;
import fmg.jfx.img.Flag;
import fmg.jfx.img.JfxCanvasView;
import fmg.jfx.img.Logo;
import fmg.jfx.img.MosaicImg;

/** MVC: view. JavaFX implementation over node-control {@link Canvas} */
public class MosaicCanvasView implements IMosaicView<Canvas> {

    private final JfxCanvasView<MosaicModel> proxyView;
    private final MosaicModel model;
    private final Flag.FlagJfxImageController imgFlag;
    private final Logo.LogoJfxImageController imgMine;
    private final Collection<BaseCell> modifiedCells = new HashSet<>();
    private boolean drawBk = true;

    public MosaicCanvasView(
        MosaicModel model,
        Flag.FlagJfxImageController imgFlag,
        Logo.LogoJfxImageController imgMine)
    {
        this.model = model;
        this.imgFlag = imgFlag;
        this.imgMine = imgMine;

        proxyView = new JfxCanvasView<>(model, this::draw);
    }

    @Override
    public Canvas getImage() {
        return proxyView.getImage();
    }

    private void draw(GraphicsContext g) {
        try {
            var drawContext = new MosaicDrawContext<>(
                    model,
                    drawBk,
                    model::getBackgroundColor,
                    modifiedCells.isEmpty()
                        ? model::getMatrix
                        : () -> modifiedCells,
                    imgMine::getImage,
                    imgFlag::getImage);
            MosaicImg.draw(g, drawContext);
        } finally {
            drawBk = true;
            modifiedCells.clear();
        }
    }

    @Override
    public void invalidate() {
        drawBk = true;
        modifiedCells.clear();
        proxyView.invalidate();
    }

    @Override
    public void invalidate(Collection<BaseCell> modifiedCells) {
        Objects.requireNonNull(modifiedCells);
        if (modifiedCells.isEmpty())
            throw new IllegalArgumentException("Required not empty");

        drawBk = false;
        this.modifiedCells.addAll(modifiedCells);

        proxyView.invalidate();
    }

    @Override
    public boolean isValid() {
        return proxyView.isValid();
    }

    @Override
    public void reset() {
        drawBk = true;
        modifiedCells.clear();
        proxyView.reset();
    }

}
