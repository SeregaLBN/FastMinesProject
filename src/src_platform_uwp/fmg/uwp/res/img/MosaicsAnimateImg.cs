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
using fmg.uwp.draw.mosaic.bmp;

namespace fmg.uwp.res.img {

   public class MosaicsAnimateImg : MosaicsImg {

      public MosaicsAnimateImg(EMosaic mosaicType, Matrisize sizeField, int widthAndHeight = DefaultImageSize, int? padding = null)
         : base(mosaicType, sizeField, widthAndHeight, padding)
      { }

      public MosaicsAnimateImg(EMosaic mosaicType, Matrisize sizeField, Size sizeImage, Bound padding)
         : base(mosaicType, sizeField, sizeImage, padding)
      { }


      protected override void DrawBody() {
         if (OnlySyncDraw || LiveImage()) {
            // sync draw
            var w = Width;
            var h = Height;
            var img = Image;

            img.FillPolygon(new[] { 0, 0, w, 0, w, h, 0, h, 0, 0 }, BackgroundColor.ToWinColor());

            var paint = new PaintableBmp(img);

            for (var i=0; i<Matrix.Count; ++i)
               if (!_rotateElemIndexPositive.ContainsKey(i))
                  CellPaint.Paint(Matrix[i], paint);

            if (!_rotateElemIndexPositive.Any())
               return;

            var pb = GContext.PenBorder;
            var attr = CellAttr;
            var angle = RotateAngle;
            // save
            var borderWidth = BorderWidth;
            var borderColor = BorderColor;
            var area = Area;
            // modify
            pb.Width = 2 * borderWidth;
            pb.ColorLight = pb.ColorShadow = borderColor.Darker(0.5);

            var transform = _rotateElemIndexPositive.Select(pair => {
               var angleOffset = pair.Value;
               System.Diagnostics.Debug.Assert(angleOffset >= 0);

               var angle2 = angle + angleOffset;
               if (angle2 >= 360) {
                  angle2 -= 360;
               } else {
                  if (angle2 < 0)
                     angle2 += 360;
               }
               System.Diagnostics.Debug.Assert(angle2 < 360);
               System.Diagnostics.Debug.Assert(angle2 >= 0);
               // (un)comment next line to view result changes...
               angle2 = Math.Sin((angle2 / 4).ToRadian()) * angle2; // ускоряшка..

               // (un)comment next line to view result changes...
               var area2 = (int)(area * (1 + Math.Sin((angle2 / 2).ToRadian()))); // zoom'ирую
               return new KeyValuePair<KeyValuePair<int, double>, KeyValuePair<double, int>>(pair, new KeyValuePair<double, int>(angle2, area2));
            }).OrderBy(x => x.Value.Value);
            foreach (var pair in transform) {
               var angle2 = pair.Value.Key;
               // modify
               attr.Area = (int)(area * (1 + Math.Sin((angle2 / 2).ToRadian()))); // zoom'ирую

               var index = pair.Key.Key;
               var rotatedCell = Matrix[index];

               var center = rotatedCell.getCenter();
               var coord = rotatedCell.getCoord();

               // rotate
               var cellNew = MosaicHelper.CreateCellInstance(attr, MosaicType, new Coord(coord.x, coord.y)); // 'copy' rotatedCell with zoomed Area
               var centerNew = cellNew.getCenter();
               var reg = cellNew.getRegion();
               var newReg = reg.Points
                               .Select(p => {
                                          p.x -= centerNew.x;
                                          p.y -= centerNew.y;
                                          return new PointDouble(p.x, p.y);
                                       })
                               .Rotate(((((coord.x+coord.y) & 1) == 0) ? +1 : -1) * angle2)
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
               CellPaint.Paint(cellNew, paint);

               // restore
               attr.Area = area;
            }
            // restore
            pb.Width = borderWidth; //BorderWidth = borderWidth;
            pb.ColorLight = pb.ColorShadow = borderColor; //BorderColor = borderColor;
            //attr.Area = area;

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
         }
      }

      //protected override void OnTimer() {
      //   base.OnTimer();

      //}

      private readonly IDictionary<int /* cell index */, double /* rotate angle ofset (negative) */> _rotateElemIndexNegative = new Dictionary<int, double>();
      private readonly IDictionary<int /* cell index */, double /* rotate angle ofset (positive) */> _rotateElemIndexPositive = new Dictionary<int, double>();

      private void RandomRotateElemenIndex() {
         _rotateElemIndexNegative.Clear();
         _rotateElemIndexPositive.Clear();

         if (!Rotate)
            return;

         // create random cells indexes  and  base rotate offset (negative)
         var len = Matrix.Count;
         for (var i = 0; i < len/4.5;) {
            var pos = Rand.Next(len);
            if (_rotateElemIndexNegative.ContainsKey(pos))
               continue;
            _rotateElemIndexNegative.Add(pos, (_rotateElemIndexNegative.Count == 0) ? 0 : -Rand.Next(360));
            ++i;
         }
      }

      protected override void OnTimer() {
         base.OnTimer();

         if (!_rotateElemIndexNegative.Any())
            return;
         var angle = RotateAngle;
         foreach (var index in new List<int>(_rotateElemIndexNegative.Keys)) {
            var angleOffset = _rotateElemIndexNegative[index];
            if (angleOffset + angle < 0)
               continue;
            var positive = _rotateElemIndexPositive[index] = 360 + angleOffset; // negative value as positive
            System.Diagnostics.Debug.Assert(positive <= 360);
            System.Diagnostics.Debug.Assert(positive >= 0);
            _rotateElemIndexNegative.Remove(index);
         }
      }

   }

}
