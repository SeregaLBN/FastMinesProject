using System;
using Windows.UI.Xaml.Data;

namespace FastMines.Presentation
{
   /// <summary> Add to image size margined 1 pixel left/top  and 1 pixel right/bottom </summary>
   public class ImageSizeWithMarginConverter : IValueConverter
   {
      public object Convert(object value, Type targetType, object parameter, string language)
      {
         return System.Convert.ToDouble((int)value + 2);
      }

      public object ConvertBack(object value, Type targetType, object parameter, string language)
      {
         return System.Convert.ToDouble((int)value - 2);
      }
   }
}
