using System;
using System.Linq;
using System.Collections.Generic;
using System.ComponentModel;
using Windows.UI.Core;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
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
      private const bool RandomCellBkColor = true;
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
               using (DispozedRedraw()) {
                  var old = Entity;
                  Entity = value;
                  Area = 0;
                  _matrix.Clear();
                  CellAttr = null;
                  OnPropertyChanged(this, new PropertyChangedExEventArgs<EMosaic>(value, old));
               }
            }
         }
      }

      private Matrisize _sizeField;
      public Matrisize SizeField {
         get { return _sizeField; }
         set {
            if (SetProperty(ref _sizeField, value)) {
               using (DispozedRedraw()) {
                  Area = 0;
                  _matrix.Clear();
               }
            }
         }
      }

      public BaseCell getCell(Coord coord) { return Matrix[coord.x * SizeField.n + coord.y]; }

      private BaseCell.BaseAttribute _attr;
      public BaseCell.BaseAttribute CellAttr {
         get {
            if (_attr == null)
               CellAttr = MosaicHelper.CreateAttributeInstance(MosaicType, Area); // call this setter
            return _attr;
         }
         private set {
            if (SetProperty(ref _attr, value)) {
               using (DispozedRedraw(value == null)) {
                  Dependency_GContext_CellAttribute();
                  Dependency_CellAttribute_Area();
               }
            }
         }
      }

      private ICellPaint<PaintableBmp> _cellPaint;
      public ICellPaint<PaintableBmp> CellPaint {
         get {
            if (_cellPaint == null)
               CellPaint = new CellPaintBmp(); // call this setter
            return _cellPaint;
         }
         private set {
            if (SetProperty(ref _cellPaint, value)) {
               using (DispozedRedraw(value == null)) {
                  Dependency_CellPaint_GContext();
               }
            }
         }
      }

      private readonly List<BaseCell> _matrix = new List<BaseCell>();
      /// <summary>матрица ячеек, представленная(развёрнута) в виде вектора</summary>
      public IList<BaseCell> Matrix {
         get {
            if (!_matrix.Any()) {
               using (DispozedRedraw()) {
                  var attr = CellAttr;
                  var type = MosaicType;
                  var size = SizeField;
                  for (var i = 0; i < size.m; i++)
                     for (var j = 0; j < size.n; j++)
                        _matrix.Add(MosaicHelper.CreateCellInstance(attr, type, new Coord(i, j)));
                  OnPropertyChanged(this, new PropertyChangedEventArgs("Matrix"));
               }
            }
            return _matrix;
         }
      }

      private void RecalcArea(bool reset) {
         if (reset) {
            Area = 0;
            return;
         }
         var w = Width;
         var h = Height;
         var pad = Padding;
         var sizeImageIn = new Size(w - pad.LeftAndRight, h - pad.TopAndBottom);
         var sizeImageOut = new Size(sizeImageIn);
         var area = MosaicHelper.FindAreaBySize(MosaicType, SizeField, ref sizeImageOut);
         Area = area; // call setter
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
      }

      private int _area;
      public int Area {
         get {
            if (_area <= 0)
               RecalcArea(false);
            return _area;
         }
         set {
            if (SetProperty(ref _area, value)) {
               using (DispozedRedraw(value == 0)) {
                  Dependency_CellAttribute_Area();
               }
            }
         }
      }

      private Bound _paddingFull;
      public Bound PaddingFull {
         get { return _paddingFull; }
         protected set {
            if (SetProperty(ref _paddingFull, value)) {
               using (DispozedRedraw()) {
                  Dependency_GContext_PaddingFull();
               }
            }
         }
      }

      private GraphicContext _gContext;
      protected GraphicContext GContext {
         get {
            if (_gContext == null)
               GContext = new GraphicContext(true); // call this setter
            return _gContext;
         }
         set {
            if (SetProperty(ref _gContext, value)) {
               using (DispozedRedraw(value == null)) {
                  Dependency_GContext_CellAttribute();
                  Dependency_GContext_PaddingFull();
                  Dependency_CellPaint_GContext();
                  Dependency_GContext_BorderWidth();
                  Dependency_GContext_BorderColor();
               }
            }
         }
      }

      protected override WriteableBitmap CreateImage() {
         var w = Width;
         var h = Height;
         return BitmapFactory.New(w, h); // new WriteableBitmap(w, h); // 
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
         var img = Image;

#if DEBUG
            {
               var attr = _attr;
               if (attr != null) {
                  var innerSize = attr.GetOwnerSize(SizeField);
                  LoggerSimple.Put("pixelSize={0}; padding={1}", Size, PaddingFull);
                  System.Diagnostics.Debug.Assert(w == innerSize.width + PaddingFull.LeftAndRight);
                  System.Diagnostics.Debug.Assert(h == innerSize.height + PaddingFull.TopAndBottom);
               }
            }
#endif

         Action funcFillBk = () => img.FillPolygon(new[] { 0, 0, w, 0, w, h, 0, h, 0, 0 }, BackgroundColor.ToWinColor());

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
      }

      private void OnBasePropertyChanged(object sender, PropertyChangedEventArgs propertyChangedEventArgs) {
         if (!ReferenceEquals(sender, this))
            return;
         switch (propertyChangedEventArgs.PropertyName) {
         case "Size":
         case "Padding":
            RecalcArea(true);
            break;
         case "BorderWidth":
            Dependency_GContext_BorderWidth();
            break;
         case "BorderColor":
            Dependency_GContext_BorderColor();
            break;
         }
      }

      #region Dependencys
      void Dependency_GContext_CellAttribute() {
         if ((_attr == null) || (_gContext == null))
            return;
         if (RandomCellBkColor)
            GContext.BkFill.Mode = 1 + _random.Next(CellAttr.getMaxBackgroundFillModeValue());
      }
      void Dependency_CellAttribute_Area() {
         if (_attr == null)
            return;
         CellAttr.Area = Area;
      }
      void Dependency_GContext_PaddingFull() {
         if (_gContext == null)
            return;
         GContext.Padding = PaddingFull;
      }
      void Dependency_GContext_BorderWidth() {
         if (_gContext == null)
            return;
         GContext.PenBorder.Width = BorderWidth;
      }
      void Dependency_GContext_BorderColor() {
         if (_gContext == null)
            return;
         var pb = GContext.PenBorder;
         pb.ColorLight = pb.ColorShadow = BorderColor;
      }
      void Dependency_CellPaint_GContext() {
         if (_cellPaint == null)
            return;
         var cellPaintBmp = CellPaint as CellPaintBmp;
         if (cellPaintBmp != null)
            cellPaintBmp.GContext = GContext;
         System.Diagnostics.Debug.Assert(cellPaintBmp != null);
      }
      #endregion

      protected override void DrawBegin() {
         LoggerSimple.Put(GetType().Name + "::DrawBegin");
         base.DrawBegin();
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
