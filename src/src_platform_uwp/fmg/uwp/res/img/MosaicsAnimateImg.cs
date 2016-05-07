using System;
using System.Collections.Generic;
using System.Linq;
using System.ComponentModel;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.common.geom.util;
using fmg.core.types;
using fmg.core.mosaic;
using fmg.core.mosaic.cells;
using fmg.uwp.draw.mosaic.bmp;

namespace fmg.uwp.res.img {

   public class MosaicsAnimateImg : MosaicsImg {

      public MosaicsAnimateImg(EMosaic mosaicType, Matrisize sizeField, int widthAndHeight = DefaultImageSize, int? padding = null)
         : base(mosaicType, sizeField, widthAndHeight, padding)
      { }

      public MosaicsAnimateImg(EMosaic mosaicType, Matrisize sizeField, Size sizeImage, Bound padding)
         : base(mosaicType, sizeField, sizeImage, padding)
      { }

      /// <summary> need redraw the static part of the cache </summary>
      private bool _invalidateCache = true;
      /// <summary>
      /// Cached static part of the picture.
      /// ! Recreated only when changing the original image size (minimizing CreateImage calls).
      /// </summary>
      private WriteableBitmap _imageCache;

      private IEnumerable<Tuple<int /* cell index */, double /* cell rotate angle */, int /* cell area */>> RotatedCells {
         get {
            var angle = RotateAngle;
            var area = Area;
            var transform = _rotatedElements.Select(pair => {
               var index = pair.Key;
               var angleOffset = pair.Value;
               System.Diagnostics.Debug.Assert(angleOffset >= 0);
               var angle2 = angle - angleOffset;
               if (angle2 < 0)
                  angle2 += 360;
               System.Diagnostics.Debug.Assert(angle2 < 360);
               System.Diagnostics.Debug.Assert(angle2 >= 0);
               // (un)comment next line to view result changes...
               angle2 = Math.Sin((angle2 / 4).ToRadian()) * angle2; // ускоряшка..

               // (un)comment next line to view result changes...
               var area2 = (int)(area * (1 + Math.Sin((angle2 / 2).ToRadian()))); // zoom'ирую
               return new Tuple<int, double, int>(index, angle2, area2);
            }).OrderBy(t => t.Item3); // order by area2
            return transform;
         }
      }

