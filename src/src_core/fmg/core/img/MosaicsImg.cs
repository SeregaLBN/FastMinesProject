using System;
using System.Linq;
using System.Collections.Generic;
using System.ComponentModel;
using Windows.UI.Core;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.common.geom.util;
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
   public class MosaicsImg : RotatedImg<EMosaic, WriteableBitmap>, IMosaic<PaintableBmp> {
      private const bool RandomCellBkColor = true;
      protected Random Rand => GraphicContext.Rand;

      public MosaicsImg(EMosaic mosaicType, Matrisize sizeField, int widthAndHeight = DefaultImageSize, int? padding = null)
         : base(mosaicType, widthAndHeight, padding)
      {
         _sizeField = sizeField;
      }

      public MosaicsImg(EMosaic mosaicType, Matrisize sizeField, Size sizeImage, Bound padding)
         : base(mosaicType, sizeImage, padding)
      {
         _sizeField = sizeField;
      }

      /// <summary>из каких фигур состоит мозаика поля</summary>
      public EMosaic MosaicType {
         get { return Entity; }
         set {
            if (value != Entity) {
               var old = Entity;
               Entity = value;
               Dependency_MosaicType_As_Entity(value, old);
            }
         }
      }

      private Matrisize _sizeField;
      public Matrisize SizeField {
         get { return _sizeField; }
         set {
            if (SetProperty(ref _sizeField, value)) {
               RecalcArea();
               _matrix.Clear();
               Invalidate();
            }
         }
      }

      public BaseCell getCell(Coord coord) { return Matrix[coord.x * SizeField.n + coord.y]; }

      private BaseCell.BaseAttribute _cellAttr;
      public BaseCell.BaseAttribute CellAttr {
         get {
            if (_cellAttr == null)
               CellAttr = MosaicHelper.CreateAttributeInstance(MosaicType, Area); // call this setter
            return _cellAttr;
         }
         private set {
            if (SetProperty(ref _cellAttr, value)) {
               Dependency_GContext_CellAttribute();
               Dependency_CellAttribute_Area();
               Invalidate();
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
            if (SetProperty(ref _cellPaint, value))  {
               Dependency_CellPaint_GContext();
               Invalidate();
            }
         }
      }

      /// <summary> caching rotated values </summary>
      private readonly List<BaseCell> _matrixRotated = new List<BaseCell>();
      private readonly List<BaseCell> _matrix = new List<BaseCell>();
      /// <summary>матрица ячеек, представленная(развёрнута) в виде вектора</summary>
      public IList<BaseCell> Matrix {
         get {
            if (!_matrix.Any()) {
               _matrixRotated.Clear();
               var attr = CellAttr;
               var type = MosaicType;
               var size = SizeField;
               for (var i = 0; i < size.m; i++)
                  for (var j = 0; j < size.n; j++)
                     _matrix.Add(MosaicHelper.CreateCellInstance(attr, type, new Coord(i, j)));
               OnPropertyChanged(this, new PropertyChangedEventArgs("Matrix"));
               Invalidate();
            }
            return _matrix;
         }
      }

      private IList<BaseCell> RotatedMatrix {
         get {
            if (Math.Abs(RotateAngle) < 0.1)
               return Matrix;
            if (!_matrixRotated.Any()) {
               // create copy Matrix
               var attr = CellAttr;
               var type = MosaicType;
               var size = SizeField;
               for (var i = 0; i < size.m; i++)
                  for (var j = 0; j < size.n; j++)
                     _matrixRotated.Add(MosaicHelper.CreateCellInstance(attr, type, new Coord(i, j)));
            } else {
               // restore base coords
               foreach (var cell in _matrixRotated)
                  cell.Init();
            }

            var center = new PointDouble(Width / 2.0 - _paddingFull.Left, Height / 2.0 - _paddingFull.Top);
            foreach(var cell in _matrixRotated) {
               var reg = cell.getRegion();
               var newReg = reg.Points
                               .Select(p => new PointDouble(p))
                               .Select(p => {
                                          p.X -= center.X;
                                          p.Y -= center.Y;
                                          return p;
                                       })
                               .Rotate(RotateAngle)
                               .Select(p => {
                                          p.X += center.X;
                                          p.Y += center.Y;
                                          return p;
                                       });
               var i = 0;
               foreach(var p in newReg) {
                  reg.SetPoint(i++, (int)p.X, (int)p.Y);
               }
            }

            return _matrixRotated;
         }
      }

      private void RecalcArea() {
         var w = Width;
         var h = Height;
         var pad = Padding;
         var sizeImageIn = new Size(w - pad.LeftAndRight, h - pad.TopAndBottom);
         var sizeImageOut = new SizeDouble(sizeImageIn.Width, sizeImageIn.Height);
         var area = MosaicHelper.FindAreaBySize(MosaicType, SizeField, ref sizeImageOut);
         Area = area; // call setter
         System.Diagnostics.Debug.Assert(w >= (sizeImageOut.Width + pad.LeftAndRight));
         System.Diagnostics.Debug.Assert(h >= (sizeImageOut.Height + pad.TopAndBottom));
         var paddingOut = new BoundDouble(
            (w - sizeImageOut.Width)/2,
            (h - sizeImageOut.Height)/2,
            (w - sizeImageOut.Width)/2,
            (h - sizeImageOut.Height)/2);
         System.Diagnostics.Debug.Assert((sizeImageOut.Width + paddingOut.LeftAndRight).HasMinDiff(w));
         System.Diagnostics.Debug.Assert((sizeImageOut.Height + paddingOut.TopAndBottom).HasMinDiff(h));

         PaddingFull = paddingOut;
      }

      private double _area;
      public double Area {
         get {
            if (_area <= 0)
               RecalcArea();
            return _area;
         }
         set {
            if (SetProperty(ref _area, value)) {
               Dependency_CellAttribute_Area();
               Invalidate();
            }
         }
      }

      private BoundDouble _paddingFull;
      public BoundDouble PaddingFull {
         get { return _paddingFull; }
         protected set {
            if (SetProperty(ref _paddingFull, value)) {
               Dependency_GContext_PaddingFull();
               Invalidate();
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
               Dependency_GContext_CellAttribute();
               Dependency_GContext_PaddingFull();
               Dependency_CellPaint_GContext();
               Dependency_GContext_BorderWidth();
               Dependency_GContext_BorderColor();
               Invalidate();
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
         var img = Image;

         Action funcFillBk = () => { img.Clear(BackgroundColor.ToWinColor()); };

         var matrix = RotatedMatrix;
         var paint = new PaintableBmp(img);
         var cp = CellPaint;
         if (OnlySyncDraw || LiveImage()) {
            // sync draw
            funcFillBk();
            foreach (var cell in matrix)
               cp.Paint(cell, paint);
         } else {
            // async draw
            AsyncRunner.InvokeFromUiLater(() => {
               funcFillBk();
               foreach (var cell in matrix) {
                  var tmp = cell;
                  AsyncRunner.InvokeFromUiLater(
                     () => cp.Paint(tmp, paint),
                     ((Rand.Next() & 1) == 0)
                        ? CoreDispatcherPriority.Low
                        : CoreDispatcherPriority.Normal
                  );
               }
            }, CoreDispatcherPriority.Normal);
         }
      }

      protected override void OnPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         //LoggerSimple.Put("OnPropertyChanged: {0}: PropertyName={1}", Entity, ev.PropertyName);
         base.OnPropertyChanged(sender, ev);
         switch (ev.PropertyName) {
         case "Entity":
            var ev2 = ev as PropertyChangedExEventArgs<EMosaic>;
            Dependency_MosaicType_As_Entity(ev2?.NewValue, ev2?.OldValue);
            break;
         case "Size":
         case "Padding":
            RecalcArea();
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
         if ((_cellAttr == null) || (_gContext == null))
            return;
         if (RandomCellBkColor)
            GContext.BkFill.Mode = 1 + Rand.Next(CellAttr.getMaxBackgroundFillModeValue());
      }
      void Dependency_CellAttribute_Area() {
         if (_cellAttr == null)
            return;
         CellAttr.Area = Area;
         if (_matrix.Any())
            foreach (var cell in Matrix)
               cell.Init();
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
         System.Diagnostics.Debug.Assert(CellPaint is CellPaintBmp);
         ((CellPaintBmp)CellPaint).GContext = GContext;
      }
      void Dependency_MosaicType_As_Entity(EMosaic? newValue, EMosaic? oldValue) {
         Area = 0;
         _matrix.Clear();
         _matrixRotated.Clear();
         CellAttr = null;
         if ((newValue == null) || (oldValue == null))
            OnPropertyChanged(this, new PropertyChangedEventArgs("MosaicType"));
         else
            OnPropertyChanged(this, new PropertyChangedExEventArgs<EMosaic>(newValue.Value, oldValue.Value, "MosaicType"));
      }
      #endregion

      protected override void Dispose(bool disposing) {
         if (disposing) {
            GContext = null; // call setter
         }

         base.Dispose(disposing);
      }

   }

}
