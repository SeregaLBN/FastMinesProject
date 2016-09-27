using fmg.common.geom;
using System;
using Windows.UI.Xaml.Data;

namespace fmg.common.Converters {

   public sealed class SizeToWidthConverter : IValueConverter {
      public object Convert(object value, Type targetType, object parameter, string language) {
         return System.Convert.ToDouble(((Size)value).Width);
      }

      public object ConvertBack(object value, Type targetType, object parameter, string language) {
         throw new NotImplementedException("Not supported...");
      }
   }

}
