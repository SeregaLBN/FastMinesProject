using System;
using System.ComponentModel;
using Windows.UI.Xaml.Media;
using fmg.core.types;
using fmg.data.controller.types;
using fmg.uwp.res.img;

namespace FastMines.DataModel.Items {

   /// <summary> Mosaic skill level item for data model </summary>
   public class MosaicSkillDataItem : BaseData<ESkillLevel>, IDisposable {

      public MosaicSkillDataItem(ESkillLevel eSkill) : base(eSkill) {
         Title = eSkill.GetDescription();
      }

      public ESkillLevel SkillLevel => UniqueId;

      public string UnicodeChar => SkillLevel.UnicodeChar();

      public override ImageSource Image => null;

      private int _imageSize = MosaicsGroupImg.DefaultImageSize;
      public override int ImageSize {
         get { return _imageSize; }
         set { }
      }

      private void OnMosaicGroupImagePropertyChanged(object sender, PropertyChangedEventArgs ev) {
         var pn = ev.PropertyName;
         if (pn == "Image") {
            OnPropertyChanged(this, ev); // ! notify parent container
         }
      }

      public void Dispose() {
      }

   }
}