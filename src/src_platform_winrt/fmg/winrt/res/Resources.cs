using System;
using System.Globalization;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Runtime.InteropServices.WindowsRuntime;
using Windows.Storage;
using Windows.UI.Xaml.Media.Imaging;
using Windows.Graphics.Imaging;
using fmg.common.geom;
using fmg.winrt.utils;
using fmg.core.types;
using fmg.winrt.res.img;

namespace fmg.winrt.res {

   /// <summary> Мультимедиа ресурсы программы </summary>
   public static class Resources {
      //public static readonly WriteableBitmap ImgDummy1x1 = new WriteableBitmap(1, 1);

      public static readonly Windows.UI.Color DefaultBkColor = Windows.UI.Color.FromArgb(0xFF, 0xff, 0x8c, 0x00);

      private static WriteableBitmap _imgLogoPng;
      private static Dictionary<Tuple<Size, uint, uint>, WriteableBitmap> _imgLogos;

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
      private static Dictionary<EMosaicGroup, MosaicsGroupImg> _imgsMosaicGroup;
      private static Dictionary<EMosaicGroup, WriteableBitmap> _imgsMosaicGroupPng;
      private static Dictionary<Tuple<EMosaic, Size, int, Windows.UI.Color, Size>, MosaicsImg> _imgsMosaic;
      private static Dictionary<Tuple<EMosaic, bool>, WriteableBitmap> _imgsMosaicPng;
      private static Dictionary<CultureInfo, WriteableBitmap> _imgsLang;

      private static async Task<WriteableBitmap> GetImage(string path) {
         return await ImgUtils.GetImage(new Uri("ms-appx:///res/" + path)) ??
                await ImgUtils.GetImage(new Uri("ms-appx:///" + path));
      }

      private static BitmapImage GetImageSync(string path) {
         return ImgUtils.GetImageSync(new Uri("ms-appx:///res/" + path)) ??
                ImgUtils.GetImageSync(new Uri("ms-appx:///" + path));
      }


      public static async Task<WriteableBitmap> GetImgLogoPng(string subdir = "Tile", int scale = 100) {
         if (_imgLogoPng == null)
            //_imgLogoPng = await GetImage("Logo/Logo_128x128.png");
            _imgLogoPng = await GetImage(string.Format("Logo/{0}/Logo.scale-{1}.png", subdir, scale));
         return _imgLogoPng;
      }

      public static WriteableBitmap GetImgLogo(Size sizeImage, uint loopMix = 0, uint margin = 3) {
         if (_imgLogos == null)
            _imgLogos = new Dictionary<Tuple<Size, uint, uint>, WriteableBitmap>();
         var key = new Tuple<Size, uint, uint>(sizeImage, loopMix, margin);
         if (_imgLogos.ContainsKey(key))
            return _imgLogos[key];
         var imgLogo = new Logo {
            Margin = margin,
            ZoomX = Logo.CalcZoom(sizeImage.width, margin),
            ZoomY = Logo.CalcZoom(sizeImage.height, margin)
         };
         imgLogo.MixLoopColor(loopMix);
         return _imgLogos[key] = imgLogo.Image;
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
      public static MosaicsGroupImg GetImgMosaicGroup(EMosaicGroup key) {
         if (_imgsMosaicGroup == null)
            _imgsMosaicGroup = new Dictionary<EMosaicGroup, MosaicsGroupImg>(EMosaicGroupEx.GetValues().Length);
         if (_imgsMosaicGroup.ContainsKey(key))
            return _imgsMosaicGroup[key];
         return _imgsMosaicGroup[key] = new MosaicsGroupImg(key, true);
      }

      /// <summary> из ресурсов </summary>
      public static async Task<WriteableBitmap> GetImgMosaicPng(EMosaic mosaicType, bool smallIco) {
         if (_imgsMosaicPng == null)
            _imgsMosaicPng = new Dictionary<Tuple<EMosaic, bool>, WriteableBitmap>(EMosaicEx.GetValues().Length);
         var key = new Tuple<EMosaic, bool>(mosaicType, smallIco);
         if (_imgsMosaicPng.ContainsKey(key))
            return _imgsMosaicPng[key];
         return _imgsMosaicPng[key] = await GetImage("Mosaic/" + (smallIco ? "32x32" : "48x32") + '/' + mosaicType.GetDescription(true) + ".png");
      }

      public static BitmapImage GetImgMosaicPngSync(EMosaic mosaicType, bool smallIco)
      {
         return GetImageSync("Mosaic/" + (smallIco ? "32x32" : "48x32") + '/' + mosaicType.GetDescription(true) + ".png");
      }

      /// <summary> самостоятельная отрисовка </summary>
      public static MosaicsImg GetImgMosaic(EMosaic mosaicType, Size sizeField, int area, Windows.UI.Color bkColor, Size bound) {
         if (_imgsMosaic == null)
            _imgsMosaic = new Dictionary<Tuple<EMosaic, Size, int, Windows.UI.Color, Size>, MosaicsImg>(EMosaicEx.GetValues().Length);
         var key = new Tuple<EMosaic, Size, int, Windows.UI.Color, Size>(mosaicType, sizeField, area, bkColor, bound);
         if (_imgsMosaic.ContainsKey(key))
            return _imgsMosaic[key];
         return _imgsMosaic[key] = new MosaicsImg {
            MosaicType = mosaicType,
            SizeField = sizeField,
            Area = area,
            BackgroundColor = bkColor,
            Bound = bound
         };
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