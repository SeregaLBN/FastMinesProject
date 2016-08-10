using Windows.Foundation;
using Windows.UI.Xaml.Controls;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.UI;
using Microsoft.Graphics.Canvas.UI.Xaml;
using fmg.core.types;
using fmg.data.controller.types;
using fmg.common;
using fmg.common.Controls;
using MosaicsImg = fmg.uwp.draw.img.win2d.MosaicsImg<Microsoft.Graphics.Canvas.CanvasBitmap>.CanvasBmp;
using System.Collections.Generic;

namespace fmg {
   /// <summary>
   /// An empty page that can be used on its own or navigated to within a Frame.
   /// </summary>
   public sealed partial class SelectMosaicPage : Page {
      public static EMosaicGroup DefaultMosaicGroup;
      public static ESkillLevel DefaultSkillLevel;

      public SelectMosaicPage() {
         this.InitializeComponent();
         ViewModel = new MosaicsViewModel();

         CurrentMosaicGroup = DefaultMosaicGroup;
         CurrentSkillLevel = DefaultSkillLevel;
      }

      public MosaicsViewModel ViewModel { get; private set; }

      private void Selector_OnSelectionChanged(object sender, SelectionChangedEventArgs e)
      {
         //throw new NotImplementedException();
      }

      private void ListViewBase_OnItemClick(object sender, ItemClickEventArgs e)
      {
         //throw new NotImplementedException();
      }

      public EMosaicGroup CurrentMosaicGroup {
         //get { return ViewModel.MosaicsDs.CurrentGroup; }
         set { ViewModel.MosaicsDs.CurrentGroup = value; }
      }

      public ESkillLevel CurrentSkillLevel {
         //get { return ViewModel.MosaicsDs.CurrentSkill; }
         set { ViewModel.MosaicsDs.CurrentSkill = value; }
      }

      public MosaicTailItem CurrentElement {
         //get { return ViewModel.MosaicsDs.CurrentElement; }
         set { ViewModel.MosaicsDs.CurrentElement = value; }
      }

      private void CanvasControl_DataContextChanged(Windows.UI.Xaml.FrameworkElement sender, Windows.UI.Xaml.DataContextChangedEventArgs ev) {
         if (ev.NewValue == null)
            return;
         var canvasControl = sender as CanvasControl;
         System.Diagnostics.Debug.Assert(ev.NewValue is MosaicsImg);
         if (map.ContainsKey(canvasControl))
            map[canvasControl] = ev.NewValue as MosaicsImg;
         else
            map.Add(canvasControl, ev.NewValue as MosaicsImg);
         canvasControl.Invalidate();
         ev.Handled = true;
      }

      IDictionary<CanvasControl, MosaicsImg> map = new Dictionary<CanvasControl, MosaicsImg>();
      private void CanvasControl_Draw(CanvasControl canvasControl, CanvasDrawEventArgs ev) {
         var img = map[canvasControl];
         ev.DrawingSession.DrawImage(img.Image, new Rect(0, 0, canvasControl.Width, canvasControl.Height));
      }
   }

}
