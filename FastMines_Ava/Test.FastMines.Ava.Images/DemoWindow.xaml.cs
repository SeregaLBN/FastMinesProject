using System.Collections.Generic;
using System.Linq;
using Avalonia;
using Avalonia.Controls;
using Avalonia.Input;
using Avalonia.Markup.Xaml;
using Avalonia.Media.Imaging;
using Avalonia.Threading;
using Avalonia.VisualTree;
using fmg.ava.draw.img;

namespace Test.FastMines.Ava.Images
{

   public class DemoWindow : Window
   {
      private class Modelka
      {
         public Modelka(IControl img)
         {
            MosaicImg = new MosaicsGroupImg.CanvasBmp(null, img);
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
