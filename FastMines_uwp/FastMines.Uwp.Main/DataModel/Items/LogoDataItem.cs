using Microsoft.Graphics.Canvas;
using Fmg.Common;
using Fmg.Core.Img;
using LogoView       = Fmg.Uwp.Img.Win2d.Logo.CanvasBmpView;
using LogoController = Fmg.Uwp.Img.Win2d.Logo.CanvasBmpController;

namespace Fmg.DataModel.Items {

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
