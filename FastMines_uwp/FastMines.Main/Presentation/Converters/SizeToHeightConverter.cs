using fmg.common.geom;
using System;
using Windows.UI.Xaml.Data;

namespace fmg.common.Converters {

   public sealed class SizeToHeightConverter : IValueConverter {
      public object Convert(object value, Type targetType, object parameter, string language) {
         return System.Convert.ToDouble(((Size)value).Height);
      }

      public object ConvertBack(object value, Type targetType, object parameter, string language) {
         throw new NotImplementedException("Not supported...");
      }
   }

}
