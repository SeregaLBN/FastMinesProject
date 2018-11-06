using System.Collections.Generic;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.core.img;
using fmg.core.mosaic;
using fmg.core.mosaic.cells;
using fmg.uwp.mosaic.wbmp;

namespace fmg.uwp.img.wbmp {

    /// <summary>
    /// Representable <see cref="fmg.core.types.EMosaic"/> as image.
    /// UWP implementation over <see cref="WriteableBitmap"/>.
    /// </summary>
    public class MosaicImg : MosaicWBmpView<Nothing, MosaicAnimatedModel<Nothing>> {

        protected bool _useBackgroundColor = true;

        protected MosaicImg()
            : base(new MosaicAnimatedModel<Nothing>())
        { }

        protected override void DrawModified(ICollection<BaseCell> modifiedCells) {
            DrawWBmp(modifiedCells, null, _useBackgroundColor);
        }

        protected override void DrawBody() {
            //base.DrawBody(); // !hide base implementation

            MosaicAnimatedModel<Nothing> model = Model;

            _useBackgroundColor = true;
            switch (model.RotateMode) {
            case MosaicAnimatedModel<Nothing>.ERotateMode.fullMatrix:
                DrawModified(model.Matrix);
                break;
            case MosaicAnimatedModel<Nothing>.ERotateMode.someCells:
                // draw static part
                DrawModified(model.GetNotRotatedCells());

                // draw rotated part
                _useBackgroundColor = false;
                model.GetRotatedCells(rotatedCells => DrawModified(rotatedCells));
                break;
            }
        }

        protected override void Disposing() {
            Model.Dispose();
            base.Disposing();
        }

        /////////////////////////////////////////////////////////////////////////////////////////////////////
        //    custom implementations
        /////////////////////////////////////////////////////////////////////////////////////////////////////

        /// <summary> Smile image controller implementation for <see cref="MosaicImg"/> </summary>
        public class Controller : MosaicImageController<WriteableBitmap, MosaicImg> {

            public Controller()
                : base(new MosaicImg())
            {
                _notifier.DeferredNotifications = !Windows.ApplicationModel.DesignMode.DesignModeEnabled;
            }

            protected override void Disposing() {
                View.Dispose();
                base.Disposing();
            }

        }

    }

}
