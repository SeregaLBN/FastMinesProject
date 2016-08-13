package fmg.swing.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.Icon;

import fmg.common.geom.Size;
import fmg.swing.draw.img.BackgroundPause;
import fmg.swing.draw.img.Flag;
import fmg.swing.draw.img.Mine;
import fmg.swing.draw.img.Smile;

/** Мультимедиа ресурсы программы */
public final class Resources {

   private Icon imgFlag, imgMine;
   private Icon imgPause;
   private Map<Locale, Icon> imgsLang;

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

   public Icon getImgSmile(Smile.EType type, int width, int height) {
      Icon ico = getIcon("ToolBarButton/" + type + ".png"); // сначала из ресурсов
      if (ico != null)
         return ImgUtils.zoom(ico, width, height);

      // иначе - своя картинка из кода
      return new Smile(new Size(width, height), type);
   }

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
