using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI.Xaml;

namespace fmg.uwp.mosaic.win2d {

    /// <summary> MVC: controller. UWP Win2D implementation over control <see cref="CanvasSwapChainPanel"/> </summary>
    public class MosaicCanvasSwapChainPanelController : MosaicFrameworkElementController<CanvasSwapChainPanel, CanvasBitmap, MosaicCanvasSwapChainPanelView> {

        public MosaicCanvasSwapChainPanelController(ICanvasResourceCreator resourceCreator, CanvasSwapChainPanel control = null)
            : base(new MosaicCanvasSwapChainPanelView(resourceCreator, control)) { }

        public override CanvasSwapChainPanel Control => View.Control;

        protected override void Disposing() {
            base.Disposing();
            View.Dispose();
        }

    }

}
