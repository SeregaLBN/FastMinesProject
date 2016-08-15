using System;
using System.Threading.Tasks;
using Windows.UI;
using Windows.ApplicationModel;
using Windows.Foundation;
using Windows.Graphics.Display;
using Windows.Storage;
using Microsoft.Graphics.Canvas;

namespace fmg.uwp.utils.win2d {

   /// <summary>
   /// вспомогательный класс для преобразований картинок
   /// </summary>
   public static class ImgUtils {

      /// <summary> загрузить картинку из локальных ресурсов </summary>
      public static async Task<CanvasBitmap> GetImage(Uri uri, ICanvasResourceCreator rc) {
         return await LoadBitmap(uri, rc);
      }

      //public static CanvasBitmap GetImageSync(Uri uri) {
      //   //return GetImage(uri).Result;
      //   throw new NotImplementedException();
      //}

      /// <summary> загрузить картинку из локальных ресурсов </summary>
      private static async Task<CanvasBitmap> LoadBitmap(Uri uri, ICanvasResourceCreator rc) {
         try {
            return await CanvasBitmap.LoadAsync(rc, uri);
         } catch (Exception ex) {
            System.Diagnostics.Debug.Assert(false, ex.Message);
         }
         return null;
      }

      /// <summary> загрузить картинку из локальных ресурсов </summary>
      private static async Task<CanvasBitmap> LoadBitmap(string resourceName, ICanvasResourceCreator rc) {
         if (string.IsNullOrWhiteSpace(resourceName))
            return null;
         resourceName = resourceName.TrimStart('\\'); // @"res\240x240.png";
         try {
            var file = await Package.Current.InstalledLocation.GetFileAsync(resourceName);
            var stream = await file.OpenAsync(FileAccessMode.Read);
            return await CanvasBitmap.LoadAsync(rc, stream);
         } catch (Exception ex) {
            System.Diagnostics.Debug.Assert(false, ex.Message);
         }
         return null;
      }

      /// <summary> change image size </summary>
      public static CanvasBitmap Zoom(CanvasBitmap img, int newWidth, int newHeight, ICanvasResourceCreator rc /* = CanvasDevice.GetSharedDevice() */) {
         if (img == null)
            return null;
         if ((newWidth < 1) || (newHeight < 1))
            return img;

         var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
         var tmp = new CanvasRenderTarget(rc, newWidth, newHeight, dpi);
         using (var ds = tmp.CreateDrawingSession()) {
            ds.DrawImage(img, new Rect(0, 0, newWidth, newHeight), new Rect(0, 0, img.Size.Width, img.Size.Height));
            return tmp;
         }
      }

      //public static CanvasBitmap Rotate(CanvasBitmap inputImage, double degrees) {
      //   throw new NotImplementedException();
      //}

      private static CanvasBitmap _failedImage;
      // TODO переделать...
      private static CanvasBitmap GetFailedImage() {
         if (null == _failedImage) {
            int maxX = 1024, maxY = 1024;
            var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
            var device = CanvasDevice.GetSharedDevice();
            var image = new CanvasRenderTarget(device, maxX, maxY, dpi);
            using (var ds = image.CreateDrawingSession()) {
               ds.FillRectangle(new Rect(10, 10, maxX, maxY),
                  DesignMode.DesignModeEnabled
                     ? Colors.Green
                     : Colors.Red);
               //image.DrawRectangle(...);
               var clr = Color.FromArgb(0xFF, 0xFF, 0xFF, 0xFF);
               ds.DrawLine(10, 10, 200, 200, clr);
               ds.DrawLine(10, 10, 10, maxY, clr);
               ds.DrawLine(10, maxY, maxY, maxY, clr);
               ds.DrawLine(maxX, maxY, maxX, 10, clr);
               ds.DrawLine(maxX, 10, 10, 10, clr);

               _failedImage = image;
            }
         }
         return _failedImage;
      }

   }

}
