using Microsoft.Graphics.Canvas;
using fmg.common;
using fmg.core.img;
using LogoView       = fmg.uwp.img.win2d.Logo.CanvasBmpView;
using LogoController = fmg.uwp.img.win2d.Logo.CanvasBmpController;

namespace fmg.DataModel.Items {

    /// <summary> Logo as data model </summary>
    public class LogoDataItem : BaseDataItem<Nothing, LogoModel, LogoView, LogoController> {

        public LogoDataItem()
            : base(null)
        {
            Title = "Mosaics";
        }

        public override LogoController Entity {
            get {
                if (entity == null) {
                    var tmp = new LogoController(CanvasDevice.GetSharedDevice());
                    var m = tmp.Model;
                    m.BorderWidth = 3;
                    m.RotateMode = LogoModel.ERotateMode.Color;
                    Entity = tmp; // call this setter
                }
                return entity;
            }
        }

    }

}
