using System;
using Windows.UI.Xaml.Data;
using fmg.common.geom;

namespace fmg.common.Converters {

   public class ImageSizeWidthOpenPaneConverter : IValueConverter {
      private static int Addition = Main.MenuTextWidth + 12 /* https://msdn.microsoft.com/en-us/library/system.windows.systemparameters.verticalscrollbarwidth.aspx */;

      public object Convert(object value, Type targetType, object parameter, string language) {
         return System.Convert.ToDouble(((Size)value).Width + Addition);
      }

      public object ConvertBack(object value, Type targetType, object parameter, string language) {
         throw new NotImplementedException("Not supported...");
      }
   }
}
