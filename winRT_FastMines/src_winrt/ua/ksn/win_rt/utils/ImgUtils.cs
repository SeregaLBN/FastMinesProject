using System;
using System.IO;
using System.Runtime.InteropServices.WindowsRuntime;
using System.Threading.Tasks;
using Windows.ApplicationModel;
using Windows.Foundation;
using Windows.Graphics.Imaging;
using Windows.Storage;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Media.Imaging;

namespace ua.ksn.win_rt.utils {

   /// <summary>
   /// вспомогательный класс для преобразований картинок
   /// </summary>
   public class ImgUtils {

      /// <summary> загрузить картинку из локальных ресурсов </summary>
      public static async Task<WriteableBitmap> GetImage(Uri uri) {
         return await LoadBitmap(uri);
      }

      /// <summary> загрузить картинку из локальных ресурсов </summary>
      private static async Task<WriteableBitmap> LoadBitmap(Uri uri) {
         try {
            return await new WriteableBitmap(1, 1).FromContent(uri);
            //return await BitmapFactory.New(1, 1).FromContent(uri);
            //return await LoadBitmapAsync(uri.LocalPath.Replace('/', '\\'));
         }
         catch (Exception ex) {
            System.Diagnostics.Debug.Assert(false, ex.Message);
         }
         return null;
      }

      /// <summary> загрузить картинку из локальных ресурсов </summary>
      private static async Task<WriteableBitmap> LoadBitmap(string resourceName) {
         if (string.IsNullOrWhiteSpace(resourceName))
            return null;
         resourceName = resourceName.TrimStart('\\'); // @"res\240x240.png";
         try {
#if !true // variant 1
            var storage = await Package.Current.InstalledLocation.GetFileAsync(resourceName);
            var stream = await storage.OpenReadAsync();
            var bmp = new WriteableBitmap(1, 1);
            bmp.SetSource(stream);
            return bmp;

#elif !false // variant 2
   // Decode pixel data
   //var file = await StorageFile.GetFileFromApplicationUriAsync(uri);
                var file = await Package.Current.InstalledLocation.GetFileAsync(resourceName);
                var ra = file.OpenAsync(FileAccessMode.Read).GetResults();
                var decoder = BitmapDecoder.CreateAsync(ra).GetResults();
                var transform = new global::Windows.Graphics.Imaging.BitmapTransform();
                var pixelData =
                    await
                        decoder.GetPixelDataAsync(decoder.BitmapPixelFormat, decoder.BitmapAlphaMode, transform,
                            ExifOrientationMode.RespectExifOrientation, ColorManagementMode.ColorManageToSRgb);
                var pixels = pixelData.DetachPixelData();

                // Copy to WriteableBitmap
                var bmp = new WriteableBitmap((int) decoder.PixelWidth, (int) decoder.PixelHeight);
                using (var bmpStream = bmp.PixelBuffer.AsStream())
                {
                    bmpStream.Seek(0, SeekOrigin.Begin);
                    bmpStream.Write(pixels, 0, (int) bmpStream.Length);
                    return bmp;
                }
#elif !false // variant 3 failed...
                using (
                    System.IO.Stream imageStream =
                        typeof (BlankPage1).GetTypeInfo().Assembly.GetManifestResourceStream(resourceName))
                {
                    var stream = new InMemoryRandomAccessStream();
                    imageStream.CopyTo(stream.AsStreamForWrite());
                    stream.Seek(0);

                    var bitmap = new BitmapImage();
                    await bitmap.SetSourceAsync(stream);

                    // convert to a writable bitmap so we can get the PixelBuffer back out later...
                    // in case we need to edit and/or re-encode the image.
                    var bmp = new WriteableBitmap(bitmap.PixelHeight, bitmap.PixelWidth);
                    stream.Seek(0);
                    bmp.SetSource(stream);

                    return bmp;
                }
#endif
         }
         catch (Exception ex) {
            System.Diagnostics.Debug.Assert(false, ex.Message);
         }
         return null;
      }

      /// <summary> change image size </summary>
      public static WriteableBitmap Zoom(WriteableBitmap img, int newWidth, int newHeight) {
         if (img == null)
            return null;
         var tmp = new WriteableBitmap(newWidth, newHeight);
         tmp.Blit(new Rect(0, 0, newWidth, newHeight), img, new Rect(0, 0, img.PixelWidth, img.PixelHeight));
         return tmp;
      }

      public static ImageSource Rotate(ImageSource inputImage, double degrees) {
         throw new NotImplementedException();
      }
   }
}