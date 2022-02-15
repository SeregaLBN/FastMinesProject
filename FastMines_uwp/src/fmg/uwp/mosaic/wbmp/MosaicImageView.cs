using System.Linq;
using System.ComponentModel;
using System.Collections.Generic;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Media.Imaging;
using Fmg.Common.Notifier;
using Fmg.Common.Geom;
using Fmg.Core.Mosaic;
using Fmg.Core.Mosaic.Cells;
using Fmg.Uwp.Img.Wbmp;
using Fmg.Uwp.Utils.Wbmp;

namespace Fmg.Uwp.Mosaic.Wbmp {

    /// <summary> MVC: view. UWP <see cref="WriteableBitmap"/> implementation over control <see cref="Image"/> </summary>
    public class MosaicImageView : MosaicView<Image, WriteableBitmap, MosaicDrawModel<WriteableBitmap>> {

        /// <summary> MVC: view. Encapsulating a view through a <see cref="WriteableBitmap"/> </summary>
        class InnerView : MosaicWBmpView<WriteableBitmap, MosaicDrawModel<WriteableBitmap>> {

            private readonly MosaicImageView _owner;

            public InnerView(MosaicImageView self)
                : base(self.Model)
            {
                _owner = self;
            }

            protected override void DrawModified(ICollection<BaseCell> modifiedCells) {
                bool drawBk = (modifiedCells == null) || !modifiedCells.Any();
                DrawWBmp(modifiedCells, drawBk);
            }

        }

        private InnerView _innerView;
        private Image _control;
        private Flag.WBmpController _img = new Flag.WBmpController();
        private Mine.WBmpController _imgMine = new Mine.WBmpController();

        public MosaicImageView()
            : base(new MosaicDrawModel<WriteableBitmap>())
        {
            _innerView = new InnerView(this);
            _innerView.PropertyChanged += OnInnerViewPropertyChanged;
            ChangeSizeImagesMineFlag();
        }

        public WriteableBitmap InnerImage => _innerView.Image;

        protected override Image CreateImage() {
            // will return once created window
            return Control;
        }

        public Image Control {
            get {
                if (_control == null) {
                    _control = new Image {
                        Stretch = Stretch.None
                    };
                }
                return _control;
            }
        }

        public override void Invalidate(ICollection<BaseCell> modifiedCells) {
            base.Invalidate(modifiedCells);
            _innerView.Invalidate(modifiedCells);
        }

        protected override void DrawModified(ICollection<BaseCell> modifiedCells) {
            //var callImplicitDrawIfNeeded = _innerView.Image;
            // none... only the internal Draw method is called
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

        private void OnInnerViewPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            switch (ev.PropertyName) {
            case nameof(InnerView.Image):
                // refire
                if (ev is PropertyChangedExEventArgs<WriteableBitmap> ev2)
                    _notifier.FirePropertyChanged(ev2.OldValue, ev2.NewValue, nameof(InnerImage));
                else
                    _notifier.FirePropertyChanged(nameof(InnerImage));
                break;
            }
        }

        /// <summary> переустанавливаю заного размер мины/флага для мозаики </summary>
        protected void ChangeSizeImagesMineFlag() {
            MosaicDrawModel<WriteableBitmap> model = Model;
            var sq = model.Shape.GetSq(model.PenBorder.Width);
            if (sq <= 0) {
                System.Diagnostics.Debug.WriteLine("Error: too thick pen! There is no area for displaying the flag/mine image...");
                sq = 3; // ат балды...
            }

            const int max = 30;
            if (sq > max) {
                _img.Model.Size = new SizeDouble(sq, sq);
                _imgMine.Model.Size = new SizeDouble(sq, sq);
                model.ImgFlag = _img.Image;
                model.ImgMine = _imgMine.Image;
            } else {
                _img.Model.Size = new SizeDouble(max, max);
                model.ImgFlag = ImgUtils.Zoom(_img.Image, sq, sq);
                _imgMine.Model.Size = new SizeDouble(max, max);
                model.ImgMine = ImgUtils.Zoom(_imgMine.Image, sq, sq);
            }
        }

        protected override void Disposing() {
            _innerView.PropertyChanged -= OnInnerViewPropertyChanged;
            _innerView.Dispose();
            _innerView = null;
            base.Disposing();
            Model.Dispose();
            _control = null;
            _img.Dispose();
            _imgMine.Dispose();
            _img = null;
            _imgMine = null;
        }

    }

}
