using System;
using Windows.UI.Xaml.Data;

namespace fmg.common.Converters {

   public class ImageSizeOpenPaneConverter : IValueConverter {
      private static int Addition = Shell.MenuTextWidth + 12 /* https://msdn.microsoft.com/en-us/library/system.windows.systemparameters.verticalscrollbarwidth.aspx */;

      public object Convert(object value, Type targetType, object parameter, string language) {
         return System.Convert.ToDouble((int) value + Addition);
      }

      public object ConvertBack(object value, Type targetType, object parameter, string language) {
         return System.Convert.ToDouble((int) value - Addition);
      }
   }
}
