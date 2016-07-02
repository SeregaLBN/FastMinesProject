using System;
using System.Globalization;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Runtime.InteropServices.WindowsRuntime;
using Windows.Storage;
using Windows.UI.Xaml.Media.Imaging;
using Windows.Graphics.Imaging;
using fmg.common.geom;
using fmg.core.types;
using fmg.uwp.draw.img.wbmp;

namespace fmg.uwp.utils {

   /// <summary> Мультимедиа ресурсы программы </summary>
   public static class Resources {

      private static WriteableBitmap _imgLogoPng;
      private static WriteableBitmap _imgFlag, _imgMine;
      private static WriteableBitmap _imgPause;

      public enum EBtnNewGameState {
         eNormal,
         ePressed,
         eSelected,
         eDisabled,
         eDisabledSelected,
         eRollover,
         eRolloverSelected,

         // addons
         eNormalMosaic,
         eNormalWin,
         eNormalLoss
      }

      public static string GetDescription(this EBtnNewGameState self) {
         return self.ToString().Substring(1);
      }

      public enum EBtnPauseState {
         eNormal,
         ePressed,
         eSelected,
         eDisabled,
         eDisabledSelected,
         eRollover,
         eRolloverSelected,

         // типа ход ассистента - задел на будущее
         eAssistant
      }

      public static string GetDescription(this EBtnPauseState self) {
         return self.ToString().Substring(1);
      }

      private static Dictionary<EBtnNewGameState, WriteableBitmap> _imgsBtnNew;
      private static Dictionary<EBtnPauseState, WriteableBitmap> _imgsBtnPause;
      private static Dictionary<EMosaicGroup, WriteableBitmap> _imgsMosaicGroupPng;
      private static Dictionary<CultureInfo, WriteableBitmap> _imgsLang;

      private static async Task<WriteableBitmap> GetImage(string path) {
         return await ImgUtils.GetImage(new Uri("ms-appx:///res/" + path)) ??
                await ImgUtils.GetImage(new Uri("ms-appx:///" + path));
      }

      private static BitmapImage GetImageSync(string path) {
         return ImgUtils.GetImageSync(new Uri("ms-appx:///res/" + path)) ??
                ImgUtils.GetImageSync(new Uri("ms-appx:///" + path));
      }


      public static async Task<WriteableBitmap> GetImgLogoPng(string subdir = "TileSq150", int scale = 100) {
         if (_imgLogoPng == null)
            //_imgLogoPng = await GetImage("Logo/Logo_128x128.png");
            _imgLogoPng = await GetImage(string.Format("Logo/{0}/Logo.scale-{1}.png", subdir, scale));
         return _imgLogoPng;
      }

      public static WriteableBitmap GetImgLogo(int sizeImage, int padding = 3) {
         var imgLogo = new Logo {
            UseGradient = true,
            SizeInt = sizeImage,
            PaddingInt = padding
         };
         return imgLogo.Image;
      }

      public static async Task<WriteableBitmap> GetImgFlag() {
         if (_imgFlag == null) {
            _imgFlag = await GetImage("CellState/Flag.png"); // сначала из ресурсов
            if (_imgFlag == null)
               _imgFlag = new Flag().Image; // иначе - своя картинка из кода
         }
         return _imgFlag;
      }

      public static async Task<WriteableBitmap> GetImgMine() {
         if (_imgMine == null) {
            _imgMine = await GetImage("CellState/Mine.png"); // сначала из ресурсов
            if (_imgMine == null)
               _imgMine = new Mine().Image; // иначе - своя картинка из кода
         }
         return _imgMine;
      }

      public static async Task<WriteableBitmap> GetImgPause() {
         if (_imgPause == null) {
            _imgPause = await GetImage("Background/Pause.png"); // сначала из ресурсов
            if (_imgPause == null)
               _imgPause = new BackgroundPause().Image; // иначе - своя картинка из кода
         }
         return _imgPause;
      }

      public static async Task<WriteableBitmap> GetImgBtnNew(EBtnNewGameState key) {
         if (_imgsBtnNew == null) {
            var vals = Enum.GetValues(typeof (EBtnNewGameState));
            _imgsBtnNew = new Dictionary<EBtnNewGameState, WriteableBitmap>(vals.Length);

            foreach (EBtnNewGameState val in vals)
               _imgsBtnNew.Add(val, await GetImage("ToolBarButton/new" + val.GetDescription() + ".png"));
         }
         return _imgsBtnNew[key];
      }

