using System.Linq;
using System.ComponentModel;
using System.Collections.Generic;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common.geom;
using fmg.common.notyfier;
using fmg.common.Converters;
using fmg.core.mosaic;
using fmg.core.mosaic.cells;
using fmg.uwp.img.wbmp;
using fmg.uwp.utils.wbmp;

namespace fmg.uwp.mosaic.wbmp {

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

         public override void Draw(IEnumerable<BaseCell> modifiedCells) {
            RectDouble? rc = new RectDouble(0, 0, _owner._control.Width, _owner._control.Height);
            if (double.IsNaN(rc.Value.Width) || double.IsNaN(rc.Value.Height))
               rc = null;
            Draw(modifiedCells, rc, (modifiedCells==null) || !modifiedCells.Any());
         }

      }

      private InnerView _innerView;
      private Image _control;
      private Flag.Controller _imgFlag = new Flag.Controller();
      private Mine.Controller _imgMine = new Mine.Controller();

      public MosaicImageView()
         : base(new MosaicDrawModel<WriteableBitmap>())
      {
         _notifier.DeferredNotifications = true;
         _innerView = new InnerView(this);
         _innerView.PropertyChanged += OnInnerViewPropertyChanged;
         ChangeSizeImagesMineFlag();
      }

      public WriteableBitmap InnerImage => _innerView.Image;

      protected override Image CreateImage() {
         // will return once created window
         return GetControl();
      }

      public Image GetControl() {
         if (_control == null) {
            _control = new Image {
               Stretch = Stretch.None
            };
            _control.SetBinding(Windows.UI.Xaml.Controls.Image.SourceProperty, new Binding {
               Source = this,
               Path = new PropertyPath(nameof(InnerImage)),
               Mode = BindingMode.OneWay
            });
            _control.SetBinding(Windows.UI.Xaml.Controls.Image.WidthProperty, new Binding {
               Source = this,
               Path = new PropertyPath(nameof(Size)),
               Mode = BindingMode.OneWay,
               Converter = new SizeToWidthConverter()
            });
            _control.SetBinding(FrameworkElement.HeightProperty, new Binding {
               Source = this,
               Path = new PropertyPath(nameof(Size)),
               Mode = BindingMode.OneWay,
               Converter = new SizeToHeightConverter()
            });
         }
         return _control;
      }

      public override void Draw(IEnumerable<BaseCell> modifiedCells) {
         var callImplicitDrawIfNeeded = _innerView.Image;
      }

      protected override void OnPropertyModelChanged(object sender, PropertyChangedEventArgs ev) {
         base.OnPropertyModelChanged(sender, ev);
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
               _notifier.OnPropertyChanged(ev2.OldValue, ev2.NewValue, nameof(InnerImage));
            else
               _notifier.OnPropertyChanged(nameof(InnerImage));
            break;
         }
      }

      /** переустанавливаю заного размер мины/флага для мозаики */
      protected void ChangeSizeImagesMineFlag() {
         MosaicDrawModel<WriteableBitmap> model = Model;
         int sq = (int)model.CellAttr.GetSq(model.PenBorder.Width);
         if (sq <= 0) {
            System.Diagnostics.Debug.WriteLine("Error: too thick pen! There is no area for displaying the flag/mine image...");
            sq = 3; // ат балды...
         }

         const int max = 30;
         if (sq > max) {
            _imgFlag.Model.SetSize(sq);
            _imgMine.Model.SetSize(sq);
            model.ImgFlag = _imgFlag.Image;
            model.ImgMine = _imgMine.Image;
         } else {
            _imgFlag.Model.SetSize(max);
            model.ImgFlag = ImgUtils.Zoom(_imgFlag.Image, sq, sq);
            _imgMine.Model.SetSize(max);
            model.ImgMine = ImgUtils.Zoom(_imgMine.Image, sq, sq);
         }
      }

      protected override void Disposing() {
         _innerView.PropertyChanged -= OnInnerViewPropertyChanged;
         _innerView.Dispose();
         Model.Dispose();
         base.Disposing();
         _control = null;
         _imgFlag.Dispose();
         _imgMine.Dispose();
      }

   }

}
