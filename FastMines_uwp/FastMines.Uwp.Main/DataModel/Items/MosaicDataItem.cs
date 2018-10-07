using System.ComponentModel;
using Microsoft.Graphics.Canvas;
using fmg.common.geom;
using fmg.core.types;
using MosaicsCanvasBmp = fmg.uwp.draw.img.win2d.MosaicsImg.CanvasBmp;

namespace fmg.DataModel.Items {

    /// <summary> Mosaic item for data model </summary>
    public class MosaicDataItem : BaseData<EMosaic, MosaicsCanvasBmp> {

        public MosaicDataItem(EMosaic mosaicType)
            : base(mosaicType)
        {
            Title = FixTitle(mosaicType);
        }

        public EMosaic MosaicType => UniqueId;

        private ESkillLevel _skillLevel;
        public ESkillLevel SkillLevel {
            get { return _skillLevel; }
            set {
                if (SetProperty(ref _skillLevel, value)) {
                    Image.SizeField = CaclSizeField(value);
                }
            }
        }

        public override int Zoom() { return 2; }

        private MosaicsCanvasBmp _mosaicImg;
        public override MosaicsCanvasBmp Image {
            get {
                if (_mosaicImg == null) {
                    var sizeField = MosaicType.SizeTileField(SkillLevel);
                    var tmp = new MosaicsCanvasBmp(CanvasDevice.GetSharedDevice()) {
                        MosaicType = MosaicType,
                        SizeField = sizeField,
                        PaddingInt = 5 * Zoom(),
                        RotateMode = MosaicsCanvasBmp.ERotateMode.SomeCells,
                        //BackgroundColor = MosaicsCanvasBmp.DefaultBkColor,
                        BorderWidth = 3 * Zoom()//,
                        //RotateAngle = 45 * ThreadLocalRandom.Current.Next(7)
                    };
                    //var bmp = tmp.Image;
                    //System.Diagnostics.Debug.Assert(bmp.PixelWidth == ImageSize * ZoomKoef);
                    //System.Diagnostics.Debug.Assert(bmp.PixelHeight == ImageSize * ZoomKoef);
                    Image = tmp; // call this setter
                }
                return _mosaicImg;
            }
            protected set {
                var old = _mosaicImg;
                if (SetProperty(ref _mosaicImg, value)) {
                    if (old != null) {
                        old.PropertyChanged -= OnImagePropertyChanged;
                        old.Dispose();
                    }
                    if (value != null) {
                        value.PropertyChanged += OnImagePropertyChanged;
                    }
                    OnPropertyChanged(nameof(this.Image));
                }
            }
        }

        protected override void OnPropertyChanged(PropertyChangedEventArgs ev) {
            base.OnPropertyChanged(ev);
            switch(ev.PropertyName) {
            case nameof(this.UniqueId):
                OnPropertyChanged<EMosaic>(ev, nameof(this.MosaicType)); // recall with another property name
                Image.MosaicType = MosaicType;
                Image.SizeField = CaclSizeField(SkillLevel);
                Title = FixTitle(MosaicType);
                break;
            }
        }

        private static string FixTitle(EMosaic mosaicType) {
            return mosaicType.GetDescription(false);//.Replace("-", "\u2006-\u2006");
        }

        private Matrisize CaclSizeField(ESkillLevel skill) {
            return MosaicType.SizeTileField((skill == ESkillLevel.eCustom)
                                                ? ESkillLevel.eBeginner
                                                : skill);
        }

    }

}
