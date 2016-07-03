using System;
using System.Collections.Generic;
using System.Globalization;
using System.Threading.Tasks;
using Windows.ApplicationModel;
using Windows.Foundation;
using Windows.Graphics.Display;
using Windows.Storage;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Media.Imaging;
using Microsoft.Graphics.Canvas;
using fmg.common.geom;
using fmg.core.types;
using fmg.uwp.draw.img.win2d;
using MosaicsGroupImg = fmg.uwp.draw.img.win2d.MosaicsGroupImg<Microsoft.Graphics.Canvas.CanvasBitmap>.CanvasBmp;
using Logo = fmg.uwp.draw.img.win2d.Logo<Microsoft.Graphics.Canvas.CanvasBitmap>.CanvasBmp;

namespace fmg.uwp.utils.win2d {

   /// <summary> Мультимедиа ресурсы программы </summary>
   public static class Resources {

      private static CanvasBitmap _imgLogoPng;
      private static CanvasBitmap _imgFlag, _imgMine;
      private static CanvasBitmap _imgPause;

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

      private static Dictionary<EBtnNewGameState, CanvasBitmap> _imgsBtnNew;
      private static Dictionary<EBtnPauseState, CanvasBitmap> _imgsBtnPause;
      private static Dictionary<EMosaicGroup, CanvasBitmap> _imgsMosaicGroupPng;
      private static Dictionary<CultureInfo, CanvasBitmap> _imgsLang;

      private static async Task<CanvasBitmap> GetImage(string path) {
         return await ImgUtils.GetImage(new Uri("ms-appx:///res/" + path)) ??
                await ImgUtils.GetImage(new Uri("ms-appx:///" + path));
      }

      private static BitmapImage GetImageSync(string path) {
         return ImgUtils.GetImageSync(new Uri("ms-appx:///res/" + path)) ??
                ImgUtils.GetImageSync(new Uri("ms-appx:///" + path));
      }


      public static async Task<CanvasBitmap> GetImgLogoPng(string subdir = "TileSq150", int scale = 100) {
         if (_imgLogoPng == null)
            //_imgLogoPng = await GetImage("Logo/Logo_128x128.png");
            _imgLogoPng = await GetImage(string.Format("Logo/{0}/Logo.scale-{1}.png", subdir, scale));
         return _imgLogoPng;
      }

      public static CanvasBitmap GetImgLogo(int sizeImage, int padding = 3) {
         var imgLogo = new Logo {
            UseGradient = true,
            SizeInt = sizeImage,
            PaddingInt = padding
         };
         return imgLogo.Image;
      }

      public static async Task<CanvasBitmap> GetImgFlag() {
         if (_imgFlag == null) {
            _imgFlag = await GetImage("CellState/Flag.png"); // сначала из ресурсов
            if (_imgFlag == null)
               _imgFlag = new Flag().Image; // иначе - своя картинка из кода
         }
         return _imgFlag;
      }

      public static async Task<CanvasBitmap> GetImgMine() {
         if (_imgMine == null) {
            _imgMine = await GetImage("CellState/Mine.png"); // сначала из ресурсов
            if (_imgMine == null)
               _imgMine = new Mine().Image; // иначе - своя картинка из кода
         }
         return _imgMine;
      }

      public static async Task<CanvasBitmap> GetImgPause() {
         if (_imgPause == null) {
            _imgPause = await GetImage("Background/Pause.png"); // сначала из ресурсов
            if (_imgPause == null)
               _imgPause = new BackgroundPause().Image; // иначе - своя картинка из кода
         }
         return _imgPause;
      }

      public static async Task<CanvasBitmap> GetImgBtnNew(EBtnNewGameState key) {
         if (_imgsBtnNew == null) {
            var vals = Enum.GetValues(typeof (EBtnNewGameState));
            _imgsBtnNew = new Dictionary<EBtnNewGameState, CanvasBitmap>(vals.Length);

            foreach (EBtnNewGameState val in vals)
               _imgsBtnNew.Add(val, await GetImage("ToolBarButton/new" + val.GetDescription() + ".png"));
         }
         return _imgsBtnNew[key];
      }

      public static async Task<CanvasBitmap> GetImgBtnPause(EBtnPauseState key) {
         if (_imgsBtnPause == null) {
            var vals = Enum.GetValues(typeof(EBtnPauseState));
            _imgsBtnPause = new Dictionary<EBtnPauseState, CanvasBitmap>(vals.Length);

            foreach (EBtnPauseState val in vals)
               _imgsBtnPause.Add(val, await GetImage("ToolBarButton/pause" + val.GetDescription() + ".png"));
         }
         return _imgsBtnPause[key];
      }

      /// <summary> из ресурсов </summary>
      public async static Task<CanvasBitmap> GetImgMosaicGroupPng(EMosaicGroup key) {
         if (_imgsMosaicGroupPng == null)
            _imgsMosaicGroupPng = new Dictionary<EMosaicGroup, CanvasBitmap>(EMosaicGroupEx.GetValues().Length);
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
      public static async Task<CanvasBitmap> GetImgMosaicPng(EMosaic mosaicType, bool smallIco) {
         return await GetImage("Mosaic/" + (smallIco ? "32x32" : "48x32") + '/' + mosaicType.GetDescription(true) + ".png");
      }

      public static BitmapImage GetImgMosaicPngSync(EMosaic mosaicType, bool smallIco)
      {
         return GetImageSync("Mosaic/" + (smallIco ? "32x32" : "48x32") + '/' + mosaicType.GetDescription(true) + ".png");
      }

      public static async Task<Dictionary<CultureInfo, CanvasBitmap>> getImgsLang() {
         if (_imgsLang == null) {
            _imgsLang = new Dictionary<CultureInfo, CanvasBitmap>(4);

            CanvasBitmap imgEng = await GetImage("Lang/English.png");
            CanvasBitmap imgUkr = await GetImage("Lang/Ukrainian.png");
            CanvasBitmap imgRus = await GetImage("Lang/Russian.png");

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
