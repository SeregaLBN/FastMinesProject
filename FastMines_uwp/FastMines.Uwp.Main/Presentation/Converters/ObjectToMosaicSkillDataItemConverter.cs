using System;
using Windows.UI.Xaml.Data;
using fmg.DataModel.Items;

namespace fmg.common.Converters {

   public class ObjectToMosaicSkillDataItemConverter : IValueConverter {
      public object Convert(object value, Type targetType, object parameter, string language) {
         return value;
      }

      public object ConvertBack(object value, Type targetType, object parameter, string language) {
         return value as MosaicSkillDataItem;
      }
   }

}
