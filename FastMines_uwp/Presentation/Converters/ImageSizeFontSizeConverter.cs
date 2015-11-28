using System;
using Windows.UI.Xaml.Data;

namespace FastMines.Presentation.Converters
{
   /// <summary> text size inscribed ещ image </summary>
   public class ImageSizeFontSizeConverter : IValueConverter
   {
      public object Convert(object value, Type targetType, object parameter, string language)
      {
         return (int)value * 90.0 / 100.0;
      }

      public object ConvertBack(object value, Type targetType, object parameter, string language)
      {
         return (int)value * 100.0 / 90.0;
      }
   }
}
