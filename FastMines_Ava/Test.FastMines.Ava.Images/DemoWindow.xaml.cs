using System.Collections.Generic;
using System.Linq;
using Avalonia;
using Avalonia.Controls;
using Avalonia.Input;
using Avalonia.Markup.Xaml;
using Avalonia.Media.Imaging;
using Avalonia.Threading;
using Avalonia.VisualTree;
using fmg.common.notyfier;
using fmg.ava.draw.img;

namespace Test.FastMines.Ava.Images
{

   public class DemoWindow : Window
   {
      private class Modelka : NotifyPropertyChanged
      {
         public Modelka(IControl img)
         {
            var mosaicImg = new MosaicsGroupImg.CanvasBmp(null, img)
            {
               RotateAngleDelta = 300,
               Rotate = true,
               PolarLights = true
            };
            MosaicImg = mosaicImg;

            mosaicImg.PropertyChanged += (sender, ev) => {
               if (ev.PropertyName == nameof(MosaicImg.Image)) {
                  this.OnSelfPropertyChanged(nameof(Modelka.Bitmap));
                  Dispatcher.UIThread.InvokeTaskAsync(() => img.InvalidateVisual());//.Wait()
               }
            };
         }

         public MosaicsGroupImg.CanvasBmp MosaicImg { get; }
         public IBitmap Bitmap { get => MosaicImg.Image; }
      }

      private Modelka _viewModel;
      // private IControl _img;

      public DemoWindow()
      {
         InitializeComponent();
         this.AttachDevTools();

         DataContext = _viewModel;
      }

      private void InitializeComponent()
      {
         AvaloniaXamlLoaderPortableXaml.Load(this);

         IControl img = ((StackPanel)Content).Children.First();

         _viewModel = new Modelka(img);
      }

   }
}
