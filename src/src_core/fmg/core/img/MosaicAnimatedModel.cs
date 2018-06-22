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

   /// <summary> Representable <see cref="EMosaic"/> as animated image </summary>
   public class MosaicAnimatedModel<TImage> : MosaicDrawModel<TImage>
      where TImage : class
   {
      public enum ERotateMode {
         /// <summary> rotate full matrix (all cells) </summary>
         fullMatrix,
         /// <summary> rotate some cells (independently of each other) </summary>
         someCells
      }

      private ERotateMode _rotateMode = ERotateMode.fullMatrix;
      /// <summary> 0° .. +360° </summary>
      private double _rotateAngle;
      /// <summary> list of offsets rotation angles prepared for cells </summary>
      private readonly IList<double /* angle offset */ > _prepareList = new List<double>();
      private readonly IList<RotatedCellContext> _rotatedElements = new List<RotatedCellContext>();
      private boolean _disableCellAttributeListener = false;
      private boolean _disableListener = false;

      public ERotateMode RotateMode {
         get { return _rotateMode; }
         set { _notifier.SetProperty(ref _rotateMode, value); }
      }

      /// <summary> 0° .. +360° </summary>
      public double RotateAngle {
         get { return _rotateAngle; }
         set {
            value = ImageModel.FixAngle(value);
            _notifier.SetProperty(ref _rotateAngle, value);
         }
      }

      protected override void OnPropertyChanged(PropertyChangedEventArgs ev) {
         if (_disableListener)
            return;
         base.OnPropertyChanged(ev);
         switch (ev.PropertyName) {
         case nameof(this.RotateMode):
         case nameof(this.SizeField):
            if (RotateMode == ERotateMode.someCells)
               RandomRotateElemenIndex();
            break;
         }
      }

      /** ///////////// ================= PART {@link ERotateMode#fullMatrix} ======================= ///////////// */

      public void RotateMatrix() { RotateMatrix(true); }
      private void RotateMatrix(bool reinit) {
         var size = CellAttr.getSize(SizeField);
         var center = new PointDouble(size.Width  / 2,
                                      size.Height / 2);
         double rotateAngle = RotateAngle;
         for (var cell : Matrix) {
            cell.Init(); // restore base coords

            FigureHelper.RotateCollection(cell.Region.Points, rotateAngle, center);
         }
         _notifier.OnPropertyChanged(nameof(MosaicGameModel.Matrix));
      }

      /** ///////////// ================= PART {@link ERotateMode#someCells} ======================= ///////////// */

      private boolean _rotateCellAlterantive;

      public sealed class RotatedCellContext {
         public RotatedCellContext(int index, double angleOffset, double area) {
            this.index = index;
            this.angleOffset = angleOffset;
            this.area = area;
         }
         public readonly int index;
         public readonly double angleOffset;
         public double area;
      }

      /// <summary> rotate BaseCell from original Matrix with modified Region </summary>
      protected void rotateCells() {
         BaseAttribute attr = CellAttr;
         List<BaseCell> matrix = Matrix;
         double area = getArea();
         double angle = getRotateAngle();

         _rotatedElements.forEach(cntxt => {
            System.Diagnostics.Debug.Assert(cntxt.angleOffset >= 0);
            double angle2 = angle - cntxt.angleOffset;
            if (angle2 < 0)
               angle2 += 360;
            System.Diagnostics.Debug.Assert(angle2 < 360);
            System.Diagnostics.Debug.Assert(angle2 >= 0);
            // (un)comment next line to look result changes...
            angle2 = Math.Sin((angle2 / 4).ToRadian()) * angle2; // accelerate / ускоряшка..

            // (un)comment next line to look result changes...
            cntxt.area = area * (1 + Math.Sin((angle2 / 2).ToRadian()); // zoom'ирую


            BaseCell cell = matrix[cntxt.index];

            cell.Init();
            PointDouble center = cell.getCenter();
            Coord coord = cell.getCoord();

            // modify
            _disableCellAttributeListener = true; // disable handling MosaicGameModel.onCellAttributePropertyChanged(where event.propertyName == BaseCell.BaseAttribute.PROPERTY_AREA)
            attr.Area = cntxt.area;

            // rotate
            cell.Init();
            PointDouble centerNew = cell.getCenter();
            PointDouble delta = new PointDouble(center.x - centerNew.x, center.y - centerNew.y);
            cell.getRegion().Points
               .RotateList((((coord.x + coord.y) & 1) == 0) ? +angle2 : -angle2, _rotateCellAlterantive ? center : centerNew)
               .MoveList(delta);

            // restore
            attr.Area = area;
            _disableCellAttributeListener = false;
         });

         // Z-ordering
         _rotatedElements.Sort((x, y) => x.area.CompareTo(y.area));
      }

      public IList<BaseCell> GetNotRotatedCells() {
         if (_rotatedElements.Empty)
            return Matrix;

         var matrix = Matrix;
         var indexes = _rotatedElements.Select(cntxt => cntxt.index);
         var notRotated = new List<>(matrix.Count - indexes.Count);
         int i = 0;
         for (var cell in matrix) {
            if (!indexes.Contains(i))
               notRotated.Add(cell);
            ++i;
         }
         return notRotated;
      }

      public void GetRotatedCells(Action<IList<BaseCell>> rotatedCellsFunctor) {
         if (_rotatedElements.Empty)
            return;

         PenBorder pb = PenBorder;
         // save
         var borderWidth = pb.Width;
         var colorLight  = pb.ColorLight;
         var colorShadow = pb.ColorShadow;
         // modify
         _disableListener = true;
         pb.Width = 2 * borderWidth;
         pb.ColorLight = colorLight.Darker(0.5);
         pb.ColorShadow = colorShadow.Darker(0.5);

         var matrix = Matrix;
         var rotatedCells = new List<>(_rotatedElements.Count);
         for (var cntxt in _rotatedElements)
            rotatedCells.Add(matrix[cntxt.index]);
         rotatedCellsFunctor(rotatedCells);

         // restore
         pb.Width = borderWidth;
         pb.ColorLight = colorLight;
         pb.ColorShadow = colorShadow;
         _disableListener = false;
      }

      private void RandomRotateElemenIndex() {
         _prepareList.Clear();
         if (!_rotatedElements.Empty) {
            _rotatedElements.Clear();
            _notifier.OnPropertyChanged(nameof(this.RotatedElements));
         }

   //      if (!Animated)
   //         return;

         // create random cells indexes  and  base rotate offset (negative)
         int len = Matrix.Size;
         for (int i = 0; i < len/4.5; ++i) {
            AddRandomToPrepareList(i==0);
         }
      }

      private void AddRandomToPrepareList(bool zero) {
         double offset = (zero ? 0 : ThreadLocalRandom.Current.nextInt(360)) + RotateAngle;
         if (offset > 360)
            offset -= 360;
         _prepareList.Add(offset);
      }

      private int NextRandomIndex() {
         int len = getMatrix().Count;
         System.Diagnostics.Debug.Assert(_rotatedElements.Count < len);
         Random rand = ThreadLocalRandom.Current;
         do {
            int index = rand.NextInt(len);
            if (_rotatedElements.AnyMatch(ctxt => ctxt.index == index))
               continue;
            return index;
         } while(true);
      }

      public void UpdateAnglesOffsets(double rotateAngleDelta) {
         double angleNew = RotateAngle;
         double angleOld = angleNew - rotateAngleDelta;
         double rotateDelta = rotateAngleDelta;
         double area = Area;

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
                  _notifier.OnPropertyChanged(nameof(this.RotatedElements));
               }
            }
         }

         List<RotatedCellContext> toRemove = null;
         foreach (var cntxt in RotatedElements) {
            double angle2 = angleNew - cntxt.angleOffset;
            if (angle2 < 0)
               angle2 += 360;
            System.Diagnostics.Debug.Assert(angle2 < 360);
            System.Diagnostics.Debug.Assert(angle2 >= 0);

            // prepare to next step - exclude current cell from rotate and add next random cell
            double angle3 = angle2 + rotateDelta;
            if ((angle3 >= 360) || (angle3 < 0)) {
               if (toRemove == null)
                  toRemove = new List<RotatedCellContext>();
               toRemove.add(cntxt);
            }
         }
         if (toRemove != null) {
            var matrix = Matrix;
            foreach (var cntxt in toRemove) {
               matrix[cntxt.index].Init(); // restore original region coords
               _rotatedElements.Remove(cntxt);
               if (!_rotatedElements.Any())
                  _rotateCellAlterantive = !_rotateCellAlterantive;
               AddRandomToPrepareList(false, rand);
            }
            _notifier.OnPropertyChanged(nameof(this.RotatedElements));
         }
      }

      protected override void OnCellAttributePropertyChanged(PropertyChangedEventArgs ev) {
         if (_disableCellAttributeListener)
            return;
         base.OnCellAttributePropertyChanged(ev);

         string propName = ev.getPropertyName();
         if (nameof(BaseCell.BaseAttribute.Area) == propName)
            switch (RotateMode) {
            case fullMatrix:
               if (!_rotateAngle.HasMinDiff(0))
                  RotateMatrix(false);
               break;
            case someCells:
               //UpdateAnglesOffsets(rotateAngleDelta);
               //RotateCells();
               break;
            }
      }

   }

}
