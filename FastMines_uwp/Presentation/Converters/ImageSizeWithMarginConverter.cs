using System;
using Windows.UI.Xaml.Data;

namespace FastMines.Presentation.Converters {
   /// <summary> Add to image size margined 1 pixel left/top  and 1 pixel right/bottom </summary>
   public class ImageSizeWithMarginConverter : IValueConverter {
      private static int Addition = 2;

      public object Convert(object value, Type targetType, object parameter, string language) {
         return System.Convert.ToDouble((int) value + Addition);
      }

      public object ConvertBack(object value, Type targetType, object parameter, string language) {
         return System.Convert.ToDouble((int) value - Addition);
      }
   }
}