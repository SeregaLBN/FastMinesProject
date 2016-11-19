using fmg.common.geom;
using System;
using Windows.UI.Xaml.Data;

namespace fmg.common.Converters {

   public sealed class SizeToWidthConverter : IValueConverter {
      public object Convert(object value, Type targetType, object parameter, string language) {
         LoggerSimple.Put($"SizeToWidthConverter::Convert: value={value}, targetType={targetType}, parameter={parameter}, language={language}");
         var size = value as Size?;
         if (size != null)
            return System.Convert.ToDouble(size.Value.Width);
         var sizeD = value as SizeDouble?;
         if (sizeD != null)
            return sizeD.Value.Width;
         throw new NotImplementedException("Not supported...");
      }

      public object ConvertBack(object value, Type targetType, object parameter, string language) {
         throw new NotImplementedException("Not supported...");
      }
   }

}
