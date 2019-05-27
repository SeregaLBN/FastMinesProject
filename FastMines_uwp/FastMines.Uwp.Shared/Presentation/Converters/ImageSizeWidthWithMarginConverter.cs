using System;
using Windows.UI.Xaml.Data;
using Fmg.Common.Geom;

namespace Fmg.Common.Converters {

    /// <summary> Add to image size.Width margined 1 pixel left/top  and 1 pixel right/bottom </summary>
    public class ImageSizeWidthWithMarginConverter : IValueConverter {

        private static int Addition = 2;

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
