package fmg.core.img;

import static fmg.core.img.PropertyConst.PROPERTY_PADDING;
import static fmg.core.img.PropertyConst.PROPERTY_SIZE;

import java.util.function.Consumer;

import fmg.common.geom.BoundDouble;
import fmg.common.geom.SizeDouble;

/** Model of the smile/face image */
public class SmileModel2 implements IImageModel2 {

    /** @see <a href="https://unicode-table.com/blocks/emoticons/">Emoticons (Emoji)</a>
        * <br>  <a href="https://unicode-table.com/blocks/miscellaneous-symbols-and-pictographs/">Miscellaneous Symbols and Pictographs</a>
        */
    public enum EFaceType {
        /** :) ☺ -  White Smiling Face (Незакрашенное улыбающееся лицо) U+263A */
        Face_WhiteSmiling,

        /** :( 😞 - Disappointed Face (Разочарованное лицо) U+1F61E */
        Face_Disappointed,

        /** 😀 - Grinning Face (Ухмыляющееся лицо) U+1F600 */
        Face_Grinning,

        /** 😎 - Smiling Face with Sunglasses (Улыбающееся лицо в солнечных очках) U+1F60E */
        Face_SmilingWithSunglasses,

        /** 😋 - Face Savouring Delicious Food (Лицо, смакующее деликатес) U+1F60B */
        Face_SavouringDeliciousFood,


        /** like as Professor: 🎓 - Graduation Cap (Выпускная шапочка) U+1F393 */
        Face_Assistant,

        /** 👀 - Eyes (Глаза) U+1F440 */
        Eyes_OpenDisabled,

        Eyes_ClosedDisabled,

        Face_EyesOpen,

        Face_WinkingEyeLeft,
        Face_WinkingEyeRight,

        Face_EyesClosed
    }

    public static final String PROPERTY_FACE_TYPE = "FaceType";

    private EFaceType faceType;
    private final SizeDouble size = new SizeDouble(ImageHelper.DEFAULT_IMAGE_SIZE, ImageHelper.DEFAULT_IMAGE_SIZE);
    private final BoundDouble pad = new BoundDouble(ImageHelper.DEFAULT_PADDING);
    private Consumer<String> changedCallback;

    public SmileModel2(EFaceType faceType) {
        this.faceType = faceType;
    }

    @Override
    public SizeDouble getSize() {
        return size;
    }

    @Override
    public void setSize(SizeDouble size) {
        if (this.size.equals(size))
            return;

        ImageHelper.checkSize(size);

        SizeDouble oldSize = new SizeDouble(this.size);
        this.size.width  = size.width;
        this.size.height = size.height;

        firePropertyChanged(PROPERTY_SIZE);

        setPadding(ImageHelper.recalcPadding(pad, size, oldSize));
    }

    @Override
    public BoundDouble getPadding() {
        return pad;
    }

    @Override
    public void setPadding(BoundDouble padding) {
        if (pad.equals(padding))
            return;

        ImageHelper.checkPadding(size, padding);

        this.pad.left   = padding.left;
        this.pad.right  = padding.right;
        this.pad.top    = padding.top;
        this.pad.bottom = padding.bottom;

        firePropertyChanged(PROPERTY_PADDING);
    }

    public EFaceType getFaceType() {
        return faceType;
    }
    public void setFaceType(EFaceType faceType) {
        if (this.faceType == faceType)
            return;

        this.faceType = faceType;

        firePropertyChanged(PROPERTY_FACE_TYPE);
    }

    @Override
    public void setListener(Consumer<String> callback) {
        if ((callback != null) && (changedCallback != null))
            throw new IllegalArgumentException("Can only set the controller once");
        changedCallback = callback;
    }

    private void firePropertyChanged(String propertyName) {
        if (changedCallback != null)
            changedCallback.accept(propertyName);
    }

}
