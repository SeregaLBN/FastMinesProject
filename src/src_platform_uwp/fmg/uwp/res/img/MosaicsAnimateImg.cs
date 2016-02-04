using System;
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

            var rotatedCell = !Rotate ? null : Matrix[_rotateElemIndex];
            var paint = new PaintableBmp(img);

            foreach (var cell in Matrix)
               if (!ReferenceEquals(rotatedCell, cell))
                  CellPaint.Paint(cell, paint);

            if (rotatedCell != null) {
               var pb = GContext.PenBorder;
               var center = rotatedCell.getCenter();
               var coord = rotatedCell.getCoord();
               var attr = CellAttr;

               // save
               var borderWidth = BorderWidth;
               var borderColor = BorderColor;
               var area = Area;

               { // modify
                  pb.Width = 2 * borderWidth;
                  pb.ColorLight = pb.ColorShadow = borderColor.Darker(0.5);
                  attr.Area = (int)(area * (1 + Math.Sin((RotateAngle / 2).ToRadian()))); // zoom'ирую
               }

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
                               .Rotate((((coord.x+coord.y) & 1) == 0) ? +RotateAngle : -RotateAngle)
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

               { // restore
                  pb.Width = borderWidth; //BorderWidth = borderWidth;
                  pb.ColorLight = pb.ColorShadow = borderColor; //BorderColor = borderColor;
                  attr.Area = area;
               }
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

      private int _rotateElemIndex;

      private void RandomRotateElemenIndex() {
         if (!Rotate)
            return;
         _rotateElemIndex = Rand.Next(SizeField.m * SizeField.n);
      }

   }

}