      public static async Task<WriteableBitmap> GetImgBtnPause(EBtnPauseState key) {
         if (_imgsBtnPause == null) {
            var vals = Enum.GetValues(typeof(EBtnPauseState));
            _imgsBtnPause = new Dictionary<EBtnPauseState, WriteableBitmap>(vals.Length);

            foreach (EBtnPauseState val in vals)
               _imgsBtnPause.Add(val, await GetImage("ToolBarButton/pause" + val.GetDescription() + ".png"));
         }
         return _imgsBtnPause[key];
      }

      /// <summary> из ресурсов </summary>
      public async static Task<WriteableBitmap> GetImgMosaicGroupPng(EMosaicGroup key) {
         if (_imgsMosaicGroupPng == null)
            _imgsMosaicGroupPng = new Dictionary<EMosaicGroup, WriteableBitmap>(EMosaicGroupEx.GetValues().Length);
         if (_imgsMosaicGroupPng.ContainsKey(key))
            return _imgsMosaicGroupPng[key];
         return _imgsMosaicGroupPng[key] = await GetImage("MosaicGroup/" + key.GetDescription() + ".png");
      }

      /// <summary> самостоятельная отрисовка </summary>
      [Obsolete("???")]
      public static MosaicsGroupImg GetImgMosaicGroup(EMosaicGroup key, int widthAndHeight = MosaicsGroupImg.DefaultImageSize) {
         return new MosaicsGroupImg(key) { SizeInt = widthAndHeight, PolarLights = true };
      }

      /// <summary> из ресурсов </summary>
      public static async Task<WriteableBitmap> GetImgMosaicPng(EMosaic mosaicType, bool smallIco) {
         return await GetImage("Mosaic/" + (smallIco ? "32x32" : "48x32") + '/' + mosaicType.GetDescription(true) + ".png");
      }

      public static BitmapImage GetImgMosaicPngSync(EMosaic mosaicType, bool smallIco)
      {
         return GetImageSync("Mosaic/" + (smallIco ? "32x32" : "48x32") + '/' + mosaicType.GetDescription(true) + ".png");
      }

      public static async Task<Dictionary<CultureInfo, WriteableBitmap>> getImgsLang() {
         if (_imgsLang == null) {
            _imgsLang = new Dictionary<CultureInfo, WriteableBitmap>(4);

            WriteableBitmap imgEng = await GetImage("Lang/English.png");
            WriteableBitmap imgUkr = await GetImage("Lang/Ukrainian.png");
            WriteableBitmap imgRus = await GetImage("Lang/Russian.png");

            foreach (var lang in Windows.System.UserProfile.GlobalizationPreferences.Languages) {
               var locale = new CultureInfo(lang);

               if (lang == "EN")
                  _imgsLang.Add(locale, imgEng);
               else if ("GBR" == locale.EnglishName)
                  _imgsLang.Add(locale, imgEng);
               else if ("UKR" == locale.EnglishName)
                  _imgsLang.Add(locale, imgUkr);
               else if ("RUS" == locale.EnglishName)
                  _imgsLang.Add(locale, imgRus);
            }
         }
         return _imgsLang;
      }

      public static async Task<StorageFile> SaveToFile(this WriteableBitmap writeableBitmap, string fileName, StorageFolder storageFolder = null) {
         if (storageFolder == null)
            storageFolder = KnownFolders.PicturesLibrary;
         var outputFile = await storageFolder.CreateFileAsync(fileName, CreationCollisionOption.ReplaceExisting);
         await SaveToFile(writeableBitmap, outputFile);
         return outputFile;
      }

      public static async Task SaveToFile(this WriteableBitmap writeableBitmap, StorageFile outputFile) {
         byte[] pixels;
         using (var stream = writeableBitmap.PixelBuffer.AsStream()) {
            pixels = new byte[stream.Length];
            await stream.ReadAsync(pixels, 0, pixels.Length);
         }

         using (var writeStream = await outputFile.OpenAsync(FileAccessMode.ReadWrite)) {
            var encoder = await BitmapEncoder.CreateAsync(BitmapEncoder.PngEncoderId, writeStream);
            encoder.SetPixelData(BitmapPixelFormat.Bgra8, BitmapAlphaMode.Premultiplied,
               (uint)writeableBitmap.PixelWidth, (uint)writeableBitmap.PixelHeight, 96, 96, pixels);
            await encoder.FlushAsync();

            using (var outputStream = writeStream.GetOutputStreamAt(0))
               await outputStream.FlushAsync();
         }
      }
   }
}