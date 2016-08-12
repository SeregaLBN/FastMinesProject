package fmg.swing.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.Icon;

import fmg.swing.draw.img.BackgroundPause;
import fmg.swing.draw.img.Flag;
import fmg.swing.draw.img.Mine;
import fmg.swing.draw.img.Smile;
import fmg.swing.draw.img.Smile.EType;

/** Мультимедиа ресурсы программы */
public final class Resources {

   private Icon imgFlag, imgMine;
   private Icon imgPause;

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
      eNormalLoss;

      public String getDescription() {
         return this.toString().substring(1);
      }

      public Smile.EType mapToSmileType() {
         switch (this) {
         case eNormal          : return EType.Face_WhiteSmiling;
         case ePressed         : return EType.Face_SavouringDeliciousFood; // +1,1
         case eSelected        : return null;
         case eDisabled        : return null;
         case eDisabledSelected: return null;
         case eRollover        : return EType.Face_WhiteSmiling; // +1,1
         case eRolloverSelected: return null;
         case eNormalMosaic    : return EType.Face_Grinning;
         case eNormalWin       : return EType.Face_SmilingWithSunglasses;
         case eNormalLoss      : return EType.Face_Disappointed;
         }
         throw new RuntimeException("Map me...");
      }
   }
   public enum EBtnPauseState {
      eNormal,
      ePressed,
      eSelected,
      eDisabled,
      eDisabledSelected,
      eRollover,
      eRolloverSelected,

      /** типа ход ассистента - задел на будущее */
      eAssistant;

      public String getDescription() {
         return this.toString().substring(1);
      }

      public Smile.EType mapToSmileType() {
         switch (this) {
         case eNormal          : return EType.Face_EyesOpen;
         case ePressed         : return EType.Face_WinkingEyeLeft; // +1,1
         case eSelected        : return EType.Face_EyesClosed;
         case eDisabled        : return EType.Eyes_OpenDisabled;
         case eDisabledSelected: return EType.Eyes_ClosedDisabled;
         case eRollover        : return EType.Face_EyesOpen; // +1,1
         case eRolloverSelected: return EType.Face_WinkingEyeLeft;
         case eAssistant       : return EType.Face_Assistant;
         }
         throw new RuntimeException("Map me...");
      }
   }

   private Map<EBtnNewGameState, Icon> imgsBtnNew;
   private Map<EBtnPauseState  , Icon> imgsBtnPause;
   private Map<Locale          , Icon> imgsLang;

   private Icon getIcon(String path) {
      Icon img = ImgUtils.getIcon("res/"+path);
      if (img == null)
         img = ImgUtils.getIcon(path);
      return img;
   }

   public Icon getImgFlag(int width, int height) {
      if (imgFlag == null) {
         imgFlag = getIcon("CellState/Flag.png"); // сначала из ресурсов
         if (imgFlag == null)
            // иначе - своя картинка из кода
            imgFlag = new Flag();
      }
      return ImgUtils.zoom(imgFlag, width, height);
   }
   public Icon getImgMine(int width, int height) {
      if (imgMine == null) {
         imgMine = getIcon("CellState/Mine.png"); // сначала из ресурсов
         if (imgMine == null)
            // иначе - своя картинка из кода
            imgMine = new Mine();
      }
      return ImgUtils.zoom(imgMine, width, height);
   }
   public Icon getImgPause() {
      if (imgPause == null) {
         imgPause = getIcon("Background/Pause.png"); // сначала из ресурсов
         if (imgPause == null)
            imgPause = ImgUtils.toIco(ImgUtils.toImg(new BackgroundPause())); // иначе - своя картинка из кода
      }
      return imgPause;
   }

   public Icon getImgBtnNew(EBtnNewGameState key) {
      if (imgsBtnNew == null) {
         imgsBtnNew = new HashMap<>(EBtnNewGameState.values().length);

         for (EBtnNewGameState val: EBtnNewGameState.values()) {
            Icon ico = getIcon("ToolBarButton/new" + val.getDescription() + ".png"); // сначала из ресурсов
            if (ico == null) {
               // иначе - своя картинка из кода
               Smile.EType type = val.mapToSmileType();
               if (type != null) {
                  ico = ImgUtils.zoom(new Smile(300, type), 24, 24);
               }
            }
            imgsBtnNew.put(val, ico);
         }
      }
      return imgsBtnNew.get(key);
   }
//   public Icon getImgBtnNew(EBtnNewGameState key, int newWidth, int newHeight) {
//      Icon original = getImgBtnNew(key);
//      if (original == null) return null;
//      return ImgUtils.zoom(original, newWidth, newHeight);
//   }

   public Icon getImgBtnPause(EBtnPauseState key) {
      if (imgsBtnPause == null) {
         imgsBtnPause = new HashMap<>(EBtnPauseState.values().length);

         for (EBtnPauseState val: EBtnPauseState.values()) {
            Icon ico = getIcon("ToolBarButton/pause" + val.getDescription() + ".png"); // сначала из ресурсов
            if (ico == null) {
               // иначе - своя картинка из кода
               Smile.EType type = val.mapToSmileType();
               if (type != null) {
                  ico = ImgUtils.zoom(new Smile(300, type), 24, 24);
               }
            }
            imgsBtnPause.put(val, ico);
         }
      }
      return imgsBtnPause.get(key);
   }
//   public Icon getImgBtnPause(EPauseState key, int newWidth, int newHeight) {
//      Icon original = getImgBtnPause(key);
//      if (original == null) return null;
//      return ImgUtils.zoom(original, newWidth, newHeight);
//   }

   public Map<Locale, Icon> getImgsLang() {
      if (imgsLang == null) {
         imgsLang = new HashMap<>(4);

         Icon imgEng = getIcon("Lang/English.png");
         Icon imgUkr = getIcon("Lang/Ukrainian.png");
         Icon imgRus = getIcon("Lang/Russian.png");

         for (Locale locale: Locale.getAvailableLocales()) {
            if (locale.equals(Locale.ENGLISH))
               imgsLang.put(locale, imgEng);
            else
            if ("GBR".equals(locale.getISO3Country()))
               imgsLang.put(locale, imgEng);
            else
            if ("UKR".equals(locale.getISO3Country()))
               imgsLang.put(locale, imgUkr);
            else
            if ("RUS".equals(locale.getISO3Country()))
               imgsLang.put(locale, imgRus);
         }
      }
      return imgsLang;
   }
}
