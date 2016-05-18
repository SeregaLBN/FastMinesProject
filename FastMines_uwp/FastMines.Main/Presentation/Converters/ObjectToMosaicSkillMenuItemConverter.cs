using System;
using Windows.UI.Xaml.Data;
using fmg.common.Controls;

namespace fmg.common.Converters {

   public class ObjectToMosaicSkillMenuItemConverter : IValueConverter {
      public object Convert(object value, Type targetType, object parameter, string language) {
         return value;
      }

      public object ConvertBack(object value, Type targetType, object parameter, string language) {
         return value as MosaicSkillMenuItem;
      }
   }

}
