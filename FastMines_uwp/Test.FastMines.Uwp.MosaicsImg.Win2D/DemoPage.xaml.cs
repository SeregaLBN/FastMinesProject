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

namespace Test.FastMines.Uwp.MosaicsImg.Win2D {

   /// <summary>
   /// An empty page that can be used on its own or navigated to within a Frame.
   /// </summary>
   public sealed partial class DemoPage : Page {

      private readonly MosaicsSkillImg _msi;
      private readonly MosaicsGroupImg _mgi;
      private readonly fmg.uwp.res.img.win2d.MosaicsImg _mi;

      public DemoPage() {
         this.InitializeComponent();
         this.Unloaded += (sender, args) => {
            _mgi.Dispose();
         };

         {
            _msi = new MosaicsSkillImg(ESkillLevel.eCrazy, canvasControl2) {
               Rotate = true
            };
            _msi.PropertyChanged += (sender, ev) => {
               if (ev.PropertyName == "Image")
                  canvasControl2.Invalidate();
            };
         }
         {
            _mi = new fmg.uwp.res.img.win2d.MosaicsImg(EMosaic.eMosaicPentagonT10, new Matrisize(5, 5), canvasControl3) {
               Rotate = true,
               RotateMode = fmg.uwp.res.img.win2d.MosaicsImg.ERotateMode.SomeCells
            };
            _mi.PropertyChanged += (sender, ev) => {
               if (ev.PropertyName == "Image")
                  canvasControl3.Invalidate();
            };
         }
         {
            _mgi = new MosaicsGroupImg(EMosaicGroup.ePentagons, canvasControl4) {
               Rotate = true
            };
            _mgi.PropertyChanged += (sender, ev) => {
               if (ev.PropertyName == "Image")
                  canvasControl4.Invalidate();
            };
         }
      }

      void canvasControl_Draw1(CanvasControl sender, CanvasDrawEventArgs args) {
         using (var logo = new Logo(true, sender)) {
            args.DrawingSession.DrawImage(logo.Image, new Rect(25, 25, logo.Size, logo.Size));
         }
      }

      void canvasControl_Draw2(CanvasControl sender, CanvasDrawEventArgs args) {
         args.DrawingSession.DrawImage(_msi.Image, new Rect(25, 25, _msi.Width, _msi.Height));
      }

      void canvasControl_Draw3(CanvasControl sender, CanvasDrawEventArgs args) {
         args.DrawingSession.DrawImage(_mi.Image, new Rect(25, 25, _msi.Width, _msi.Height));
      }

      void canvasControl_Draw4(CanvasControl sender, CanvasDrawEventArgs args) {
         args.DrawingSession.DrawImage(_mgi.Image, new Rect(25, 25, _mgi.Width, _mgi.Height));
      }

   }

}
