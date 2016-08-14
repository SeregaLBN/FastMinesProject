using System;
using System.Globalization;
using System.Threading.Tasks;
using Windows.Graphics.Display;
using Windows.Storage;
using Microsoft.Graphics.Canvas;

namespace fmg.uwp.utils.win2d {

   /// <summary> Мультимедиа ресурсы программы </summary>
   public static class Resources {

      private static async Task<CanvasBitmap> GetImage(string path, ICanvasResourceCreator rc) {
         return await ImgUtils.GetImage(new Uri("ms-appx:///res/" + path), rc) ??
                await ImgUtils.GetImage(new Uri("ms-appx:///" + path), rc);
      }

      //private static CanvasBitmap GetImageSync(string path) {
      //   return ImgUtils.GetImageSync(new Uri("ms-appx:///res/" + path)) ??
      //          ImgUtils.GetImageSync(new Uri("ms-appx:///" + path));
      //}


      public static async Task<CanvasBitmap> GetImgLogoPng(string subdir /* = "TileSq150" */, int scale /* = 100 */, ICanvasResourceCreator rc /* = CanvasDevice.GetSharedDevice() */) {
         return await GetImage(string.Format("Logo/{0}/Logo.scale-{1}.png", subdir, scale), rc);
      }

      ///// <summary> из ресурсов </summary>
      //public static async Task<CanvasBitmap> GetImgMosaicPng(EMosaic mosaicType, bool smallIco, ICanvasResourceCreator rc) {
      //   return await GetImage("Mosaic/" + (smallIco ? "32x32" : "48x32") + '/' + mosaicType.GetDescription(true) + ".png", rc);
      //}

      public static async Task<CanvasBitmap> getImgsLang(ICanvasResourceCreator rc) {
         foreach (var lang in Windows.System.UserProfile.GlobalizationPreferences.Languages) {
            var locale = new CultureInfo(lang);

            if ((lang == "EN") || ("GBR" == locale.EnglishName))
               return await GetImage("Lang/English.png", rc);
            if ("UKR" == locale.EnglishName)
               return await GetImage("Lang/Ukrainian.png", rc);
            if ("RUS" == locale.EnglishName)
               return await GetImage("Lang/Russian.png", rc);
         }
         return null;
      }

      public static async Task<StorageFile> SaveToFile(this CanvasBitmap canvasBitmap, string fileName, StorageFolder storageFolder = null) {
         if (storageFolder == null)
            storageFolder = KnownFolders.PicturesLibrary;
         var outputFile = await storageFolder.CreateFileAsync(fileName, CreationCollisionOption.ReplaceExisting);
         await SaveToFile(canvasBitmap, outputFile);
         return outputFile;
      }

      public static async Task SaveToFile(this CanvasBitmap canvasBitmap, StorageFile outputFile) {
         using (var stream = await outputFile.OpenAsync(FileAccessMode.ReadWrite)) {
            var dpi = DisplayInformation.GetForCurrentView().LogicalDpi;
            var device = CanvasDevice.GetSharedDevice();
            await CanvasImage.SaveAsync(canvasBitmap, canvasBitmap.Bounds, dpi, device, stream, CanvasBitmapFileFormat.Png);
         }
      }

   }

}
