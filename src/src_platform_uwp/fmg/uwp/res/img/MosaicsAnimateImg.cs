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
               if (!_rotateElemIndex.ContainsKey(i) || (_rotateElemIndex[i] < 0))
                  CellPaint.Paint(Matrix[i], paint);

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
            foreach(var index in new List<int>(_rotateElemIndex.Keys)) {
               var angleOffset = _rotateElemIndex[index];
               if (angleOffset >= 0)
                  continue;
               if (angleOffset + angle < 0)
                  continue;
               _rotateElemIndex[index] = 360 - angleOffset;
            }
            foreach (var pair in _rotateElemIndex) {
               var angleOffset = pair.Value;
               if (angleOffset < 0)
                  continue;

               var angle2 = angle + angleOffset;
               if (angle2 >= 360) {
                  angle2 -= 360;
               } else {
                  if (angle2 < 0)
                     angle2 += 360;
               }

               var rotatedCell = Matrix[pair.Key];

               var center = rotatedCell.getCenter();
               var coord = rotatedCell.getCoord();

               // modify
               //attr.Area = (int)(area * (1 + Math.Sin((angle2 / 2).ToRadian()))); // zoom'ирую

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

            }
            { // restore
               pb.Width = borderWidth; //BorderWidth = borderWidth;
               pb.ColorLight = pb.ColorShadow = borderColor; //BorderColor = borderColor;
               attr.Area = area;
            }

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

      private readonly IDictionary<int /* cell index */, double /* rotate angle ofset */> _rotateElemIndex = new Dictionary<int, double>();

      private void RandomRotateElemenIndex() {
         _rotateElemIndex.Clear();

         if (!Rotate)
            return;

         // create random cells indexes  and  base rotate offset (negative)
         var len = Matrix.Count;
         for (var i = 0; i < len/3.5;) {
            var pos = Rand.Next(len);
            if (_rotateElemIndex.ContainsKey(pos))
               continue;
            _rotateElemIndex.Add(pos, -Rand.Next(360));
            ++i;
         }
         var max = _rotateElemIndex.Max(p => p.Value);
         new List<int>(_rotateElemIndex.Keys).ForEach(key => _rotateElemIndex[key] -= max);
      }

   }

}
