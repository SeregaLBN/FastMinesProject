using System;
using Windows.UI.Xaml.Data;

namespace fmg.common.Converters {

    /// <summary> Check to null </summary>
    public sealed class SelectedItemToBoolConverter : IValueConverter {

        public object Convert(object value, Type targetType, object parameter, string language) {
            return value != null;
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language) {
            throw new NotSupportedException();
        }

    }

}
