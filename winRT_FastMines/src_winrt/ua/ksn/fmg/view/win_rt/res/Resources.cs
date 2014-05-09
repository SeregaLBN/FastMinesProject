using System;
using System.Linq;
using System.Globalization;
using System.Collections.Generic;
using System.Runtime.InteropServices.WindowsRuntime;
using System.Threading.Tasks;
using Windows.UI.Core;
using Windows.UI.Xaml.Media.Imaging;
using FastMines.Common;
using ua.ksn.win_rt.utils;
using ua.ksn.fmg.model.mosaics;
using ua.ksn.fmg.view.win_rt.res.img;

namespace ua.ksn.fmg.view.win_rt.res {

   /// <summary> Мультимедиа ресурсы программы </summary>
   public static class Resources {

      private static WriteableBitmap _imgLogo;

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
      private static Dictionary<EMosaicGroup, WriteableBitmap> _imgsMosaicGroup;
      private static Dictionary<EMosaic, WriteableBitmap> _imgsMosaicSmall, _imgsMosaicWide;
      private static Dictionary<CultureInfo, WriteableBitmap> _imgsLang;

      private static async Task<WriteableBitmap> GetImage(string path) {
         var img = await ImgUtils.GetImage(new Uri("ms-appx:///res/" + path));
         if (img == null)
            img = await ImgUtils.GetImage(new Uri("ms-appx:///" + path));
         return img;
      }

      public static async Task<WriteableBitmap> GetImgLogo() {
         if (_imgLogo == null)
            _imgLogo = await GetImage("Logo/Logo_128x128.png");
         return _imgLogo;
      }

      public static async Task<WriteableBitmap> GetImgFlag(int width, int height) {
         if (_imgFlag == null) {
            _imgFlag = await GetImage("CellState/Flag.png"); // сначала из ресурсов
            if (_imgFlag == null)
               _imgFlag = new Flag().Image; // иначе - своя картинка из кода
         }
         return ImgUtils.Zoom(_imgFlag, width, height);
      }

      public static async Task<WriteableBitmap> GetImgMine(int width, int height) {
         if (_imgMine == null) {
            _imgMine = await GetImage("CellState/Mine.png"); // сначала из ресурсов
            if (_imgMine == null)
               _imgMine = new Mine().Image; // иначе - своя картинка из кода
         }
         return ImgUtils.Zoom(_imgMine, width, height);
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
            _imgsBtnNew =
               new Dictionary<EBtnNewGameState, WriteableBitmap>(Enum.GetValues(typeof (EBtnNewGameState)).Length);

            foreach (EBtnNewGameState val in Enum.GetValues(typeof (EBtnNewGameState)))
               _imgsBtnNew.Add(val, await GetImage("ToolBarButton/new" + val.GetDescription() + ".png"));
         }
         return _imgsBtnNew[key];
      }

//	public static WriteableBitmap getImgBtnNew(EBtnNewGameState key, int newWidth, int newHeight) {
//		ImageIcon original = getImgBtnNew(key);
//		if (original == null) return null;
//		return ImgUtils.toIco(original.getImage(), newWidth, newHeight);
//	}

      public static async Task<WriteableBitmap> GetImgBtnPause(EBtnPauseState key) {
         if (_imgsBtnPause == null) {
            _imgsBtnPause =
               new Dictionary<EBtnPauseState, WriteableBitmap>(Enum.GetValues(typeof (EBtnPauseState)).Length);

            foreach (EBtnPauseState val in Enum.GetValues(typeof (EBtnPauseState)))
               _imgsBtnPause.Add(val, await GetImage("ToolBarButton/pause" + val.GetDescription() + ".png"));
         }
         return _imgsBtnPause[key];
      }

//	public static WriteableBitmap getImgBtnPause(EPauseState key, int newWidth, int newHeight) {
//		ImageIcon original = getImgBtnPause(key);
//		if (original == null) return null;
//		return ImgUtils.toIco(original.getImage(), newWidth, newHeight);
//	}

      public static async Task<WriteableBitmap> GetImgMosaicGroup(EMosaicGroup key) {
         if (_imgsMosaicGroup == null) {
            _imgsMosaicGroup = new Dictionary<EMosaicGroup, WriteableBitmap>(Enum.GetValues(typeof(EMosaicGroup)).Length);

            foreach (EMosaicGroup val in Enum.GetValues(typeof (EMosaicGroup)))
               _imgsMosaicGroup.Add(val, await GetImage("MosaicGroup/" + val.GetDescription() + ".png"));
         }
         return _imgsMosaicGroup[key];
      }

      public static async Task<WriteableBitmap> GetImgMosaicGroup(EMosaicGroup key, int newWidth, int newHeight) {
         var original = await GetImgMosaicGroup(key);
         if (original == null) return null;
         return ImgUtils.Zoom(original, newWidth, newHeight);
      }

      public static WriteableBitmap GetImgMosaic(EMosaic key, bool smallIco, Action<WriteableBitmap> imageSetter) {
         if (smallIco) {
            if (_imgsMosaicSmall == null)
               _imgsMosaicSmall = new Dictionary<EMosaic, WriteableBitmap>(Enum.GetValues(typeof (EMosaic)).Length);
         } else {
            if (_imgsMosaicWide == null)
               _imgsMosaicWide = new Dictionary<EMosaic, WriteableBitmap>(Enum.GetValues(typeof (EMosaic)).Length);
         }
         var imgsMosaic = smallIco ? _imgsMosaicSmall : _imgsMosaicWide;

         if (imgsMosaic.ContainsKey(key))
            return imgsMosaic[key];

         try {
            // 1. Сразу отадаю пустую картинку (1x1)
            var img = new WriteableBitmap(1, 1);
            imgsMosaic.Add(key, img);
            return img;
         } finally {
            // 2. и начинаю грузить картинку с файла...
            AsyncRunner.InvokeLater(async () => {
               imageSetter(await GetImage("Mosaic/" + (smallIco ? "32x32" : "48x32") + '/' + key.GetDescription(true) + ".png")); // сначала из ресурсов

               // 3. ... а потом  -  и самостоятельная отрисовка
               AsyncRunner.InvokeLater(async () => imageSetter(await new MosaicsImg(key, smallIco).GetImage()), CoreDispatcherPriority.Low); // своя картинка из кода
            }, CoreDispatcherPriority.High);
         }
      }

      //public static async Task<WriteableBitmap> GetImgMosaic(EMosaic key, bool smallIco, int newWidth, int newHeight) {
      //   var original = await GetImgMosaic(key, smallIco);
      //   if (original == null)
      //      return null;
      //   return ImgUtils.Zoom(original, newWidth, newHeight);
      //}

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
   }
}