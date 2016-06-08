using System;
using System.Linq;
using System.Collections.Generic;
using System.ComponentModel;
using fmg.common.geom;
using fmg.common.geom.util;
using fmg.core.types;
using fmg.core.mosaic;
using fmg.core.mosaic.draw;
using fmg.core.mosaic.cells;
using fmg.common.notyfier;

namespace fmg.core.img {

   /// <summary> Representable <see cref="EMosaic"/> as image </summary>
   /// <typeparam name="TPaintable">see <see cref="IPaintable"/></typeparam>
   /// <typeparam name="TImage">plaform specific image</typeparam>
   /// <typeparam name="TPaintContext">see <see cref="PaintContext{TImage}"/></typeparam>
   public abstract class MosaicsImg<TPaintable, TImage, TPaintContext> : RotatedImg<EMosaic, TImage>, IMosaic<TPaintable, TImage, TPaintContext>
      where TPaintable : IPaintable
      where TImage : class
      where TPaintContext : PaintContext<TImage>
   {
      protected MosaicsImg(EMosaic mosaicType, Matrisize sizeField, int widthAndHeight = DefaultImageSize, int? padding = null)
         : base(mosaicType, widthAndHeight, padding) {
         _sizeField = sizeField;
      }

      protected MosaicsImg(EMosaic mosaicType, Matrisize sizeField, Size sizeImage, Bound padding)
         : base(mosaicType, sizeImage, padding) {
         _sizeField = sizeField;
      }

      public enum ERotateMode {
         FullMatrix,
         SomeCells
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

      public abstract ICellPaint<TPaintable, TImage, TPaintContext> CellPaint { get; }

      private BaseCell.BaseAttribute _cellAttr;
      public BaseCell.BaseAttribute CellAttr {
         get {
            if (_cellAttr == null)
               CellAttr = MosaicHelper.CreateAttributeInstance(MosaicType, Area); // call this setter
            return _cellAttr;
         }
         private set {
            if (SetProperty(ref _cellAttr, value)) {
               Dependency_CellAttribute_Area();
               Invalidate();
            }
         }
      }

      /// <summary> caching rotated values </summary>
      private readonly List<BaseCell> _matrix = new List<BaseCell>();
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
               OnPropertyChanged(this, new PropertyChangedEventArgs("Matrix"));
               Invalidate();
            }
            return _matrix;
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
            (w - sizeImageOut.Width) / 2,
            (h - sizeImageOut.Height) / 2,
            (w - sizeImageOut.Width) / 2,
            (h - sizeImageOut.Height) / 2);
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
               Invalidate();
            }
         }
      }

      private ERotateMode _rotateMode = ERotateMode.FullMatrix;
      public ERotateMode RotateMode {
         get { return _rotateMode; }
         set { SetProperty(ref _rotateMode, value); }
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
         case "Rotate":
         case "RotateMode":
         case "SizeField":
            if (RotateMode == ERotateMode.SomeCells)
               RandomRotateElemenIndex();
            break;
         }
      }

      #region Dependencys
      void Dependency_CellAttribute_Area() {
         if (_cellAttr == null)
            return;
         CellAttr.Area = Area;
         if (_matrix.Any())
            foreach (var cell in Matrix)
               cell.Init();
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

      protected override void OnTimer() {
         var rotateMode = RotateMode;

         double? anglePrevis = null;
         if (rotateMode == ERotateMode.SomeCells)
            anglePrevis = RotateAngle;
         base.OnTimer();

         switch (rotateMode) {
         case ERotateMode.FullMatrix:
            RotatedMatrix();
            break;
         case ERotateMode.SomeCells:
            UpdateAnglesOffsets(anglePrevis.Value);
            RotatedCells();
            break;
         }
      }

      #region PART ERotateMode.FullMatrix

      private void RotatedMatrix() {
         var center = new PointDouble(Width / 2.0 - _paddingFull.Left, Height / 2.0 - _paddingFull.Top);
         foreach (var cell in Matrix) {
            cell.Init(); // restore base coords
            var reg = cell.getRegion();
            var newReg = reg.Points
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
            foreach (var p in newReg) {
               reg.SetPoint(i++, (int)p.X, (int)p.Y);
            }
         }
      }

      #endregion

      #region PART ERotateMode.SomeCells

      protected class RotatedCellContext {
         public RotatedCellContext(int index, double angleOffset, double area) {
            this.index = index;
            this.angleOffset = angleOffset;
            this.area = area;
         }
         public readonly int index;
         public readonly double angleOffset;
         public double area;
      }

      private void RotatedCells() {
         var attr = CellAttr;
         var matrix = Matrix;
         var angle = RotateAngle;
         var area = Area;

         foreach (var cntxt in _rotatedElements) {
            System.Diagnostics.Debug.Assert(cntxt.angleOffset >= 0);
            var angle2 = angle - cntxt.angleOffset;
            if (angle2 < 0)
               angle2 += 360;
            System.Diagnostics.Debug.Assert(angle2 < 360);
            System.Diagnostics.Debug.Assert(angle2 >= 0);
            // (un)comment next line to view result changes...
            angle2 = Math.Sin((angle2 / 4).ToRadian()) * angle2; // accelerate / ускоряшка..

            // (un)comment next line to view result changes...
            cntxt.area = area * (1 + Math.Sin((angle2 / 2).ToRadian())); // zoom'ирую


            var cell = Matrix[cntxt.index];

            cell.Init();
            var center = cell.getCenter();
            var coord = cell.getCoord();

            // modify
            attr.Area = cntxt.area;

            // rotate
            cell.Init();
            var centerNew = cell.getCenter();
            var reg = cell.getRegion();
            var newReg = reg.Points
                              .Select(p => {
                                 p.X -= centerNew.X;
                                 p.Y -= centerNew.Y;
                                 return p;
                              })
                              .Rotate((((coord.x + coord.y) & 1) == 0) ? +angle2 : -angle2)
                              .Select(p => {
                                 p.X += center.X;
                                 p.Y += center.Y;
                                 return p;
                              });
            var i = 0;
            foreach (var p in newReg) {
               reg.SetPoint(i++, (int)p.X, (int)p.Y);
            }

            // restore
            attr.Area = area;
         }

         // Z-ordering
         _rotatedElements.Sort((x, y) => x.area.CompareTo(y.area));
      }

      /// <summary> list of offsets rotation angles prepared for cells </summary>
      private readonly IList<double /* angle offset */ > _prepareList = new List<double>();
      protected readonly List<RotatedCellContext> _rotatedElements = new List<RotatedCellContext>();

      private void RandomRotateElemenIndex() {
         _prepareList.Clear();
         if (_rotatedElements.Any()) {
            _rotatedElements.Clear();
            OnPropertyChanged("RotatedElements");
         }

         if (!Rotate)
            return;

         // create random cells indexes  and  base rotate offset (negative)
         var len = Matrix.Count;
         var rand = new Random(Guid.NewGuid().GetHashCode());
         for (var i = 0; i < len / 4.5; ++i) {
            AddRandomToPrepareList(i == 0, rand);
         }
      }

      private void AddRandomToPrepareList(bool zero, Random rand) {
         var offset = (zero ? 0 : rand.Next(360)) + RotateAngle;
         if (offset > 360)
            offset -= 360;
         _prepareList.Add(offset);
      }

      private int NextRandomIndex(Random rand) {
         var len = Matrix.Count;
         System.Diagnostics.Debug.Assert(_rotatedElements.Count < len);
         do {
            var index = rand.Next(len);
            if (_rotatedElements.Any(ctxt => ctxt.index == index))
               continue;
            return index;
         } while (true);
      }

      protected void UpdateAnglesOffsets(double angleOld) {
         var angleNew = RotateAngle;
         var rotateDelta = RotateAngleDelta;
         var area = Area;
         Random rand = new Random(Guid.NewGuid().GetHashCode());

         if (_prepareList.Any()) {
            var copyList = new List<double>(_prepareList);
            for (var i = copyList.Count - 1; i >= 0; --i) {
               var angleOffset = copyList[i];
               if ((rotateDelta >= 0)
                  ? ((angleOld <= angleOffset && angleOffset <  angleNew && angleOld < angleNew) || // example: old=10   offset=15   new=20
                     (angleOld <= angleOffset && angleOffset >  angleNew && angleOld > angleNew) || // example: old=350  offset=355  new=0
                     (angleOld >  angleOffset && angleOffset <= angleNew && angleOld > angleNew))   // example: old=355  offset=0    new=5
                  : ((angleOld >= angleOffset && angleOffset >  angleNew && angleOld > angleNew) || // example: old=20   offset=15   new=10
                     (angleOld <  angleOffset && angleOffset >  angleNew && angleOld < angleNew) || // example: old=0    offset=355  new=350
                     (angleOld >= angleOffset && angleOffset <= angleNew && angleOld < angleNew)))  // example: old=5    offset=0    new=355
               {
                  _prepareList.RemoveAt(i);
                  _rotatedElements.Add(new RotatedCellContext(NextRandomIndex(rand), angleOffset, area));
                  OnPropertyChanged("RotatedElements");
               }
            }
         }

         List<RotatedCellContext> toRemove = null;
         foreach (var cntxt in _rotatedElements) {
            var angle2 = angleNew - cntxt.angleOffset;
            if (angle2 < 0)
               angle2 += 360;
            System.Diagnostics.Debug.Assert(angle2 < 360);
            System.Diagnostics.Debug.Assert(angle2 >= 0);

            // prepare to next step - exclude current cell from rotate and add next random cell
            var angle3 = angle2 + rotateDelta;
            if ((angle3 >= 360) || (angle3 < 0)) {
               if (toRemove == null)
                  toRemove = new List<RotatedCellContext>();
               toRemove.Add(cntxt);
            }
         }
         if (toRemove != null) {
            foreach (var cntxt in toRemove ) {
               Matrix[cntxt.index].Init(); // restore original region coords
               _rotatedElements.Remove(cntxt);
               AddRandomToPrepareList(false, rand);
            }
            OnPropertyChanged("RotatedElements");
         }
      }

      #endregion

   }

}
