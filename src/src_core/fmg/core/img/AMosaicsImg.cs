using System;
using System.Linq;
using System.Collections.Generic;
using System.ComponentModel;
using fmg.common;
using fmg.common.geom;
using fmg.common.geom.util;
using fmg.core.types;
using fmg.core.mosaic;
using fmg.core.mosaic.cells;

namespace fmg.core.img {

   /// <summary> Abstract representable <see cref="EMosaic"/> as image </summary>
   /// <typeparam name="TImage">plaform specific image</typeparam>
   public abstract class AMosaicsImg<TImage> : RotatedImg<TImage>
      where TImage : class
   {

      public enum ERotateMode {
         FullMatrix,
         SomeCells
      }

      /// <summary> MVC: model </summary>
      private Mosaic _mosaic;

      /// <summary> MVC: model </summary>
      public Mosaic Mosaic {
         get {
            if (_mosaic == null)
               Mosaic = new Mosaic(); // call this setter
            return _mosaic;
         }
         protected set {
            if (_mosaic != null) {
               _mosaic.PropertyChanged -= OnMosaicPropertyChanged;
               _mosaic.Dispose();
            }
            _mosaic = value;
            if (_mosaic != null) {
               _mosaic.PropertyChanged += OnMosaicPropertyChanged;
            }
         }
      }

      /// <summary>из каких фигур состоит мозаика поля</summary>
      public EMosaic MosaicType {
         get { return Mosaic.MosaicType; }
         set { Mosaic.MosaicType = value; }
      }

      public Matrisize SizeField {
         get { return Mosaic.SizeField; }
         set { Mosaic.SizeField = value; }
      }

      public BaseCell getCell(Coord coord) { return Matrix[coord.x * SizeField.n + coord.y]; }

      /// <summary>матрица ячеек, представленная(развёрнута) в виде вектора</summary>
      public IList<BaseCell> Matrix => Mosaic.Matrix;

      private void RecalcArea() {
         var w = Size.Width;
         var h = Size.Height;
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

      public double Area {
         get { return Mosaic.Area; }
         set { Mosaic.Area = value; }
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

      private void OnMosaicPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         switch (ev.PropertyName) {
         case nameof(Mosaic.SizeField):
            RecalcArea();
            OnPropertyChanged<Matrisize>(ev, nameof(this.SizeField));
            Invalidate();
            break;
         case nameof(Mosaic.MosaicType):
            RecalcArea();
            OnPropertyChanged<EMosaic>(ev, nameof(this.MosaicType));
            Invalidate();
            break;
         case nameof(Mosaic.Area):
            OnPropertyChanged<double>(ev, nameof(this.Area));
            Invalidate();
            break;
         case nameof(Mosaic.CellAttr):
            Invalidate();
            break;
         case nameof(Mosaic.Matrix):
            OnPropertyChanged(nameof(this.Matrix));
            Invalidate();
            break;
         default:
            break;
         }
      }

      protected override void OnPropertyChanged(PropertyChangedEventArgs ev) {
         //LoggerSimple.Put($"OnPropertyChanged: {Entity}: PropertyName={ev.PropertyName}");
         base.OnPropertyChanged(ev);
         switch (ev.PropertyName) {
         case nameof(this.Size):
         case nameof(this.Padding):
            RecalcArea();
            break;
         case nameof(this.Rotate):
         case nameof(this.RotateMode):
         case nameof(this.SizeField):
            if (RotateMode == ERotateMode.SomeCells)
               RandomRotateElemenIndex();
            break;
         }
      }


      protected override void OnTimer() {
         var rotateMode = RotateMode;

         double? anglePrevis = null;
         if (rotateMode == ERotateMode.SomeCells)
            anglePrevis = RotateAngle;
         base.OnTimer();

         switch (rotateMode) {
         case ERotateMode.FullMatrix:
            RotateMatrix();
            break;
         case ERotateMode.SomeCells:
            UpdateAnglesOffsets(anglePrevis.Value);
            RotateCells();
            break;
         }
      }

      #region PART ERotateMode.FullMatrix

      private void RotateMatrix() {
         var center = new PointDouble(Size.Width / 2.0 - _paddingFull.Left, Size.Height / 2.0 - _paddingFull.Top);
         foreach (var cell in Matrix) {
            cell.Init(); // restore base coords
            cell.getRegion().Points.RotateList(RotateAngle, center);
         }
      }

      #endregion

      #region PART ERotateMode.SomeCells

      private bool rotateCellAlterantive;

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

      private void RotateCells() {
         var attr = Mosaic.CellAttr;
         var matrix = Matrix;
         var angle = RotateAngle;
         var area = Area;

         foreach (var cntxt in RotatedElements) {
            System.Diagnostics.Debug.Assert(cntxt.angleOffset >= 0);
            var angle2 = angle - cntxt.angleOffset;
            if (angle2 < 0)
               angle2 += 360;
            System.Diagnostics.Debug.Assert(angle2 < 360);
            System.Diagnostics.Debug.Assert(angle2 >= 0);
            // (un)comment next line to look result changes...
            angle2 = Math.Sin((angle2 / 4).ToRadian()) * angle2; // accelerate / ускоряшка..

            // (un)comment next line to look result changes...
            cntxt.area = area * (1 + Math.Sin((angle2 / 2).ToRadian())); // zoom'ирую


            var cell = matrix[cntxt.index];

            cell.Init();
            var center = cell.getCenter();
            var coord = cell.getCoord();

            var m = Mosaic;
            // modify
            m.EnableCellAttributePropertyListener(false); // disable handling Mosaic.OnCellAttributePropertyChanged(where event.PropertyName == BaseCell.BaseAttribute.Area)
            attr.Area = cntxt.area;

            // rotate
            cell.Init();
            var centerNew = cell.getCenter();
            var delta = new PointDouble(center.X - centerNew.X, center.Y - centerNew.Y);
            cell.getRegion().Points
               .RotateList((((coord.x + coord.y) & 1) == 0) ? +angle2 : -angle2, rotateCellAlterantive ? center : centerNew)
               .MoveList(delta);

            // restore
            attr.Area = area;
            m.EnableCellAttributePropertyListener(true);
         }

         // Z-ordering
         RotatedElements.Sort((x, y) => x.area.CompareTo(y.area));
      }

      /// <summary> list of offsets rotation angles prepared for cells </summary>
      private readonly IList<double /* angle offset */ > _prepareList = new List<double>();
      protected List<RotatedCellContext> RotatedElements { get; } = new List<RotatedCellContext>();

      private void RandomRotateElemenIndex() {
         _prepareList.Clear();
         if (RotatedElements.Any()) {
            RotatedElements.Clear();
            OnPropertyChanged(nameof(this.RotatedElements));
         }

         if (!Rotate)
            return;

         // create random cells indexes  and  base rotate offset (negative)
         var len = Matrix.Count;
         var rand = ThreadLocalRandom.Current;
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
         System.Diagnostics.Debug.Assert(RotatedElements.Count < len);
         do {
            var index = rand.Next(len);
            if (RotatedElements.Any(ctxt => ctxt.index == index))
               continue;
            return index;
         } while (true);
      }

      protected void UpdateAnglesOffsets(double angleOld) {
         var angleNew = RotateAngle;
         var rotateDelta = RotateAngleDelta;
         var area = Area;
         Random rand = ThreadLocalRandom.Current;

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
                  RotatedElements.Add(new RotatedCellContext(NextRandomIndex(rand), angleOffset, area));
                  OnPropertyChanged(nameof(this.RotatedElements));
               }
            }
         }

         List<RotatedCellContext> toRemove = null;
         foreach (var cntxt in RotatedElements) {
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
            foreach (var cntxt in toRemove) {
               Matrix[cntxt.index].Init(); // restore original region coords
               RotatedElements.Remove(cntxt);
               if (!RotatedElements.Any())
                  rotateCellAlterantive = !rotateCellAlterantive;
               AddRandomToPrepareList(false, rand);
            }
            OnPropertyChanged(nameof(this.RotatedElements));
         }
      }

      #endregion

      protected override void Dispose(bool disposing) {
         if (Disposed)
            return;

         base.Dispose(disposing);

         if (disposing)
            Mosaic = null;
      }

   }

}
