using System;
using System.Collections.Generic;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Media.Imaging;
using ua.ksn.geom;
using ua.ksn.fmg.model.mosaics;
using ua.ksn.fmg.model.mosaics.cell;
using ua.ksn.fmg.view.win_rt.draw;
using ua.ksn.fmg.view.win_rt.draw.mosaics;

namespace ua.ksn.fmg.view.win_rt.res.img {

   /// <summary>
   /// картинка поля конкретной мозаики. Используется для меню, кнопок, etc... 
   /// </summary>
   public class MosaicsImg {
      private readonly BaseCell.BaseAttribute attr;
      private readonly CellPaint gInfo;
      private readonly List<BaseCell> arrCell;
      private readonly static GraphicContext gContext;
	   private readonly Size sizeField;
      private readonly int area;

      static MosaicsImg() {
         gContext = new GraphicContext(true, new Size(7,7));
         gContext.PenBorder.Width = 2;
         gContext.PenBorder.ColorLight = gContext.PenBorder.ColorShadow;
      }

      public MosaicsImg(EMosaic mosaicType, bool smallIco) : this(mosaicType, smallIco, 3000) { }
      public MosaicsImg(EMosaic mosaicType, bool smallIco, int area) {
         this.area = area;
         attr = CellFactory.CreateAttributeInstance(mosaicType, area);
         arrCell = new List<BaseCell>();
         gInfo = new CellPaint(gContext);
         sizeField = attr.sizeIcoField(smallIco);
         for (int i=0; i < sizeField.width; i++)
            for (int j=0; j < sizeField.height; j++)
               arrCell.Add(CellFactory.CreateCellInstance(attr, mosaicType, new Coord(i, j)));
#if DEBUG
         gContext.BkFill.Mode = 1 + new Random().Next(attr.getMaxBackgroundFillModeValue());
#endif
      }

      private WriteableBitmap _image;
      public ImageSource Image {
         get {
            if (_image == null) {
               var pixelSize = attr.CalcOwnerSize(sizeField, area);
               _image = BitmapFactory.New(pixelSize.width + gContext.Bound.width * 2, pixelSize.height + gContext.Bound.height * 2);
#if DEBUG
               var points = new[] { 0, 0,
                  pixelSize.width + gContext.Bound.width*2, 0,
                  pixelSize.width + gContext.Bound.width*2, pixelSize.height + gContext.Bound.height*2,
                  0, pixelSize.height + gContext.Bound.height*2,
                  0, 0 };
               _image.FillPolygon(points, Windows.UI.Color.FromArgb(0xFF, 0xff, 0x8c, 0x00)); // debug
#endif
               
               foreach (var cell in arrCell)
                  gInfo.Paint(cell, _image);
            }
            return this._image;
         }
      }
   }
}