namespace fmg.uwp.mosaic.win2d {

   /// <summary> MVC: controller. Win2D implementation </summary>
   public class MosaicController : fmg.core.mosaic.MosaicController<fmg.uwp.mosaic.win2d.MosaicView,
                                                                    fmg.uwp.draw.mosaic.win2d.PaintableWin2D,
                                                                    Microsoft.Graphics.Canvas.CanvasBitmap,
                                                                    fmg.uwp.draw.mosaic.PaintUwpContext<Microsoft.Graphics.Canvas.CanvasBitmap>>
   { }

}
