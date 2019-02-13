using System.ComponentModel;
using Microsoft.Graphics.Canvas;
using fmg.common.geom;
using fmg.core.types;
using MosaicModel      = fmg.core.img.MosaicAnimatedModel<fmg.common.Nothing>;
using MosaicView       = fmg.uwp.img.win2d.MosaicImg.CanvasBmp;
using MosaicController = fmg.uwp.img.win2d.MosaicImg.ControllerBitmap;

namespace fmg.DataModel.Items {

    /// <summary> Mosaic item for data model </summary>
    public class MosaicDataItem : BaseDataItem<EMosaic, MosaicModel, MosaicView, MosaicController> {

        private ESkillLevel _skillLevel;

        public MosaicDataItem(EMosaic mosaicType)
            : base(mosaicType)
        {
            Title = FixTitle(mosaicType);
        }

        public EMosaic MosaicType {
            get => UniqueId;
            set {  UniqueId = value; }
        }

        public ESkillLevel SkillLevel {
            get { return _skillLevel; }
            set {
                notifier.SetProperty(ref _skillLevel, value);
            }
        }

        public override MosaicController Entity {
            get {
                if (entity == null) {
                    var sizeField = SkillLevel.SizeTileField(MosaicType);
                    var tmp = new MosaicController(CanvasDevice.GetSharedDevice());
                    var m = tmp.Model;
                    m.MosaicType = MosaicType;
                    m.SizeField = sizeField;
                    m.Padding = new BoundDouble(5 * Zoom());
                    m.RotateMode = MosaicModel.ERotateMode.someCells;
                    //m.BackgroundColor = MosaicDrawModelConst.DefaultBkColor;
                    m.PenBorder.Width = 3 * Zoom();
                    //m.RotateAngle = 45 * ThreadLocalRandom.Current.Next(7);

                    //var bmp = tmp.Image;
                    //System.Diagnostics.Debug.Assert(bmp.SizeInPixels.Width  == (int)(Size.Width  * Zoom()));
                    //System.Diagnostics.Debug.Assert(bmp.SizeInPixels.Height == (int)(Size.Height * Zoom()));
                    Entity = tmp; // call this setter
                }
                return entity;
            }
        }

        protected override void OnPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            base.OnPropertyChanged(sender, ev);
            switch(ev.PropertyName) {
            case nameof(this.UniqueId):
                notifier.OnPropertyChanged(nameof(this.MosaicType)); // recall with another property name
                Entity.MosaicType = MosaicType;
                Entity.SizeField = CalcSizeField(SkillLevel);
                Title = FixTitle(MosaicType);
                break;
            case nameof(this.SkillLevel):
                Entity.SizeField = CalcSizeField(SkillLevel);
                break;
            }
        }

        private static string FixTitle(EMosaic mosaicType) {
            return mosaicType.GetDescription(false);//.Replace("-", "\u2006-\u2006");
        }

        private Matrisize CalcSizeField(ESkillLevel skill) {
            return ((skill == ESkillLevel.eCustom)
                        ? ESkillLevel.eBeginner
                        : skill)
                    .SizeTileField(MosaicType);
        }

    }

}
