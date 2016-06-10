using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.UI;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Microsoft.Graphics.Canvas.UI.Xaml;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;
using fmg.uwp.res.img.win2d;

// The Blank Page item template is documented at http://go.microsoft.com/fwlink/?LinkId=402352&clcid=0x409

namespace Test.FastMines.Uwp.MosaicsImg.Win2D {

   /// <summary>
   /// An empty page that can be used on its own or navigated to within a Frame.
   /// </summary>
   public sealed partial class DemoPage : Page {

      public DemoPage() {
         this.InitializeComponent();
      }

      void canvasControl_Draw(CanvasControl sender, CanvasDrawEventArgs args) {
         //args.DrawingSession.DrawEllipse(155, 115, 80, 30, Colors.Black, 3);
         //args.DrawingSession.DrawText("Hello, world!", 100, 100, Colors.Yellow);
         using (var logo = new Logo(true, sender)) {
            args.DrawingSession.DrawImage(logo.Image, new Rect(25, 25, logo.Size, logo.Size));
         }
      }

   }

}
