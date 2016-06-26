using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
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
using fmg.common.geom;
using fmg.core.img;
using fmg.core.types;
using fmg.data.controller.types;
using fmg.uwp.draw.mosaic;
using fmg.uwp.draw.mosaic.win2d;
using fmg.uwp.res.img.win2d;
using Microsoft.Graphics.Canvas;
using Rect = Windows.Foundation.Rect;

// The Blank Page item template is documented at http://go.microsoft.com/fwlink/?LinkId=402352&clcid=0x409

namespace Test.FastMines.Uwp.Images.Win2D {

   /// <summary>
   /// An empty page that can be used on its own or navigated to within a Frame.
   /// </summary>
   public sealed partial class DemoPage : Page {

      private readonly Logo _logo;
      private readonly MosaicsSkillImg _msi;
      private readonly MosaicsGroupImg _mgi;
      private readonly MosaicsImg _mi;

      private static readonly Random Rnd = new Random(Guid.NewGuid().GetHashCode());
      private static int R(int max) => Rnd.Next(max);
      private static bool Bl => (R(2) == 1); // random bool
      private static int Np => (Bl ? -1 : +1); // negative or positive

      public DemoPage() {
         this.InitializeComponent();
         this.Unloaded += (sender, args) => {
            _logo.Dispose();
            _msi.Dispose();
            _mgi.Dispose();
            _mi.Dispose();
         };

         _logo = new Logo(canvasControl1);
         _msi = new MosaicsSkillImg(ESkillLevelEx.GetValues()[R(ESkillLevelEx.GetValues().Length)], canvasControl2);
         _mi = new MosaicsImg(EMosaicEx.GetValues()[R(EMosaicEx.GetValues().Length)], new Matrisize(3 + R(4), 4 + R(3)), canvasControl3);
         _mgi = new MosaicsGroupImg(EMosaicGroupEx.GetValues()[R(EMosaicGroupEx.GetValues().Length)], canvasControl4);

         ApplyRandom(_logo, canvasControl1);
         ApplyRandom(_msi , canvasControl2);
         ApplyRandom(_mi, canvasControl3);
         ApplyRandom(_mgi, canvasControl4);
      }

      private void ApplyRandom<T>(RotatedImg<T, CanvasBitmap> img, CanvasControl canvasControl)
      {
         img.Rotate = true;
         img.RotateAngleDelta = (3 + R(5)) * (Bl ? +1 : -1);
         img.RedrawInterval = 50;
         img.BorderWidth = Bl ? 1 : 2;
         img.PropertyChanged += (sender, ev) => {
               if (ev.PropertyName == "Image")
                  canvasControl.Invalidate();
            };

         var logoImg = img as Logo;
         if (logoImg != null) {
            var vals = (Logo.ERotateMode[])Enum.GetValues(typeof(Logo.ERotateMode));
            logoImg.RotateMode = vals[R(vals.Length)];
            logoImg.UseGradient = Bl;
         }

         var mosaicsImg = img as MosaicsImg;
         if (mosaicsImg != null) {
            mosaicsImg.RotateMode = Bl ? MosaicsImg.ERotateMode.FullMatrix : MosaicsImg.ERotateMode.SomeCells;
         }
      }

      void canvasControl_Draw1(CanvasControl sender, CanvasDrawEventArgs args) {
         args.DrawingSession.DrawImage(_logo.Image, new Rect(25, 25, _logo.Width, _logo.Height));
      }
      void canvasControl_Draw2(CanvasControl sender, CanvasDrawEventArgs args) {
         args.DrawingSession.DrawImage(_msi.Image, new Rect(25, 25, _msi.Width, _msi.Height));
      }
      void canvasControl_Draw3(CanvasControl sender, CanvasDrawEventArgs args) {
         args.DrawingSession.DrawImage(_mi.Image, new Rect(25, 25, _mi.Width, _mi.Height));
      }
      void canvasControl_Draw4(CanvasControl sender, CanvasDrawEventArgs args) {
         args.DrawingSession.DrawImage(_mgi.Image, new Rect(25, 25, _mgi.Width, _mgi.Height));
      }

   }

}
