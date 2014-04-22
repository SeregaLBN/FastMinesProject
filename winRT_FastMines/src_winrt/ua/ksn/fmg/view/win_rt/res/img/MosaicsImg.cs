using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Windows.System.Threading;
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
      private readonly GraphicContext _gContext;
      private readonly Size _sizeField;
      private static readonly Random _random = new Random();

      public MosaicsImg(EMosaic mosaicType, bool smallIco) : this(mosaicType, smallIco, 3000) {}

      public MosaicsImg(EMosaic mosaicType, bool smallIco, int area) {
         _attr = CellFactory.CreateAttributeInstance(mosaicType, area);
         _arrCell = new List<BaseCell>();

         _gContext = new GraphicContext(true, new Size(7, 7));
         _gContext.PenBorder.Width = 2;
         _gContext.PenBorder.ColorLight = _gContext.PenBorder.ColorShadow;
         if (_randomCellBkColor)
            _gContext.BkFill.Mode = 1 + _random.Next(_attr.getMaxBackgroundFillModeValue());

         _gInfo = new CellPaint(_gContext);

         _sizeField = _attr.sizeIcoField(smallIco);
#if DEBUG
         // visual test drawig...
         _sizeField.height += _random.Next() & 3;
         _sizeField.width += _random.Next() & 3;
#endif
         for (int i = 0; i < _sizeField.width; i++)
            for (int j = 0; j < _sizeField.height; j++)
               _arrCell.Add(CellFactory.CreateCellInstance(_attr, mosaicType, new Coord(i, j)));
      }

      private WriteableBitmap _image;

      private static Windows.Foundation.IAsyncAction ExecuteOnUIThread(Windows.UI.Core.DispatchedHandler action) {
         return Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(Windows.UI.Core.CoreDispatcherPriority.Low, action);
      }

      /// <summary> return painted mosaic bitmap </summary>
      public async Task<WriteableBitmap> GetImage() {
         {
            if (_image == null) {
               var pixelSize = _attr.CalcOwnerSize(_sizeField, _attr.Area);
               var w = pixelSize.width + _gContext.Bound.width*2;
               var h = pixelSize.height + _gContext.Bound.height*2;

               WriteableBitmap bmp = null;
               var funcFillBk = new Action(() => {
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
               });

#if false
               bmp = BitmapFactory.New(w, h);
               ThreadPool.RunAsync(async delegate {
                  await ExecuteOnUIThread(funcFillBk.Invoke);
                  foreach (var cell in _arrCell)
                     ExecuteOnUIThread(() => _gInfo.Paint(cell, bmp));
               });
#elif false
               await ThreadPool.RunAsync(async delegate {
                  await ExecuteOnUIThread(() => { bmp = BitmapFactory.New(w, h); });
                  ExecuteOnUIThread(funcFillBk.Invoke);
                  foreach (var cell in _arrCell)
                     ExecuteOnUIThread(() => _gInfo.Paint(cell, bmp));
               });
#elif false
               await ExecuteOnUIThread(() => {
                  bmp = BitmapFactory.New(w, h);
                  funcFillBk.Invoke();

                  foreach (var cell in _arrCell)
                     _gInfo.Paint(cell, bmp);
               });
#elif true
               await ExecuteOnUIThread(() => {
                  bmp = BitmapFactory.New(w, h);
                  funcFillBk.Invoke();

                  foreach (var cell in _arrCell)
                     new Task(async () => {
                        //await Task.Delay(TimeSpan.FromMilliseconds(0));
                        await ExecuteOnUIThread(() => _gInfo.Paint(cell, bmp));
                     }).Start();
               });
#endif
               this._image = bmp;
            }
            return this._image;
         }
      }
   }
}