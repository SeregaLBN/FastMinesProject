using System;
using System.Linq;
using System.Collections.Generic;
using Windows.UI.Core;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.mosaic;
using fmg.core.mosaic.draw;
using fmg.core.mosaic.cells;
using fmg.winrt.draw;
using fmg.winrt.draw.mosaic.bmp;
using FastMines.Common;

namespace fmg.winrt.res.img {

   /// <summary>
   /// картинка поля конкретной мозаики. Используется для меню, кнопок, etc... 
   /// </summary>
   public class MosaicsImg : IMosaic<PaintableBmp> {
      private const bool _randomCellBkColor = true;

      private EMosaic _mosaicType;
      private Size _sizeField;
      private int _area = 230;
      private BaseCell.BaseAttribute _attr;
      private List<BaseCell> _matrix = new List<BaseCell>();
      private ICellPaint<PaintableBmp> _cellPaint;
      private GraphicContext _gContext;
      private Windows.UI.Color _bkColor;
      private WriteableBitmap _image;
      private static readonly Random _random = new Random();

      public Size SizeField
      {
         get {
            if (_sizeField.width < 1)
            {
               // reset
               _image = null;
               _matrix.Clear();

               _sizeField.width = 4;
            }
            if (_sizeField.height < 1)
            {
               // reset
               _image = null;
               _matrix.Clear();

               _sizeField.width = 3;
            }
            return _sizeField;
         }
         set
         {
            // reset
            _matrix.Clear();
            _image = null;

            _sizeField = value;
         }
      }

      public void SetSmallIco(EMosaic mosaicType, bool smallIco)
      {
         SizeField = mosaicType.SizeIcoField(smallIco);
      }

      public BaseCell getCell(Coord coord) { return Matrix[coord.x * SizeField.height + coord.y]; }

      public BaseCell.BaseAttribute CellAttr
      {
         get
         {
            if (_attr == null)
               _attr = CellFactory.CreateAttributeInstance(MosaicType, Area);
            return _attr;
         }
      }

      public ICellPaint<PaintableBmp> CellPaint {
         get
         {
            if (_cellPaint == null)
            {
               _cellPaint = new CellPaintBmp(GContext);
            }
            return _cellPaint;
         }
      }

      /// <summary>матрица ячеек, представленная(развёрнута) в виде вектора</summary>
      public IList<BaseCell> Matrix
      {
         get
         {
            if (!_matrix.Any())
            {
               var attr = CellAttr;
               var type = MosaicType;
               var size = SizeField;
               for (var i = 0; i < size.width; i++)
                  for (var j = 0; j < size.height; j++)
                     _matrix.Add(CellFactory.CreateCellInstance(attr, type, new Coord(i, j)));
            }
            return _matrix;
         }
      }

      /// <summary>из каких фигур состоит мозаика поля</summary>
      public EMosaic MosaicType {
         get { return _mosaicType; }
         set
         {
            // reset
            _matrix.Clear();
            _attr = null;
            _image = null;

            _mosaicType = value;
         }
      }

      public int Area
      {
         get { return _area; }
         set
         {
            // reset
            _image = null;

            _area = value;
         }
      }

      public Windows.UI.Color BackgroundColor
      {
         get { return _bkColor; }
         set {
            // reset
            _image = null;

            _bkColor = value;
         }
      }

      public GraphicContext GContext
      {
         get {
            if (_gContext == null)
            {
               _gContext = new GraphicContext(true);
               _gContext.PenBorder.Width = 2;
               _gContext.PenBorder.ColorLight = _gContext.PenBorder.ColorShadow;
               if (_randomCellBkColor)
                  _gContext.BkFill.Mode = 1 + _random.Next(CellAttr.getMaxBackgroundFillModeValue());

            }
            return _gContext;
         }
      }

      public Size Bound
      {
         get { return GContext.Bound; }
         set
         {
            // reset
            _image = null;

            GContext.Bound = value;
         }
      }

      public WriteableBitmap Image
      {
         get { return GetImage(false); }
      }

      /// <summary> Return painted mosaic bitmap </summary>
      /// <param name="drawAsync">== true Сама картинка возвращается сразу.
      /// Но вот её отрисовка - в фоне.
      /// Ф-ция без ключевого слова async, т.к. результат есть DependencyObject, т.е. владелец может сам отслеживать отрисовку...</param>
      /// <returns></returns>
      public WriteableBitmap GetImage(bool drawAsync) {
         if (_image != null)
            return _image;

         var pixelSize = CellAttr.CalcOwnerSize(SizeField, CellAttr.Area);
         var w = pixelSize.width + GContext.Bound.width*2;
         var h = pixelSize.height + GContext.Bound.height*2;

         _image = BitmapFactory.New(w, h); // new WriteableBitmap(w, h); // 
         Action funcFillBk = () => _image.FillPolygon(new[] { 0, 0, w, 0, w, h, 0, h, 0, 0 }, BackgroundColor);

         if (!drawAsync) {
            // sync draw
            funcFillBk();
            foreach (var cell in Matrix)
               CellPaint.Paint(cell, new PaintableBmp(_image));
         } else {
#pragma warning disable CS4014 // Because this call is not awaited, execution of the current method continues before the call is completed
            // async draw
            AsyncRunner.InvokeLater(() => {
               funcFillBk();
               foreach (var cell in Matrix) {
                  var tmp = cell;
                  AsyncRunner.InvokeLater(() => CellPaint.Paint(tmp, new PaintableBmp(_image)),
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