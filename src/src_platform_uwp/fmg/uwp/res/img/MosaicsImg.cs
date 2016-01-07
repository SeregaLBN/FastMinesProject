using System;
using System.Linq;
using System.Collections.Generic;
using System.ComponentModel;
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

   /// <summary> картинка поля конкретной мозаики. Используется для меню, кнопок, etc... </summary>
   public class MosaicsImg : StaticImg<EMosaic, WriteableBitmap>, IMosaic<PaintableBmp>, IDisposable {
      private const bool _randomCellBkColor = true;

      private Matrisize _sizeField;
      private BaseCell.BaseAttribute _attr;
      private readonly List<BaseCell> _matrix = new List<BaseCell>();
      private CellPaintBmp _cellPaint;
      private Windows.UI.Color _bkColor;
      private readonly Random _random = new Random(Guid.NewGuid().GetHashCode());

      public MosaicsImg(EMosaic mosaicType, Matrisize sizeField, int widthAndHeight = DefaultImageSize, int? padding = null)
         : base(mosaicType, widthAndHeight, padding)
      {
         _sizeField = sizeField;
         base.PropertyChanged += OnBasePropertyChanged;
      }

      public MosaicsImg(EMosaic mosaicType, Matrisize sizeField, Size sizeImage, Bound padding)
         : base(mosaicType, sizeImage, padding)
      {
         _sizeField = sizeField;
         base.PropertyChanged += OnBasePropertyChanged;
      }

      /// <summary>из каких фигур состоит мозаика поля</summary>
      public EMosaic MosaicType {
         get { return Entity; }
         set {
            if (value != Entity) {
               Entity = value;
               _matrix.Clear();
               _attr = null;
               _gContext = null;
               RecalcAll();
            }
         }
      }

      public Matrisize SizeField
      {
         get { return _sizeField; }
         set {
            if (SetProperty(ref _sizeField, value)) {
               // reset
               Image = null;
               _matrix.Clear();
               RecalcAll();
            }
         }
      }

      public bool SmallIco {
         set { SizeField = MosaicType.SizeIcoField(value); }
      }

      public BaseCell getCell(Coord coord) { return Matrix[coord.x * SizeField.n + coord.y]; }

      public BaseCell.BaseAttribute CellAttr => _attr ?? (_attr = MosaicHelper.CreateAttributeInstance(MosaicType, Area));

      public ICellPaint<PaintableBmp> CellPaint => CellPaintBitmap;
      protected CellPaintBmp CellPaintBitmap => _cellPaint ?? (_cellPaint = new CellPaintBmp { GContext = GContext });

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

      private int _area;
      public int Area {
         get { return _area; }
         set {
            throw new NotImplementedException("Not supported... Use Size property");
         }
      }

      private void RecalcAll() {
         _area = CellAttr.Area = RecalcArea();
         DrawAsync();
      }

      private int RecalcArea() {
         var w = Width;
         var h = Height;
         var pad = Padding;
         var sizeImageIn = new Size(w - pad.LeftAndRight, h - pad.TopAndBottom);
         var sizeImageOut = new Size(sizeImageIn);
         var area = MosaicHelper.FindAreaBySize(MosaicType, SizeField, ref sizeImageOut);
         System.Diagnostics.Debug.Assert(w >= (sizeImageOut.width + pad.LeftAndRight));
         System.Diagnostics.Debug.Assert(h >= (sizeImageOut.height + pad.TopAndBottom));
         var paddingOut = new Bound(
                  (w - sizeImageOut.width) / 2,
                  (h - sizeImageOut.height) / 2,
                  (w - sizeImageOut.width) / 2 + (w - sizeImageOut.width) % 2,
                  (h - sizeImageOut.height) / 2 + (h - sizeImageOut.height) % 2);
         System.Diagnostics.Debug.Assert(w == sizeImageOut.width + paddingOut.LeftAndRight);
         System.Diagnostics.Debug.Assert(h == sizeImageOut.height + paddingOut.TopAndBottom);
         PaddingFull = paddingOut;
         return area;
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

      protected Bound PaddingFull { get; set; }

      private GraphicContext _gContext;
      protected GraphicContext GContext {
         get {
            if (_gContext == null) {
               var tmp = new GraphicContext(true);

               tmp.PenBorder.Width = 2;
               tmp.PenBorder.ColorLight = tmp.PenBorder.ColorShadow;
               tmp.Padding = PaddingFull;
               if (_randomCellBkColor)
                  tmp.BkFill.Mode = 1 + _random.Next(CellAttr.getMaxBackgroundFillModeValue());

               GContext = tmp; // call this setter
            }
            return _gContext;
         }
         set {
            if (SetProperty(ref _gContext, value)) {
               CellPaintBitmap.GContext = value;
               if (value != null) {
                  value.PenBorder.Width = BorderWidth;
               }
               DrawAsync();
            }
         }
      }

      /// <summary> Return painted mosaic bitmap 
      /// if (!OnlySyncDraw) {
      ///   Сама картинка возвращается сразу.
      ///   Но вот её отрисовка - в фоне.
      ///   Т.к. WriteableBitmap есть DependencyObject, то его владелец может сам отслеживать отрисовку...
      /// }
      /// </summary>
      protected override void DrawBody() {
         var w = Width;
         var h = Height;
         var img = ImageInternal;
         var isNew = (img == null);
         if (isNew) {
            img = BitmapFactory.New(w, h); // new WriteableBitmap(w, h); // 

#if DEBUG
            {
               var attr = _attr;
               if (attr != null)
               {
                  var innerSize = attr.GetOwnerSize(SizeField);
                  //System.Diagnostics.Debug.WriteLine("pixelSize={0}; padding={1}", pixelSize, GContext.Padding);
                  System.Diagnostics.Debug.Assert(w == innerSize.width + GContext.Padding.Left + GContext.Padding.Right);
                  System.Diagnostics.Debug.Assert(h == innerSize.height + GContext.Padding.Top + GContext.Padding.Bottom);
               }
            }
#endif
         }

         Action funcFillBk = () => img.FillPolygon(new[] { 0, 0, w, 0, w, h, 0, h, 0, 0 }, BackgroundColor);

         if (OnlySyncDraw) {
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
                  if (!ReferenceEquals(img, Image)) {
                     // aborted...
                     System.Diagnostics.Debug.Assert(false, "убедись под дебагером что реально что-то сбросило _image");
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

         if (isNew)
            Image = img;
      }

      private void OnBasePropertyChanged(object sender, PropertyChangedEventArgs propertyChangedEventArgs) {
         if (!ReferenceEquals(sender, this))
            return;
         if (propertyChangedEventArgs.PropertyName == "BorderWidth")
            GContext.PenBorder.Width = ((PropertyChangedExEventArgs<int>)propertyChangedEventArgs).NewValue;
         if (propertyChangedEventArgs.PropertyName == "Padding")
            RecalcAll();
      }

      public void Dispose() {
         Dispose(true);
      }

      protected virtual void Dispose(bool disposing) {
         if (disposing) {
            // free managed resources
            GContext = null; // call setter
            base.PropertyChanged -= OnBasePropertyChanged;
         }
         // free native resources if there are any.
      }

   }

}
