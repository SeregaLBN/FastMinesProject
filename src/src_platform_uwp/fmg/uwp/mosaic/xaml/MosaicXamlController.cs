namespace fmg.uwp.mosaic.xaml {

   /// <summary> MVC: controller. Xaml shapes implementation </summary>
   public class MosaicControllerXaml : fmg.core.mosaic.MosaicController<fmg.uwp.mosaic.xaml.MosaicViewXaml,
                                                                        fmg.uwp.draw.mosaic.xaml.PaintableShapes,
                                                                        Windows.UI.Xaml.Media.ImageSource,
                                                                        fmg.uwp.draw.mosaic.PaintUwpContext<Windows.UI.Xaml.Media.ImageSource>>
   { }

}
