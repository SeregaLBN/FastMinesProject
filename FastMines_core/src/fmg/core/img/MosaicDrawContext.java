package fmg.core.img;

import java.util.Collection;
import java.util.function.Supplier;

import fmg.common.Color;
import fmg.core.mosaic.MosaicModel2;
import fmg.core.mosaic.cells.BaseCell;

public class MosaicDrawContext<T> {

    public final MosaicModel2 model;
    public final boolean drawBackground;
    public final Supplier<Color> getBackgroundColor;
    public final Supplier<Collection<BaseCell>> drawCells;
    public final Supplier<T> mineImage;
    public final Supplier<T> flagImage;

    public MosaicDrawContext(
        MosaicModel2 model,
        boolean drawBackground,
        Supplier<Color> getBackgroundColor,
        Supplier<Collection<BaseCell>> drawCells,
        Supplier<T> mineImage,
        Supplier<T> flagImage)
    {
        this.model = model;
        this.drawBackground = drawBackground;
        this.getBackgroundColor = getBackgroundColor;
        this.drawCells = drawCells;
        this.mineImage = mineImage;
        this.flagImage = flagImage;
    }

}
