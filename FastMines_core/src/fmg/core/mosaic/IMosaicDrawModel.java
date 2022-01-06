package fmg.core.mosaic;

import fmg.common.Color;
import fmg.common.geom.SizeDouble;
import fmg.core.img.IImageModel;
import fmg.core.types.draw.ColorText;
import fmg.core.types.draw.FontInfo;
import fmg.core.types.draw.PenBorder;

/** MVC: draw model of mosaic field.
 * @param <TImageInner> platform specific view/image/picture or other display context/canvas/window/panel
 **/
public interface IMosaicDrawModel<TImageInner> extends IImageModel, IMosaic {

    boolean getAutoFit();
    void setAutoFit(boolean autoFit);

    /** get mosaic size in pixels */
    SizeDouble getMosaicSize();

    /** Offset to mosaic.
     * Определяется Padding'ом  и, дополнительно, смещением к мозаике (т.к. мозаика равномерно вписана в InnerSize) */
    SizeDouble getMosaicOffset();
    /** set offset to mosaic */
    void setMosaicOffset(SizeDouble offset);

    TImageInner getImgMine();
    //void setImgMine(TImageInner img);

    TImageInner getImgFlag();
    //void setImgFlag(TImageInner img);

    ColorText getColorText();
    void setColorText(ColorText colorText);

    PenBorder getPenBorder();
    void setPenBorder(PenBorder penBorder);

    BackgroundFill getBackgroundFill();
    void setBackgroundFill(BackgroundFill backgroundFill);

    FontInfo getFontInfo();
    void setFontInfo(FontInfo fontInfo);

    Color getBackgroundColor();
    void setBackgroundColor(Color color);

    TImageInner getImgBckgrnd();
    //void setImgBckgrnd(TImageInner imgBckgrnd);

}
