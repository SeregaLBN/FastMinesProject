using System;
using System.ComponentModel;
using Windows.UI.Xaml.Media;
using fmg.core.types;
using fmg.data.controller.types;
using fmg.uwp.res.img;

namespace FastMines.DataModel.Items {

   /// <summary> Mosaic skill level item for data model </summary>
   public class MosaicSkillDataItem : BaseData<ESkillLevel>, IDisposable {
      private const int ZoomKoef = 1;

      public MosaicSkillDataItem(ESkillLevel eSkill) : base(eSkill) {
         Title = eSkill.GetDescription();
      }

      public ESkillLevel SkillLevel => UniqueId;

      public string UnicodeChar => SkillLevel.UnicodeChar().ToString();

      private MosaicsSkillImg _mosaicSkillImg;
      public MosaicsSkillImg MosaicSkillImg
      {
         get
         {
            if (_mosaicSkillImg == null)
            {
               var tmp = MosaicSkillImg = new MosaicsSkillImg(SkillLevel, ImageSize * ZoomKoef)
               {
                  BorderWidth = 3,
                  RotateAngle = new Random(Guid.NewGuid().GetHashCode()).Next(90)
               };
               System.Diagnostics.Debug.Assert(tmp.Size == ImageSize * ZoomKoef);
               System.Diagnostics.Debug.Assert(tmp.Width == ImageSize * ZoomKoef);
               System.Diagnostics.Debug.Assert(tmp.Height == ImageSize * ZoomKoef);
            }
            return _mosaicSkillImg;
         }
         private set
         {
            var old = _mosaicSkillImg;
            if (SetProperty(ref _mosaicSkillImg, value))
            {
               if (old != null)
               {
                  old.PropertyChanged -= OnMosaicGroupImagePropertyChanged;
                  old.Dispose();
               }
               if (value != null)
               {
                  value.PropertyChanged += OnMosaicGroupImagePropertyChanged;
               }
               OnPropertyChanged("Image");
            }
         }
      }

      public override ImageSource Image => null; // MosaicSkillImg.Image;

      private int _imageSize = MosaicsSkillImg.DefaultImageSize;
      public override int ImageSize {
         get { return _imageSize; }
         set { SetProperty(ref _imageSize, value); }
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