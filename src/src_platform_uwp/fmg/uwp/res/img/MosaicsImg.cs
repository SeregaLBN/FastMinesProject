using System;
using System.Linq;
using System.ComponentModel;
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
         get {
            {
               var attr = _attr;
               if (attr != null)
                  return attr.Area;
            }

            var w = Width;
            var h = Height;
            var pad = Padding;
            var sizeImageIn = new Size(w - pad * 2, h - pad * 2);
            var sizeImageOut = new Size(sizeImageIn);
            var area = MosaicHelper.FindAreaBySize(_mosaicType, SizeField, ref sizeImageOut);
            System.Diagnostics.Debug.Assert(w >= (sizeImageOut.width + pad * 2));
            System.Diagnostics.Debug.Assert(h >= (sizeImageOut.height + pad * 2));
            var paddingOut = new Bound(
                     (w - sizeImageOut.width) / 2,
                     (h - sizeImageOut.height) / 2,
                     (w - sizeImageOut.width) / 2 + (w - sizeImageOut.width) % 2,
                     (h - sizeImageOut.height) / 2 + (h - sizeImageOut.height) % 2);
            System.Diagnostics.Debug.Assert(w == sizeImageOut.width + paddingOut.Left + paddingOut.Right);
            System.Diagnostics.Debug.Assert(h == sizeImageOut.height + paddingOut.Top + paddingOut.Bottom);
            GContext.Padding = paddingOut;
            return area;
         }
         set {
            throw new InvalidOperationException("Not supported...");
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

      private GraphicContext _gContext;
      protected GraphicContext GContext {
         get {
            if (_gContext == null) {
               var tmp = new GraphicContext(true);
               tmp.PenBorder.Width = 2;
               tmp.PenBorder.ColorLight = tmp.PenBorder.ColorShadow;
               if (_randomCellBkColor)
                  tmp.BkFill.Mode = 1 + _random.Next(CellAttr.getMaxBackgroundFillModeValue());

               GContext = tmp; // call this setter
            }
            return _gContext;
         }
         set {
            CellPaintBitmap.GContext = value;

            var old = _gContext;
            if (SetProperty(ref _gContext, value)) {
               if (old != null) {
                  old.PropertyChanged -= OnGContextPropertyChanged;
                  //old.Dispose();
               }
               if (value != null) {
                  value.PropertyChanged += OnGContextPropertyChanged;
               }

               // reset
               Image = null;
            }
         }
      }

      public int BorderWidth {
         get { return GContext.PenBorder.Width; }
         set {
            var w = GContext.PenBorder.Width;
            if (value != w) {
               GContext.PenBorder.Width = value;
               OnPropertyChanged();
               DrawAsync();
            }
         }
      }

      private int _size;
      /// <summary> width and height in pixel </summary>
      public int Size {
         get { return _size; }
         set {
            if (SetProperty(ref _size, value)) {
               _image = null;
               DrawAsync();
            }
         }
      }

      /// <summary> width image </summary>
      public int Width => Size;
      /// <summary> height image </summary>
      public int Height => Size;

      private int _padding;
      /// <summary> inside padding </summary>
      public int Padding {
         get { return _padding; }
         set {
            if (value * 2 >= Size)
               throw new ArgumentException("Padding size is very large. Should be less than Size / 2.");
            if (SetProperty(ref _padding, value)) {
               DrawAsync();
            }
         }
      }

      public WriteableBitmap Image {
         get { return GetImage(false); }
         set { SetProperty(ref _image, value); }
      }
      /// <summary> отдаст или уже готовую картинку; или новую, на которой контент отрисуется потом </summary>
      public WriteableBitmap ImageAsync {
         get { return GetImage(true); }
      }

      /// <summary> Return painted mosaic bitmap </summary>
      /// <param name="drawAsync">== true Сама картинка возвращается сразу.
      /// Но вот её отрисовка - в фоне.
      /// Ф-ция без ключевого слова async, т.к. результат есть DependencyObject, т.е. владелец может сам отслеживать отрисовку...</param>
      /// <returns></returns>
      private WriteableBitmap GetImage(bool drawAsync) {
         if (_image != null)
            return _image;

         var pixelSize = CellAttr.GetOwnerSize(SizeField);
         //System.Diagnostics.Debug.WriteLine("pixelSize={0}; padding={1}", pixelSize, GContext.Padding);
         var w = pixelSize.width + GContext.Padding.Left + GContext.Padding.Right;
         var h = pixelSize.height + GContext.Padding.Top + GContext.Padding.Bottom;

         var img = BitmapFactory.New(w, h); // new WriteableBitmap(w, h); // 
         Action funcFillBk = () => img.FillPolygon(new[] { 0, 0, w, 0, w, h, 0, h, 0, 0 }, BackgroundColor);

         if (!drawAsync) {
            // sync draw
            funcFillBk();
            var paint = new PaintableBmp(img);
            foreach (var cell in Matrix)
               CellPaint.Paint(cell, paint);
         } else {
            // async draw
            AsyncRunner.InvokeFromUiLater(() => {
               funcFillBk();
               var paint = new PaintableBmp(img);
               foreach (var cell in Matrix) {
                  if (!ReferenceEquals(img, _image)) {
                     // aborted...
                     System.Diagnostics.Debug.Assert(false, "убедись под дебагером что реально чтото сбросило _image");
                     break;
                  }
                  var tmp = cell;
                  AsyncRunner.InvokeFromUiLater(
                     () => CellPaint.Paint(tmp, paint),
                     ((_random.Next() & 1) == 0)
                        ? CoreDispatcherPriority.Low
                        : CoreDispatcherPriority.Normal
                  );
               }
            }, CoreDispatcherPriority.Normal);
         }
         Image = img;
         return _image;
      }


      private void DrawAsync() {
         // TODO заменить на чесную перерисовку, подобно как в fmg.uwp.res.img.StaticImg
         Image = null;
      }

      private void OnGContextPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         //...
      }
      public void Dispose() {
         Dispose(true);
      }

      protected virtual void Dispose(bool disposing) {
         if (disposing) {
            // free managed resources
            GContext = null; // call setter
         }
         // free native resources if there are any.
      }

   }

}
