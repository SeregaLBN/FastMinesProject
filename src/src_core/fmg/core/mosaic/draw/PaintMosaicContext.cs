using fmg.common;

namespace fmg.core.mosaic.draw {

   public class PaintMosaicContext<TImage> : PaintCellContext<TImage>
      where TImage : class
   {
      private Color _colorBk;
      private TImage _imgBckgrnd;

      public PaintMosaicContext(bool iconicMode) :
         base(iconicMode)
      {
         _colorBk = DefaultBackgroundWindowColor.Darker(0.4);
      }

      public Color ColorBk {
         get { return _colorBk; }
         set { SetProperty(ref _colorBk, value); }
      }

      public TImage ImgBckgrnd {
         get { return _imgBckgrnd; }
         set { SetProperty(ref _imgBckgrnd, value); }
      }

   }

}
