using System;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Data;

namespace fmg.common.Converters {

    /// <summary>  Value converter that translates true to <see cref="Visibility.Visible"/> and false to <see cref="Visibility.Collapsed"/> </summary>
    public sealed class BooleanToVisibilityConverter : IValueConverter {

        public object Convert(object value, Type targetType, object parameter, string language) {
            if (value is bool)
                return (bool)value ? Visibility.Visible : Visibility.Collapsed;
            if (value is Visibility)
                return (Visibility)value == Visibility.Visible;
            throw new ArgumentException("Unsupported type " + value.GetType().FullName);
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language) {
            return Convert(value, targetType, parameter, language);
        }

    }

}
