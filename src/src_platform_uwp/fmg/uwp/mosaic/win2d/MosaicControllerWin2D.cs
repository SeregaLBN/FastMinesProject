namespace fmg.uwp.mosaic.win2d {
#if false

    /// <summary> MVC: controller. Win2D implementation </summary>
    public class MosaicControllerWin2D<TMosaicViewWin2D> : fmg.core.mosaic.MosaicController<TMosaicViewWin2D,
                                                                                            fmg.uwp.draw.mosaic.win2d.PaintableWin2D,
                                                                                            Microsoft.Graphics.Canvas.CanvasBitmap,
                                                                                            fmg.uwp.draw.mosaic.win2d.PaintWin2DContext>
        where TMosaicViewWin2D : AMosaicViewWin2D, new()
    { }

#endif
}
