using System;
using Windows.UI.Xaml.Data;
using Fmg.Common.Geom;

namespace Fmg.Uwp.App.Presentation.Converters {

    public class ImageSizeWidthOpenPaneConverter : IValueConverter {

        private static int Addition = MainPage.MenuTextWidth + 12 /* https://msdn.microsoft.com/en-us/library/system.windows.systemparameters.verticalscrollbarwidth.aspx */;

        public object Convert(object value, Type targetType, object parameter, string language) {
            if (value is SizeDouble v2)
                return System.Convert.ToDouble(v2.Width + Addition);
            return System.Convert.ToDouble(((Size)value).Width + Addition);
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language) {
            throw new NotImplementedException("Not supported...");
        }

    }

}
