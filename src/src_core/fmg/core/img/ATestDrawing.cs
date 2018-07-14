using System;
using System.Linq;
using System.Collections.Generic;
using fmg.common;
using fmg.common.geom;
using fmg.core.mosaic;
using System.Reflection;

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

      public void ApplyRandom<TImage, TImageView, TAImageView, TImageModel, TAnimatedModel>(IImageController<TImage, TImageView, TImageModel> ctrller, bool testTransparent)
         where TImage : class
         where TImageView : IImageView<TImage, TImageModel>
         where TAImageView : IImageView<TImage, TAnimatedModel>
         where TImageModel : IImageModel
         where TAnimatedModel : IAnimatedModel
      {
         testTransparent = testTransparent || Bl;

         TImageModel model = ctrller.Model;

         if (model is IAnimatedModel am) {
            am.Animated = Bl || Bl;
            if (am.Animated) {
               am.AnimatePeriod = 1000 + R(2000);
               am.TotalFrames = 40 + R(20);
            }
         }
         if (ctrller is AnimatedImgController<TImage, TAImageView, TAnimatedModel> aCtrller) {
         //if (IsSubClassOfGeneric(typeof(AnimatedImgController<>), ctrller.GetType())) {
            if (aCtrller.Model.Animated) {
               aCtrller.UseRotateTransforming(Bl);
               aCtrller.UsePolarLightFgTransforming(Bl);
               aCtrller.AddModelTransformer(new PolarLightBkTransformer());
            }
         }


         Color bkClr = Color.RandomColor();
         if (testTransparent)
            bkClr.A = (byte)(50 + R(10));

         if (model is AnimatedImageModel) {
            AnimatedImageModel aim = model as AnimatedImageModel;

            aim.BorderWidth = R(3);

            double pad = Math.Min(aim.Size.Height/3, aim.Size.Width/3);
            aim.SetPadding(-pad/4 + R((int)pad));

            aim.BackgroundColor = bkClr;

            aim.ForegroundColor = Color.RandomColor();//.brighter()
            if (testTransparent) {
               // test transparent
               Color clr = aim.ForegroundColor;
               if ((aim.BorderWidth > 0) && (R(4) == 0)) {
                  clr.A = Color.Transparent.A;
               } else {
                  clr.A = (byte)(150 + R(255-150));
               }
               aim.ForegroundColor = clr;
            }

            aim.PolarLights = Bl;
            aim.AnimeDirection = Bl;

            if (model is LogoModel) {
               LogoModel lm = model as LogoModel;
               lm.UseGradient = Bl;
            }
         }
         if (model is MosaicGameModel) {
            MosaicGameModel mgm = model as MosaicGameModel;
            mgm.SizeField = new Matrisize(3+R(2), 3 + R(2));

            if (model is MosaicDrawModel<TImage>) {
               var mdm = model as MosaicDrawModel<TImage>;
               mdm.BackgroundColor = bkClr;

               mdm.BkFill.Mode = 1 + R(mdm.CellAttr.GetMaxBackgroundFillModeValue());

               mdm.PenBorder.Width = R(3);
               SizeDouble size = mdm.Size;
               double padLeftRight = R((int)(size.Width /3));
               double padTopBottom = R((int)(size.Height/3));
               mdm.Padding = new BoundDouble(padLeftRight, padTopBottom, padLeftRight, padTopBottom);

               if (model is MosaicAnimatedModel<TImage>) {
                  MosaicAnimatedModel<TImage> mam = model as MosaicAnimatedModel<TImage>;

                  MosaicAnimatedModel<TImage>.ERotateMode[] vals =
                     (MosaicAnimatedModel<TImage>.ERotateMode[])
                     Enum.GetValues(typeof(MosaicAnimatedModel<TImage>.ERotateMode));
                  mam.RotateMode = vals[R(vals.Length)];
               }
            }
         }
      }

      public class CellTilingInfo {
         public int i; // index of column
         public int j; // index of row
         public PointDouble imageOffset;
      }

      public class CellTilingResult<TImage, TImageController, TImageView, TImageModel>
         where TImage : class
         where TImageController : ImageController<TImage, TImageView, TImageModel>
         where TImageView : IImageView<TImage, TImageModel>
         where TImageModel : IImageModel
      {
         public SizeDouble imageSize;
         public Size tableSize;
         public Func<TImageController /* image */, CellTilingInfo> itemCallback;
      }

      public CellTilingResult<TImage, TImageController, TImageView, TImageModel> CellTiling<TImage, TImageController, TImageView, TImageModel>(RectDouble rc, IList<TImageController> images, bool testTransparent)
         where TImage : class
         where TImageController : ImageController<TImage, TImageView, TImageModel>
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

         CellTilingInfo itemCallback(TImageController item) {
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
         }

         return new CellTilingResult<TImage, TImageController, TImageView, TImageModel> {
            imageSize = imgSize,
            tableSize = new Size(cols, rows),
            itemCallback = itemCallback
         };
      }

      public string GetTitle<TImage, TImageController, TImageView, TImageModel>(List<TImageController> images)
         where TImage : class
         where TImageController : ImageController<TImage, TImageView, TImageModel>
         where TImageView : IImageView<TImage, TImageModel>
         where TImageModel : IImageModel
      {
         string friendlyName(Type type) {
            var all = type.FullName.Split('.');
            return string.Join(".", all.Skip(all.Length - 2)
               .Select(s => s.Replace('+', '.')));
         }

         return titlePrefix + " test paints: " + string.Join(" & ", images
            .Select(i => friendlyName(i.GetType()))
            .GroupBy(x => x)
            .Select(x => x.First()));
      }

   }

}
