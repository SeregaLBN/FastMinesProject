using System.Collections.Generic;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.core.img;
using fmg.core.mosaic;
using fmg.core.mosaic.cells;
using fmg.uwp.mosaic.wbmp;

namespace fmg.uwp.img.wbmp {

    /// <summary> Representable <see cref="fmg.core.types.EMosaic"/> as image </summary>
    public static class MosaicImg {

        /////////////////////////////////////////////////////////////////////////////////////////////////////
        //    custom implementations
        /////////////////////////////////////////////////////////////////////////////////////////////////////

        /// <summary>
        /// Representable <see cref="fmg.core.types.EMosaic"/> as image view.
        /// UWP implementation over <see cref="WriteableBitmap"/>.
        /// </summary>
        public class WBmpView : MosaicWBmpView<Nothing, MosaicAnimatedModel<Nothing>> {

            protected bool _useBackgroundColor = true;

            public WBmpView()
                : base(new MosaicAnimatedModel<Nothing>())
            { }

            protected override void DrawModified(ICollection<BaseCell> modifiedCells) {
                DrawWBmp(modifiedCells, _useBackgroundColor);
            }

            protected override void DrawBody() {
                //base.DrawBody(); // !hide base implementation

                MosaicAnimatedModel<Nothing> model = Model;

                _useBackgroundColor = true;
                switch (model.RotateMode) {
                case EMosaicRotateMode.fullMatrix:
                    DrawModified(model.Matrix);
                    break;
                case EMosaicRotateMode.someCells:
                    // draw static part
                    DrawModified(model.GetNotRotatedCells());

                    // draw rotated part
                    _useBackgroundColor = false;
                    model.GetRotatedCells(rotatedCells => DrawModified(rotatedCells));
                    break;
                }
            }

            protected override void Disposing() {
                base.Disposing();
                Model.Dispose();
            }

        }

        /// <summary> Smile image controller implementation for <see cref="WBmpView"/> </summary>
        public class WBmpController : MosaicImageController<WriteableBitmap, WBmpView> {

            public WBmpController()
                : base(new WBmpView())
            { }

            protected override void Disposing() {
                base.Disposing();
                View.Dispose();
            }

        }

    }

}
