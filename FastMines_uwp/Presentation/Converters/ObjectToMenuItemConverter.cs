using System;
using Windows.UI.Xaml.Data;
using FastMines.Presentation.Menu;

namespace FastMines.Presentation.Converters
{
    public class ObjectToMenuItemConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            return value;
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            return value as MosaicGroupMenuItem;
        }
    }
}
