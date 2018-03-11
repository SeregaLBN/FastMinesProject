package fmg.core.img;

import fmg.common.geom.Size;
import fmg.common.notyfier.NotifyPropertyChanged;

/** Model of the smile/face image */
public class SmileModel extends NotifyPropertyChanged implements IImageModel {

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

   private EFaceType _faceType;
   private Size _size;

   public SmileModel(EFaceType faceType) {
      _faceType = faceType;
      _size = new Size(40, 40);
   }

   /** width and height in pixel */
   @Override
   public Size getSize() { return _size; }
   public void setSize(int widht, int height) { setSize(new Size(widht, height)) ; }
   @Override
   public void setSize(Size size) {
      setProperty(_size, size, PROPERTY_SIZE);
   }

   public EFaceType getFaceType() {
      return _faceType;
   }
   public void setFaceType(EFaceType faceType) {
      setProperty(_faceType, faceType, PROPERTY_SIZE);
   }

}
