using Windows.UI.Xaml.Controls;
using fmg.core.types;
using fmg.data.controller.types;
using FastMines.Presentation;

namespace FastMines {
   /// <summary>
   /// An empty page that can be used on its own or navigated to within a Frame.
   /// </summary>
   public sealed partial class SelectMosaicPage : Page {
      public static EMosaicGroup DefaultMosaicGroup;
      public static ESkillLevel DefaultSkillLevel;

      public SelectMosaicPage() {
         this.InitializeComponent();
         ViewModel = new MosaicsViewModel();

         ViewModel.MosaicsDs.CurrentGroup = DefaultMosaicGroup;
         ViewModel.MosaicsDs.CurrentSkill = DefaultSkillLevel;
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
   }
}
