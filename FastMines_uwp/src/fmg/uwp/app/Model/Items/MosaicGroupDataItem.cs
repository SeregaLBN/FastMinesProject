using System.ComponentModel;
using Microsoft.Graphics.Canvas;
using Fmg.Common;
using Fmg.Common.Notifier;
using Fmg.Common.Geom;
using Fmg.Core.Types;
using Fmg.Core.Img;
using MosaicGroupView       = Fmg.Uwp.Img.Win2d.MosaicGroupImg.CanvasBmpView;
using MosaicGroupController = Fmg.Uwp.Img.Win2d.MosaicGroupImg.CanvasBmpController;

namespace Fmg.Uwp.App.Model.Items {

    /// <summary> Mosaic group item for data model </summary>
    public class MosaicGroupDataItem : BaseDataItem<EMosaicGroup?, MosaicGroupModel, MosaicGroupView, MosaicGroupController> {

        public MosaicGroupDataItem(EMosaicGroup? eMosaicGroup)
            : base(eMosaicGroup)
        {
            Title = eMosaicGroup?.GetDescription();
        }

        public EMosaicGroup? MosaicGroup {
            get => UniqueId;
            set { UniqueId = value; }
        }

        public override MosaicGroupController Entity {
            get {
                if (entity == null) {
                    var tmp = new MosaicGroupController(MosaicGroup, CanvasDevice.GetSharedDevice());
                    var m = tmp.Model;
                    m.BorderWidth = 3;
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
                _notifier.FirePropertyChanged(nameof(this.MosaicGroup)); // recall with another property name
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
