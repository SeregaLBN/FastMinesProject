using System;
using System.Collections.Generic;
using System.Threading.Tasks;
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
      private const bool _randomCellBkColor = true;
      private const bool _fillBkColor = true;

      private readonly BaseCell.BaseAttribute _attr;
      private readonly CellPaint _gInfo;
      private readonly List<BaseCell> _arrCell;
      private static readonly GraphicContext GContext;
      private readonly Size _sizeField;
      private readonly int _area;

      static MosaicsImg() {
         GContext = new GraphicContext(true, new Size(7, 7));
         GContext.PenBorder.Width = 2;
         GContext.PenBorder.ColorLight = GContext.PenBorder.ColorShadow;
      }

      public MosaicsImg(EMosaic mosaicType, bool smallIco) : this(mosaicType, smallIco, 3000) {}

      public MosaicsImg(EMosaic mosaicType, bool smallIco, int area) {
         this._area = area;
         _attr = CellFactory.CreateAttributeInstance(mosaicType, area);
         _arrCell = new List<BaseCell>();
         _gInfo = new CellPaint(GContext);
         _sizeField = _attr.sizeIcoField(smallIco);
#if DEBUG
         // visual test drawig...
         //_sizeField.height += 5;
         //_sizeField.width += 5;
#endif
         for (int i = 0; i < _sizeField.width; i++)
            for (int j = 0; j < _sizeField.height; j++)
               _arrCell.Add(CellFactory.CreateCellInstance(_attr, mosaicType, new Coord(i, j)));
         if (_randomCellBkColor)
            GContext.BkFill.Mode = 1 + new Random().Next(_attr.getMaxBackgroundFillModeValue());
      }

      private WriteableBitmap _image;

      private static Windows.Foundation.IAsyncAction ExecuteOnUIThread(Windows.UI.Core.DispatchedHandler action) {
         return Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(Windows.UI.Core.CoreDispatcherPriority.Low, action);
      }

      /// <summary> Create and draw mosaic bitmap </summary>
      public async Task<WriteableBitmap> CreateImage() {
         {
            if (_image == null) {
               WriteableBitmap bmp = null;
               //_image = await Task<WriteableBitmap>.Factory.StartNew(() => { // :(   Exception from HRESULT 0x8001010E RPC_E_WRONG_THREAD
               await ExecuteOnUIThread(() => {
                  var pixelSize = _attr.CalcOwnerSize(_sizeField, _area);
                  var w = pixelSize.width + GContext.Bound.width * 2;
                  var h = pixelSize.height + GContext.Bound.height * 2;
                  bmp = BitmapFactory.New(w, h);
                  if (_fillBkColor) {
                     var points = new[] {
                        0, 0,
                        w, 0,
                        w, h,
                        0, h,
                        0, 0
                     };
                     bmp.FillPolygon(points, Windows.UI.Color.FromArgb(0xFF, 0xff, 0x8c, 0x00));
                  }

                  foreach (var cell in _arrCell)
                     _gInfo.Paint(cell, bmp);
               });

               this._image = bmp;
            }
            return this._image;
         }
      }
   }
}