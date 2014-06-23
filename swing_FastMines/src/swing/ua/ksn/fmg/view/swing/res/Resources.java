package ua.ksn.fmg.view.swing.res;

import java.awt.Image;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.ImageIcon;

import ua.ksn.fmg.model.mosaics.EMosaic;
import ua.ksn.fmg.model.mosaics.EMosaicGroup;
import ua.ksn.fmg.view.swing.res.img.BackgroundPause;
import ua.ksn.fmg.view.swing.res.img.Flag;
import ua.ksn.fmg.view.swing.res.img.Mine;
import ua.ksn.fmg.view.swing.res.img.MosaicsImg;
import ua.ksn.swing.utils.ImgUtils;

/** Мультимедиа ресурсы программы */
public final class Resources {

	private Image imgLogo;

	private ImageIcon imgFlag, imgMine;	
	private ImageIcon imgPause;

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

	private Map<EBtnNewGameState, ImageIcon> imgsBtnNew;
	private Map<EBtnPauseState  , ImageIcon> imgsBtnPause;
	private Map<EMosaicGroup    , ImageIcon> imgsMosaicGroup;
	private Map<EMosaic         , ImageIcon> imgsMosaicSmall, imgsMosaicWide;
	private Map<Locale          , Image> imgsLang;

	private Image getImage(String path) {
		Image img = ImgUtils.getImage("res/"+path);
		if (img == null)
			img = ImgUtils.getImage(path);
		return img;
	}
	private ImageIcon getImageIcon(String path) {
		ImageIcon img = ImgUtils.getImageIcon("res/"+path);
		if (img == null)
			img = ImgUtils.getImageIcon (path);
		return img;
	}

	public Image getImgLogo() {
		if (imgLogo == null)
			imgLogo = getImage("Logo/Logo_128x128.png");
		return imgLogo;
	}
	public ImageIcon getImgFlag(int width, int height) {
		if (imgFlag == null) {
			imgFlag = getImageIcon("CellState/Flag.png"); // сначала из ресурсов
			if (imgFlag == null)
				imgFlag = ImgUtils.toImgIco(new Flag()); // иначе - своя картинка из кода
		}
		return ImgUtils.zoom(imgFlag, width, height);
	}
	public ImageIcon getImgMine(int width, int height) {
		if (imgMine == null) {
			imgMine = getImageIcon("CellState/Mine.png"); // сначала из ресурсов
			if (imgMine == null)
				imgMine = ImgUtils.toImgIco(new Mine()); // иначе - своя картинка из кода
		}
		return ImgUtils.zoom(imgMine, width, height);
	}
	public ImageIcon getImgPause() {
		if (imgPause == null) {
			imgPause = getImageIcon("Background/Pause.png"); // сначала из ресурсов
			if (imgPause == null)
				imgPause = ImgUtils.toImgIco(ImgUtils.toImg(new BackgroundPause())); // иначе - своя картинка из кода
		}
		return imgPause;
	}

	public ImageIcon getImgBtnNew(EBtnNewGameState key) {
		if (imgsBtnNew == null) {
			imgsBtnNew = new HashMap<Resources.EBtnNewGameState, ImageIcon>(EBtnNewGameState.values().length);

			for (EBtnNewGameState val: EBtnNewGameState.values())
				imgsBtnNew.put(val, getImageIcon("ToolBarButton/new" + val.getDescription() + ".png"));
		}
		return imgsBtnNew.get(key);
	}
//	public ImageIcon getImgBtnNew(EBtnNewGameState key, int newWidth, int newHeight) {
//		ImageIcon original = getImgBtnNew(key);
//		if (original == null) return null;
//		return ImgUtils.toIco(original.getImage(), newWidth, newHeight);
//	}

	public ImageIcon getImgBtnPause(EBtnPauseState key) {
		if (imgsBtnPause == null) {
			imgsBtnPause = new HashMap<EBtnPauseState, ImageIcon>(EBtnPauseState.values().length);

			for (EBtnPauseState val: EBtnPauseState.values())
				imgsBtnPause.put(val, getImageIcon("ToolBarButton/pause" + val.getDescription() + ".png"));
		}
		return imgsBtnPause.get(key);
	}
//	public ImageIcon getImgBtnPause(EPauseState key, int newWidth, int newHeight) {
//		ImageIcon original = getImgBtnPause(key);
//		if (original == null) return null;
//		return ImgUtils.toIco(original.getImage(), newWidth, newHeight);
//	}

	public ImageIcon getImgMosaicGroup(EMosaicGroup key) {
		if (imgsMosaicGroup == null) {
			imgsMosaicGroup = new HashMap<EMosaicGroup, ImageIcon>(EMosaicGroup.values().length);

			for (EMosaicGroup val: EMosaicGroup.values())
				imgsMosaicGroup.put(val, getImageIcon("MosaicGroup/" + val.getDescription() + ".png"));
		}
		return imgsMosaicGroup.get(key);
	}
	public ImageIcon getImgMosaicGroup(EMosaicGroup key, int newWidth, int newHeight) {
		ImageIcon original = getImgMosaicGroup(key);
		if (original == null) return null;
		return ImgUtils.toImgIco(original.getImage(), newWidth, newHeight);
	}

	public ImageIcon getImgMosaic(EMosaic key, boolean smallIco) {
		if (smallIco) {
			if (imgsMosaicSmall != null)
				return imgsMosaicSmall.get(key);
			imgsMosaicSmall = new HashMap<EMosaic, ImageIcon>(EMosaic.values().length);
		} else {
			if (imgsMosaicWide != null)
				return imgsMosaicWide.get(key);
			imgsMosaicWide = new HashMap<EMosaic, ImageIcon>(EMosaic.values().length);
		}
		Map<EMosaic, ImageIcon> imgsMosaic = smallIco ? imgsMosaicSmall : imgsMosaicWide;

		for (EMosaic val: EMosaic.values()) {
			ImageIcon imgMosaic = getImageIcon("Mosaic/" + (smallIco ? "32x32" : "48x32") + '/' + val.getDescription(true)+".png"); // сначала из ресурсов
			if (imgMosaic == null) // иначе - своя картинка из кода
				imgMosaic = ImgUtils.toImgIco(ImgUtils.toImg(new MosaicsImg(val, smallIco)));
			imgsMosaic.put(val, imgMosaic);
		}
		return imgsMosaic.get(key);
	}
	public ImageIcon getImgMosaic(EMosaic key, boolean smallIco, int newWidth, int newHeight) {
		ImageIcon original = getImgMosaic(key, smallIco);
		if (original == null) return null;
		return ImgUtils.toImgIco(original.getImage(), newWidth, newHeight);
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
