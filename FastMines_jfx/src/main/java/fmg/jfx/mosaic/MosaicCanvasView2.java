package fmg.jfx.mosaic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import fmg.core.img.MosaicDrawContext;
import fmg.core.mosaic.IMosaicView2;
import fmg.core.mosaic.MosaicModel2;
import fmg.core.mosaic.cells.BaseCell;
import fmg.jfx.img.Flag2;
import fmg.jfx.img.JfxCanvasView;
import fmg.jfx.img.Logo2;
import fmg.jfx.img.MosaicImg2;

/** MVC: view. JavaFX implementation over node-control {@link Canvas} */
public class MosaicCanvasView2 implements IMosaicView2<Canvas> {

    private final JfxCanvasView<MosaicModel2> proxyView;
    private final MosaicModel2 model;
    private final Flag2.FlagJfxImageController imgFlag;
    private final Logo2.LogoJfxImageController imgMine;
    private final Collection<BaseCell> modifiedCells = new HashSet<>();
    private boolean drawBk = true;

    public MosaicCanvasView2(
        MosaicModel2 model,
        Flag2.FlagJfxImageController imgFlag,
        Logo2.LogoJfxImageController imgMine)
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
            MosaicImg2.draw(g, drawContext);
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
