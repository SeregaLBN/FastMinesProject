using System;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Media;
using Fmg.Uwp.Utils;

namespace Fmg.Uwp.App.Presentation.Converters {

    /// <summary> Convert 'Fmg.Common.Color' to 'Windows.UI.Xaml.Media.Brush' </summary>
    public sealed class FmColorToBrushConverter : IValueConverter {

        public object Convert(object value, Type targetType, object parameter, string language) {
            return new SolidColorBrush(((Fmg.Common.Color) value).ToWinColor());
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language) {
            var brush = value as SolidColorBrush;
            if (brush == null)
                throw new NotImplementedException("Unknown brush type: " + value.GetType().Name); // return null;
            return brush.Color.ToFmColor();
        }

    }

}
