package fmg.swing.res;

import java.awt.Image;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.Icon;

import fmg.common.Color;
import fmg.core.types.EMosaic;
import fmg.core.types.EMosaicGroup;
import fmg.swing.res.img.BackgroundPause;
import fmg.swing.res.img.Flag;
import fmg.swing.res.img.Mine;
import fmg.swing.res.img.MosaicsImg;
import fmg.swing.utils.ImgUtils;

/** Мультимедиа ресурсы программы */
public final class Resources {

    public static final Color DefaultBkColor = new Color(0xFF, 0xFF, 0x8C, 0x00);

   private Image imgLogo;

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
   }

   private Map<EBtnNewGameState, Icon> imgsBtnNew;
   private Map<EBtnPauseState  , Icon> imgsBtnPause;
   private Map<EMosaicGroup    , Icon> imgsMosaicGroup;
   private Map<EMosaic         , Icon> imgsMosaicSmall, imgsMosaicWide;
   private Map<Locale          , Image> imgsLang;

   private Image getImage(String path) {
      Image img = ImgUtils.getImage("res/"+path);
      if (img == null)
         img = ImgUtils.getImage(path);
      return img;
   }
   private Icon getImageIcon(String path) {
      Icon img = ImgUtils.getIcon("res/"+path);
      if (img == null)
         img = ImgUtils.getIcon(path);
      return img;
   }

   public Image getImgLogo() {
      if (imgLogo == null)
         imgLogo = getImage("Logo/Logo_128x128.png");
      return imgLogo;
   }
   public Icon getImgFlag(int width, int height) {
      if (imgFlag == null) {
         imgFlag = getImageIcon("CellState/Flag.png"); // сначала из ресурсов
         if (imgFlag == null)
            // иначе - своя картинка из кода
            imgFlag = ImgUtils.toIco(new Flag());
      }
      return ImgUtils.zoom(imgFlag, width, height);
   }
   public Icon getImgMine(int width, int height) {
      if (imgMine == null) {
         imgMine = getImageIcon("CellState/Mine.png"); // сначала из ресурсов
         if (imgMine == null)
            // иначе - своя картинка из кода
            imgMine = ImgUtils.toIco(new Mine());
      }
      return ImgUtils.zoom(imgMine, width, height);
   }
   public Icon getImgPause() {
      if (imgPause == null) {
         imgPause = getImageIcon("Background/Pause.png"); // сначала из ресурсов
         if (imgPause == null)
            imgPause = ImgUtils.toIco(ImgUtils.toImg(new BackgroundPause())); // иначе - своя картинка из кода
      }
      return imgPause;
   }

   public Icon getImgBtnNew(EBtnNewGameState key) {
      if (imgsBtnNew == null) {
         imgsBtnNew = new HashMap<Resources.EBtnNewGameState, Icon>(EBtnNewGameState.values().length);

         for (EBtnNewGameState val: EBtnNewGameState.values())
            imgsBtnNew.put(val, getImageIcon("ToolBarButton/new" + val.getDescription() + ".png"));
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
         imgsBtnPause = new HashMap<EBtnPauseState, Icon>(EBtnPauseState.values().length);

         for (EBtnPauseState val: EBtnPauseState.values())
            imgsBtnPause.put(val, getImageIcon("ToolBarButton/pause" + val.getDescription() + ".png"));
      }
      return imgsBtnPause.get(key);
   }
//   public Icon getImgBtnPause(EPauseState key, int newWidth, int newHeight) {
//      Icon original = getImgBtnPause(key);
//      if (original == null) return null;
//      return ImgUtils.zoom(original, newWidth, newHeight);
//   }

   public Icon getImgMosaicGroup(EMosaicGroup key) {
      if (imgsMosaicGroup == null) {
         imgsMosaicGroup = new HashMap<EMosaicGroup, Icon>(EMosaicGroup.values().length);

         for (EMosaicGroup val: EMosaicGroup.values())
            imgsMosaicGroup.put(val, getImageIcon("MosaicGroup/" + val.getDescription() + ".png"));
      }
      return imgsMosaicGroup.get(key);
   }
   public Icon getImgMosaicGroup(EMosaicGroup key, int newWidth, int newHeight) {
      Icon original = getImgMosaicGroup(key);
      if (original == null) return null;
      return ImgUtils.zoom(original, newWidth, newHeight);
   }

   public Icon getImgMosaic(EMosaic key, boolean smallIco) {
      if (smallIco) {
         if (imgsMosaicSmall != null)
            return imgsMosaicSmall.get(key);
         imgsMosaicSmall = new HashMap<EMosaic, Icon>(EMosaic.values().length);
      } else {
         if (imgsMosaicWide != null)
            return imgsMosaicWide.get(key);
         imgsMosaicWide = new HashMap<EMosaic, Icon>(EMosaic.values().length);
      }
      Map<EMosaic, Icon> imgsMosaic = smallIco ? imgsMosaicSmall : imgsMosaicWide;

      for (EMosaic mosaicType: EMosaic.values()) {
         Icon imgMosaic = getImageIcon("Mosaic/" + (smallIco ? "32x32" : "48x32") + '/' + mosaicType.getDescription(true)+".png"); // сначала из ресурсов
         if (imgMosaic == null) { // иначе - своя картинка из кода
            MosaicsImg img = new MosaicsImg();
            img.setMosaicType(mosaicType);
            img.setSizeField(mosaicType.sizeIcoField(smallIco));
            //img.setBackgroundColor(java.awt.Color.ORANGE); // test
            imgMosaic = ImgUtils.toIco(ImgUtils.toImg(img));
         }
         imgsMosaic.put(mosaicType, imgMosaic);
      }
      return imgsMosaic.get(key);
   }
   public Icon getImgMosaic(EMosaic key, boolean smallIco, int newWidth, int newHeight) {
      Icon original = getImgMosaic(key, smallIco);
      if (original == null) return null;
      return ImgUtils.zoom(original, newWidth, newHeight);
   }

   public Map<Locale, Image> getImgsLang() {
      if (imgsLang == null) {
         imgsLang = new HashMap<Locale, Image>(4);

         Image imgEng = getImage("Lang/English.png");
         Image imgUkr = getImage("Lang/Ukrainian.png");
         Image imgRus = getImage("Lang/Russian.png");

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
