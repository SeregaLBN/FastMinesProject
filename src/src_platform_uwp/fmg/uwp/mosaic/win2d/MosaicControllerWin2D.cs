namespace fmg.uwp.mosaic.win2d {

   /// <summary> MVC: controller. Win2D implementation </summary>
   public class MosaicControllerWin2D : fmg.core.mosaic.MosaicController<fmg.uwp.mosaic.win2d.MosaicViewWin2D,
                                                                         fmg.uwp.draw.mosaic.win2d.PaintableWin2D,
                                                                         Microsoft.Graphics.Canvas.CanvasBitmap,
                                                                         fmg.uwp.draw.mosaic.PaintUwpContext<Microsoft.Graphics.Canvas.CanvasBitmap>>
   { }

}
