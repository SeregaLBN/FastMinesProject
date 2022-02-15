using System.ComponentModel;
using Windows.UI.Xaml;
using Microsoft.Graphics.Canvas;
using Fmg.Common.Geom;
using Fmg.Core.Mosaic;
using Fmg.Uwp.Img.Win2d;

namespace Fmg.Uwp.Mosaic.Win2d {

    /// summary> MVC: view. UWP Win2D implementation. Base implementation View located into control <see cref="Windows.UI.Xaml.FrameworkElement"/> */
    public abstract class MosaicFrameworkElementView<TControl> : MosaicWin2DView<TControl, CanvasBitmap, MosaicDrawModel<CanvasBitmap>>
        where TControl : FrameworkElement
    {
        protected readonly ICanvasResourceCreator _resourceCreator;
        private Mine.CanvasBmpController _imgMine;
        private Flag.CanvasBmpController _imgFlag;

        protected MosaicFrameworkElementView(ICanvasResourceCreator resourceCreator, TControl control = null)
            : base(new MosaicDrawModel<CanvasBitmap>())
        {
            _resourceCreator = resourceCreator;
            Control = control;
            ChangeSizeImagesMineFlag();
        }

        protected override TControl CreateImage() {
            return Control;
        }

        public virtual TControl Control { get; protected set; }

        private Mine.CanvasBmpController ImgMine {
            get {
                if (_imgMine == null)
                    _imgMine = new Mine.CanvasBmpController(_resourceCreator);
                return _imgMine;
            }
        }

        private Flag.CanvasBmpController ImgFlag {
            get {
                if (_imgFlag == null)
                    _imgFlag = new Flag.CanvasBmpController(_resourceCreator);
                return _imgFlag;
            }
        }

        protected override void OnModelPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            base.OnModelPropertyChanged(sender, ev);
            switch (ev.PropertyName) {
            case nameof(MosaicGameModel.MosaicType):
            case nameof(MosaicGameModel.Area):
                ChangeSizeImagesMineFlag();
                break;
            }
        }

        /// <summary> переустанавливаю заного размер мины/флага для мозаики </summary>
        protected void ChangeSizeImagesMineFlag() {
            // PS: картинки не зависят от размера ячейки...
            MosaicDrawModel<CanvasBitmap> model = Model;
            var sq = model.Shape.GetSq(model.PenBorder.Width);
            if (sq <= 0) {
                System.Diagnostics.Debug.Assert(false, "Error: слишком толстое перо! Нет области для вывода картиники флага/мины...");
                sq = 3; // ат балды...
            }
            //model.ImgFlag = null;
            //model.ImgMine = null;

            if (sq >= 50) { // ignore small sizes
                ImgFlag.Model.Size = new SizeDouble(sq, sq);
                ImgMine.Model.Size = new SizeDouble(sq, sq);
            }
            model.ImgFlag = ImgFlag.Image;
            model.ImgMine = ImgMine.Image;
        }

        protected override void Disposing() {
            Control = null; // ! call virtual setter
            _imgFlag?.Dispose();
            _imgMine?.Dispose();
            _imgFlag = null;
            _imgMine = null;
            base.Disposing();
        }

    }

}
