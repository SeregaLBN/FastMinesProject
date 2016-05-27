using Windows.UI.Xaml.Shapes;
using Windows.UI.Xaml.Controls;
using fmg.core.mosaic.draw;

namespace fmg.uwp.draw.mosaic.xaml {

   /// <summary> Container for <see cref="Polygon"/> and <see cref="TextBlock"/> and <see cref="Image"/> </summary>
   public class PaintableShapes : IPaintable {

      public PaintableShapes(Polygon poly, TextBlock txt, Image img) { Poly = poly; Txt = txt; Img = img; }

      public Polygon Poly{ get; private set; }
      public TextBlock Txt { get; private set; }
      public Image Img { get; private set; }

   }

}
