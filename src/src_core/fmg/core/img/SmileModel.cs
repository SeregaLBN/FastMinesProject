using fmg.common.geom;
using fmg.common.notyfier;
using System;
using System.ComponentModel;

namespace fmg.core.img {

    /// <summary> Model of the smile/face image </summary>
    public class SmileModel : IImageModel {

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

        private EFaceType _faceType;
        private SizeDouble _size = new SizeDouble(AnimatedImageModelConst.DefaultImageSize, AnimatedImageModelConst.DefaultImageSize);
        private BoundDouble padding = new BoundDouble(AnimatedImageModelConst.DefaultPadding);
        public event PropertyChangedEventHandler PropertyChanged;
        protected readonly NotifyPropertyChanged _notifier;

        public SmileModel(EFaceType faceType) {
            _faceType = faceType;
            _notifier = new NotifyPropertyChanged(this, ev => PropertyChanged?.Invoke(this, ev));
        }

        /// <summary> width and height in pixel </summary>
        public SizeDouble Size {
            get { return _size; }
            set {
                this.CheckSize(value);
                var oldSize = _size;
                if (_notifier.SetProperty(ref _size, value))
                    Padding = this.RecalcPadding(Padding, Size, oldSize);
            }
        }

        public BoundDouble Padding{
            get { return padding; }
            set {
                this.CheckPadding(value);
                _notifier.SetProperty(ref padding, value);
            }
        }
        public EFaceType FaceType {
            get { return _faceType; }
            set { _notifier.SetProperty(ref _faceType, value); }
        }

        public void Dispose() {
            _notifier.Dispose();
            GC.SuppressFinalize(this);
        }

    }

}
