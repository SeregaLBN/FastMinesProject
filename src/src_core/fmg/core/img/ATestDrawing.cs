using System;
using System.Linq;
using System.Collections.Generic;
using fmg.common;
using fmg.common.geom;
using fmg.core.mosaic.draw;

namespace fmg.core.img {

   public abstract class ATestDrawing {

      private static readonly Random Rnd = new Random(Guid.NewGuid().GetHashCode());
      public Random GetRandom => Rnd;
      public int R(int max) { return GetRandom.Next(max); }
      public bool Bl => GetRandom.Next(2) == 1; // random bool
      public int NP => Bl ? -1 : +1; // negative or positive

      public void ApplyRandom<TPaintable, TImage, TPaintContext, TImageInner>(StaticImg<TImage> img, bool testTransparent)
         where TPaintable : IPaintable
         where TImage : class
         where TImageInner : class
         where TPaintContext : PaintContext<TImageInner>
      {
         if (img is RotatedImg<TImage>) {
            RotatedImg<TImage> rImg = (RotatedImg<TImage>)img;
            rImg.Rotate = true;
            rImg.RotateAngleDelta = (3 + R(5)) * NP;
            rImg.RedrawInterval = 50;
            rImg.BorderWidth = Bl ? 1 : 2;
            rImg.PaddingInt = 4;
         }

         if (img is PolarLightsImg<TImage>) {
            PolarLightsImg<TImage> plImg = (PolarLightsImg<TImage>)img;
            plImg.PolarLights = true;
         }

         if (img is ALogo<TImage>) {
            ALogo<TImage> logoImg = (ALogo<TImage>)img;
            ALogo<TImage>.ERotateMode[] vals =
               (ALogo<TImage>.ERotateMode[])
               Enum.GetValues(typeof(ALogo<TImage>.ERotateMode));
            logoImg.RotateMode = vals[R(vals.Length)];
            logoImg.UseGradient = Bl;
         }

         if (img is AMosaicsImg<TPaintable, TImage, TPaintContext, TImageInner>) {
            AMosaicsImg<TPaintable, TImage, TPaintContext, TImageInner> mosaicsImg = (AMosaicsImg<TPaintable, TImage, TPaintContext, TImageInner>)img;
            AMosaicsImg<TPaintable, TImage, TPaintContext, TImageInner>.ERotateMode[] vals =
               (AMosaicsImg<TPaintable, TImage, TPaintContext, TImageInner>.ERotateMode[])
               Enum.GetValues(typeof(AMosaicsImg<TPaintable, TImage, TPaintContext, TImageInner>.ERotateMode));
            mosaicsImg.RotateMode = vals[R(vals.Length)];
         }

         if (testTransparent || Bl) {
            // test transparent
            HSV bkClr = new HSV(ColorExt.RandomColor(GetRandom));
            bkClr.a = (byte)(50 + R(10));
            img.PropertyChanged += (s, ev) => {
               if (ev.PropertyName == nameof(RotatedImg<TImage>.RotateAngle)) {
                  bkClr.h = img.RotateAngle;
                  img.BackgroundColor = bkClr.ToColor();
               }
            };
         } else {
            img.BackgroundColor = ColorExt.RandomColor(GetRandom).Brighter();
         }
      }

      public class CellTilingInfo {
         public int i; // index of column
         public int j; // index of row
         public PointDouble imageOffset;
      }

      public class CellTilingResult<TImageEx>
         where TImageEx : class
      {
         public Size imageSize;
         public Size tableSize;
         public Func<TImageEx, CellTilingInfo> itemCallback;
      }

      public CellTilingResult<TImageEx> CellTiling<TImageEx, TImage>(RectDouble rc, IList<TImageEx> images, bool testTransparent)
            where TImageEx : class
            where TImage : class
      {
         int len = images.Count;
         int cols = (int)Math.Round(Math.Sqrt(len) + 0.4999999999); // columns
         int rows = (int)Math.Round(len / (double)cols + 0.4999999999);
         double dx = rc.Width / cols; // cell tile width
         double dy = rc.Height / rows; // cell tile height

         int pad = 2; // cell padding
         double addonX = (cols == 1) ? 0 : !testTransparent ? 0 : dx / 4; // test intersection
         double addonY = (rows == 1) ? 0 : !testTransparent ? 0 : dy / 4; // test intersection
         Size imgSize = new Size((int)(dx - 2 * pad + addonX),  // dx - 2*pad;
                                 (int)(dy - 2 * pad + addonY)); // dy - 2*pad;

         Func<TImageEx, CellTilingInfo> itemCallback = item => {
            if (item is BurgerMenuImg<TImage>) {
               var brgrImg = item as BurgerMenuImg<TImage>;
               brgrImg.ResetPaddingBurgerMenu();
            }

            int pos = images.IndexOf(item);
            if (pos == -1)
               throw new Exception("Illegal usage...");

            int i = pos % cols;
            int j = pos / cols;
            PointDouble offset = new PointDouble(rc.X + i * dx + pad,
                                                 rc.Y + j * dy + pad);
            if (i == (cols - 1))
               offset.X -= addonX;
            if (j == (rows - 1))
               offset.Y -= addonY;

            return new CellTilingInfo {
               i = i,
               j = j,
               imageOffset = offset
            };
         };

         return new CellTilingResult<TImageEx> {
            imageSize = imgSize,
            tableSize = new Size(cols, rows),
            itemCallback = itemCallback
         };
      }

      public string GetTitle<TImage>(List<TImage> images)
         where TImage : class
      {
         Func<Type, string> friendlyName = type => {
            var all = type.FullName.Split('.');
            return string.Join(".", all.Skip(all.Length - 2)
               .Select(s => s.Replace('+', '.')));
         };

         return "test paints: " + string.Join(" & ", images
            .Select(i => friendlyName(i.GetType()))
            .GroupBy(x => x)
            .Select(x => x.First()));
      }

   }
}
