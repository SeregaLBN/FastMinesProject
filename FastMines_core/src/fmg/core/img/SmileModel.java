package fmg.core.img;

import java.beans.PropertyChangeListener;

import fmg.common.geom.BoundDouble;
import fmg.common.geom.SizeDouble;
import fmg.common.notifier.NotifyPropertyChanged;
import fmg.core.types.Property;

/** Model of the smile/face image */
public class SmileModel implements IImageModel {

    /** @see http://unicode-table.com/blocks/emoticons/
        * <br>  http://unicode-table.com/blocks/miscellaneous-symbols-and-pictographs/
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

    @Property(PROPERTY_FACE_TYPE)
    private EFaceType faceType;

    @Property(PROPERTY_SIZE)
    private SizeDouble size = new SizeDouble(AnimatedImageModel.DEFAULT_IMAGE_SIZE, AnimatedImageModel.DEFAULT_IMAGE_SIZE);

    @Property(PROPERTY_PADDING)
    private BoundDouble padding = new BoundDouble(AnimatedImageModel.DEFAULT_PADDING);

    protected NotifyPropertyChanged notifier = new NotifyPropertyChanged(this);

    public SmileModel(EFaceType faceType) {
        this.faceType = faceType;
    }

    /** width and height in pixel */
    @Override
    public SizeDouble getSize() { return size; }
    @Override
    public void setSize(SizeDouble size) {
        IImageModel.checkSize(size);
        SizeDouble oldSize = this.size;
        if (notifier.setProperty(this.size, size, PROPERTY_SIZE))
            setPadding(IImageModel.recalcPadding(getPadding(), getSize(), oldSize));
    }

    @Override
    public BoundDouble getPadding() { return padding; }
    @Override
    public void setPadding(BoundDouble padding) {
        IImageModel.checkPadding(this, padding);
        notifier.setProperty(this.padding, new BoundDouble(padding), PROPERTY_PADDING);
    }

    public EFaceType getFaceType() {
        return faceType;
    }
    public void setFaceType(EFaceType faceType) {
        notifier.setProperty(this.faceType, faceType, PROPERTY_FACE_TYPE);
    }

    @Override
    public void close() {
        notifier.close();
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        notifier.addListener(listener);
    }
    @Override
    public void removeListener(PropertyChangeListener listener) {
        notifier.removeListener(listener);
    }

}
