using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;

namespace fmg.uwp.mosaic.win2d {

    /// <summary> MVC: controller. UWP Win2D implementation over control <see cref="CanvasVirtualControl"/> </summary>
    public class MosaicCanvasVirtualControlController : MosaicFrameworkElementController<CanvasVirtualControl, CanvasBitmap, MosaicCanvasVirtualControlView> {

        public MosaicCanvasVirtualControlController(ICanvasResourceCreator resourceCreator, CanvasVirtualControl control = null)
            : base(new MosaicCanvasVirtualControlView(resourceCreator, control))
        { }

        public override CanvasVirtualControl Control => View.Control;

        public void OnRegionsInvalidated(CanvasVirtualControl sender, CanvasRegionsInvalidatedEventArgs ev) {
            View.OnRegionsInvalidated(sender, ev);
        }

        protected override void Disposing() {
            base.Disposing();
            View.Dispose();
        }

    }

}
