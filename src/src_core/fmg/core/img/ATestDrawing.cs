using System;
using System.Linq;
using System.Collections.Generic;
using fmg.common;
using fmg.common.geom;
using fmg.core.mosaic.draw;

namespace fmg.core.img {

   public abstract class ATestDrawing {

      public Random GetRandom => ThreadLocalRandom.Current;
      public int R(int max) { return GetRandom.Next(max); }
      public bool Bl => GetRandom.Next(2) == 1; // random bool
      public int NP => Bl ? -1 : +1; // negative or positive

      private readonly string titlePrefix;

      protected ATestDrawing(string titlePrefix) {
         this.titlePrefix = titlePrefix;
      }

      public void ApplyRandom<TImage, TImageView, TImageModel>(IImageController<TImage, TImageView, TImageModel> ctrller, bool testTransparent)
         where TImage : class
         where TImageView : IImageView<TImage, TImageModel>
         where TImageModel : IImageModel
      {
         testTransparent = testTransparent || Bl();

         TImageModel model = ctrller.Model;

         if (ctrller is AnimatedImgController<TImage, TImageView, TImageModel>) {
            var aCtrller = (AnimatedImgController<TImage, TImageView, TImageModel>)ctrller;
            aCtrller.Animated = bl() || bl();
            if (aCtrller.Animated) {
               aCtrller.AnimatePeriod = 1000 + R(2000);
               aCtrller.TotalFrames = 40 + R(20);

               if (model is AnimatedImageModel) {
                  aCtrller.UseRotateTransforming(Bl());
                  aCtrller.UsePolarLightFgTransforming(Bl());
                  aCtrller.AddModelTransformer(new PolarLightBkTransformer());
               }
            }
         }


         Color bkClr = Color.RandomColor();
         if (testTransparent)
            bkClr.A = 50 + R(10);

         if (model is ImageModel) {
            ImageModel ip = model as ImageModel;

            ip.BorderWidth = R(3);

            double pad = Math.Min(ip.Size.Height/3, ip.Size.Width/3);
            ip.Padding = -pad/4 + R((int)pad);

            ip.BackgroundColor = bkClr;

            ip.ForegroundColor = Color.RandomColor();//.brighter()
            if (testTransparent) {
               // test transparent
               Color clr = ip.ForegroundColor;
               if ((ip.BorderWidth > 0) && (R(4) == 0)) {
                  clr.A = Color.Transparent.getA();
               } else {
                  clr.A = 150 + r(255-150);
               }
               ip.ForegroundColor = clr;
            }

            if (model is AnimatedImageModel) {
               AnimatedImageModel aim = model as AnimatedImageModel;
               aim.PolarLights = bl();
               aim.AnimeDirection = Bl();

               if (model is LogoModel) {
                  LogoModel lm = model as LogoModel;
                  lm.UseGradient = Bl();
               }
            }
         }
         if (model is MosaicGameModel) {
            MosaicGameModel mgm = model as MosaicGameModel;
            mgm.SizeField = new Matrisize(3+R(2), 3 + R(2));

            if (model is MosaicDrawModel<TImage>) {
               var mdm = model as MosaicDrawModel<TImage>;
               mdm.BackgroundColor = bkClr;

               mdm.BackgroundFill.Mode = 1 + R(mdm.CellAttr.MaxBackgroundFillModeValue);

               mdm.PenBorder().Width = R(3);
               SizeDouble size = mdm.Size();
               double padLeftRight = R((int)(size.Width /3));
               double padTopBottom = R((int)(size.Height/3));
               mdm.Padding = new BoundDouble(padLeftRight, padTopBottom, padLeftRight, padTopBottom);

               if (model is MosaicAnimatedModel<TImage>) {
                  MosaicAnimatedModel<TImage> mam = model as MosaicAnimatedModel<TImage>;

                  MosaicAnimatedModel<TImage>.ERotateMode[] vals =
                     (MosaicAnimatedModel<TImage>.ERotateMode[])
                     Enum.GetValues(typeof(MosaicAnimatedModel<TImage>.ERotateMode));
                  mam.RotateMode = eRotateModes[vals[R(vals.Length)];
               }
            }
         }
      }

      public class CellTilingInfo {
         public int i; // index of column
         public int j; // index of row
         public PointDouble imageOffset;
      }

      public class CellTilingResult<TImage, TImageView, TImageModel>
         where TImage : class
         where TImageView : IImageView<TImage, TImageModel>
         where TImageModel : IImageModel
      {
         public Size imageSize;
         public Size tableSize;
         public Func<IImageController<TImage, TImageView, TImageModel> /* image */, CellTilingInfo> itemCallback;
      }

      public CellTiling<TImage, TImageView, TImageModel>(RectDouble rc, IList<IImageController<TImage, TImageView, TImageModel>> images, bool testTransparent)
         where TImage : class
         where TImageView : IImageView<TImage, TImageModel>
         where TImageModel : IImageModel
      {
         int len = images.Count;
         int cols = (int)Math.Round(Math.Sqrt(len) + 0.4999999999); // columns
         int rows = (int)Math.Round(len / (double)cols + 0.4999999999);
         double dx = rc.Width / cols; // cell tile width
         double dy = rc.Height / rows; // cell tile height

         int pad = 2; // cell padding
         double addonX = (cols == 1) ? 0 : !testTransparent ? 0 : dx / 4; // test intersection
         double addonY = (rows == 1) ? 0 : !testTransparent ? 0 : dy / 4; // test intersection
         var imgSize = new SizeDouble(dx - 2 * pad + addonX,  // dx - 2*pad;
                                      dy - 2 * pad + addonY); // dy - 2*pad;

         Func<IImageController<TImage, TImageView, TImageModel>, CellTilingInfo> itemCallback = item => {
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

      public string GetTitle<TImage>(List<IImageController<TImage, TImageView, TImageModel>> images)
         where TImage : class
         where TImageView : IImageView<TImage, TImageModel>
         where TImageModel : IImageModel
      {
         Func<Type, string> friendlyName = type => {
            var all = type.FullName.Split('.');
            return string.Join(".", all.Skip(all.Length - 2)
               .Select(s => s.Replace('+', '.')));
         };

         return titlePrefix + " test paints: " + string.Join(" & ", images
            .Select(i => friendlyName(i.GetType()))
            .GroupBy(x => x)
            .Select(x => x.First()));
      }

   }
}
