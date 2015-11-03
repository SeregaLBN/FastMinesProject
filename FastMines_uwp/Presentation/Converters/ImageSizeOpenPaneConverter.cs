using System;
using Windows.UI.Xaml.Data;

namespace FastMines.Presentation.Converters
{
   public class ImageSizeOpenPaneConverter : IValueConverter
   {
      public object Convert(object value, Type targetType, object parameter, string language)
      {
         return System.Convert.ToDouble((int)value + 110);
      }

      public object ConvertBack(object value, Type targetType, object parameter, string language)
      {
         return System.Convert.ToDouble((int)value - 110);
      }
   }
}
