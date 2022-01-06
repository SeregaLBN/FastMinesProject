using System;
using System.ComponentModel;
using Microsoft.Graphics.Canvas;
using Fmg.Common;
using Fmg.Common.Notifier;
using Fmg.Common.Geom;
using Fmg.Core.Types;
using Fmg.Core.Img;
using MosaicSkillView       = Fmg.Uwp.Img.Win2d.MosaicSkillImg.CanvasBmpView;
using MosaicSkillController = Fmg.Uwp.Img.Win2d.MosaicSkillImg.CanvasBmpController;

namespace Fmg.Uwp.App.Model.Items {

    /// <summary> Mosaic skill level item for data model </summary>
    public class MosaicSkillDataItem : BaseDataItem<ESkillLevel?, MosaicSkillModel, MosaicSkillView, MosaicSkillController> {

        public MosaicSkillDataItem(ESkillLevel? eSkill)
            : base(eSkill)
        {
            Title = eSkill?.GetDescription();
        }

        public ESkillLevel? SkillLevel => UniqueId;

        [Obsolete]
        public string UnicodeChar => SkillLevel?.UnicodeChar().ToString();

        public override MosaicSkillController Entity {
            get {
                if (entity == null) {
                    var tmp = new MosaicSkillController(SkillLevel, CanvasDevice.GetSharedDevice());
                    var m = tmp.Model;
                    m.BorderWidth = 2;
                    m.RotateAngle = ThreadLocalRandom.Current.Next(90);
                    tmp.BurgerMenuModel.PropertyChanged += OnBurgerMenuModelPropertyChanged;
                    Entity = tmp; // call this setter
                }
                return entity;
            }
        }

        public BoundDouble PaddingBurgerMenu {
            get {
                var pad = Entity.BurgerMenuModel.Padding;
                var zoom = Zoom();
                return new BoundDouble(pad.Left / zoom, pad.Top / zoom, pad.Right / zoom, pad.Bottom / zoom);
            }
            set {
                Entity.BurgerMenuModel.Padding = ZoomPadding(value);
            }
        }

        protected override void OnPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            base.OnPropertyChanged(sender, ev);

            switch(ev.PropertyName) {
            case nameof(this.UniqueId):
                _notifier.FirePropertyChanged(nameof(this.SkillLevel)); // recall with another property name
                break;
            }
        }

        protected void OnBurgerMenuModelPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            switch (ev.PropertyName) {
            case nameof(BurgerMenuModel.Padding):
                if (ev is PropertyChangedExEventArgs<BoundDouble> evx)
                    _notifier.FirePropertyChanged(ZoomPadding(evx.OldValue), ZoomPadding(evx.NewValue), nameof(this.PaddingBurgerMenu));
                else
                    _notifier.FirePropertyChanged(nameof(this.PaddingBurgerMenu));
                break;
            }
        }

        protected override void Disposing() {
            Entity.BurgerMenuModel.PropertyChanged -= OnBurgerMenuModelPropertyChanged;
            base.Disposing();
        }

    }

}
