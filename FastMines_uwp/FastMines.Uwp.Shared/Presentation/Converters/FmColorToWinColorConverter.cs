using System;
using Windows.UI.Xaml.Data;
using Fmg.Uwp.Utils;

namespace Fmg.Common.Converters {

    /// <summary> Convert 'Fmg.Common.Color' to 'Windows.UI.Color' </summary>
    public sealed class FmColorToWinColorConverter : IValueConverter {

        public object Convert(object value, Type targetType, object parameter, string language) {
            return ((Fmg.Common.Color) value).ToWinColor();
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language) {
            return ((Windows.UI.Color) value).ToFmColor();
        }

    }

}
