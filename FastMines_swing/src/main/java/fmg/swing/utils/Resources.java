package fmg.swing.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.Icon;

/** Мультимедиа ресурсы программы */
public final class Resources {

    private Map<Locale, Icon> imgsLang;

    private Icon getIcon(String path) {
        Icon img = ImgUtils.getIcon("res/"+path);
        if (img == null)
            img = ImgUtils.getIcon(path);
        return img;
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
