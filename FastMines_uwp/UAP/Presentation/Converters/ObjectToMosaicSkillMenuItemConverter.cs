using System;
using Windows.UI.Xaml.Data;
using FastMines.Presentation.Controls;

namespace FastMines.Presentation.Converters {

   public class ObjectToMosaicSkillMenuItemConverter : IValueConverter {
      public object Convert(object value, Type targetType, object parameter, string language) {
         return value;
      }

      public object ConvertBack(object value, Type targetType, object parameter, string language) {
         return value as MosaicSkillMenuItem;
      }
   }

}