      private void DrawRotatedCells(Action<BaseCell> drawCellFunc) {
         var attr = CellAttr;
         // save
         var area = Area;

         foreach (var tuple in RotatedCells) {
            var index = tuple.Item1;
            var angle2 = tuple.Item2;
            var area2 = tuple.Item3;

            var rotatedCell = Matrix[index];

            var center = rotatedCell.getCenter();
            var coord = rotatedCell.getCoord();

            // modify
            attr.Area = area2;

            // rotate
            var cellNew = MosaicHelper.CreateCellInstance(attr, MosaicType, new Coord(coord.x, coord.y)); // 'copy' rotatedCell with zoomed Area
            var centerNew = cellNew.getCenter();
            var reg = cellNew.getRegion();
            var newReg = reg.Points
                            .Select(p => {
                               p.X -= centerNew.X;
                               p.Y -= centerNew.Y;
                               return new PointDouble(p.X, p.Y);
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

            // draw rotated cell
            drawCellFunc.Invoke(cellNew);

            // restore
            attr.Area = area; // ! before next call IEnumerable<> RotatedCells
         }
      }

      protected override void DrawBody() {
         if (OnlySyncDraw || LiveImage()) {
            // sync draw
            var w = Width;
            var h = Height;
            if (_imageCache == null) {
               _imageCache = CreateImage();
               _invalidateCache = true;
            }
            var img = _imageCache;
            if (_invalidateCache) {
               _invalidateCache = false;

               img.FillPolygon(new[] {0, 0, w, 0, w, h, 0, h, 0, 0}, BackgroundColor.ToWinColor());

               var paint0 = new PaintableBmp(img);
               for (var i = 0; i < Matrix.Count; ++i)
                  if (!_rotatedElements.ContainsKey(i))
                     CellPaint.Paint(Matrix[i], paint0);
            }

            { // copy cached image to original
               var rc = new Windows.Foundation.Rect(0,0,w,h);
               Image.Blit(rc, img, rc);
               img = Image;
            }

            if (!_rotatedElements.Any())
               return;

            var paint = new PaintableBmp(img);
            var pb = GContext.PenBorder;
            // save
            var borderWidth = BorderWidth;
            var borderColor = BorderColor;
            // modify
            pb.Width = 2 * borderWidth;
            pb.ColorLight = pb.ColorShadow = borderColor.Darker(0.5);

            DrawRotatedCells(rotatedCell => CellPaint.Paint(rotatedCell, paint));

            // restore
            pb.Width = borderWidth; //BorderWidth = borderWidth;
            pb.ColorLight = pb.ColorShadow = borderColor; //BorderColor = borderColor;

         } else {
            // async draw
            base.DrawBody();
         }
      }

      protected override void OnPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         base.OnPropertyChanged(sender, ev);
         switch (ev.PropertyName) {
         case "Rotate":
         case "SizeField":
            RandomRotateElemenIndex();
            break;
         case "Image":
            _invalidateCache = true;
            _imageCache = null;
            break;
         }
      }

      /// <summary> list of offsets rotation angles prepared for cells </summary>
      private readonly IList<double> _prepareList = new List<double>();
      private readonly Dictionary<int /* cell index */, double /* rotate angle offset */> _rotatedElements = new Dictionary<int, double>();

      private void RandomRotateElemenIndex() {
         _prepareList.Clear();
         if (_rotatedElements.Any()) {
            _rotatedElements.Clear();
            _invalidateCache = true;
         }

         if (!Rotate)
            return;

         // create random cells indexes  and  base rotate offset (negative)
         var len = Matrix.Count;
         for (var i = 0; i < len/4.5; ++i) {
            AddRandomToPrepareList(i==0);
         }
      }

      private void AddRandomToPrepareList(bool zero) {
         var offset = (zero?0:Rand.Next(360)) + RotateAngle;
         if (offset > 360)
            offset -= 360;
         _prepareList.Add(offset);
      }

      private int NextRandomIndex() {
         var len = Matrix.Count;
         System.Diagnostics.Debug.Assert(_rotatedElements.Count < len);
         do {
            var index = Rand.Next(len);
            if (_rotatedElements.ContainsKey(index))
               continue;
            return index;
         } while(true);
      }

      protected override void OnTimer() {
         var angleOld = RotateAngle;
         base.OnTimer();
         var angleNew = RotateAngle;

         if (_prepareList.Any()) {
            var copyList = new List<double>(_prepareList);
            for (var i = copyList.Count-1; i >= 0; --i) {
               var angleOffset = copyList[i];
               if ((angleOld <= angleOffset && angleOffset < angleNew && angleOld < angleNew) || // example: old=10   offset=15   new=20
                   (angleOld <= angleOffset && angleOffset > angleNew && angleOld > angleNew) || // example: old=350  offset=355  new=0
                   (angleOld > angleOffset && angleOffset <= angleNew && angleOld > angleNew))   // example: old=355  offset=0    new=5
               {
                  _prepareList.RemoveAt(i);
                  _rotatedElements.Add(NextRandomIndex(), angleOffset);
                  _invalidateCache = true;
               }
            }
         }

         var rotateDelta = RotateAngleDelta;
         List<int> toRemove = null;
         foreach(var kvp in _rotatedElements) {
            var index = kvp.Key;
            var angleOffset = kvp.Value;

            var angle2 = angleNew - angleOffset;
            if (angle2 < 0)
               angle2 += 360;
            System.Diagnostics.Debug.Assert(angle2 < 360);
            System.Diagnostics.Debug.Assert(angle2 >= 0);

            // prepare to next step - exclude current cell from rotate and add next random cell
            var angle3 = angle2 + rotateDelta;
            if ((angle3 >= 360) || (angle3 < 0)) {
               if (toRemove == null)
                  toRemove = new List<int>();
               toRemove.Add(index);
            }
         }
         toRemove?.ForEach(index => {
                              _rotatedElements.Remove(index);
                              AddRandomToPrepareList(false);
                              _invalidateCache = true;
                           });
      }

   }

}
