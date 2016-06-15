using System.Linq;
using Windows.UI.Core;
using Windows.UI.Xaml.Media.Imaging;
using fmg.core.types;
using fmg.common;
using fmg.common.geom;
using fmg.core.img;
using fmg.uwp.utils;

namespace fmg.uwp.res.img {

   /// <summary> Representable <see cref="EMosaicGroup"/> as image.
   /// <br/>
   /// WriteableBitmap impl
   /// </summary>
   public class MosaicsGroupImg : core.img.MosaicsGroupImg<WriteableBitmap> {

      static MosaicsGroupImg() {
         if (StaticImgConsts.DeferrInvoker == null)
            StaticImgConsts.DeferrInvoker = doRun => AsyncRunner.InvokeFromUiLater(() => doRun(), CoreDispatcherPriority.Normal);
         if (RotatedImgConst.TimerCreator == null)
            RotatedImgConst.TimerCreator = () => new ui.Timer();
      }

      public MosaicsGroupImg(EMosaicGroup group, int widthAndHeight = DefaultImageSize, int? padding = null)
         : base(group, widthAndHeight, padding)
      {
         SyncDraw = Windows.ApplicationModel.DesignMode.DesignModeEnabled;
      }

      protected override WriteableBitmap CreateImage() {
         //LoggerSimple.Put("CreateImage: Width={0}; Height={1}: {2}", Width, Height, Entity);
         return new WriteableBitmap(Width, Height);
      }

      protected override void DrawBody() {
         var bmp = Image;

         bmp.Clear(BackgroundColor.ToWinColor());

         var points = GetCoords().PointsAsXyxyxySequence(true).ToArray();
         bmp.FillPolygon(points, ForegroundColorAttenuate.ToWinColor());

         // draw perimeter border
         var clr = BorderColor;
         if (clr.A != Color.Transparent.A) {
            var clrWin = clr.ToWinColor();
            var bw = BorderWidth;
            for (var i = 0; i < points.Length - 2; i += 2) {
               bmp.DrawLineAa(points[i], points[i + 1], points[i + 2], points[i + 3], clrWin, bw);
            }
         }
      }

   }

}
