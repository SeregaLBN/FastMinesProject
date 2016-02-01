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
      private Random Rand => GraphicContext.Rand;

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
            if (value != Entity)
               using (Deferring()) {
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
            if (SetProperty(ref _sizeField, value))
               using (Deferring()) {
                  RecalcArea();
                  _matrix.Clear();
                  RandomRotateElemenIndex();
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
            if (SetProperty(ref _attr, value))
               using (Deferring()) {
                  Dependency_GContext_CellAttribute();
                  Dependency_CellAttribute_Area();
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
            if (SetProperty(ref _cellPaint, value))
               using (Deferring()) {
                  Dependency_CellPaint_GContext();
               }
         }
      }

      private readonly List<BaseCell> _matrix = new List<BaseCell>();
      /// <summary>матрица ячеек, представленная(развёрнута) в виде вектора</summary>
      public IList<BaseCell> Matrix {
         get {
            if (!_matrix.Any()) {
               using (Deferring()) {
                  var attr = CellAttr;
                  var type = MosaicType;
                  var size = SizeField;
                  for (var i = 0; i < size.m; i++)
                     for (var j = 0; j < size.n; j++)
                        _matrix.Add(MosaicHelper.CreateCellInstance(attr, type, new Coord(i, j)));
               }
               OnPropertyChanged(this, new PropertyChangedEventArgs("Matrix"));
            }
            return _matrix;
         }
      }

      private void RecalcArea() {
         using (Deferring()) {
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
               (w - sizeImageOut.width)/2,
               (h - sizeImageOut.height)/2,
               (w - sizeImageOut.width)/2 + (w - sizeImageOut.width)%2,
               (h - sizeImageOut.height)/2 + (h - sizeImageOut.height)%2);
            System.Diagnostics.Debug.Assert(w == sizeImageOut.width + paddingOut.LeftAndRight);
            System.Diagnostics.Debug.Assert(h == sizeImageOut.height + paddingOut.TopAndBottom);

            PaddingFull = paddingOut;
         }
      }

      private int _area;
      public int Area {
         get {
            if (_area <= 0)
               RecalcArea();
            return _area;
         }
         set {
            if (SetProperty(ref _area, value))
               using (Deferring()) {
                  Dependency_CellAttribute_Area();
               }
         }
      }

      private Bound _paddingFull;
      public Bound PaddingFull {
         get { return _paddingFull; }
         protected set {
            if (SetProperty(ref _paddingFull, value))
               using (Deferring()) {
                  Dependency_GContext_PaddingFull();
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
            if (SetProperty(ref _gContext, value))
               using (Deferring()) {
                  Dependency_GContext_CellAttribute();
                  Dependency_GContext_PaddingFull();
                  Dependency_CellPaint_GContext();
                  Dependency_GContext_BorderWidth();
                  Dependency_GContext_BorderColor();
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

         Action funcFillBk = () => img.FillPolygon(new[] { 0, 0, w, 0, w, h, 0, h, 0, 0 }, BackgroundColor.ToWinColor());

         if (OnlySyncDraw || LiveImage()) {
            // sync draw
            funcFillBk();
            var rotatedCell = !Rotate ? null : Matrix[_rotateElemIndex];
            var paint = new PaintableBmp(img);
            foreach (var cell in Matrix)
               if (!ReferenceEquals(rotatedCell, cell))
                  CellPaint.Paint(cell, paint);
            if (rotatedCell != null) {
               // rotate
               var copy = MosaicHelper.CreateCellInstance(CellAttr, MosaicType, new Coord(rotatedCell.getCoord().x, rotatedCell.getCoord().y));
               var center = copy.getCenter();
               var reg = copy.getRegion();
               var newReg = reg.Points
                               .Select(p => {
                                          p.x -= center.x;
                                          p.y -= center.y;
                                          return new PointDouble(p);
                                       })
                               .Rotate(RotateAngle)
                               .Select(p => {
                                          p.x += center.x;
                                          p.y += center.y;
                                          return p;
                                       });
               var i = 0;
               foreach (var p in newReg) {
                  reg.setPoint(i++, (int)p.x, (int)p.y);
               }

               // draw rotated cell
               var borderWidth = BorderWidth;
               var borderColor = BorderColor;
               var pb = GContext.PenBorder;
               pb.Width = 2 * borderWidth; //BorderWidth = 5 * borderWidth;
               pb.ColorLight = pb.ColorShadow = borderColor.Darker(0.5); //BorderColor = borderColor.Darker(0.5);
               CellPaint.Paint(copy, paint);
               pb.Width = borderWidth; //BorderWidth = borderWidth;
               pb.ColorLight = pb.ColorShadow = borderColor; //BorderColor = borderColor;
            }
         } else {
            // async draw
            AsyncRunner.InvokeFromUiLater(() => {
               funcFillBk();
               var paint = new PaintableBmp(img);
               foreach (var cell in Matrix) {
                  //if (!ReferenceEquals(img, Image)) {
                  //   // aborted...
                  //   System.Diagnostics.Debug.Assert(false, "убедись под дебагером что реально что-то сбросило _image");
                  //   break;
                  //}
                  var tmp = cell;
                  AsyncRunner.InvokeFromUiLater(
                     () => CellPaint.Paint(tmp, paint),
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
            using (Deferring()) {
               Dependency_MosaicType_As_Entity(ev2?.NewValue, ev2?.OldValue);
            }
            break;
         case "Size":
         case "Padding":
            RecalcArea();
            break;
         case "BorderWidth":
            using (Deferring()) {
               Dependency_GContext_BorderWidth();
            }
            break;
         case "BorderColor":
            using (Deferring()) {
               Dependency_GContext_BorderColor();
            }
            break;
         case "Rotate":
            RandomRotateElemenIndex();
            break;
         }
      }

      private int _rotateElemIndex;

      private void RandomRotateElemenIndex() {
         if (!Rotate)
            return;
         _rotateElemIndex = Rand.Next(SizeField.m * SizeField.n);
      }


      #region Dependencys
      void Dependency_GContext_CellAttribute() {
         if ((_attr == null) || (_gContext == null))
            return;
         if (RandomCellBkColor)
            GContext.BkFill.Mode = 1 + Rand.Next(CellAttr.getMaxBackgroundFillModeValue());
      }
      void Dependency_CellAttribute_Area() {
         if (_attr == null)
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
