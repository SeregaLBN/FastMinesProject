using System;
using System.Collections.Generic;
using Windows.UI.Core;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.mosaic;
using fmg.core.mosaic.cells;
using fmg.winrt.draw;
using fmg.winrt.draw.mosaic;
using FastMines.Common;

namespace fmg.winrt.res.img {

   /// <summary>
   /// картинка поля конкретной мозаики. Используется для меню, кнопок, etc... 
   /// </summary>
   public class MosaicsImg {
      private const bool _randomCellBkColor = true;

      private readonly BaseCell.BaseAttribute _attr;
      private readonly ICellPaint<PaintableWBmp> _gInfo;
      private readonly List<BaseCell> _arrCell;
      private readonly GraphicContext _gContext;
      private readonly Size _sizeField;
      private readonly Windows.UI.Color _bkColor;
      private static readonly Random _random = new Random();
      private WriteableBitmap _image;

      public MosaicsImg(EMosaic mosaicType, bool smallIco, int area, Windows.UI.Color bkColor, Size bound) : this(mosaicType, mosaicType.SizeIcoField(smallIco), area, bkColor, bound) { }

      public MosaicsImg(EMosaic mosaicType, Size sizeField, int area, Windows.UI.Color bkColor, Size bound) {
         _attr = CellFactory.CreateAttributeInstance(mosaicType, area);
         _arrCell = new List<BaseCell>();
         _bkColor = bkColor;

         _gContext = new GraphicContext(true, bound);
         _gContext.PenBorder.Width = 2;
         _gContext.PenBorder.ColorLight = _gContext.PenBorder.ColorShadow;
         if (_randomCellBkColor)
            _gContext.BkFill.Mode = 1 + _random.Next(_attr.getMaxBackgroundFillModeValue());

         _gInfo = new CellPaint_WriteableBitmap(_gContext);

         _sizeField = sizeField;
         for (var i = 0; i < _sizeField.width; i++)
            for (var j = 0; j < _sizeField.height; j++)
               _arrCell.Add(CellFactory.CreateCellInstance(_attr, mosaicType, new Coord(i, j)));
      }

      /// <summary> Return painted mosaic bitmap </summary>
      /// <param name="drawAsync">== true Сама картинка возвращается сразу.
      /// Но вот её отрисовка - в фоне.
      /// Ф-ция без ключевого слова async, т.к. результат есть DependencyObject, т.е. владелец может сам отслеживать отрисовку...</param>
      /// <returns></returns>
      public WriteableBitmap GetImage(bool drawAsync) {
         if (_image != null)
            return _image;

         var pixelSize = _attr.CalcOwnerSize(_sizeField, _attr.Area);
         var w = pixelSize.width + _gContext.Bound.width*2;
         var h = pixelSize.height + _gContext.Bound.height*2;

         _image = BitmapFactory.New(w, h); // new WriteableBitmap(w, h); // 
         Action funcFillBk = () => _image.FillPolygon(new[] { 0, 0, w, 0, w, h, 0, h, 0, 0 }, _bkColor);

         if (!drawAsync) {
            // sync draw
            funcFillBk();
            foreach (var cell in _arrCell)
               _gInfo.Paint(cell, new PaintableWBmp(_image));
         } else {
#pragma warning disable CS4014 // Because this call is not awaited, execution of the current method continues before the call is completed
            // async draw
            AsyncRunner.InvokeLater(() => {
               funcFillBk();
               foreach (var cell in _arrCell) {
                  var tmp = cell;
                  AsyncRunner.InvokeLater(() => _gInfo.Paint(tmp, new PaintableWBmp(_image)),
                     ((_random.Next() & 1) == 0)
                        ? CoreDispatcherPriority.Low
                        : CoreDispatcherPriority.Normal);
               }
            }, CoreDispatcherPriority.Normal);
#pragma warning restore CS4014 // Because this call is not awaited, execution of the current method continues before the call is completed
         }
         return _image;
      }
   }
}