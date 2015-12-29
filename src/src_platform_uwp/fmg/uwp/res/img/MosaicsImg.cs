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
using fmg.uwp.draw;
using fmg.uwp.draw.mosaic.bmp;
using FastMines.Common;
using FastMines.Presentation.Notyfier;

namespace fmg.uwp.res.img {

   /// <summary>
   /// картинка поля конкретной мозаики. Используется для меню, кнопок, etc... 
   /// </summary>
   public class MosaicsImg : NotifyPropertyChanged, IMosaic<PaintableBmp>, IDisposable {
      private const bool _randomCellBkColor = true;

      private EMosaic _mosaicType;
      private Matrisize _sizeField;
      private int _area = MosaicBase<IPaintable>.AREA_MINIMUM;
      private BaseCell.BaseAttribute _attr;
      private readonly List<BaseCell> _matrix = new List<BaseCell>();
      private CellPaintBmp _cellPaint;
      private Windows.UI.Color _bkColor;
      private WriteableBitmap _image;
      private readonly Random _random = new Random(Guid.NewGuid().GetHashCode());

      public Matrisize SizeField
      {
         get {
            if ((_sizeField.m < 1) || (_sizeField.n < 1)) {
               SizeField = new Matrisize(
                  (_sizeField.m < 1) ? 4 : _sizeField.m,
                  (_sizeField.n < 1) ? 4 : _sizeField.n);
            }
            return _sizeField;
         }
         set {
            if (SetProperty(ref _sizeField, value)) {
               // reset
               _matrix.Clear();
               Image = null;
            }
         }
      }

      public void SetSmallIco(EMosaic mosaicType, bool smallIco) {
         SizeField = mosaicType.SizeIcoField(smallIco);
      }

      public BaseCell getCell(Coord coord) { return Matrix[coord.x * SizeField.n + coord.y]; }

      public BaseCell.BaseAttribute CellAttr => _attr ?? (_attr = MosaicHelper.CreateAttributeInstance(MosaicType, Area));

      public ICellPaint<PaintableBmp> CellPaint => CellPaintBitmap;
      protected CellPaintBmp CellPaintBitmap => _cellPaint ?? (_cellPaint = new CellPaintBmp());

      /// <summary>матрица ячеек, представленная(развёрнута) в виде вектора</summary>
      public IList<BaseCell> Matrix {
         get {
            if (!_matrix.Any()) {
               var attr = CellAttr;
               var type = MosaicType;
               var size = SizeField;
               for (var i = 0; i < size.m; i++)
                  for (var j = 0; j < size.n; j++)
                     _matrix.Add(MosaicHelper.CreateCellInstance(attr, type, new Coord(i, j)));
            }
            return _matrix;
         }
      }

      /// <summary>из каких фигур состоит мозаика поля</summary>
      public EMosaic MosaicType {
         get { return _mosaicType; }
         set {
            if (SetProperty(ref _mosaicType, value)) {
               // reset
               _matrix.Clear();
               _attr = null;
               Image = null;
            }
         }
      }

      public int Area {
         get { return _area; }
         set {
            if (SetProperty(ref _area, value)) {
               // reset
               Image = null;
            }
         }
      }

      public Windows.UI.Color BackgroundColor {
         get {
            //if (_bkColor == null) {
            //   _bkColor = CellPaint.DefaultBackgroundFillColor.ToWinColor();
            //}
            return _bkColor;
         }
         set {
            if (SetProperty(ref _bkColor, value)) {
               DrawAsync();
            }
         }
      }

      public GraphicContext GContext {
         get {
            var gContext = CellPaintBitmap.GContext;
            if (gContext == null) {
               CellPaintBitmap.GContext = gContext = new GraphicContext(true);
               gContext.PenBorder.Width = 2;
               gContext.PenBorder.ColorLight = gContext.PenBorder.ColorShadow;
               if (_randomCellBkColor)
                  gContext.BkFill.Mode = 1 + _random.Next(CellAttr.getMaxBackgroundFillModeValue());
            }
            return gContext;
         }
         set {
            CellPaintBitmap.GContext = value;
            OnPropertyChanged("GContext");
            // reset
            Image = null;
         }
      }

      public Bound Padding {
         get { return GContext.Padding; }
         set {
            GContext.Padding = value;
            OnPropertyChanged("Padding");
            // reset
            Image = null;
         }
      }

      public WriteableBitmap Image {
         get { return GetImage(false); }
         set { SetProperty(ref _image, value); }
      }

      private void DrawAsync() {
         // TODO заменить на чесную перерисовку, подобно как в fmg.uwp.res.img.StaticImg
         Image = null;
      }

      /// <summary> Return painted mosaic bitmap </summary>
      /// <param name="drawAsync">== true Сама картинка возвращается сразу.
      /// Но вот её отрисовка - в фоне.
      /// Ф-ция без ключевого слова async, т.к. результат есть DependencyObject, т.е. владелец может сам отслеживать отрисовку...</param>
      /// <returns></returns>
      public WriteableBitmap GetImage(bool drawAsync) {
         if (_image != null)
            return _image;

         var pixelSize = CellAttr.GetOwnerSize(SizeField);
         var w = pixelSize.width + GContext.Padding.Left + GContext.Padding.Right;
         var h = pixelSize.height + GContext.Padding.Top + GContext.Padding.Bottom;

         _image = BitmapFactory.New(w, h); // new WriteableBitmap(w, h); // 
         Action funcFillBk = () => _image.FillPolygon(new[] { 0, 0, w, 0, w, h, 0, h, 0, 0 }, BackgroundColor);

         if (!drawAsync) {
            // sync draw
            funcFillBk();
            foreach (var cell in Matrix)
               CellPaint.Paint(cell, new PaintableBmp(_image));
         } else {
            // async draw
            AsyncRunner.InvokeFromUiLater(() => {
               funcFillBk();
               foreach (var cell in Matrix) {
                  var tmp = cell;
                  AsyncRunner.InvokeFromUiLater(() => CellPaint.Paint(tmp, new PaintableBmp(_image)),
                     ((_random.Next() & 1) == 0)
                        ? CoreDispatcherPriority.Low
                        : CoreDispatcherPriority.Normal);
               }
            }, CoreDispatcherPriority.Normal);
         }
         return _image;
      }


      public void Dispose() {
         Dispose(true);
      }

      protected virtual void Dispose(bool disposing) {
         if (disposing) {
            // free managed resources
         }
         // free native resources if there are any.
      }

   }

}