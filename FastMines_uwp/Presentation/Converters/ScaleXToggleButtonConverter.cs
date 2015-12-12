using System;
using Windows.UI.Xaml.Data;

namespace FastMines.Presentation.Converters
{
   public class ScaleXToggleButtonConverter : IValueConverter
   {
      public object Convert(object value, Type targetType, object parameter, string language)
      {
         return (int)value / (48-0.5); // 48 - see ToggleButtonExtStyle
      }

      public object ConvertBack(object value, Type targetType, object parameter, string language)
      {
         throw new NotImplementedException();
      }
   }
}
