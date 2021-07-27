using System.ComponentModel;
using Microsoft.Graphics.Canvas;
using Fmg.Common.Geom;
using Fmg.Core.Types;
using Fmg.Core.Img;
using MosaicModel      = Fmg.Core.Img.MosaicAnimatedModel<Fmg.Common.Nothing>;
using MosaicView       = Fmg.Uwp.Img.Win2d.MosaicImg.CanvasBmpView;
using MosaicController = Fmg.Uwp.Img.Win2d.MosaicImg.CanvasBmpController;

namespace Fmg.Uwp.App.Model.Items {

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
                _notifier.SetProperty(ref _skillLevel, value);
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
                    m.RotateMode = EMosaicRotateMode.someCells;
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
                _notifier.FirePropertyChanged(nameof(this.MosaicType)); // recall with another property name
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
