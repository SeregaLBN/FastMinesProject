using System;
using System.Globalization;
using System.Collections.Generic;
using System.Threading.Tasks;
using Windows.UI.Xaml.Media.Imaging;
using ua.ksn.win_rt.utils;
using ua.ksn.fmg.model.mosaics;
using ua.ksn.fmg.view.win_rt.res.img;

namespace ua.ksn.fmg.view.win_rt.res {

   /// <summary> Мультимедиа ресурсы программы </summary>
   public static class Resources {

      private static WriteableBitmap imgLogo;

      private static WriteableBitmap imgFlag, imgMine;
      private static WriteableBitmap imgPause;

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

      private static Dictionary<EBtnNewGameState, WriteableBitmap> imgsBtnNew;
      private static Dictionary<EBtnPauseState, WriteableBitmap> imgsBtnPause;
      private static Dictionary<EMosaicGroup, WriteableBitmap> imgsMosaicGroup;
      private static Dictionary<EMosaic, WriteableBitmap> imgsMosaicSmall, imgsMosaicWide;
      private static Dictionary<CultureInfo, WriteableBitmap> imgsLang;

      private static async Task<WriteableBitmap> GetImage(string path) {
         var img = await ImgUtils.GetImage(new Uri("ms-appx:///res/" + path));
         if (img == null)
            img = await ImgUtils.GetImage(new Uri("ms-appx:///" + path));
         return img;
      }

      public static async Task<WriteableBitmap> GetImgLogo() {
         if (imgLogo == null)
            imgLogo = await GetImage("Logo/Logo_128x128.png");
         return imgLogo;
      }

      public static async Task<WriteableBitmap> GetImgFlag(int width, int height) {
         if (imgFlag == null) {
            imgFlag = await GetImage("CellState/Flag.png"); // сначала из ресурсов
            if (imgFlag == null)
               imgFlag = new Flag().Image; // иначе - своя картинка из кода
         }
         return ImgUtils.Zoom(imgFlag, width, height);
      }

      public static async Task<WriteableBitmap> GetImgMine(int width, int height) {
         if (imgMine == null) {
            imgMine = await GetImage("CellState/Mine.png"); // сначала из ресурсов
            if (imgMine == null)
               imgMine = new Mine().Image; // иначе - своя картинка из кода
         }
         return ImgUtils.Zoom(imgMine, width, height);
      }

      public static async Task<WriteableBitmap> GetImgPause() {
         if (imgPause == null) {
            imgPause = await GetImage("Background/Pause.png"); // сначала из ресурсов
            if (imgPause == null)
               imgPause = new BackgroundPause().Image; // иначе - своя картинка из кода
         }
         return imgPause;
      }

      public static async Task<WriteableBitmap> GetImgBtnNew(EBtnNewGameState key) {
         if (imgsBtnNew == null) {
            imgsBtnNew =
               new Dictionary<EBtnNewGameState, WriteableBitmap>(Enum.GetValues(typeof (EBtnNewGameState)).Length);

            foreach (EBtnNewGameState val in Enum.GetValues(typeof (EBtnNewGameState)))
               imgsBtnNew.Add(val, await GetImage("ToolBarButton/new" + val.GetDescription() + ".png"));
         }
         return imgsBtnNew[key];
      }

//	public static WriteableBitmap getImgBtnNew(EBtnNewGameState key, int newWidth, int newHeight) {
//		ImageIcon original = getImgBtnNew(key);
//		if (original == null) return null;
//		return ImgUtils.toIco(original.getImage(), newWidth, newHeight);
//	}

      public static async Task<WriteableBitmap> GetImgBtnPause(EBtnPauseState key) {
         if (imgsBtnPause == null) {
            imgsBtnPause =
               new Dictionary<EBtnPauseState, WriteableBitmap>(Enum.GetValues(typeof (EBtnPauseState)).Length);

            foreach (EBtnPauseState val in Enum.GetValues(typeof (EBtnPauseState)))
               imgsBtnPause.Add(val, await GetImage("ToolBarButton/pause" + val.GetDescription() + ".png"));
         }
         return imgsBtnPause[key];
      }

//	public static WriteableBitmap getImgBtnPause(EPauseState key, int newWidth, int newHeight) {
//		ImageIcon original = getImgBtnPause(key);
//		if (original == null) return null;
//		return ImgUtils.toIco(original.getImage(), newWidth, newHeight);
//	}

      public static async Task<WriteableBitmap> GetImgMosaicGroup(EMosaicGroup key) {
         if (imgsMosaicGroup == null) {
            imgsMosaicGroup = new Dictionary<EMosaicGroup, WriteableBitmap>(Enum.GetValues(typeof (EMosaicGroup)).Length);

            foreach (EMosaicGroup val in Enum.GetValues(typeof (EMosaicGroup)))
               imgsMosaicGroup.Add(val, await GetImage("MosaicGroup/" + val.GetDescription() + ".png"));
         }
         return imgsMosaicGroup[key];
      }

      public static async Task<WriteableBitmap> GetImgMosaicGroup(EMosaicGroup key, int newWidth, int newHeight) {
         var original = await GetImgMosaicGroup(key);
         if (original == null) return null;
         return ImgUtils.Zoom(original, newWidth, newHeight);
      }

      public static async Task<WriteableBitmap> GetImgMosaic(EMosaic key, bool smallIco) {
         Dictionary<EMosaic, WriteableBitmap> imgsMosaic = smallIco ? imgsMosaicSmall : imgsMosaicWide;
         if (imgsMosaic == null) {
            imgsMosaic = new Dictionary<EMosaic, WriteableBitmap>(Enum.GetValues(typeof (EMosaic)).Length);

            foreach (EMosaic val in Enum.GetValues(typeof (EMosaic))) {
               WriteableBitmap imgMosaic =
                  await GetImage("Mosaic/" + (smallIco ? "32x32" : "48x32") + '/' + val.GetDescription(true) + ".png");
                  // сначала из ресурсов
               if (imgMosaic == null) // иначе - своя картинка из кода
                  imgMosaic = new MosaicsImg(val, true).Image;
               imgsMosaic.Add(val, imgMosaic);
            }
         }
         return imgsMosaic[key];
      }

      public static async Task<WriteableBitmap> GetImgMosaic(EMosaic key, bool smallIco, int newWidth, int newHeight) {
         var original = await GetImgMosaic(key, smallIco);
         if (original == null) return null;
         return ImgUtils.Zoom(original, newWidth, newHeight);
      }

      public static async Task<Dictionary<CultureInfo, WriteableBitmap>> getImgsLang() {
         if (imgsLang == null) {
            imgsLang = new Dictionary<CultureInfo, WriteableBitmap>(4);

            WriteableBitmap imgEng = await GetImage("Lang/English.png");
            WriteableBitmap imgUkr = await GetImage("Lang/Ukrainian.png");
            WriteableBitmap imgRus = await GetImage("Lang/Russian.png");

            foreach (var lang in Windows.System.UserProfile.GlobalizationPreferences.Languages) {
               var locale = new CultureInfo(lang);

               if (lang == "EN")
                  imgsLang.Add(locale, imgEng);
               else if ("GBR" == locale.EnglishName)
                  imgsLang.Add(locale, imgEng);
               else if ("UKR" == locale.EnglishName)
                  imgsLang.Add(locale, imgUkr);
               else if ("RUS" == locale.EnglishName)
                  imgsLang.Add(locale, imgRus);
            }
         }
         return imgsLang;
      }
   }
}